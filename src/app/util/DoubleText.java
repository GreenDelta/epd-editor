package app.util;

import java.util.function.Consumer;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.commons.Strings;

import app.editors.IEditor;

public class DoubleText {

	private final IEditor editor;
	private final Composite parent;
	private final FormToolkit tk;

	private String label;
	private String tooltip;
	private Double initial;
	private Consumer<Double> onChange;

	private DoubleText(
		IEditor editor, Composite parent, FormToolkit tk
	) {
		this.editor = editor;
		this.parent = parent;
		this.tk = tk;
	}

	public static DoubleText on(
		IEditor editor, Composite parent, FormToolkit tk
	) {
		return new DoubleText(editor, parent, tk);
	}

	public DoubleText withLabel(String label) {
		this.label = label;
		return this;
	}

	public DoubleText withTooltip(String tooltip) {
		this.tooltip = tooltip;
		return this;
	}

	public DoubleText withInitial(Double initial) {
		this.initial = initial;
		return this;
	}

	public DoubleText onChange(Consumer<Double> onChange) {
		this.onChange = onChange;
		return this;
	}

	public void render() {
		var text = UI.formText(parent, tk, label, tooltip);
		if (initial != null) {
			text.setText(Double.toString(initial));
		}
		text.addModifyListener(_ -> {
			var s = text.getText();
			if (Strings.isBlank(s)) {
				onChange.accept(null);
				editor.setDirty();
				return;
			}
			try {
				double d = Double.parseDouble(s.trim());
				onChange.accept(d);
				text.setBackground(Colors.white());
			} catch (Exception ex) {
				text.setBackground(Colors.errorColor());
			}
			editor.setDirty();
		});
	}
}
