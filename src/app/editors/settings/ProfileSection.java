package app.editors.settings;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import app.App;
import app.M;
import app.rcp.Icon;
import app.store.EpdProfiles;
import app.util.Tables;
import app.util.UI;
import epd.model.EpdProfile;
import epd.util.Strings;

class ProfileSection {

	private final SettingsPage page;
	private TableViewer table;
	private final List<EpdProfile> profiles = new ArrayList<>();

	ProfileSection(SettingsPage page) {
		this.page = page;
		profiles.addAll(EpdProfiles.getAll());
		profiles.sort((p1, p2) -> Strings.compare(p1.name, p2.name));
	}

	void render(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, "EPD Profiles");
		Composite comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		table = Tables.createViewer(comp,
				M.Name, M.Description, M.Default);
		table.setLabelProvider(new Label());
		Tables.bindColumnWidths(table, 0.2, 0.6, 0.2);
		table.setInput(profiles);
	}

	private class Label extends LabelProvider
			implements ITableLabelProvider, ITableFontProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			if (!(obj instanceof EpdProfile))
				return null;
			if (col != 2)
				return null;
			EpdProfile profile = (EpdProfile) obj;
			if (Strings.nullOrEqual(profile.id,
					App.settings().profile)) {
				return Icon.CHECK_TRUE.img();
			}
			return Icon.CHECK_FALSE.img();
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof EpdProfile))
				return null;
			EpdProfile profile = (EpdProfile) obj;
			switch (col) {
			case 0:
				return profile.name;
			case 1:
				return profile.description;
			default:
				return null;
			}
		}

		@Override
		public Font getFont(Object obj, int col) {
			if (!(obj instanceof EpdProfile))
				return null;
			EpdProfile profile = (EpdProfile) obj;
			if (Strings.nullOrEqual(profile.id,
					App.settings().profile)) {
				return UI.boldFont();
			}
			return null;
		}
	}

}
