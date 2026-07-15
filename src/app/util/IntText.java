package app.util;

import java.util.function.Consumer;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.commons.Strings;

import app.editors.IEditor;

public class IntText {

	private final IEditor editor;
	private final Composite parent;
	private final FormToolkit tk;

	private String label;
	private String tooltip;
	private Integer initial;
	private Consumer<Integer> onChange;

	private IntText(
		IEditor editor, Composite parent, FormToolkit tk
	) {
		this.editor = editor;
		this.parent = parent;
		this.tk = tk;
	}

	public static IntText on(
		IEditor editor, Composite parent, FormToolkit tk
	) {
		return new IntText(editor, parent, tk);
	}

	public IntText withLabel(String label) {
		this.label = label;
		return this;
	}

	public IntText withTooltip(String tooltip) {
		this.tooltip = tooltip;
		return this;
	}

	public IntText withInitial(Integer initial) {
		this.initial = initial;
		return this;
	}

	public IntText onChange(Consumer<Integer> onChange) {
		this.onChange = onChange;
		return this;
	}

	public void render() {
		var text = UI.formText(parent, tk, label, tooltip);
		if (initial != null) {
			text.setText(initial.toString());
		}
		text.addModifyListener(_ -> {
			var s = text.getText();
			if (Strings.isBlank(s)) {
				onChange.accept(null);
				editor.setDirty();
				return;
			}
			try {
				int i = Integer.parseInt(s.trim());
				onChange.accept(i);
				text.setBackground(Colors.white());
			} catch (Exception ex) {
				text.setBackground(Colors.errorColor());
			}
			editor.setDirty();
		});
	}
}
