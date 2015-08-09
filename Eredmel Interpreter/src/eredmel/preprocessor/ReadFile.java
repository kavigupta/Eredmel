package eredmel.preprocessor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
public class ReadFile<LINE extends Line<?>, TAB> implements CharSequence {
	/**
	 * The list of lines, backing a file
	 */
	final List<LINE> lines;
	/**
	 * The list of the starting offset of the character in the file
	 */
	final List<Integer> offsets;
	/**
	 * The path that the original file is contained in
	 */
	public final Path path;
	/**
	 * The tabwidth of this file
	 */
	public final TAB tabwidth;
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
	ReadFile(List<LINE> lines, Path path, TAB tabwidth) {
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
		this.path = path;
		this.tabwidth = tabwidth;
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
	public ReadFile<LINE, TAB> concat(ReadFile<LINE, TAB> other) {
		ArrayList<LINE> lines = new ArrayList<>(this.lines);
		lines.addAll(other.lines);
		return new ReadFile<>(lines, this.path, this.tabwidth);
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
	public static <TAB> ReadFile<EredmelLine, TAB> replace(
			ReadFile<EredmelLine, TAB> replThis, String replWith) {
		ArrayList<String> replWithLines = new ArrayList<>();
		int lastTerm = 0;
		for (int i = 0; i < replWith.length(); i++) {
			if (replWith.charAt(i) == '\n' || i == replWith.length() - 1) {
				if (lastTerm != i + 1)
					replWithLines.add(replWith.substring(lastTerm, i + 1));
				lastTerm = i + 1;
			}
		}
		double scale = (double) replWithLines.size() / replThis.numLines();
		ArrayList<EredmelLine> replacement = new ArrayList<>();
		for (int i = 0; i < replWithLines.size(); i++) {
			int tabs = 0;
			for (char c : replWithLines.get(i).toCharArray())
				if (c == '\t') tabs++;
			EredmelLine el = new EredmelLine(replThis.path,
					(int) Math.round(i * scale), replWithLines.get(i)
							.substring(tabs), tabs);
			if (el.length() == 0)
				throw new IllegalArgumentException(replWithLines.get(i));
			replacement.add(el);
			// TODO fix
		}
		return new ReadFile<>(replacement, replThis.path, replThis.tabwidth);
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
	public ReadFile<LINE, TAB> subSequence(int start, int end) {
		Pair<Integer, Integer> stLC = lineCol(start), endLC = lineCol(end);
		if (stLC.key.intValue() == endLC.key.intValue()) {
			if (stLC.value.intValue() == endLC.value.intValue())
				return new ReadFile<>(new ArrayList<>(), path, tabwidth);
			return new ReadFile<>(Arrays.asList(subLine(stLC.key,
					stLC.value, endLC.value)), path, tabwidth);
		}
		List<LINE> lines = new ArrayList<>();
		lines.add(subLine(stLC.key, stLC.value));
		lines.addAll(this.lines.subList(stLC.key + 1, endLC.key));
		if (0 != endLC.value) lines.add(subLine(endLC.key, 0, endLC.value));
		return new ReadFile<>(lines, path, tabwidth);
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
