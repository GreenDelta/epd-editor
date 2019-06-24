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

	public static String Q4;
	public static String Q41;
	public static String Q42;
	public static String Q43;
	public static String Q44;

	public static String Q5;
	public static String Q51;
	public static String Q52;
	public static String Q53;
	public static String Q54;
	public static String Q55;
	public static String Q56;
	public static String Q57;
	public static String Q58;
	public static String Q59;

	public static String Q6;
	public static String Q61;
	public static String Q62;
	public static String Q63;
	public static String Q64;

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

		case "4":
			return Q4;
		case "4.1":
			return Q41;
		case "4.2":
			return Q42;
		case "4.3":
			return Q43;
		case "4.4":
			return Q44;

		case "5":
			return Q5;
		case "5.1":
			return Q51;
		case "5.2":
			return Q52;
		case "5.3":
			return Q53;
		case "5.4":
			return Q54;
		case "5.5":
			return Q55;
		case "5.6":
			return Q56;
		case "5.7":
			return Q57;
		case "5.8":
			return Q58;

		case "6":
			return Q6;
		case "6.1":
			return Q61;
		case "6.2":
			return Q62;
		case "6.3":
			return Q63;
		case "6.4":
			return Q64;

		default:
			return "?";
		}
	}
}
