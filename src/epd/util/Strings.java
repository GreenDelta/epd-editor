package epd.util;

public class Strings {

	private Strings() {
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
