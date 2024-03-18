package app.rcp;

import org.eclipse.swt.widgets.Text;

import app.util.Colors;
import epd.util.Strings;

import java.util.OptionalDouble;
import java.util.function.Consumer;

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
		var v = d != null
			? d.toString()
			: "";
		text.setText(v);
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
		validateNumber(text, $ -> {});
	}

	public static void validateNumber(Text text, Consumer<OptionalDouble> fn) {
		if (text == null)
			return;
		text.addModifyListener(_e -> {
			var s = text.getText();
			if (Strings.nullOrEmpty(s)) {
				text.setBackground(Colors.white());
				text.setToolTipText(null);
				fn.accept(OptionalDouble.empty());
				return;
			}
			try {
				var d = 	Double.parseDouble(s);
				text.setBackground(Colors.white());
				text.setToolTipText(null);
				fn.accept(OptionalDouble.of(d));
			} catch (Exception e) {
				text.setBackground(Colors.errorColor());
				text.setToolTipText(s + " is not a number");
			}
		});
	}
}
