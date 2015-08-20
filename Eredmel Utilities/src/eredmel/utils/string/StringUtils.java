package eredmel.utils.string;

import java.util.Arrays;

public class StringUtils {
	public static String repeat(char c, int nRepeats) {
		char[] repeated = new char[nRepeats];
		Arrays.fill(repeated, '\t');
		return new String(repeated);
	}
}
