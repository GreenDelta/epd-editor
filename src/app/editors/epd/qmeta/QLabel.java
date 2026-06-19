package app.editors.epd.qmeta;

class QLabel {

	private QLabel() {
	}

	static String wrap(String text) {
		if (text == null)
			return "";
		var words = text.split("\\s");
		var s = new StringBuilder();
		var line = new StringBuilder();
		for (String w : words) {
			if (!line.isEmpty()
				&& (line.length() + w.length() > 120)) {
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
