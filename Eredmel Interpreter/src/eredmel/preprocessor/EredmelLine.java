package eredmel.preprocessor;

import java.nio.file.Path;
import java.util.Arrays;

public class EredmelLine {
	final Path path;
	final int lineNumber;
	final String line;
	final int tabs;
	EredmelLine(Path path, int lineNumber, String restOfLine, int tabs) {
		this.path = path;
		this.lineNumber = lineNumber;
		this.line = restOfLine;
		this.tabs = tabs;
	}
	public String display() {
		char[] tabs = new char[this.tabs];
		Arrays.fill(tabs, '\t');
		return new String(tabs) + line;
	}
}
