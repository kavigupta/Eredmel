package eredmel.preprocessor;

import java.nio.file.Path;

/**
 * A line associated with a context in an original file
 * 
 * @author Kavi Gupta
 */
public class NumberedLine extends Line<NumberedLine> {
	/**
	 * The text of the line
	 */
	public final String line;
	/**
	 * Copies parameters into fields
	 */
	NumberedLine(Path path, int lineNumber, String line) {
		super(path, lineNumber);
		this.line = line;
	}
	/**
	 * Counts the whitespace before this line and composes it into a
	 * {@link MeasuredLine} object
	 * 
	 * @return the Measured version of this line
	 */
	public MeasuredLine countWhitespace() {
		int spaces = 0, tabs = 0;
		int i = 0;
		for (; i < line.length(); i++) {
			if (line.charAt(i) == ' ')
				spaces++;
			else if (line.charAt(i) == '\t')
				tabs++;
			else break;
		}
		return new MeasuredLine(path, tabs, line.substring(i), spaces, tabs);
	}
	@Override
	public String canonicalRepresentation() {
		return line;
	}
	@Override
	public char charAt(int index) {
		return line.charAt(index);
	}
	@Override
	public int length() {
		return line.length();
	}
	@Override
	public NumberedLine subSequence(int start, int end) {
		return new NumberedLine(path, lineNumber, line.substring(start, end));
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((line == null) ? 0 : line.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		NumberedLine other = (NumberedLine) obj;
		if (line == null) {
			if (other.line != null) return false;
		} else if (!line.equals(other.line)) return false;
		return true;
	}
}
