package eredmel.test.preprocessor;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.junit.Test;

import eredmel.preprocessor.EredmelLine;
import eredmel.preprocessor.EredmelPreprocessor;
import eredmel.preprocessor.NumberedLine;
import eredmel.preprocessor.ReadFile;

public class NormalizerTest {
	@Test
	public void tabTest() {
		testNormalization(4, "tab.edmh");
	}
	@Test
	public void tabSpaceDefaultTest() {
		testNormalization(4, "tab_space_default.edmh");
	}
	@Test
	public void tabSpaceTest() {
		testNormalization(4, "tab_space_4.edmh");
		testNormalization(5, "tab_space_5.edmh");
	}
	@Test
	public void spacesPureTest() {
		testNormalization(2, "spaces_2.edmh");
		testNormalization(3, "spaces_3.edmh");
		testNormalization(4, "spaces_4.edmh");
		testNormalization(5, "spaces_5.edmh");
		testNormalization(8, "spaces_8.edmh");
	}
	@Test
	public void slightlyOffTestWDecl() {
		testNormalization(4, "spaces_4_off_decl.edmh");
		testNormalization(5, "spaces_5_off_decl.edmh");
	}
	/**
	 * Should be viewed as a warning to always make an explicit declaration of
	 * tabwidth
	 */
	@Test
	public void slightlyOffTestWODecl() {
		testNormalization(1, "spaces_4_off_nodecl.edmh",
				"normalized_4tab.edmh");
	}
	@Test
	public void spaces8Actually4() {
		testNormalization(4, "spaces_8_actually_4.edmh",
				"normalized_doubletab.edmh");
	}
	public static void testNormalization(int expectedTabwidth, String... paths) {
		try {
			ReadFile<NumberedLine> original = readAll(paths[0]);
			ReadFile<NumberedLine> normExpected = readAll(paths.length == 1 ? "normalized.edmh"
					: paths[1]);
			ReadFile<EredmelLine> normActual = EredmelPreprocessor
					.normalize(original);
			assertEquals("Tabwidth", expectedTabwidth, normActual.config()
					.tabwidth());
			for (int i = 0; i < normExpected.numLines(); i++) {
				assertEquals(format("Line %s:", i),
						normExpected.lineAt(i).line, normActual.lineAt(i)
								.displayWithTabs());
			}
		} catch (IOException | URISyntaxException e) {
			throw new AssertionError(e);
		}
	}
	public static ReadFile<NumberedLine> readAll(String path)
			throws IOException, URISyntaxException {
		return EredmelPreprocessor.readFile(Paths
				.get("eg/normalizer/" + path));
	}
}
