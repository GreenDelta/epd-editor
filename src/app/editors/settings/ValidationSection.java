package app.editors.settings;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.okworx.ilcd.validation.profile.Profile;

import app.M;
import app.rcp.Icon;
import app.store.validation.ValidationProfiles;
import app.util.Actions;
import app.util.FileChooser;
import app.util.MsgBox;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;
import epd.util.Strings;

class ValidationSection {

	private final SettingsPage page;
	private TableViewer table;
	private final List<ProfileInfo> infos = new ArrayList<>();

	ValidationSection(SettingsPage page) {
		this.page = page;
		for (File file : ValidationProfiles.getFiles()) {
			infos.add(new ProfileInfo(file));
		}
		Collections.sort(infos, (i1, i2) -> Strings
				.compare(i1.profile.getName(), i2.profile.getName()));
	}

	void render(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, M.ValidationProfiles);
		Composite comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		table = Tables.createViewer(comp, M.Name, M.Version, M.File);
		table.setLabelProvider(new ProfileLabel());
		Tables.bindColumnWidths(table, 0.4, 0.3, 0.3);
		bindActions(section);
		table.setInput(infos);
	}

	private void bindActions(Section section) {
		Action ref = Actions.create(M.SetAsActiveProfile, Icon.OK.des(),
				this::selectActive);
		Action add = Actions.create(M.Add, Icon.ADD.des(), this::add);
		Action del = Actions.create(M.Remove, Icon.DELETE.des(), this::remove);
		Actions.bind(table, ref, add, del);
		Actions.bind(section, ref, add, del);
	}

	private void add() {
		File file = FileChooser.open("*.jar");
		if (file == null)
			return;
		if (ValidationProfiles.contains(file)) {
			MsgBox.error(M.AlreadyExists,
					"#A profile with this name already exists.");
			return;
		}
		file = ValidationProfiles.put(file);
		if (file == null)
			return;
		infos.add(new ProfileInfo(file));
		table.setInput(infos);
	}

	private void remove() {
		ProfileInfo info = Viewers.getFirstSelected(table);
		if (info == null)
			return;
		boolean b = MsgBox.ask(M.Delete,
				"#Delete selected validation profile?");
		if (!b)
			return;
		infos.remove(info);
		if (Strings.nullOrEqual(page.settings.validationProfile,
				info.file.getName())) {
			page.settings.validationProfile = null;
			page.setDirty();
		}
		info.file.delete();
		table.setInput(infos);
	}

	private void selectActive() {
		ProfileInfo info = Viewers.getFirstSelected(table);
		if (info == null)
			return;
		page.settings.validationProfile = info.file.getName();
		page.setDirty();
		table.refresh();
	}

	private class ProfileInfo {

		final File file;
		final Profile profile;

		ProfileInfo(File file) {
			this.file = file;
			Profile p = null;
			try (JarFile jar = new JarFile(file)) {
				URL url = file.toURI().toURL();
				Manifest mf = jar.getManifest();
				p = new Profile(url);
				Attributes atts = mf.getAttributes("ILCD-Validator-Profile");
				if (atts != null) {
					p.setName(atts.getValue("Profile-Name"));
					p.setVersion(atts.getValue("Profile-Version"));
				}
			} catch (Exception e) {
				Logger log = LoggerFactory.getLogger(getClass());
				log.error("failed to load profile " + file, e);
			}
			profile = p;
		}

		String name() {
			return profile == null ? "ERROR" : profile.getName();
		}

		String version() {
			return profile == null ? null : profile.getVersion();
		}
	}

	private class ProfileLabel extends LabelProvider
			implements ITableLabelProvider, ITableFontProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof ProfileInfo))
				return null;
			ProfileInfo pi = (ProfileInfo) obj;
			switch (col) {
			case 0:
				return pi.name();
			case 1:
				return pi.version();
			case 2:
				return pi.file.getName();
			default:
				return null;
			}
		}

		@Override
		public Font getFont(Object obj, int col) {
			if (!(obj instanceof ProfileInfo))
				return null;
			ProfileInfo pi = (ProfileInfo) obj;
			if (Strings.nullOrEqual(page.settings.validationProfile,
					pi.file.getName()))
				return UI.boldFont();
			return null;
		}
	}
}
