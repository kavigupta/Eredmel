package eredmel.preprocessor;

import java.nio.file.Path;

/**
 * An interface representing a line in a text file
 * 
 * @author Kavi Gupta
 */
public abstract class Line<SELF extends Line<SELF>> implements CharSequence {
	/**
	 * The path of the original document
	 */
	final Path path;
	/**
	 * The line number in the original document
	 */
	final int lineNumber;
	protected Line(Path path, int lineNumber) {
		this.path = path;
		this.lineNumber = lineNumber;
	}
	/**
	 * A representation of the original line in an equivalent form
	 */
	public abstract String canonicalRepresentation();
	/**
	 * The line number in the original file
	 */
	public abstract int lineNumber();
	/**
	 * The path of the original file
	 */
	public abstract Path path();
	@Override
	public abstract SELF subSequence(int start, int end);
}
