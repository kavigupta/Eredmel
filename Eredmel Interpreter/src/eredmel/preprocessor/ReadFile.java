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
	public int nLines() {
		return lines.size();
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
		if (stLC.key == endLC.key) { return new ReadFile<>(
				Arrays.asList(subLine(stLC.key, stLC.value, endLC.value)),
				path, tabwidth); }
		List<LINE> lines = new ArrayList<>();
		lines.add(subLine(stLC.key, stLC.value));
		lines.addAll(lines.subList(stLC.key + 1, endLC.key));
		lines.add(subLine(stLC.key, 0, endLC.value));
		return new ReadFile<>(lines, path, tabwidth);
	}
	@Override
	public String toString() {
		return "ReadFile [lines=" + lines + ", path=" + path + ", tabwidth="
				+ tabwidth + "]";
	}
}
