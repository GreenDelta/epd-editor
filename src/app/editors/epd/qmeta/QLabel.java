package app.editors.epd.qmeta;

import org.eclipse.osgi.util.NLS;

public class QLabel extends NLS {

	public static String Q1;
	public static String Q11;
	public static String Q12;
	public static String Q13;
	public static String Q14;

	public static String Q2;
	public static String Q21;
	public static String Q22;
	public static String Q23;
	public static String Q24;

	public static String Q3;
	public static String Q31;
	public static String Q32;
	public static String Q33;
	public static String Q34;

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

		case "2":
			return Q2;
		case "2.1":
			return Q21;
		case "2.2":
			return Q22;
		case "2.3":
			return Q23;
		case "2.4":
			return Q24;

		case "3":
			return Q3;
		case "3.1":
			return Q31;
		case "3.2":
			return Q32;
		case "3.3":
			return Q33;
		case "3.4":
			return Q34;

		default:
			return "?";
		}
	}
}
