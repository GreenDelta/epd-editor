package app.util;

import java.util.function.Consumer;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import app.editors.IEditor;

public class TextBuilder {

	private final IEditor editor;
	private final FormToolkit tk;

	public TextBuilder(IEditor editor, FormToolkit tk) {
		this.editor = editor;
		this.tk = tk;
	}

	public void text(
			Composite comp,
			String label,
			String tooltip,
			String initial,
			Consumer<String> fn) {
		Text t = UI.formText(comp, tk, label, tooltip);
		if (initial != null)
			t.setText(initial);
		t.addModifyListener(e -> {
			fn.accept(t.getText());
			editor.setDirty();
		});
	}

}
