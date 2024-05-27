package app.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.commons.LangString;

import app.App;
import app.M;
import epd.util.Strings;

public class LangTextDialog extends FormDialog {

	private final List<LangString> strings;
	private final boolean multiLine;

	private LangTextDialog(List<LangString> strings, boolean multiLine) {
		super(UI.shell());
		this.strings = strings;
		this.multiLine = multiLine;
	}

	static Optional<List<LangString>> open(List<LangString> initial) {
		return open(initial, false);
	}

	static Optional<List<LangString>> openMultiLine(
			List<LangString> initial) {
		return open(initial, true);
	}

	private static Optional<List<LangString>> open(
			List<LangString> initial, boolean multiLine) {
		var strings = initial == null
				? new ArrayList<LangString>()
				: new ArrayList<>(initial);
		var dialog = new LangTextDialog(strings, multiLine);
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
		int height = multiLine ? 500 : 350;
		return new Point(800, height);
	}

	@Override
	protected void createFormContent(IManagedForm mForm) {
		var tk = mForm.getToolkit();
		var body = UI.formBody(mForm.getForm(), tk);
		var boxComp = tk.createComposite(body);
		UI.gridData(boxComp, true, false);
		UI.gridLayout(boxComp, 2);

		var boxes = LangBox.allOf(strings);
		boxes.forEach(box -> box.render(boxComp, tk, multiLine));

		var langComp = tk.createComposite(body);
		langComp.setLayoutData(
				new GridData(SWT.CENTER, SWT.BEGINNING, true, false));
		UI.gridLayout(langComp, 3);
		tk.createLabel(langComp, "Add language");
		var combo = LangCombo.create(langComp);
		var btn = tk.createButton(langComp, M.Add, SWT.NONE);

		if (contains(boxes, combo.getSelected())) {
			btn.setEnabled(false);
		}
		combo.onChange(code -> btn.setEnabled(!contains(boxes, code)));
		Controls.onSelect(btn, $ -> {
			var code = combo.getSelected();
			var box = new LangBox(code, "", strings);
			boxes.add(box);
			box.render(boxComp, tk, multiLine);
			mForm.reflow(true);
			btn.setEnabled(false);
		});
	}

	static boolean contains(List<LangBox> boxes, String lang) {
		for (var box : boxes) {
			if (Strings.nullOrEqual(lang, box.lang()))
				return true;
		}
		return false;
	}

	private record LangBox(
			String lang, String val, List<LangString> strings) {

		static LangBox of(LangString s, List<LangString> strings) {
			var lang = Strings.nullOrEmpty(s.getLang())
					? "en"
					: s.getLang();
			return new LangBox(lang, s.getValue(), strings);
		}

		static List<LangBox> allOf(List<LangString> strings) {
			var boxes = new ArrayList<LangBox>();
			for (var s : strings) {
				boxes.add(LangBox.of(s, strings));
			}
			if (!contains(boxes, App.lang())) {
				boxes.add(new LangBox(App.lang(), "", strings));
			}
			boxes.sort((box1, box2) -> {
				var lang1 = box1.lang;
				var lang2 = box2.lang;
				var appLang = App.lang();
				if (Strings.nullOrEqual(lang1, appLang))
					return -1;
				if (Strings.nullOrEqual(lang2, appLang))
					return 1;
				return Strings.compare(lang1, lang2);
			});
			return boxes;
		}

		void render(Composite comp, FormToolkit tk, boolean multiLine) {
			var text = multiLine
					? UI.formMultiText(comp, tk, label())
					: UI.formText(comp, tk, label());
			if (val != null) {
				text.setText(val);
			}
			text.addModifyListener($ -> {
				var s = text.getText();
				if (Strings.notEmpty(s)) {
					LangString.set(strings, s, lang);
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

	private record LangCombo(List<String> codes, Combo combo) {

		static LangCombo create(Composite comp) {
			var combo = new Combo(comp, SWT.READ_ONLY);
			var comboGrid = UI.gridData(combo, false, false);
			comboGrid.widthHint = 200;
			comboGrid.minimumWidth = 200;

			var codeSet = new HashSet<String>();
			for (var loc : Locale.getAvailableLocales()) {
				var code = loc.getLanguage();
				if (Strings.notEmpty(code)) {
					codeSet.add(code);
				}
			}

			var codes = new ArrayList<>(codeSet);
			codes.sort(Strings::compare);
			var items = new String[codes.size()];
			for (int i = 0; i < codes.size(); i++) {
				var code = codes.get(i);
				// we want the default language for the code!
				var loc = Locale.forLanguageTag(code);
				items[i] = loc != null
						? code + " - " + loc.getDisplayLanguage()
						: code;
			}

			combo.setItems(items);
			combo.select(0);
			return new LangCombo(codes, combo);
		}

		String getSelected() {
			int idx = combo.getSelectionIndex();
			return codes.get(idx);
		}

		void onChange(Consumer<String> fn) {
			if (fn == null)
				return;
			Controls.onSelect(combo, $ -> {
				var idx = combo.getSelectionIndex();
				var code = codes.get(idx);
				fn.accept(code);
			});
		}
	}
}
