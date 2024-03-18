package app.editors.epd.contents;

import app.App;
import app.util.Controls;
import epd.util.Strings;
import org.eclipse.swt.widgets.Combo;
import org.openlca.ilcd.processes.epd.EpdContentDeclaration;
import org.openlca.ilcd.processes.epd.EpdContentElement;
import scala.collection.mutable.StringBuilder;

import java.util.ArrayList;
import java.util.function.Consumer;

class ParentCombo {

	private final EpdContentDeclaration decl;
	private final EpdContentElement<?> elem;

	private Consumer<EpdContentElement<?>> listener;
	private final ArrayList<EpdContentElement<?>> candidates = new ArrayList<>();
	private final ArrayList<Integer> levels = new ArrayList<>();

	ParentCombo(EpdContentDeclaration decl, EpdContentElement<?> elem) {
		this.decl = decl;
		this.elem = elem;
	}

	void onChange(Consumer<EpdContentElement<?>> fn) {
		this.listener = fn;
	}

	ParentCombo bind(Combo combo) {

		// fill the candidate model
		for (var candidate : decl.getElements()) {
			put(candidate, 0);
		}
		String[] items = new String[candidates.size() + 1];
		items[0] = " - none - ";
		for (int i = 0; i < candidates.size(); i++) {
			StringBuilder name = new StringBuilder();
			for (int k = 0; k < levels.get(i); k++) {
				name.append("    ");
			}
			var candidate = candidates.get(i);
			var n = App.s(candidate.getName());
			if (Strings.notEmpty(n)) {
				name.append(n);
			}
			items[i + 1] = name.toString();
		}
		combo.setItems(items);

		// initial selection
		int selection = 0;
		for (int i = 0; i < candidates.size(); i++) {
			if (Content.childs(candidates.get(i)).contains(elem)) {
				selection = i + 1;
			}
		}
		combo.select(selection);

		// handle selections
		Controls.onSelect(combo, _e -> {
			if (listener == null)
				return;
			int i = combo.getSelectionIndex();
			if (i == 0) {
				listener.accept(null);
			} else {
				listener.accept(candidates.get(i - 1));
			}
		});

		return this;
	}

	private void put(EpdContentElement<?> candidate, int level) {
		if (!Content.isPossibleParent(elem, candidate))
			return;
		candidates.add(candidate);
		levels.add(level);
		for (var child : Content.childs(candidate)) {
			put(child, level + 1);
		}
	}

}
