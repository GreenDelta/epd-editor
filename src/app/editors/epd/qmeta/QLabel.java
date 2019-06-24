package app.editors.epd.qmeta;

import org.eclipse.osgi.util.NLS;

public class QLabel extends NLS {

	public static String Q1;
	public static String Q11;
	public static String Q12;
	public static String Q13;
	public static String Q14;

	static {
		NLS.initializeMessages("app.editors.epd.qmeta.qlabels", QLabel.class);
	}

	private QLabel() {
	}

	static String get(String id) {
		if (id == null)
			return "?";

		switch (id) {
		case "1":
			return Q1;
		case "1.1":
			return Q11;
		case "1.2":
			return Q12;
		case "1.3":
			return Q13;
		case "1.4":
			return Q14;
		default:
			return "?";
		}
	}
}
