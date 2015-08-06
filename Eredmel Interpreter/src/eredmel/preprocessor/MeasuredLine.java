package eredmel.preprocessor;

import java.nio.file.Path;
import java.util.Arrays;

import eredmel.logger.EredmelMessage;

/**
 * A line associated with measurements of the numbers of spaces and tabs before
 * it, as well its location in an original file
 * 
 * @author Kavi Gupta
 */
public class MeasuredLine implements Line {
	/**
	 * The path of the original document
	 */
	final Path path;
	/**
	 * The line number in the original document
	 */
	final int lineNumber;
	/**
	 * The line, excluding the tabs and spaces
	 */
	final String restOfLine;
	/**
	 * The number of spaces before this line
	 */
	final int spaces;
	/**
	 * The number of tabs before this line
	 */
	final int tabs;
	/**
	 * Copies parameters to fields
	 */
	MeasuredLine(Path path, int lineNumber, String restOfLine, int spaces,
			int tabs) {
		this.path = path;
		this.lineNumber = lineNumber;
		this.restOfLine = restOfLine;
		this.spaces = spaces;
		this.tabs = tabs;
	}
	/**
	 * Gets the indentation level, given the given tabwidth. This divides the
	 * number of spaces by the tabwidth and adds that to the number of tabs. If
	 * rounding is needed, half-up rounding is utilized and an error is raised.
	 */
	public int indentationLevel(int tabwidth) {
		if (spaces % tabwidth != 0)
			EredmelMessage.roundingTabwidth(tabwidth, spaces, path,
					lineNumber);
		return (spaces + tabwidth / 2) / tabwidth + tabs;
	}
	@Override
	public String canonicalRepresentation() {
		char[] s = new char[spaces], t = new char[tabs];
		Arrays.fill(s, ' ');
		Arrays.fill(t, '\t');
		return new String(t) + new String(s) + restOfLine;
	}
	@Override
	public int lineNumber() {
		return lineNumber;
	}
	@Override
	public Path path() {
		return path;
	}
}
