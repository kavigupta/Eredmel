package eredmel.logger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public final class EredmelMessage {
	public static enum LoggingLevel {
		DEBUG, LOW, MED, HIGH, FATAL
	}
	public static EredmelMessage unresolvedInclusion(String path, Path file,
			int i) {
		return new EredmelMessage(EredmelMessage.LoggingLevel.HIGH,
				String.format("Unresolved Inclusion: File %s Not Found",
						path), file, i, Optional.empty());
	}
	public static EredmelMessage errorLoadingFile(IOException e, Path toLoad) {
		return new EredmelMessage(EredmelMessage.LoggingLevel.HIGH,
				String.format("Error Loading File %s: %s", toLoad, e),
				toLoad, 0, Optional.of(e));
	}
	public static EredmelMessage guessAtTabwidth(int tabwidth, Path file) {
		return new EredmelMessage(EredmelMessage.LoggingLevel.LOW,
				String.format(""), file, 0, Optional.empty());
	}
	public final EredmelMessage.LoggingLevel level;
	public final String msg;
	public final Path file;
	public final int line;
	public final Optional<? extends Throwable> ex;
	public EredmelMessage(EredmelMessage.LoggingLevel level, String msg,
			Path file, int line, Optional<? extends Throwable> ex) {
		this.level = level;
		this.msg = msg;
		this.file = file;
		this.line = line;
		this.ex = ex;
	}
	public void log() {
		log(EredmelLogger.get());
	}
	public void log(EredmelLogger logger) {
		try {
			logger.logUnsafe(this);
		} catch (Throwable e) {
			if (!file.equals(Paths.get("/")))
				new EredmelMessage(EredmelMessage.LoggingLevel.FATAL,
						"Error in logging an error", Paths.get("/"), 0,
						Optional.empty()).log(logger);
		}
		// enforces fatal -> exit. If logUnsafe() triggered an exit, then
		// exit
		if (level == EredmelMessage.LoggingLevel.FATAL) System.exit(-1);
	}
	public boolean hasError() {
		return ex.isPresent();
	}
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
