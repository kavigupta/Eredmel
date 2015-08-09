package eredmel.preprocessor;

import java.nio.file.Path;
import java.util.Arrays;

/**
 * A Eredmel Line represents a line of text, along with the number of tabs
 * before it, its line number in the original text, and the path of the original
 * file it references.
 * 
 * @author Kavi Gupta
 */
public class EredmelLine extends Line<EredmelLine> {
	/**
	 * The line, excluding the tabs and spaces
	 */
	final String line;
	/**
	 * The number of tabs before this line
	 */
	final int tabs;
	EredmelLine(Path path, int lineNumber, String restOfLine, int tabs) {
		super(path, lineNumber);
		this.line = restOfLine;
		this.tabs = tabs;
	}
	/**
	 * Returns this line, displayed with tabs in front
	 */
	public String displayWithTabs() {
		char[] tabs = new char[this.tabs];
		Arrays.fill(tabs, '\t');
		return new String(tabs) + line;
	}
	/**
	 * Returns this line, displayed with spaces in front
	 * 
	 * @param tabwidth
	 *        the number of spaces per tab
	 */
	public String displayWithSpaces(int tabwidth) {
		char[] tabs = new char[this.tabs * tabwidth];
		Arrays.fill(tabs, ' ');
		return new String(tabs) + line;
	}
	@Override
	public String canonicalRepresentation() {
		return displayWithTabs();
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
	public String toString() {
		return canonicalRepresentation();
	}
	@Override
	public char charAt(int index) {
		return index < tabs ? '\t' : line.charAt(index - tabs);
	}
	@Override
	public int length() {
		return tabs + line.length();
	}
	@Override
	public EredmelLine subSequence(int start, int end) {
		if (start < tabs) {
			if (end < tabs)
				return new EredmelLine(path, lineNumber, "", end - start);
			return new EredmelLine(path, lineNumber, line.substring(0, end
					- tabs), tabs - start);
		}
		return new EredmelLine(path, lineNumber, line.substring(start - tabs,
				end - tabs), 0);
	}
}
