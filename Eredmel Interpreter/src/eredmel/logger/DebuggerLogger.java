package eredmel.logger;

import java.util.Stack;

/**
 * A logger that stores all the error messages it receives on a stack and pops
 * them off as demanded
 * 
 * @author Kavi Gupta
 */
public class DebuggerLogger extends EredmelLogger {
	private Stack<EredmelMessage> messages;
	@Override
	public synchronized void log(EredmelMessage message) {
		messages.push(message);
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
