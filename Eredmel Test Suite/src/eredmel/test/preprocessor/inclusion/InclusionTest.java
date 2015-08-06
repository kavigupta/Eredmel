package eredmel.test.preprocessor.inclusion;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.junit.Test;

import eredmel.preprocessor.EredmelLine;
import eredmel.preprocessor.EredmelPreprocessor;
import eredmel.preprocessor.NumberedLine;
import eredmel.preprocessor.ReadFile;

public class InclusionTest {
	@Test
	public void simpleInclusion() {
		testInclusion("a/simple.edmh");
	}
	@Test
	public void wspaceInclusion() {
		testInclusion("a/includespace.edmh");
	}
	public static void testInclusion(String path) {
		ReadFile<NumberedLine, Void> normExpected;
		try {
			normExpected = EredmelPreprocessor.readFile(Paths
					.get(relative(path) + "i"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		ReadFile<EredmelLine, Integer> normActual = EredmelPreprocessor
				.loadFile(Paths.get(relative(path)), new ArrayList<>());
		for (int i = 0; i < normExpected.nLines(); i++) {
			assertEquals(format("Line %s:", i), normExpected.lineAt(i).line,
					normActual.lineAt(i).displayWithTabs());
		}
		assertEquals("File Size", normExpected.nLines(), normActual.nLines());
	}
	private static String relative(String path) {
		return "eg/inclusion/" + path;
	}
}