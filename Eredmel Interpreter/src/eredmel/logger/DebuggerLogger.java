package eredmel.logger;

import java.util.Stack;

import eredmel.logger.EredmelMessage.LoggingLevel;

/**
 * A logger that stores all the error messages it receives on a stack and pops
 * them off as demanded
 * 
 * @author Kavi Gupta
 */
public class DebuggerLogger extends EredmelLogger {
	private Stack<EredmelMessage> messages;
	/**
	 * Creates a new Debugger Logger with an empty stack
	 */
	public DebuggerLogger() {
		messages = new Stack<>();
	}
	@Override
	public synchronized void log(EredmelMessage message) {
		messages.push(message);
		if (message.level.compareTo(LoggingLevel.HIGH) >= 0)
			throw new ControlFlow();
	}
	/**
	 * Whether this logger has messages or not
	 */
	public boolean containsMessage() {
		return messages.size() != 0;
	}
	/**
	 * Clears the log
	 */
	public void clear() {
		messages.clear();
	}
	/**
	 * Gets the latest message
	 * 
	 * @return the latest message
	 */
	public EredmelMessage pop() {
		return messages.pop();
	}
}
