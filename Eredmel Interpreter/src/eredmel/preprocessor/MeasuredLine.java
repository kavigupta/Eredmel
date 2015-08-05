package eredmel.preprocessor;

import java.nio.file.Path;

public class MeasuredLine {
	final Path path;
	final int lineNumber;
	final String restOfLine;
	final int spaces, tabs;
	MeasuredLine(Path path, int lineNumber, String restOfLine, int spaces,
			int tabs) {
		this.path = path;
		this.lineNumber = lineNumber;
		this.restOfLine = restOfLine;
		this.spaces = spaces;
		this.tabs = tabs;
	}
	public int indentationLevel(int tabwidth) {
		// TODO emit warning on rounding
		return (spaces + tabwidth / 2) / tabwidth + tabs;
	}
}
