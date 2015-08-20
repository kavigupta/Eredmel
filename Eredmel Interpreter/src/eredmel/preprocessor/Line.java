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
	public final Path path;
	/**
	 * The line number in the original document
	 */
	public final int lineNumber;
	protected Line(Path path, int lineNumber) {
		this.path = path;
		this.lineNumber = lineNumber;
	}
	/**
	 * A representation of the original line in an equivalent form
	 */
	public abstract String canonicalRepresentation();
	@Override
	public abstract SELF subSequence(int start, int end);
	@Override
	public final String toString() {
		return canonicalRepresentation();
	}
	@Override
	public abstract boolean equals(Object obj);
	@Override
	public abstract int hashCode();
}
