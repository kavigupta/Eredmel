package eredmel.logger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public abstract class EredmelLogger {
	public static final EredmelLogger DEFAULT_LOGGER = ConsoleLogger.DEFAULT;
	public static enum LoggingLevel {
		DEBUG, LOW, MED, HIGH, FATAL
	}
	private static EredmelLogger _INSTANCE = DEFAULT_LOGGER;
	public static EredmelLogger get() {
		return _INSTANCE;
	}
	public static void setLogger(EredmelLogger logger) {
		EredmelLogger._INSTANCE = logger;
	}
	protected abstract void logUnsafe(LoggingLevel level, String msg,
			Path file, int line, Optional<? extends Throwable> ex)
			throws Throwable;
	public final void log(LoggingLevel level, String msg, Path file, int line,
			Optional<? extends Throwable> ex) {
		try {
			logUnsafe(level, msg, file, line, ex);
		} catch (Throwable e) {
			if (!file.equals(Paths.get("/")))
				log(LoggingLevel.FATAL, "Error in logging an error",
						Paths.get("/"), 0, Optional.empty());
		}
		// enforces fatal -> exit. If logUnsafe() triggered an exit, then exit
		if (level == LoggingLevel.FATAL) System.exit(-1);
	}
	public void unresolvedInclusion(String path, Path file, int i) {
		log(LoggingLevel.HIGH, String.format(
				"Unresolved Inclusion: File %s Not Found", path), file, i,
				Optional.empty());
	}
	public void errorLoadingFile(IOException e, Path toLoad) {
		log(LoggingLevel.HIGH,
				String.format("Error Loading File %s: %s", toLoad, e),
				toLoad, 0, Optional.of(e));
	}
	public void guessAtTabwidth(int tabwidth, Path file) {
		log(LoggingLevel.LOW, String.format(""), file, 0, Optional.empty());
	}
}
