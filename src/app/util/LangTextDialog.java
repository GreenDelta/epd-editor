package app.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.commons.LangString;

import epd.util.Strings;

public class LangTextDialog extends FormDialog {

	private final List<LangString> strings;

	private LangTextDialog(List<LangString> initial) {
		super(UI.shell());
		this.strings = new ArrayList<>(initial);
	}

	static Optional<List<LangString>> open(List<LangString> initial) {
		var dialog = new LangTextDialog(initial == null
				? Collections.emptyList()
				: initial);
		return dialog.open() == OK
				? Optional.of(dialog.strings)
				: Optional.empty();
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Edit multi-language text");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(650, 500);
	}

	@Override
	protected void createFormContent(IManagedForm mForm) {
		var tk = mForm.getToolkit();
		var body = UI.formBody(mForm.getForm(), tk);
		UI.gridLayout(body, 2);

		strings.stream()
				.map(s -> LangBox.of(s, strings))
				.sorted((box1, box2) -> Strings.compare(box1.lang(), box2.lang()))
				.forEach(box -> box.render(body, tk));

	}

	private record LangBox(
			String lang, String val, List<LangString> strings) {

		static LangBox of(LangString s, List<LangString> strings) {
			var lang = Strings.nullOrEmpty(s.getLang())
					? "en"
					: s.getLang();
			return new LangBox(lang, s.getValue(), strings);
		}

		void render(Composite comp, FormToolkit tk) {
			var text = UI.formMultiText(comp, tk, label());
			if (val != null) {
				text.setText(val);
			}
			text.addModifyListener($ -> {
				var s = text.getText();
				if (Strings.notEmpty(s)) {
					LangString.set(strings, val, lang);
				} else {
					LangString.remove(strings, lang);
				}
			});
		}

		private String label() {
			var lang = lang();
			if ("?".equals(lang))
				return "?";
			try {
				var loc = Locale.forLanguageTag(lang);
				if (loc == null)
					return lang;
				var name = loc.getDisplayLanguage();
				return Strings.notEmpty(name)
						? lang + " - " + name
						: lang;
			} catch (Exception e) {
				return lang;
			}
		}
	}
}
