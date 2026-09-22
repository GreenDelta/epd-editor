package app.editors.profiles;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.eclipse.swt.widgets.Combo;
import org.openlca.ilcd.epd.EpdProfile;

import app.store.Profiles;
import app.util.Controls;

/// A combo box for selecting an EPD profile. The available profiles are shown
/// with their names, sorted alphabetically.
public final class ProfileCombo {

	private final List<EpdProfile> profiles = Profiles.getAllSorted();
	private final Combo combo;
	private Consumer<EpdProfile> onSelect;

	private ProfileCombo(Combo combo) {
		this.combo = combo;
	}

	/// Fills the given combo with all available EPD profiles and selects the
	/// profile with the given ID.
	public static ProfileCombo fill(Combo combo, String selectedId) {
		var profileCombo = new ProfileCombo(combo);
		profileCombo.render(selectedId);
		return profileCombo;
	}

	private void render(String selectedId) {
		var items = new String[profiles.size()];
		int selected = -1;
		for (int i = 0; i < profiles.size(); i++) {
			var profile = profiles.get(i);
			items[i] = profile.getName() != null
				? profile.getName()
				: "?";
			if (Objects.equals(selectedId, profile.getId())) {
				selected = i;
			}
		}
		combo.setItems(items);
		if (selected >= 0) {
			combo.select(selected);
		}
		Controls.onSelect(combo, _ -> {
			int i = combo.getSelectionIndex();
			if (i < 0 || i >= profiles.size() || onSelect == null)
				return;
			onSelect.accept(profiles.get(i));
		});
	}

	/// Sets the handler that is called when the user selects a profile.
	public ProfileCombo onSelect(Consumer<EpdProfile> fn) {
		onSelect = fn;
		return this;
	}
}
