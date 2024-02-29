package app.rcp;

import org.eclipse.swt.widgets.Text;

import app.util.Colors;
import epd.util.Strings;

public class Texts {

	private Texts() {
	}

	public static Text set(Text text, String value) {
		if (text == null)
			return null;
		if (value == null)
			text.setText("");
		else
			text.setText(value);
		return text;
	}

	public static Text set(Text text, Double d) {
		if (text == null)
			return null;
		if (d == null)
			text.setText("");
		else
			text.setText(d.toString());
		return text;
	}

	public static String getString(Text text) {
		if (text == null)
			return null;
		String s = text.getText();
		return Strings.nullOrEmpty(s) ? null : s.trim();
	}

	public static Double getDouble(Text text) {
		String s = getString(text);
		if (s == null)
			return null;
		try {
			return Double.parseDouble(s);
		} catch (Exception e) {
			return null;
		}
	}

	public static void validateNumber(Text text) {
		if (text == null)
			return;
		text.addModifyListener(_e -> {
			String s = text.getText();
			if (Strings.nullOrEmpty(s)) {
				text.setBackground(Colors.white());
				text.setToolTipText(null);
				return;
			}
			try {
				Double.parseDouble(s);
				text.setBackground(Colors.white());
				text.setToolTipText(null);
			} catch (Exception e) {
				text.setBackground(Colors.errorColor());
				text.setToolTipText(s + " is not a number");
			}
		});
	}
}
