package eredmel.preprocessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import eredmel.logger.EredmelLogger;
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
	 * Matches a tabbed line, which contains any number of tabs and spaces and
	 * text at the end
	 */
	private static final Pattern TABBED_LINE = Pattern
			.compile("^(?<indt>[\t ]*)(?<rest>.*)$");
	/**
	 * Matches a tabwidth statement at the top of the document, which has the
	 * form {@code ##tabwidth = theTabwidth}, with any number of spaces
	 * permissible between segments
	 */
	private static final Pattern TABWIDTH = Pattern
			.compile("^\\s*##\\s*tabwidth\\s*=\\s*(?<tabwidth>\\d+)\\s*$");
	/**
	 * Matches an inclusion statement, which has the form
	 * {@code ##include pathToInclude}, with any number of spaces permissible
	 * between segments
	 */
	private static final Pattern INCLUDE = Pattern
			.compile("^\\s*##\\s*include\\s*\"(?<path>.+)\"\\s*$");
	/**
	 * Reads a file into memory
	 * 
	 * @param path
	 *        the file to read
	 * @return
	 *         the read file
	 * @throws IOException
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
	 * each line. Note that this is not always accurate and will emit an error.
	 * 
	 * The normalizer then replaces the {@code n} spaces at the beginning of
	 * each line with {@code round(n/t)} spaces, where {@code t} is the
	 * tabwidth.
	 * 
	 * @param toNormalize
	 *        The text to normalize, in the form of a list of lines
	 * @return The normalized document, in the form of a list of lines. The
	 *         tabwidth returned is either one explicitly declared, implicitly
	 *         calculated, or (when there are no declaring spaces), 4.
	 *         (normalizedDocument, tabwidth)
	 */
	public static ReadFile<EredmelLine, Integer> normalize(
			ReadFile<NumberedLine, Void> toNormalize) {
		ReadFile<NumberedLine, Optional<Integer>> twProc = processTabwidth(toNormalize);
		// (restOfLine, (spaces, tabs))
		ReadFile<MeasuredLine, Void> countedStart = countWhitespace(twProc);
		int tabwidth;
		if (!twProc.tabwidth.isPresent()) {
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
				EredmelLogger.get().guessAtTabwidth(tabwidth,
						toNormalize.path);
			}
		} else {
			tabwidth = twProc.tabwidth.get();
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
	 * Converts tabs to spaces at the beginning of lines. This method is not
	 * used by the preprocessor, but may be run at the end of a preprocessor
	 * cycle to repatriate spaces into a non-Eredmel source file.
	 * 
	 * @param lines
	 *        the file lines
	 * @param tabwidth
	 *        the number of spaces to use in replacing each tab
	 * @return
	 *         the modified lines; the original list is not modified.
	 */
	public static List<String> leadingTabsToSpaces(List<String> lines,
			int tabwidth) {
		char[] spacesC = new char[tabwidth];
		Arrays.fill(spacesC, ' ');
		String spaces = new String(spacesC);
		return lines
				.stream()
				.map(TABBED_LINE::matcher)
				.map(mat -> {
					mat.find(); // assumes find successful
					return mat.group("indt").replace("\t", spaces)
							+ mat.group("rest");
				}).collect(Collectors.toList());
	}
	/**
	 * @param path
	 *        The file to manage inclusion on
	 * @param linkedLibs
	 *        The paths where the files can be found. This should include the
	 *        current file directory as well
	 * @return The file with all the included other files
	 */
	public static LinkedFile loadFile(Path toRead, List<Path> linkedLibs) {
		return loadFile(toRead, new ArrayList<>(), linkedLibs);
	}
	private static LinkedFile loadFile(Path toRead,
			List<LinkedFile> linkedFiles, List<Path> linkedLibs) {
		for (LinkedFile linkedFile : linkedFiles)
			if (linkedFile.path.equals(toRead)) return linkedFile;
		ReadFile<EredmelLine, Integer> normalizedFile;
		try {
			normalizedFile = normalize(readFile(toRead));
		} catch (IOException e) {
			EredmelLogger.get().errorLoadingFile(e, toRead);
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
				EredmelLogger.get().unresolvedInclusion(mat.group("path"),
						normalizedFile.path, i);
				continue;
			}
			withInclusions.addAll(loadFile(optPath.get(), linkedFiles,
					linkedLibs).lines);
		}
		LinkedFile file = new LinkedFile(withInclusions, toRead,
				normalizedFile.tabwidth);
		linkedFiles.add(file);
		return file;
	}
	private static ReadFile<MeasuredLine, Void> countWhitespace(
			ReadFile<NumberedLine, Optional<Integer>> norm) {
		List<MeasuredLine> countedStart = new ArrayList<>();
		for (NumberedLine line : norm.lines) {
			Matcher mat = TABBED_LINE.matcher(line.line);
			mat.find();
			int spaces = 0;
			String indt = mat.group("indt");
			for (char c : indt.toCharArray())
				if (c == ' ') spaces++;
			countedStart.add(new MeasuredLine(norm.path, line.lineNumber,
					mat.group("rest"), spaces, indt.length() - spaces));
		}
		return new ReadFile<>(countedStart, norm.path, null);
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
