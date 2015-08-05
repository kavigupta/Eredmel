package eredmel.logger;

import java.nio.file.Path;
import java.util.Optional;

public class ConsoleLogger extends EredmelLogger {
	public static final ConsoleLogger DEFAULT = new ConsoleLogger(
			LoggingLevel.LOW, LoggingLevel.HIGH, LoggingLevel.FATAL);
	private final LoggingLevel printOut;
	private final LoggingLevel printErr;
	private final LoggingLevel exit;
	public ConsoleLogger(LoggingLevel printOut, LoggingLevel printErr,
			LoggingLevel exit) {
		if (printOut.compareTo(printErr) > 0)
			throw new IllegalArgumentException(String.format(
					"printOut (%s) must be lower than printErr (%s)",
					printOut, printErr));
		if (printErr.compareTo(exit) > 0)
			throw new IllegalArgumentException(String.format(
					"printErr (%s) must be lower than exit (%s)",
					printErr, exit));
		this.printOut = printOut;
		this.printErr = printErr;
		this.exit = exit;
	}
	@Override
	public synchronized void logUnsafe(LoggingLevel level, String msg,
			Path file, int line, Optional<? extends Throwable> ex) {
		if (level.compareTo(printOut) < 0) return;
		StringBuffer result = new StringBuffer();
		result.append(String.format("%s\n\tAt Path: %s\n\tAt Line: %s\n",
				msg, file, line));
		if (ex.isPresent()) {
			result.append(String.format("\tCaused By: %s\n", ex.get()));
			for (StackTraceElement stackTraceEl : ex.get().getStackTrace())
				result.append(String.format("\t\t%s\n", stackTraceEl));
		}
		(level.compareTo(printErr) >= 0 ? System.err : System.out)
				.print(result);
		// exit code -1 because obviously here in error;
		if (level.compareTo(exit) >= 0) System.exit(-1);
	}
}
