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
import epd.util.Strings;

public class TextBuilder {

	private final IEditor editor;
	private final FormPage page;
	private final FormToolkit tk;

	public TextBuilder(IEditor editor, FormPage page, FormToolkit tk) {
		this.editor = editor;
		this.page = page;
		this.tk = tk;
	}

	public void text(
			Composite comp,
			String label,
			String initial,
			Consumer<String> fn) {
		this.text(comp, label, null, initial, fn);
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

	public void text(
			Composite comp,
			String label,
			List<LangString> list) {
		text(comp, label, null, list);
	}

	public void text(
			Composite comp,
			String label,
			String tooltip,
			List<LangString> list) {
		Text t = UI.formText(comp, tk, label, tooltip);
		make(label, list, t);
	}

	public void multiText(
			Composite comp,
			String label,
			List<LangString> list) {
		multiText(comp, label, null, list);
	}

	public void multiText(
			Composite comp,
			String label,
			String tooltip,
			List<LangString> list) {
		Text t = UI.formMultiText(comp, tk, label, tooltip);
		make(label, list, t);
	}

	private void make(String label, List<LangString> list, Text t) {
		t.setText(App.s(list));
		t.addModifyListener(e -> {
			String value = t.getText();
			if (!Strings.nullOrEmpty(value)) {
				LangString.set(list, value, App.lang());
			} else {
				LangString.remove(list, App.lang());
			}
			editor.setDirty();
		});
		TranslationView.register(page, label, t, list);
	}

}
