package eredmel.preprocessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import eredmel.config.EredmelConfiguration;
import eredmel.utils.collections.Pair;

/**
 * 
 * A file that has been read into memory
 *
 * @param <LINE>
 *        The type of {@link Line} in use
 * @param <TAB>
 *        the type of tabwidth. Depending on need, it can be {@code Void}
 *        (signifying no tabwidth), {@code Optional<Integer>} (signifying that a
 *        tabwidth statement may have been declared), or {@code Integer} (a
 *        tabwidth is present -- and guaranteed to not be a garbage value)
 * 
 * @author Kavi Gupta
 */
public class ReadFile<LINE extends Line<?>> implements CharSequence {
	/**
	 * The list of lines, backing a file
	 */
	final List<LINE> lines;
	/**
	 * The list of the starting offset of the character in the file
	 */
	final List<Integer> offsets;
	/**
	 * The configuration settings of this file. Since
	 * {@code EredmelConfiguration} is non-immutable and this is, it is private
	 * and references to it are limited
	 */
	private final EredmelConfiguration config;
	/**
	 * A struct constructor that copies the arguments over to the final fields.
	 * 
	 * @param lines
	 *        The list of lines, backing a file
	 * @param path
	 *        The path that the original file is contained in
	 * @param tabwidth
	 *        The tabwidth of this file
	 */
	ReadFile(List<LINE> lines, EredmelConfiguration config) {
		this.lines = lines;
		List<Integer> offsets = new ArrayList<>();
		int off = 0;
		for (LINE line : lines) {
			offsets.add(off);
			off += line.length();
			if (line.length() == 0)
				throw new IllegalArgumentException(line.toString());
		}
		offsets.add(off);
		this.offsets = offsets;
		this.config = config;
	}
	<T extends Line<T>> ReadFile<T> copyConfig(List<T> lines) {
		return new ReadFile<T>(lines, config);
	}
	public EredmelConfiguration config() {
		return config.clone();
	}
	/**
	 * Gets the line at the line number in the <i>current</i> representation,
	 * not the original one.
	 * 
	 * @param i
	 *        the index
	 * @return
	 *         the line at index i{@code i}
	 */
	public LINE lineAt(int i) {
		return lines.get(i);
	}
	/**
	 * Gets the line at the line number in the <i>current</i> representation,
	 * not the original one.
	 * 
	 * @return the number of lines
	 */
	public int numLines() {
		return lines.size();
	}
	/**
	 * Gets the number of lines in the original code that this file spanned
	 * 
	 * @return the number of lines
	 */
	public int lineSpan() {
		if (lines.size() == 0) return 0;
		return 1 + lines.get(lines.size() - 1).lineNumber
				- lines.get(0).lineNumber;
	}
	/**
	 * Concatenates the data from the other file to that of this one,
	 * preserving this file's context and tabwidth metadata.
	 * 
	 * Note that this does not modify either file, this can be compared to the
	 * function {@code (String a, String b) -> a + b}
	 * 
	 * @param other
	 *        the other file to attach to this one
	 * @return the concatenation of this file and other
	 */
	public ReadFile<LINE> concat(ReadFile<LINE> other) {
		ArrayList<LINE> lines = new ArrayList<>(this.lines);
		lines.addAll(other.lines);
		return new ReadFile<>(lines, this.config);
	}
	/**
	 * Replaces the given file with a string representation and returns a file
	 * with line numbers scaled linearly
	 * 
	 * @param replThis
	 *        the file to replace
	 * @param replWith
	 *        the string to replace it with
	 * @return the file with lines replaced and numbers scaled linearly
	 */
	public static ReadFile<EredmelLine> replace(
			ReadFile<EredmelLine> replThis, String replWith) {
		ArrayList<String> replWithLines = new ArrayList<>();
		int lastTerm = 0;
		for (int i = 0; i < replWith.length(); i++) {
			if (replWith.charAt(i) == '\n' || i == replWith.length() - 1) {
				if (lastTerm != i + 1)
					replWithLines.add(replWith.substring(lastTerm, i + 1));
				lastTerm = i + 1;
			}
		}
		if (replThis.lines.size() == 0)
			throw new IllegalArgumentException(
					"Cannot Replace an empty segment with other data");
		ArrayList<EredmelLine> orReprLines = new ArrayList<>();
		for (int i = 0; i < replThis.numLines(); i++) {
			orReprLines.add(replThis.lineAt(i));
			while (i < replThis.numLines()
					&& replThis.lineAt(i).canonicalRepresentation()
							.contains("\n"))
				i++;
		}
		double scale = (double) orReprLines.size() / replWithLines.size();
		ArrayList<EredmelLine> replThisLines = new ArrayList<>();
		for (int i = 0; i < replWithLines.size(); i++) {
			EredmelLine origReprLine = orReprLines.get((int) (scale * i));
			EredmelLine replWithLine = new NumberedLine(origReprLine.path,
					origReprLine.lineNumber, replWithLines.get(i))
					.countWhitespace().applyTabwidth(
							replThis.config.tabwidth());
			replThisLines.add(replWithLine);
		}
		return new ReadFile<>(replThisLines, replThis.config);
	}
	/**
	 * Gets the line and column numbers associated with the given index
	 * 
	 * @param index
	 *        the index, in chars, of the location in the file being searched
	 * @return (line, col)
	 */
	private Pair<Integer, Integer> lineCol(int index) {
		int line = Collections.binarySearch(offsets, index);
		if (line < 0) {
			// not exact match. If -line-1 is the insertion point, then
			// -line-2 is the line it's in
			line = -line - 2;
		}
		return Pair.getInstance(line, index - offsets.get(line));
	}
	/**
	 * Gets part of a line
	 * 
	 * @param line
	 *        the line to slice
	 * @param start
	 *        the starting column, inclusive
	 * @param end
	 *        the ending column, exclusive
	 * @return the slice of the line
	 */
	private LINE subLine(int line, int start, int end) {
		@SuppressWarnings("unchecked")
		LINE lin = (LINE) lines.get(line).subSequence(start, end);
		return lin;
	}
	/**
	 * Gets a slice of a line, from the given index all the way to the end
	 * 
	 * @param line
	 *        the line to slice
	 * @param start
	 *        the starting column, inclusive
	 * @return the slice of the line, all the way to the end
	 */
	private LINE subLine(int line, int start) {
		return subLine(line, start, lines.get(line).length());
	}
	@Override
	public char charAt(int index) {
		Pair<Integer, Integer> lineCol = lineCol(index);
		return lines.get(lineCol.key).charAt(lineCol.value);
	}
	@Override
	public int length() {
		return offsets.get(lines.size());
	}
	@Override
	public ReadFile<LINE> subSequence(int start, int end) {
		Pair<Integer, Integer> stLC = lineCol(start), endLC = lineCol(end);
		if (stLC.key.intValue() == endLC.key.intValue()) {
			if (stLC.value.intValue() == endLC.value.intValue())
				return new ReadFile<>(new ArrayList<>(), config);
			return new ReadFile<>(Arrays.asList(subLine(stLC.key,
					stLC.value, endLC.value)), config);
		}
		List<LINE> lines = new ArrayList<>();
		lines.add(subLine(stLC.key, stLC.value));
		lines.addAll(this.lines.subList(stLC.key + 1, endLC.key));
		if (!endLC.value.equals(0))
			lines.add(subLine(endLC.key, 0, endLC.value));
		return new ReadFile<>(lines, config);
	}
	@Override
	public String toString() {
		char[] c = new char[length()];
		for (int i = 0; i < length(); i++) {
			c[i] = charAt(i);
		}
		return new String(c);
		// return lines.stream().map(Line::toString)
		// .collect(Collectors.reducing("", (x, y) -> x + y));
		// LOWPRI performance improvement possibility here; replace +
		// concatenation with a StringBuilder
	}
}
