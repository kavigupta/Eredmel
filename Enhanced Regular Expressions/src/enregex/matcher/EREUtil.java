package enregex.matcher;

public class EREUtil {
	public static int countParenthesis(String string) {
		int paren = 0;
		for (int i = 0; i < string.length(); i++) {
			if (string.charAt(i) == '(')
				paren++;
			else if (string.charAt(i) == ')')
				paren--;
			else if (string.charAt(i) == '\'')
				i = findCloseQuote(string, i);
		}
		return paren;
	}
	public static boolean parensMatch(String string) {
		int paren = 0;
		for (int i = 0; i < string.length(); i++) {
			if (string.charAt(i) == '(')
				paren++;
			else if (string.charAt(i) == ')') {
				paren--;
				if (paren == 0 && i != string.length() - 1) return false;
			} else if (string.charAt(i) == '\'')
				i = findCloseQuote(string, i);
		}
		return paren == 0;
	}
	private static int findCloseQuote(String input, int i) {
		char open = input.charAt(i);
		int backslashstate = 0;
		for (int j = i + 1; j < input.length(); j++) {
			if (input.charAt(j) == open && backslashstate % 2 == 0)
				return j;
			if (input.charAt(j) == '\\')
				backslashstate++;
			else backslashstate = 0;
		}
		return -1;
	}
}
