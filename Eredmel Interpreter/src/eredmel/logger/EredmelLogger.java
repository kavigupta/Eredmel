package eredmel.logger;

/**
 * A class representing a logger
 * 
 * @author Kavi Gupta
 */
public abstract class EredmelLogger {
	/**
	 * The default logger, a Console Logger
	 */
	public static final EredmelLogger DEFAULT_LOGGER = ConsoleLogger.DEFAULT;
	private static EredmelLogger _INSTANCE = DEFAULT_LOGGER;
	/**
	 * Gets the current logger
	 * 
	 * @return the current logger
	 */
	public static EredmelLogger get() {
		return _INSTANCE;
	}
	/**
	 * Sets the current logger
	 * 
	 * @param logger
	 *        the logger to set this to
	 */
	public static void set(EredmelLogger logger) {
		EredmelLogger._INSTANCE = logger;
	}
	/**
	 * Logs the given message
	 * 
	 * @param message
	 *        the message to log
	 * @throws Throwable
	 *         an "Error thrown in displaying Error" is attempted to be
	 *         displayed if thrown
	 */
	protected abstract void log(EredmelMessage message) throws Throwable;
}
