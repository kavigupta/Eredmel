package eredmel.test.preprocessor;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.junit.Test;

import eredmel.preprocessor.EredmelLine;
import eredmel.preprocessor.EredmelPreprocessor;
import eredmel.preprocessor.NumberedLine;
import eredmel.preprocessor.ReadFile;

public class ReplacerTest {
	@Test
	public void simpleNonOverlappingTests() {
		testReplace("basic1");
		testReplace("basic2");
	}
	public static void testReplace(String path) {
		ReadFile<NumberedLine, Void> replExpected;
		try {
			replExpected = EredmelPreprocessor.readFile(Paths
					.get(relative(path)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		ReadFile<EredmelLine, Integer> replActual = EredmelPreprocessor
				.applyReplaces(EredmelPreprocessor.loadFile(
						Paths.get(relative(path + ".edmh")),
						new ArrayList<>()));
		assertEquals(replExpected.toString(), replActual.toString());
	}
	private static String relative(String path) {
		return "eg/replace/" + path;
	}
}
