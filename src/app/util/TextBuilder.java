package app.util;

import java.util.List;
import java.util.function.Consumer;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.commons.LangString;

import app.App;
import app.editors.IEditor;
import app.editors.TranslationView;

public class TextBuilder {

	private final IEditor editor;
	private final FormPage page;
	private final FormToolkit tk;

	public TextBuilder(IEditor editor, FormPage page, FormToolkit tk) {
		this.editor = editor;
		this.page = page;
		this.tk = tk;
	}

	public void text(Composite comp, String label, String initial,
			Consumer<String> fn) {
		Text t = UI.formText(comp, tk, label);
		if (initial != null)
			t.setText(initial);
		t.addModifyListener(e -> {
			fn.accept(t.getText());
			editor.setDirty();
		});
	}

	public void text(Composite comp, String label, List<LangString> list) {
		Text t = UI.formText(comp, tk, label);
		make(label, list, t);
	}

	public void multiText(Composite comp, String label, List<LangString> list) {
		Text t = UI.formMultiText(comp, tk, label);
		make(label, list, t);
	}

	private void make(String label, List<LangString> list, Text t) {
		t.setText(App.s(list));
		TranslationView.register(page, label, t, list);
		t.addModifyListener(e -> {
			LangString.set(list, t.getText(), App.lang);
			editor.setDirty();
		});
	}

}
