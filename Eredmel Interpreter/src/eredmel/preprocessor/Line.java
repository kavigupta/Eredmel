package eredmel.preprocessor;

import java.nio.file.Path;

/**
 * An interface representing a line in a text file
 * 
 * @author Kavi Gupta
 */
public interface Line {
	/**
	 * A representation of the original line in an equivalent form
	 */
	public String canonicalRepresentation();
	/**
	 * The line number in the original file
	 */
	public int lineNumber();
	/**
	 * The path of the original file
	 */
	public Path path();
}
