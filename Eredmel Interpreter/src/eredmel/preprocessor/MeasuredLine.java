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
public class MeasuredLine extends Line<MeasuredLine> {
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
		super(path, lineNumber);
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
	@Override
	public char charAt(int index) {
		if (index < tabs) return '\t';
		index -= tabs;
		if (index < spaces) return ' ';
		return restOfLine.charAt(index - spaces);
	}
	@Override
	public int length() {
		return tabs + spaces + restOfLine.length();
	}
	@Override
	public MeasuredLine subSequence(int start, int end) {
		if (start < tabs) {
			if (end < tabs)
				return new MeasuredLine(path, lineNumber, "", end - start,
						0);
			if (end < tabs + spaces)
				return new MeasuredLine(path, lineNumber, "", end - tabs,
						tabs - start);
			return new MeasuredLine(path, lineNumber, restOfLine.substring(
					0, end - tabs - spaces), spaces, tabs - start);
		}
		if (start < spaces) {
			if (end < spaces)
				return new MeasuredLine(path, lineNumber, "", end - start,
						0);
			return new MeasuredLine(path, lineNumber, restOfLine.substring(
					0, end - tabs - spaces), 0, spaces);
		}
		return new MeasuredLine(path, lineNumber, restOfLine.substring(start
				- tabs - spaces, end - tabs - spaces), 0, 0);
	}
}
