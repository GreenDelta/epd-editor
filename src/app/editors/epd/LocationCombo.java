package app.editors.epd;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.openlca.ilcd.lists.Location;

import app.store.Locations;
import app.util.Controls;
import app.util.UI;
import epd.util.Strings;

class LocationCombo {

	private final List<Location> locations = new ArrayList<>();
	private Combo combo;

	void create(Composite parent, String selectedCode, Consumer<String> fn) {
		initLocations(selectedCode);
		combo = new Combo(parent, SWT.NONE);
		UI.gridData(combo, true, false);
		String[] items = new String[locations.size() + 1];
		items[0] = "";
		int selectedIdx = 0;
		for (int i = 0; i < locations.size(); i++) {
			Location loc = locations.get(i);
			items[i + 1] = loc.getName();
			if (Strings.nullOrEqual(selectedCode, loc.getCode()))
				selectedIdx = i + 1;
		}
		combo.setItems(items);
		combo.select(selectedIdx);
		onSelect(fn);
	}

	private void initLocations(String selectedCode) {
		boolean found = false;
		for (Location loc : Locations.get()) {
			locations.add(loc);
			if (Strings.nullOrEqual(loc.getCode(), selectedCode))
				found = true;
		}
		if (!found && selectedCode != null) {
			Location loc = new Location();
			loc.withCode(selectedCode);
			loc.withName(selectedCode);
			locations.add(loc);
		}
		locations.sort((loc1, loc2) -> Strings.compare(loc1.getName(),
				loc2.getName()));
	}

	private void onSelect(Consumer<String> fn) {
		Controls.onSelect(combo, e -> {
			int idx = combo.getSelectionIndex();
			if (idx <= 0) {
				fn.accept(null);
				return;
			}
			Location location = locations.get(idx - 1);
			fn.accept(location.getCode());
		});
	}

}
