package eredmel.utils.io;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class IOUtils {
	/**
	 * @param workingDir
	 *        the directory of the file currently being processed
	 * @param paths
	 *        the search path to use; this should contain the paths to search
	 *        in top-bottom order of priority
	 * @param file
	 *        the relative file path
	 * @return
	 *         {@code Optional.of(the final path)}, or {@code Optional.empty()}
	 *         if the file could not be found
	 */
	public static Optional<Path> resolve(Path workingDir, List<Path> paths,
			String file) {
		paths.add(workingDir);
		Optional<Path> resolved = Optional.empty();
		for (Path path : paths) {
			// non-directory file: get parent
			if (Files.exists(path) && !Files.isDirectory(path))
				path = path.getParent();
			Path possibility = path.resolve(file).normalize();
			if (Files.exists(possibility) && !Files.isDirectory(possibility)) {
				resolved = Optional.of(possibility);
				break;
			}
		}
		paths.remove(paths.size() - 1);
		return resolved;
	}
}
