package eredmel.preprocessor;

import java.nio.file.Path;

import eredmel.utils.string.StringUtils;

/**
 * A Eredmel Line represents a line of text, along with the number of tabs
 * before it, its line number in the original text, and the path of the original
 * file it references.
 * 
 * @author Kavi Gupta
 */
public class EredmelLine extends Line<EredmelLine> {
	/**
	 * The line, excluding the preceeding tabs and spaces
	 */
	private final String line;
	/**
	 * The number of tabs before this line
	 */
	private final int tabs;
	EredmelLine(Path path, int lineNumber, String restOfLine, int tabs) {
		super(path, lineNumber);
		this.line = restOfLine;
		this.tabs = tabs;
	}
	/**
	 * Returns this line, displayed with tabs in front
	 */
	public String displayWithTabs() {
		return StringUtils.repeat('\t', this.tabs) + line;
	}
	/**
	 * Returns this line, displayed with spaces in front
	 * 
	 * @param tabwidth
	 *        the number of spaces per tab
	 */
	public String displayWithSpaces(int tabwidth) {
		return StringUtils.repeat(' ', this.tabs * tabwidth) + line;
	}
	@Override
	public String canonicalRepresentation() {
		return displayWithTabs();
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
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((line == null) ? 0 : line.hashCode());
		result = prime * result + tabs;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		EredmelLine other = (EredmelLine) obj;
		if (line == null) {
			if (other.line != null) return false;
		} else if (!line.equals(other.line)) return false;
		if (tabs != other.tabs) return false;
		return true;
	}
}
