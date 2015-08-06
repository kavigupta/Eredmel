package eredmel.logger;


public abstract class EredmelLogger {
	public static final EredmelLogger DEFAULT_LOGGER = ConsoleLogger.DEFAULT;
	private static EredmelLogger _INSTANCE = DEFAULT_LOGGER;
	public static EredmelLogger get() {
		return _INSTANCE;
	}
	public static void setLogger(EredmelLogger logger) {
		EredmelLogger._INSTANCE = logger;
	}
	protected abstract void logUnsafe(EredmelMessage message) throws Throwable;
}
