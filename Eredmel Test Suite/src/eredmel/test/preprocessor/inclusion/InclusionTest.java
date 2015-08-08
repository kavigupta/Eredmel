package eredmel.test.preprocessor.inclusion;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import eredmel.logger.DebuggerLogger;
import eredmel.logger.EredmelLogger;
import eredmel.logger.EredmelMessage;
import eredmel.logger.EredmelMessage.LoggingLevel;
import eredmel.preprocessor.EredmelLine;
import eredmel.preprocessor.EredmelPreprocessor;
import eredmel.preprocessor.NumberedLine;
import eredmel.preprocessor.ReadFile;

public class InclusionTest {
	private static final DebuggerLogger log = new DebuggerLogger();
	@Before
	public void init() {
		EredmelLogger.set(log);
	}
	@Test
	public void simpleInclusion() {
		testInclusion("a/simple.edmh");
	}
	@Test
	public void wspaceInclusion() {
		testInclusion("a/includespace.edmh");
	}
	@Test
	public void selfReference() {
		testInclusionError(
				"a/selfref.edmh",
				new EredmelMessage(
						LoggingLevel.HIGH,
						"Circular reference loop:\n\teg/inclusion/a/selfref.edmh\n\teg/inclusion/a/selfref.edmh",
						Paths.get("eg/inclusion/a/selfref.edmh"), 0,
						Optional.empty()));
	}
	@Test
	public void pairReference() {
		testInclusionError(
				"a/pairref1.edmh",
				new EredmelMessage(
						LoggingLevel.HIGH,
						"Circular reference loop:\n\teg/inclusion/a/pairref1.edmh\n\teg/inclusion/a/pairref2.edmh\n\teg/inclusion/a/pairref1.edmh",
						Paths.get("eg/inclusion/a/pairref1.edmh"), 0,
						Optional.empty()));
	}
	@Test
	public void fileNotFound() {
		testInclusionError(
				"a/404.edmh",
				new EredmelMessage(LoggingLevel.HIGH,
						"File \"eg/inclusion/a/404.edmh\" Not Found",
						Paths.get("eg/inclusion/a/404.edmh"), 0, Optional
								.empty()));
	}
	public static void testInclusionError(String path, EredmelMessage expect) {
		log.clear();
		try {
			ReadFile<EredmelLine, Integer> file = EredmelPreprocessor
					.loadFile(Paths.get(relative(path)), new ArrayList<>());
			System.out.println(file);
		} catch (Throwable t) {
			assertTrue("Has message", log.containsMessage());
			EredmelMessage actual = log.pop();
			assertEquals(expect.level, actual.level);
			assertEquals(expect.msg, actual.msg);
			assertEquals(expect.file, actual.file);
			assertEquals(expect.line, actual.line);
			assertTrue("Has only one message", !log.containsMessage());
			return;
		}
		throw new AssertionError("No error raised");
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
		for (int i = 0; i < normExpected.numLines(); i++) {
			assertEquals(format("Line %s:", i), normExpected.lineAt(i).line,
					normActual.lineAt(i).displayWithTabs());
		}
		assertEquals("File Size", normExpected.numLines(), normActual.numLines());
	}
	private static String relative(String path) {
		return "eg/inclusion/" + path;
	}
}