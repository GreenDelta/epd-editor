package app.rcp;

import org.eclipse.swt.widgets.Text;

public class Texts {

	private Texts() {
	}

	public static Text set(Text widget, String value) {
		if (widget == null)
			return widget;
		if (value == null)
			widget.setText("");
		else
			widget.setText(value);
		return widget;
	}

}
