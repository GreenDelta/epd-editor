package app.editors.epd;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.processes.epd.EpdManufacturer;
import org.openlca.ilcd.processes.epd.EpdSite;
import org.openlca.ilcd.util.Epds;

import app.App;
import app.M;
import app.editors.refs.RefLink;
import app.editors.refs.RefSelectionDialog;
import app.rcp.Icon;
import app.util.Actions;
import app.util.Controls;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;
import app.util.tables.ModifySupport;
import app.util.tables.TextModifier;

class ManufacturerSection {

	private final EpdEditor editor;
	private final List<EpdManufacturer> manufacturers;
	private Composite parent;
	private FormToolkit tk;

	ManufacturerSection(EpdEditor editor) {
		this.editor = editor;
		this.manufacturers = Epds.withManufacturers(editor.epd);
	}

	void render(Composite body, FormToolkit tk) {
		this.tk = tk;
		var section = UI.section(body, tk, M.Manufacturers);
		parent = UI.sectionClient(section, tk);
		UI.gridLayout(parent, 1);
		for (var m : manufacturers) {
			new SubSection(m);
		}
		var add = Actions.create(M.Add, Icon.ADD.des(), this::add);
		Actions.bind(section, add);
		parent.layout(true, true);
	}

	private void add() {
		var ref = RefSelectionDialog.select(DataSetType.CONTACT);
		if (ref == null)
			return;
		var m = new EpdManufacturer().withContact(ref);
		manufacturers.add(m);
		new SubSection(m);
		parent.layout(true, true);
		editor.setDirty();
	}

	private class SubSection {

		private final EpdManufacturer m;
		private Section section;
		private TableViewer siteTable;

		SubSection(EpdManufacturer m) {
			this.m = m;
			createUi();
		}

		private void createUi() {
			var name = m.getContact() != null
				? App.s(m.getContact().getName())
				: "?";
			section = UI.section(parent, tk, name);
			var comp = UI.sectionClient(section, tk);
			UI.gridLayout(comp, 1);

			contactRow(comp);
			providingDataRow(comp);
			createSiteTable(comp);

			var del = Actions.create(
				M.Remove, Icon.DELETE.des(), this::delete);
			Actions.bind(section, del);
		}

		private void contactRow(Composite comp) {
			UI.formLabel(comp, tk, "Contact");
			var link = new RefLink(comp, tk, DataSetType.CONTACT);
			link.setRef(m.getContact());
			link.onChange(ref -> {
				m.withContact(ref);
				section.setText(ref != null
					? App.s(ref.getName())
					: "?");
				editor.setDirty();
			});
		}

		private void providingDataRow(Composite comp) {
			var cb = UI.formCheckBox(comp, tk, "Is providing data");
			cb.setSelection(m.isProvidingData());
			Controls.onSelect(cb, _ -> {
				m.withProvidingData(cb.getSelection());
				editor.setDirty();
			});
		}

		private void createSiteTable(Composite comp) {
			UI.formLabel(comp, tk, M.ManufacturingSites);
			siteTable = Tables.createViewer(comp,
				"Name",
				"Facility identifier",
				"Street address",
				"Country code",
				"OLC Location code");
			siteTable.setLabelProvider(new SiteLabel());
			Tables.bindColumnWidths(siteTable, 0.2, 0.2, 0.2, 0.2, 0.2);

			var ms = new ModifySupport<EpdSite>(siteTable);
			ms.bind("Name", new SiteModifier(SiteModifier.NAME, m));
			ms.bind("Facility identifier",
				new SiteModifier(SiteModifier.FACILITY, m));
			ms.bind("Street address",
				new SiteModifier(SiteModifier.STREET, m));
			ms.bind("Country code",
				new SiteModifier(SiteModifier.COUNTRY, m));
			ms.bind("OLC Location code",
				new SiteModifier(SiteModifier.OLC, m));

			var addSite = Actions.create(
				M.Add, Icon.ADD.des(), () -> addSite());
			var remSite = Actions.create(
				M.Remove, Icon.DELETE.des(), () -> removeSites());
			Actions.bind(siteTable, addSite, remSite);

			siteTable.setInput(m.getSites());
		}

		private void addSite() {
			var site = new EpdSite();
			m.withSites().add(site);
			siteTable.setInput(m.getSites());
			editor.setDirty();
		}

		private void removeSites() {
			var list = m.withSites();
			for (var s : Viewers.getAllSelected(siteTable)) {
				list.remove(s);
			}
			siteTable.setInput(list);
			editor.setDirty();
		}

		private void delete() {
			manufacturers.remove(m);
			section.dispose();
			parent.layout(true, true);
			editor.setDirty();
		}
	}

	private static class SiteLabel extends BaseLabelProvider
		implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object o, int i) {
			return null;
		}

		@Override
		public String getColumnText(Object o, int i) {
			if (!(o instanceof EpdSite s))
				return null;
			return switch (i) {
				case 0 -> s.getName();
				case 1 -> s.getFacilityIdentifier();
				case 2 -> s.getStreetAddress();
				case 3 -> s.getGeoCode();
				case 4 -> s.getOlc();
				default -> null;
			};
		}
	}

	private class SiteModifier extends TextModifier<EpdSite> {

		static final String NAME = "Name";
		static final String FACILITY = "Facility identifier";
		static final String STREET = "Street address";
		static final String COUNTRY = "Country code";
		static final String OLC = "OLC Location code";

		private final String field;
		private final EpdManufacturer m;

		SiteModifier(String field, EpdManufacturer m) {
			this.field = field;
			this.m = m;
		}

		@Override
		protected String getText(EpdSite site) {
			if (site == null)
				return null;
			return switch (field) {
				case NAME -> site.getName();
				case FACILITY -> site.getFacilityIdentifier();
				case STREET -> site.getStreetAddress();
				case COUNTRY -> site.getGeoCode();
				case OLC -> site.getOlc();
				default -> null;
			};
		}

		@Override
		protected void setText(EpdSite site, String text) {
			if (site == null)
				return;
			var old = getText(site);
			if (Objects.equals(old, text))
				return;
			switch (field) {
				case NAME -> site.withName(text);
				case FACILITY -> site.withFacilityIdentifier(text);
				case STREET -> site.withStreetAddress(text);
				case COUNTRY -> site.withGeoCode(text);
				case OLC -> site.withOlc(text);
			}
			editor.setDirty();
		}
	}
}
