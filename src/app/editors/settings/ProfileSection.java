package app.editors.settings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
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
import app.editors.profiles.ProfileEditor;
import app.rcp.Icon;
import app.store.EpdProfiles;
import app.store.Json;
import app.util.Actions;
import app.util.FileChooser;
import app.util.MsgBox;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;
import epd.model.EpdProfile;
import epd.util.Strings;

class ProfileSection {

	private final SettingsPage page;
	private TableViewer table;
	private final List<EpdProfile> profiles = new ArrayList<>();

	ProfileSection(SettingsPage page) {
		this.page = page;
		reload();
	}

	void render(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, "#EPD Profiles");
		Composite comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		table = Tables.createViewer(comp,
				M.Name, M.Description, M.Default);
		table.setLabelProvider(new Label());
		Tables.bindColumnWidths(table, 0.2, 0.6, 0.2);
		bindActions(section);
		table.setInput(profiles);
	}

	private void bindActions(Section section) {
		Action open = Actions.create(M.Open, Icon.OPEN.des(),
				() -> ProfileEditor.open(Viewers.getFirstSelected(table)));
		Action exp = Actions.create(M.Export, Icon.EXPORT.des(),
				this::onExport);
		Action imp = Actions.create(M.Import, Icon.IMPORT.des(),
				this::onImport);
		Action ref = Actions.create("#Set as active", Icon.OK.des(),
				this::onActivate);
		Action del = Actions.create(M.Delete, Icon.DELETE.des(),
				this::onDelete);
		Actions.bind(table, ref, open, exp, imp, del);
		Actions.bind(section, exp, imp);
		Tables.onDoubleClick(table, e -> open.run());
	}

	private void onImport() {
		File file = FileChooser.open("*.json");
		if (file == null)
			return;
		EpdProfile profile = Json.read(file, EpdProfile.class);
		if (profile == null) {
			MsgBox.error("#Could not read EPD profile from " + file.getName());
			return;
		}
		if (profile.id == null || profile.name == null) {
			MsgBox.error("#An EPD profile must have an ID or name");
			return;
		}
		EpdProfile other = EpdProfiles.get(profile.id);
		if (other != null) {
			boolean b = MsgBox.ask("#Overwrite profile?",
					"A profile with this ID already exists. "
							+ "Do you want to overwrite it?");
			if (!b)
				return;
		}
		EpdProfiles.save(profile);
		if (Strings.nullOrEqual(profile.id, App.settings().profile)) {
			EpdProfiles.set(profile);
		}
		reload();
	}

	private void onExport() {
		EpdProfile profile = Viewers.getFirstSelected(table);
		if (profile == null)
			return;
		File file = FileChooser.save(profile.id + ".json", "*.json");
		if (file == null)
			return;
		Json.write(profile, file);
	}

	private void onActivate() {
		EpdProfile profile = Viewers.getFirstSelected(table);
		if (profile == null)
			return;
		page.settings.profile = profile.id;
		page.setDirty();
		table.refresh();
	}

	private void reload() {
		profiles.clear();
		profiles.addAll(EpdProfiles.getAll());
		profiles.sort((p1, p2) -> Strings.compare(p1.name, p2.name));
		if (table != null) {
			table.setInput(profiles);
		}
	}

	private void onDelete() {
		EpdProfile profile = Viewers.getFirstSelected(table);
		if (profile == null)
			return;
		if (Strings.nullOrEqual(profile.id, page.settings.profile)
				|| Strings.nullOrEqual(profile.id, App.settings().profile)) {
			MsgBox.error("#The selected profile is currently a "
					+ "default profile and therefore cannot be deleted.");
			return;
		}
		boolean b = MsgBox.ask("#Delete profile",
				"#Do you really want to delete the selected profile?");
		if (!b)
			return;
		EpdProfiles.delete(profile.id);
		reload();

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
			if (Strings.nullOrEqual(
					profile.id,
					page.settings.profile)) {
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
					page.settings.profile)) {
				return UI.boldFont();
			}
			return null;
		}
	}

}
