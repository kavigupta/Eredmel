package eredmel.test.preprocessor;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.junit.Test;

import eredmel.config.EredmelConfiguration;
import eredmel.preprocessor.EredmelLine;
import eredmel.preprocessor.EredmelPreprocessor;
import eredmel.preprocessor.NumberedLine;
import eredmel.preprocessor.ReadFile;

public class ReplacerTest {
	@Test
	public void simpleNonOverlappingTest() {
		testReplace("basic");
	}
	@Test
	public void embedTest() {
		testReplace("embed");
	}
	@Test
	public void recursiveTest() {
		testReplace("recursive");
	}
	public static void testReplace(String path) {
		ReadFile<NumberedLine> replExpected;
		try {
			replExpected = EredmelPreprocessor.readFile(
					Paths.get(relative(path)),
					EredmelConfiguration.getDefault());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		ReadFile<EredmelLine> replActual = EredmelPreprocessor
				.applyReplaces(EredmelPreprocessor.loadFile(
						Paths.get(relative(path + ".edmh")),
						new ArrayList<>(),
						EredmelConfiguration.getDefault()));
		assertEquals(replExpected.toString(), replActual.toString());
	}
	private static String relative(String path) {
		return "eg/replace/" + path;
	}
}
