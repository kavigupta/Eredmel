package eredmel.preprocessor;

import java.nio.file.Path;

public class NumberedLine {
	final Path path;
	final int lineNumber;
	public final String line;
	NumberedLine(Path path, int lineNumber, String line) {
		this.path = path;
		this.lineNumber = lineNumber;
		this.line = line;
	}
}
