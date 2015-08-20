package eredmel.preprocessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import eredmel.config.ConfigSetting;
import eredmel.config.EredmelConfiguration;
import eredmel.logger.EredmelMessage;
import eredmel.regex.EnregexType;
import eredmel.regex.Matcher;
import eredmel.regex.Pattern;
import eredmel.utils.io.IOUtils;
import eredmel.utils.math.MathUtils;

/**
 * This utility class contains methods that allow for the normalization and
 * preprocessing of Eredmel source files.
 * 
 * @author Kavi Gupta
 */
public final class EredmelPreprocessor {
	private EredmelPreprocessor() {}
	/**
	 * Matches a configuration statement at the top of the document, which has
	 * the form {@code config: name = value}, with any number of spaces
	 * permissible between tokens
	 */
	public static final String CONFIG = "config:\\s*(?<name>[A-Za-z_][A-Za-z0-9_]*)\\s*=\\s*(?<value>\\S+)\\s*$";
	/**
	 * Matches an inclusion statement, which has the form
	 * {@code include "pathToInclude"}, with any number of spaces permissible
	 * between segments
	 */
	private static final String INCLUDE = "include\\s*\"(?<path>.+)\"\\s*$";
	/**
	 * Matches the a replacement statement, which has the form
	 * {@code replace[lit] <enregex>\n\t<replacement>}, with any number of
	 * spaces
	 * permissible between segments.
	 * 
	 * The replacement string is interpreted literally if {@code replacelit} is
	 * used. However, if {@code replace} is used, it will be treated as a
	 * normal regex replacement string with one exception: {@code \n} and
	 * {@code \t} will represent a new line and an indentation level rather
	 * than {@code n} and {@code t} as they would have otherwise.
	 */
	private static final String REPLACE = "replace(?<lit>lit)?(?<enregex>.+)\n\t(?<repl>.+)\n";
	/**
	 * Reads a file into memory, and assign numbers to lines. Each line will be
	 * terminated with a new line ({@code \n}) regardless of it's original
	 * terminating character, which is platform specific but will probably be
	 * {@code \r?\n?}
	 * 
	 * @param path
	 *        the file to read
	 * @return
	 *         the read file
	 * @throws IOException
	 *         if there was an error in reading the file
	 */
	public static ReadFile<NumberedLine> readFile(Path path,
			EredmelConfiguration config) throws IOException {
		List<String> lines = Files.readAllLines(path);
		List<NumberedLine> numbered = new ArrayList<>(lines.size());
		for (int i = 0; i < lines.size(); i++) {
			numbered.add(new NumberedLine(path, i, lines.get(i) + '\n'));
		}
		return new ReadFile<>(numbered, config);
	}
	/**
	 * Normalizes a given Eredmel file.
	 * 
	 * Normalization consists of stripping out opening empty lines (or lines
	 * containing only whitespace), and interpreting configuration settings.
	 * 
	 * The normalizer then goes through each line and counts the number of tabs
	 * and spaces which it contains. If tabwidth was not defined, it tries to
	 * infer it by taking the Greatest Common Factor of the number of spaces in
	 * each line. Note that this is not always accurate and will emit a
	 * high-level warning.
	 * 
	 * The normalizer then replaces the {@code n} spaces at the beginning of
	 * each line with {@code round(n/t)} spaces, where {@code t} is the
	 * tabwidth. It will round off non-standard tabwidths, emitting a warning
	 * when it does so
	 * 
	 * @param toNormalize
	 *        The text to normalize
	 * @return The normalized document, in the form of a list of lines. The
	 *         tabwidth attached to the document is either one explicitly
	 *         declared, implicitly calculated, or (when there are no declaring
	 *         spaces), 4.
	 */
	public static ReadFile<EredmelLine> normalize(
			ReadFile<NumberedLine> toNormalize) {
		ReadFile<MeasuredLine> countedStart = countWhitespace(processConfig(toNormalize));
		int tabwidth;
		EredmelConfiguration config = countedStart.config();
		if (!config.isDefined(ConfigSetting.TABWIDTH)) {
			int gcf = 0;
			for (MeasuredLine line : countedStart.lines)
				gcf = MathUtils.gcf(gcf, line.spaces);
			if (gcf == 0) {
				// no spaces. Set to 4 to prevent divide by 0 errors and
				// allow for standard conversion to spaces
				tabwidth = 4;
			} else {
				// take a guess
				tabwidth = gcf;
				EredmelMessage.guessAtTabwidth(tabwidth,
						toNormalize.lineAt(0).path).log();
			}
			boolean validated = config.set(ConfigSetting.TABWIDTH,
					Integer.toString(tabwidth));
			assert validated : "Either the definition of TABWIDTH was changed,"
					+ " or the code to produce it was, and they are now"
					+ " unsychronized. Please file a bug report";
		}
		List<EredmelLine> normalized = new ArrayList<>(
				countedStart.numLines());
		for (MeasuredLine line : countedStart.lines) {
			normalized.add(line.applyTabwidth(config.tabwidth()));
		}
		return new ReadFile<>(normalized, config);
	}
	/**
	 * 
	 * Loads these Eredmel File into memory, normalizes it, and includes other
	 * files by loading them and dumping them into the file.
	 * 
	 * If a file is not found, an error occurs in reading the file, or a
	 * circular reference is found, a warning or fatal error is raised.
	 * 
	 * @param toRead
	 *        The files to load
	 * @param linkedLibs
	 *        The paths where inclusions can be found, not including the
	 *        current working directory, which is designated the first point in
	 *        the search path
	 * @return The files in memory, loaded completely with all other files
	 *         included completely, guaranteed to be the same size as
	 *         {@code toRead}
	 */
	public static List<ReadFile<EredmelLine>> loadFiles(List<Path> toRead,
			List<Path> linkedLibs, EredmelConfiguration config) {
		Map<Path, ReadFile<EredmelLine>> allLoaded = new HashMap<>();
		List<ReadFile<EredmelLine>> requestedLoaded = new ArrayList<>();
		for (Path individual : toRead) {
			requestedLoaded.add(loadFile(individual, linkedLibs, allLoaded,
					new ArrayList<>(), config));
		}
		return requestedLoaded;
	}
	/**
	 * Loads a single Eredmel File into memory, normalizes it, and includes
	 * other files by loading them and dumping them into the file.
	 * 
	 * If a file is not found, an error occurs in reading the file, or a
	 * circular reference is found, a high-level warning is raised (if the
	 * working mode is one that allows continuing under those circumstances,
	 * the file is simply not included)
	 * 
	 * @param toRead
	 *        The file to load
	 * @param linkedLibs
	 *        The paths where inclusions can be found.
	 * @return The file in memory, loaded completely with all other files
	 *         included completely
	 */
	public static ReadFile<EredmelLine> loadFile(Path toRead,
			List<Path> linkedLibs, EredmelConfiguration config) {
		return loadFiles(Arrays.asList(toRead), linkedLibs, config).get(0);
	}
	/**
	 * Loads a file into memory, first checking to see if it has already been
	 * loaded.
	 * 
	 * @param toRead
	 *        the file to read
	 * @param loadedFiles
	 *        a set of loaded files, which is added to every time a file is
	 *        loaded. A short-circuit return is utilized to prevent the same
	 *        file being loaded multiple times
	 * @param linkedLibs
	 *        the paths where inclusions can be found.
	 * @param inclusionChain
	 *        the chain of inclusions needed to get to this point
	 * @return the file loaded into memory
	 * 
	 */
	private static ReadFile<EredmelLine> loadFile(Path toRead,
			List<Path> linkedLibs,
			Map<Path, ReadFile<EredmelLine>> loadedFiles,
			List<Path> inclusionChain, EredmelConfiguration config) {
		if (!Files.exists(toRead))
			EredmelMessage.fileNotFound(toRead.toString(), toRead, 0).log();
		for (Entry<Path, ReadFile<EredmelLine>> linkedFile : loadedFiles
				.entrySet())
			if (linkedFile.getKey().equals(toRead))
				return linkedFile.getValue();
		int index = inclusionChain.stream()
				.map(x -> x.normalize().toString())
				.collect(Collectors.toList())
				.indexOf(toRead.normalize().toString());
		if (index >= 0) {
			List<Path> circle = inclusionChain.subList(index,
					inclusionChain.size());
			circle.add(toRead);
			EredmelMessage.circularInclusionLink(circle, toRead, 0).log();
		}
		inclusionChain = new ArrayList<>(inclusionChain);
		inclusionChain.add(toRead);
		ReadFile<EredmelLine> normalizedFile;
		try {
			normalizedFile = normalize(readFile(toRead, config));
		} catch (IOException e) {
			EredmelMessage.errorLoadingFile(e, toRead).log();
			// if this point in the code is released, return an empty file.
			return new ReadFile<>(new ArrayList<>(), null);
		}
		Pattern includePattern = normalizedFile.config().patternMatch(
				INCLUDE, 0);
		List<EredmelLine> withInclusions = new ArrayList<>(
				normalizedFile.numLines());
		for (int i = 0; i < normalizedFile.numLines(); i++) {
			Matcher inclusion = includePattern.matcher(normalizedFile
					.lineAt(i).canonicalRepresentation());
			if (!inclusion.find()) {
				withInclusions.add(normalizedFile.lineAt(i));
				continue;
			}
			Optional<Path> optPath = IOUtils.resolve(toRead, linkedLibs,
					inclusion.group("path"));
			if (!optPath.isPresent()) {
				EredmelMessage.fileNotFound(inclusion.group("path"),
						normalizedFile.lineAt(0).path, i).log();
				// just skip if this error is being ignored
				continue;
			}
			withInclusions.addAll(loadFile(optPath.get(), linkedLibs,
					loadedFiles, inclusionChain,
					config.preserveOnlySession()).lines);
		}
		ReadFile<EredmelLine> file = normalizedFile
				.copyConfig(withInclusions);
		loadedFiles.put(toRead, file);
		return file;
	}
	/**
	 * Measures each line for how many tabs and spaces it has
	 */
	private static ReadFile<MeasuredLine> countWhitespace(
			ReadFile<NumberedLine> norm) {
		return norm.copyConfig(norm.lines.stream()
				.map(NumberedLine::countWhitespace)
				.collect(Collectors.toList()));
	}
	/**
	 * Gets the tabwidth and consumes a tabwidth statement
	 * 
	 * @param original
	 *        the pre-normalzied edmh file, which may or may not contain a
	 *        tabwidth statement to begin with
	 * @return if the file begins with a tabwidth statement, it strips it and
	 *         returns the specified Optional.of(width). Otherwise, it returns
	 *         Optional.none()
	 */
	private static ReadFile<NumberedLine> processConfig(
			ReadFile<NumberedLine> original) {
		EredmelConfiguration config = original.config();
		Pattern configMatch = config.patternMatch(CONFIG, 0);
		int i = 0;
		for (i = 0; i < original.numLines(); i++) {
			if (original.lineAt(i).line.trim().length() == 0) continue;
			Matcher mat = configMatch.matcher(original.lineAt(i));
			if (!mat.find()) break;
			ConfigSetting econfig = ConfigSetting.fromConfigString(mat
					.group("name"));
			boolean validated = config.set(econfig, mat.group("value"));
			if (!validated)
				EredmelMessage.invalidConfigurationSetting(econfig,
						mat.group("value"), original.lineAt(i)).log();
		}
		return new ReadFile<>(original.lines.subList(i, original.numLines()),
				config);
	}
	/**
	 * Applies the {@code replace} and {@code replacelit} statements in
	 * this file one-by-one to the text below them, recursively.
	 * 
	 * TODO: Future versions may implement an END to the replacement scope
	 * 
	 * @param preReplace
	 *        the file before {@code replace[lit]} statements have been
	 *        applied
	 * @replace the file after {@code replace[lit]} statements have been
	 *          applied
	 */
	public static ReadFile<EredmelLine> applyReplaces(
			ReadFile<EredmelLine> preReplace) {
		ReadFile<EredmelLine> processed = preReplace
				.copyConfig(new ArrayList<>());
		// the reason for this structure is the regexes are self-modifying
		Pattern replacePattern = processed.config().patternMatch(REPLACE, 0);
		while (true) {
			Matcher findRepl = replacePattern.matcher(preReplace);
			if (!findRepl.find()) {
				break;
			} // no replace
			Pattern enregex = Pattern.compile(findRepl.group("enregex"),
					Pattern.ENHANCED_REGEX | Pattern.COMMENTS,
					EnregexType.EREDMEL_STANDARD);
			boolean lit = findRepl.group("lit") != null
					&& findRepl.group("lit").equals("lit");
			String replace = findRepl.group("repl");
			if (!lit)
				replace = replace.replace("\\t", "\t").replace("\\n", "\n");
			// pop replace off
			preReplace = preReplace.subSequence(findRepl.end(),
					preReplace.length());
			System.out.println(preReplace);
			while (true) {
				Matcher replacer = enregex.matcher(preReplace);
				if (!replacer.find()) break;
				String replacement;
				if (lit) {
					replacement = replace;
				} else {
					StringBuffer sbRepl = new StringBuffer();
					replacer.appendReplacement(sbRepl, replace, false,
							true);
					replacement = sbRepl.toString();
					// System.out.println("111111111111111111");
					// System.out.println(replace);
					// System.out.println("222222222222222222");
					// System.out.println(replacement);
					// System.out.println("333333333333333333");
				}
				ReadFile<EredmelLine> beforeMatch = preReplace.subSequence(
						0, replacer.start());
				ReadFile<EredmelLine> match = preReplace.subSequence(
						replacer.start(), replacer.end());
				ReadFile<EredmelLine> afterMatch = preReplace.subSequence(
						replacer.end(), preReplace.length());
				// System.out.println("-------------------");
				// System.out.print(beforeMatch);
				// System.out.print(match);
				// System.out.print(afterMatch);
				// System.out.println("~~~~~~~~~~~~~~~~~~~");
				// System.out.print(beforeMatch);
				// System.out.print(replacement);
				// System.out.print(afterMatch);
				// applies the replacements
				// System.out.println("```````````````````");
				// System.out.println(preReplace);
				// System.out.println(********************");
				processed = processed.concat(beforeMatch);
				preReplace = ReadFile.replace(match, replacement).concat(
						afterMatch);
				// System.out.println(processed);
				// System.out.println("@@@@@@@@@@@@@@@@@@@");
				// System.out.println(replacement);
				// System.out.println("$$$$$$$$$$$$$$$$$$$");
				// System.out.println(afterMatch);
				// System.out.println("==================>");
				// System.out.println(preReplace);
			}
			preReplace = processed.concat(preReplace);
			processed = processed.copyConfig(new ArrayList<>());
		}
		processed = processed.concat(preReplace);
		return processed;
	}
}
