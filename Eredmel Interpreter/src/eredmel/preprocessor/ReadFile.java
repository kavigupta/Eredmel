package eredmel.preprocessor;

import java.nio.file.Path;
import java.util.List;

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
public class ReadFile<LINE extends Line, TAB> {
	/**
	 * The list of lines, backing a file
	 */
	final List<LINE> lines;
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
	@Override
	public String toString() {
		return "ReadFile [lines=" + lines + ", path=" + path + ", tabwidth="
				+ tabwidth + "]";
	}
}
