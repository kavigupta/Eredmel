package eredmel.preprocessor;

import java.nio.file.Path;
import java.util.List;

public class LinkedFile {
	public final List<EredmelLine> lines;
	public final Path path;
	public final int tabwidth;
	public LinkedFile(List<EredmelLine> lines, Path path, int tabwidth) {
		this.lines = lines;
		this.path = path;
		this.tabwidth = tabwidth;
	}
	public int nLines() {
		return lines.size();
	}
	public EredmelLine lineAt(int i) {
		return lines.get(i);
	}
}
