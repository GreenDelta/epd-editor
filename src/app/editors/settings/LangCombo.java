package app.editors.settings;

import app.M;
import app.util.Controls;
import app.util.UI;
import epd.util.Strings;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import java.util.Locale;
import java.util.TreeSet;
import java.util.function.Consumer;

class LangCombo {

	private final String initial;
	private final String[] codes;

	private Combo combo;
	private Consumer<String> changeFn;

	LangCombo(String initial) {
		this.initial = initial;
		TreeSet<String> set = new TreeSet<>();
		for (Locale loc : Locale.getAvailableLocales()) {
			String lang = loc.getLanguage();
			if (!Strings.nullOrEmpty(lang))
				set.add(lang);
		}
		codes = set.toArray(new String[0]);
	}

	void onChange(Consumer<String> fn) {
		this.changeFn = fn;
	}

	void render(Composite comp, FormToolkit tk) {
		combo = UI.formCombo(comp, tk, M.Language);
		UI.gridData(combo, false, false).widthHint = 300;
		String[] items = new String[codes.length];
		int selected = -1;
		for (int i = 0; i < codes.length; i++) {
			String code = codes[i];
			items[i] = getDisplayLanguage(code);
			if (Strings.nullOrEqual(initial, code))
				selected = i;
		}
		combo.setItems(items);
		if (selected > -1)
			combo.select(selected);
		Controls.onSelect(combo, e -> {
			if (changeFn == null)
				return;
			changeFn.accept(codes[combo.getSelectionIndex()]);
		});
	}

	private String getDisplayLanguage(String code) {
		for (Locale loc : Locale.getAvailableLocales()) {
			if (Strings.nullOrEqual(code, loc.getLanguage()))
				return loc.getDisplayLanguage();
		}
		return M.Unknown;
	}

}
