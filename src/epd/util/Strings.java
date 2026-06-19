package epd.util;

public class Strings {

	private Strings() {
	}

	public static boolean nullOrEqual(String string1, String string2) {
		return (string1 == null && string2 == null)
			|| (string1 != null && string1.equals(string2));
	}

	public static String wrap(String text, int len) {
		if (text == null)
			return "";
		String[] words = text.split("\\s");
		StringBuilder s = new StringBuilder();
		StringBuilder line = new StringBuilder();
		for (String w : words) {
			if (!line.isEmpty()
				&& (line.length() + w.length() > len)) {
				if (!s.isEmpty()) {
					s.append('\n');
				}
				s.append(line);
				line = new StringBuilder();
			}
			if (!line.isEmpty()) {
				line.append(' ');
			}
			line.append(w);
		}
		if (!line.isEmpty()) {
			if (!s.isEmpty()) {
				s.append('\n');
			}
			s.append(line);
		}
		return s.toString();
	}
}
