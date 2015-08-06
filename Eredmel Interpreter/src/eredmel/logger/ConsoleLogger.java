package eredmel.logger;

public class ConsoleLogger extends EredmelLogger {
	public static final ConsoleLogger DEFAULT = new ConsoleLogger(
			EredmelMessage.LoggingLevel.LOW, EredmelMessage.LoggingLevel.HIGH, EredmelMessage.LoggingLevel.FATAL);
	private final EredmelMessage.LoggingLevel printOut;
	private final EredmelMessage.LoggingLevel printErr;
	private final EredmelMessage.LoggingLevel exit;
	public ConsoleLogger(EredmelMessage.LoggingLevel printOut, EredmelMessage.LoggingLevel printErr,
			EredmelMessage.LoggingLevel exit) {
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
	public synchronized void logUnsafe(EredmelMessage message) {
		if (message.level.compareTo(printOut) < 0) return;
		StringBuffer result = new StringBuffer();
		result.append(String.format("%s\n\tAt Path: %s\n\tAt Line: %s\n",
				message.msg, message.file, message.line));
		if (message.hasError()) {
			result.append(String.format("\tCaused By: %s\n",
					message.getError()));
			for (StackTraceElement stackTraceEl : message.getError()
					.getStackTrace())
				result.append(String.format("\t\t%s\n", stackTraceEl));
		}
		(message.level.compareTo(printErr) >= 0 ? System.err : System.out)
				.print(result);
		// exit code -1 because obviously here in error;
		if (message.level.compareTo(exit) >= 0) System.exit(-1);
	}
}
