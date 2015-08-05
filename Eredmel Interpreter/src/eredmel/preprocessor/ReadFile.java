package eredmel.preprocessor;

import java.nio.file.Path;
import java.util.List;

public class ReadFile<LINE, TAB> {
	final List<LINE> lines;
	public final Path path;
	public final TAB tabwidth;
	ReadFile(List<LINE> lines, Path path, TAB tabwidth) {
		this.lines = lines;
		this.path = path;
		this.tabwidth = tabwidth;
	}
	public LINE lineAt(int i) {
		return lines.get(i);
	}
	public int nLines() {
		return lines.size();
	}
}
