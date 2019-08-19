package app.editors.epd.contents;

import java.util.ArrayList;
import java.util.function.Consumer;

import org.eclipse.swt.widgets.Combo;

import app.App;
import app.util.Controls;
import epd.model.content.ContentDeclaration;
import epd.model.content.ContentElement;
import scala.collection.mutable.StringBuilder;

class ParentCombo {

	private final ContentDeclaration decl;
	private final ContentElement elem;

	private Consumer<ContentElement> listener;
	private final ArrayList<ContentElement> candidates = new ArrayList<>();
	private final ArrayList<Integer> levels = new ArrayList<>();

	ParentCombo(ContentDeclaration decl, ContentElement elem) {
		this.decl = decl;
		this.elem = elem;
	}

	void onChange(Consumer<ContentElement> fn) {
		this.listener = fn;
	}

	ParentCombo bind(Combo combo) {

		// fill the candidate model
		for (ContentElement candidate : decl.content) {
			put(candidate, 0);
		}
		String[] items = new String[candidates.size() + 1];
		items[0] = " - none - ";
		for (int i = 0; i < candidates.size(); i++) {
			StringBuilder name = new StringBuilder();
			for (int k = 0; k < levels.get(i); k++) {
				name.append("    ");
			}
			ContentElement candidate = candidates.get(i);
			if (candidate.name != null) {
				name.append(App.s(candidates.get(i).name));
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

	private void put(ContentElement candidate, int level) {
		if (!Content.isPossibleParent(elem, candidate))
			return;
		candidates.add(candidate);
		levels.add(level);
		for (ContentElement child : Content.childs(candidate)) {
			put(child, level + 1);
		}
	}

}
