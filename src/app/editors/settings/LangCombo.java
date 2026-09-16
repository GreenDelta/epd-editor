package app.editors.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.commons.Strings;

import app.M;
import app.util.Controls;
import app.util.UI;

class LangCombo {

	private final String initial;
	private final List<Lang> languages;

	private Combo combo;
	private Consumer<String> changeFn;

	LangCombo(String initial) {
		this.initial = initial;
		this.languages = Lang.getAll();
	}

	void onChange(Consumer<String> fn) {
		this.changeFn = fn;
	}

	void render(Composite comp, FormToolkit tk) {
		combo = UI.formCombo(comp, tk, M.Language);
		UI.stretchNone(combo).widthHint = 300;
		var items = new String[languages.size()];
		int selected = -1;
		for (int i = 0; i < languages.size(); i++) {
			var lang = languages.get(i);
			items[i] = lang.label();
			if (Strings.equalsIgnoreCase(initial, lang.code()))
				selected = i;
		}
		combo.setItems(items);
		if (selected > -1)
			combo.select(selected);
		Controls.onSelect(combo, _ -> {
			if (changeFn == null)
				return;
			int i = combo.getSelectionIndex();
			if (i < 0 || i >= languages.size())
				return;
			changeFn.accept(languages.get(i).code());
		});
	}

	/// A language with its ISO code and the name that is displayed for it.
	private record Lang(String code, String name) {

		/// Collects the languages that are available in the current JVM, sorted by
		/// their display names.
		static List<Lang> getAll() {
			var map = new HashMap<String, Lang>();
			for (var loc : Locale.getAvailableLocales()) {
				var code = loc.getLanguage();
				if (Strings.isBlank(code))
					continue;
				var key = code.toLowerCase(Locale.ROOT);
				if (map.containsKey(key))
					continue;
				map.put(key, new Lang(code, loc.getDisplayLanguage()));
			}
			var list = new ArrayList<>(map.values());
			list.sort((a, b) -> Strings.compareIgnoreCase(a.name(), b.name()));
			return list;
		}

		String label() {
			return name + " (" + code + ")";
		}
	}
}
