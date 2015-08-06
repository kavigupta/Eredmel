package eredmel.logger;

import java.util.Stack;

public class DebuggerLogger extends EredmelLogger {
	private Stack<EredmelMessage> messages;
	@Override
	public synchronized void logUnsafe(EredmelMessage message) {
		messages.push(message);
	}
	public boolean containsMessage() {
		return messages.size() != 0;
	}
	public void clear() {
		messages.clear();
	}
	public EredmelMessage pop() {
		return messages.pop();
	}
}
