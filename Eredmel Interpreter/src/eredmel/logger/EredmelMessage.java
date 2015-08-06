package eredmel.logger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * A class representing a message
 * 
 * @author Kavi Gupta
 *
 */
public final class EredmelMessage {
	/**
	 * Represents a logging level to represent
	 * 
	 * @author Kavi Gupta
	 */
	public static enum LoggingLevel {
		/**
		 * Not a warning level
		 */
		DEBUG,
		/**
		 * Lowest level warning
		 */
		LOW,
		/**
		 * Medium level warning
		 */
		MED,
		/**
		 * Highest level warning
		 */
		HIGH,
		/**
		 * Fatal error
		 */
		FATAL,
		/**
		 * Only to be used by loggers to specify no messages to be processed
		 */
		NONE
	}
	/**
	 * A message representing an unresolved inclusion, or an included file that
	 * does not exist
	 * 
	 * @param path
	 *        the path that could not be resolved
	 * @param file
	 *        the file the inclusion is in
	 * @param i
	 *        the line number the inclusion is at
	 * @return
	 *         an high-level warning message
	 */
	public static EredmelMessage inclusionNotFound(String path, Path file,
			int i) {
		return new EredmelMessage(EredmelMessage.LoggingLevel.HIGH,
				String.format("Unresolved Inclusion: File %s Not Found",
						path), file, i, Optional.empty());
	}
	/**
	 * An error occurred in loading an existing file
	 * 
	 * @param e
	 *        the error that was raised
	 * @param toLoad
	 *        the file to load
	 * @return
	 *         a high-level warning message
	 */
	public static EredmelMessage errorLoadingFile(IOException e, Path toLoad) {
		return new EredmelMessage(EredmelMessage.LoggingLevel.HIGH,
				String.format("Error Loading File %s: %s", toLoad, e),
				toLoad, 0, Optional.of(e));
	}
	/**
	 * The tabwidth needed to be guessed because spaces were used when there
	 * was no {@code tabwidth} declaration
	 * 
	 * @param tabwidth
	 *        the tabwidth guessed at
	 * @param file
	 *        the file the tabwidth had to be guessed
	 * @return a mid-level warning
	 */
	public static EredmelMessage guessAtTabwidth(int tabwidth, Path file) {
		return new EredmelMessage(EredmelMessage.LoggingLevel.MED,
				String.format(""), file, 0, Optional.empty());
	}
	/**
	 * A link loop was found, where a file must eventually be included in
	 * itself.
	 * 
	 * @param chain
	 *        the inclusion chain of paths
	 * @param toRead
	 *        the final file in the chain, which triggered the loop
	 * @param i
	 *        the location of the inclusion resulting in the chain
	 * @return a high-level warning
	 */
	public static EredmelMessage circularInclusionLink(List<Path> chain,
			Path toRead, int i) {
		StringBuilder buff = new StringBuilder("Circular reference loop:\n");
		for (Path el : chain)
			buff.append('\t').append(el).append('\n');
		return new EredmelMessage(EredmelMessage.LoggingLevel.HIGH,
				buff.toString(), toRead, i, Optional.empty());
	}
	/**
	 * The tabwidth had to be rounded
	 * 
	 * @param tabwidth
	 *        the number of spaces defined per tab
	 * @param spaces
	 *        the number of spaces in this line
	 * @param file
	 *        the file in which the rounding occurred
	 * @param line
	 *        the line at which the rounding occurred
	 * @return a low- or medium-level warning, depending on whether it was off
	 *         by 1/4 or the tabwidth or more
	 */
	public static EredmelMessage roundingTabwidth(int tabwidth, int spaces,
			Path file, int line) {
		double dist = ((double) (spaces % tabwidth) / tabwidth);
		return new EredmelMessage(
				dist < .25 || dist > .75 ? LoggingLevel.LOW
						: LoggingLevel.MED,
				String.format(
						"The number of spaces %s is not a multiple of the declared tabwidth %s and will be rounded",
						spaces, tabwidth), file, line, Optional.empty());
	}
	/**
	 * The logging level used by this message
	 */
	public final EredmelMessage.LoggingLevel level;
	/**
	 * The text of the message
	 */
	public final String msg;
	/**
	 * The file path that caused this message
	 */
	public final Path file;
	/**
	 * The file line that caused this message
	 */
	public final int line;
	/**
	 * The exception that may or may not be associated with this message
	 * (obviously optional)
	 */
	public final Optional<? extends Throwable> ex;
	/**
	 * Copies parameters to fields
	 */
	public EredmelMessage(EredmelMessage.LoggingLevel level, String msg,
			Path file, int line, Optional<? extends Throwable> ex) {
		this.level = level;
		this.msg = msg;
		this.file = file;
		this.line = line;
		this.ex = ex;
	}
	/**
	 * Logs itself against the default logger
	 */
	public void log() {
		log(EredmelLogger.get());
	}
	/**
	 * Logs itself against the given logger
	 * 
	 * @param logger
	 *        a logger to log against
	 */
	public void log(EredmelLogger logger) {
		try {
			logger.log(this);
		} catch (Throwable e) {
			if (!file.equals(Paths.get("/")))
				new EredmelMessage(EredmelMessage.LoggingLevel.FATAL,
						"Error in logging an error", Paths.get("/"), 0,
						Optional.empty()).log(logger);
		}
	}
	/**
	 * Defines if this function has an error
	 * 
	 * @return if this message is associated with an error
	 */
	public boolean hasError() {
		return ex.isPresent();
	}
	/**
	 * Gets the error associated with this function
	 * 
	 * @return If the error does not exist, gets
	 *         {@link RuntimeException#RuntimeException()}
	 */
	public Throwable getError() {
		return ex.orElseGet(RuntimeException::new);
	}
	@Override
	public String toString() {
		return "ErrorMessage [level=" + level + ", msg=" + msg + ", file="
				+ file + ", line=" + line + ", ex=" + ex + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ex == null) ? 0 : ex.hashCode());
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		result = prime * result + ((level == null) ? 0 : level.hashCode());
		result = prime * result + line;
		result = prime * result + ((msg == null) ? 0 : msg.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		EredmelMessage other = (EredmelMessage) obj;
		if (ex == null) {
			if (other.ex != null) return false;
		} else if (!ex.equals(other.ex)) return false;
		if (file == null) {
			if (other.file != null) return false;
		} else if (!file.equals(other.file)) return false;
		if (level != other.level) return false;
		if (line != other.line) return false;
		if (msg == null) {
			if (other.msg != null) return false;
		} else if (!msg.equals(other.msg)) return false;
		return true;
	}
}
