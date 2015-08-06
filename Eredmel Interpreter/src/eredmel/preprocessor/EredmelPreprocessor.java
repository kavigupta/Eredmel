package eredmel.preprocessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import eredmel.logger.EredmelMessage;
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
	/**
	 * Utility class non-constructor
	 */
	private EredmelPreprocessor() {}
	/**
	 * Matches a tabwidth statement at the top of the document, which has the
	 * form {@code ##tabwidth = theTabwidth}, with any number of spaces
	 * permissible between tokens or before the statement
	 */
	private static final Pattern TABWIDTH = Pattern
			.compile("^\\s*##\\s*tabwidth\\s*=\\s*(?<tabwidth>\\d+)\\s*$");
	/**
	 * Matches an inclusion statement, which has the form
	 * {@code ##include "pathToInclude"}, with any number of spaces permissible
	 * between segments
	 */
	private static final Pattern INCLUDE = Pattern
			.compile("^\\s*##\\s*include\\s*\"(?<path>.+)\"\\s*$");
	/**
	 * Reads a file into memory, and assign numbers to lines
	 * 
	 * @param path
	 *        the file to read
	 * @return
	 *         the read file
	 * @throws IOException
	 *         if there was an error in reading the file
	 */
	public static ReadFile<NumberedLine, Void> readFile(Path path)
			throws IOException {
		List<String> lines = Files.readAllLines(path);
		List<NumberedLine> numbered = new ArrayList<>(lines.size());
		for (int i = 0; i < lines.size(); i++) {
			numbered.add(new NumberedLine(path, i, lines.get(i)));
		}
		return new ReadFile<>(numbered, path, null);
	}
	/**
	 * Normalizes a given Eredmel file.
	 * 
	 * Normalization consists of stripping out opening empty lines (or lines
	 * containing only whitespace), interpreting a tabwidth statement (which
	 * takes the form {@code ##tabwidth = <number>}.
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
	public static ReadFile<EredmelLine, Integer> normalize(
			ReadFile<NumberedLine, Void> toNormalize) {
		ReadFile<MeasuredLine, Optional<Integer>> countedStart = countWhitespace(processTabwidth(toNormalize));
		int tabwidth;
		if (!countedStart.tabwidth.isPresent()) {
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
				EredmelMessage.guessAtTabwidth(tabwidth, toNormalize.path)
						.log();
			}
		} else {
			tabwidth = countedStart.tabwidth.get();
		}
		List<EredmelLine> normalized = new ArrayList<>(countedStart.nLines());
		for (MeasuredLine line : countedStart.lines) {
			int tabs = line.indentationLevel(tabwidth);
			normalized.add(new EredmelLine(countedStart.path, tabs,
					line.restOfLine, tabs));
		}
		return new ReadFile<>(normalized, countedStart.path, tabwidth);
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
	public static List<ReadFile<EredmelLine, Integer>> loadFiles(
			List<Path> toRead, List<Path> linkedLibs) {
		Set<ReadFile<EredmelLine, Integer>> allLoaded = new HashSet<>();
		List<ReadFile<EredmelLine, Integer>> requestedLoaded = new ArrayList<>();
		for (Path individual : toRead) {
			requestedLoaded.add(loadFile(individual, linkedLibs, allLoaded,
					new ArrayList<>()));
		}
		return requestedLoaded;
	}
	/**
	 * 
	 * Loads a single Eredmel File into memory, normalizes it, and includes
	 * other
	 * files by loading them and dumping them into the file.
	 * 
	 * If a file is not found, an error occurs in reading the file, or a
	 * circular reference is found, a warning or fatal error is raised.
	 * 
	 * @param toRead
	 *        The file to load
	 * @param linkedLibs
	 *        The paths where inclusions can be found.
	 * @return The file in memory, loaded completely with all other files
	 *         included completely
	 */
	public static ReadFile<EredmelLine, Integer> loadFile(Path toRead,
			List<Path> linkedLibs) {
		return loadFiles(Arrays.asList(toRead), linkedLibs).get(0);
	}
	/**
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
	private static ReadFile<EredmelLine, Integer> loadFile(Path toRead,
			List<Path> linkedLibs,
			Set<ReadFile<EredmelLine, Integer>> loadedFiles,
			List<Path> inclusionChain) {
		for (ReadFile<EredmelLine, Integer> linkedFile : loadedFiles)
			if (linkedFile.path.equals(toRead)) return linkedFile;
		int index = inclusionChain.indexOf(toRead);
		if (index >= 0) {
			List<Path> circle = inclusionChain.subList(index,
					inclusionChain.size());
			circle.add(toRead);
			EredmelMessage.circularInclusionLink(circle, toRead, 0);
		}
		inclusionChain = new ArrayList<>(inclusionChain);
		inclusionChain.add(toRead);
		ReadFile<EredmelLine, Integer> normalizedFile;
		try {
			normalizedFile = normalize(readFile(toRead));
		} catch (IOException e) {
			EredmelMessage.errorLoadingFile(e, toRead).log();
			// should be unreachable since this is a fatal error. Simply
			// rethrow the error
			throw new RuntimeException(e);
		}
		List<EredmelLine> withInclusions = new ArrayList<>(
				normalizedFile.nLines());
		for (int i = 0; i < normalizedFile.nLines(); i++) {
			Matcher mat = INCLUDE.matcher(normalizedFile.lineAt(i).line);
			if (!mat.find()) {
				withInclusions.add(normalizedFile.lineAt(i));
				continue;
			}
			Optional<Path> optPath = IOUtils.resolve(toRead, linkedLibs,
					mat.group("path"));
			if (!optPath.isPresent()) {
				EredmelMessage.inclusionNotFound(mat.group("path"),
						normalizedFile.path, i).log();
				continue;
			}
			withInclusions.addAll(loadFile(optPath.get(), linkedLibs,
					loadedFiles, inclusionChain).lines);
		}
		ReadFile<EredmelLine, Integer> file = new ReadFile<EredmelLine, Integer>(
				withInclusions, toRead, normalizedFile.tabwidth);
		loadedFiles.add(file);
		return file;
	}
	/**
	 * Measures each line for how many tabs and spaces it has
	 */
	private static <T> ReadFile<MeasuredLine, T> countWhitespace(
			ReadFile<NumberedLine, T> norm) {
		return new ReadFile<>(norm.lines.stream()
				.map(NumberedLine::countWhitespace)
				.collect(Collectors.toList()), norm.path, norm.tabwidth);
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
	private static ReadFile<NumberedLine, Optional<Integer>> processTabwidth(
			ReadFile<NumberedLine, Void> original) {
		int i = 0;
		while (i < original.nLines()
				&& original.lineAt(i).line.trim().length() == 0)
			i++;
		if (i < original.nLines()) {
			Matcher mat = TABWIDTH.matcher(original.lineAt(i).line);
			if (mat.find()) {
				i++;
				return new ReadFile<>(original.lines.subList(i,
						original.nLines()), original.path,
						Optional.of(Integer.parseInt(mat
								.group("tabwidth"))));
			}
		}
		return new ReadFile<>(original.lines.subList(i, original.nLines()),
				original.path, Optional.empty());
	}
}
