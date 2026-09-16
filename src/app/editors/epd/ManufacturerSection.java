package app.editors.epd;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
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
	private ScrolledForm form;

	ManufacturerSection(EpdEditor editor) {
		this.editor = editor;
		this.manufacturers = Epds.withManufacturers(editor.epd);
	}

	void render(Composite body, FormToolkit tk, ScrolledForm form) {
		this.tk = tk;
		this.form = form;
		var section = UI.section(body, tk, M.Manufacturers);
		parent = UI.sectionClient(section, tk);
		UI.gridLayout(parent, 1);
		for (var m : manufacturers) {
			new SubSection(m);
		}
		var add = Actions.create(M.Add, Icon.ADD.des(), this::add);
		Actions.bind(section, add);
		parent.layout(true, true);
		form.reflow(true);
	}

	private void add() {
		var ref = RefSelectionDialog.select(DataSetType.CONTACT);
		if (ref == null)
			return;
		var m = new EpdManufacturer().withContact(ref);
		manufacturers.add(m);
		new SubSection(m);
		parent.layout(true, true);
		form.reflow(true);
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

			var top = tk.createComposite(comp);
			UI.stretchX(top);
			UI.gridLayout(top, 2, 10, 0);
			contactRow(top);
			providingDataRow(top);
			createSiteTable(comp);

			var del = Actions.create(
				M.Remove, Icon.DELETE.des(), this::delete);
			Actions.bind(section, del);
		}

		private void contactRow(Composite comp) {
			UI.formLabel(comp, tk, M.Contact);
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
			var cb = UI.formCheckBox(comp, tk, M.IsProvidingData);
			cb.setSelection(m.isProvidingData());
			Controls.onSelect(cb, _ -> {
				m.withProvidingData(cb.getSelection());
				editor.setDirty();
			});
		}

		private void createSiteTable(Composite comp) {
			siteTable = Tables.createViewer(comp,
				M.ManufacturingSite,
				M.FacilityIdentifier,
				M.StreetAddress,
				M.CountryCode,
				M.OlcLocationCode);
			siteTable.setLabelProvider(new SiteLabel());
			Tables.bindColumnWidths(siteTable, 0.2, 0.2, 0.2, 0.2, 0.2);
			UI.stretchXY(siteTable.getControl()).heightHint = 100;

			var ms = new ModifySupport<EpdSite>(siteTable);
			ms.bind(M.ManufacturingSite, new SiteModifier(0))
				.bind(M.FacilityIdentifier, new SiteModifier(1))
				.bind(M.StreetAddress, new SiteModifier(2))
				.bind(M.CountryCode, new SiteModifier(3))
				.bind(M.OlcLocationCode, new SiteModifier(4));

			var add = Actions.create(M.Add, Icon.ADD.des(), this::addSite);
			var rem = Actions.create(M.Remove, Icon.DELETE.des(), this::removeSites);
			Actions.bind(siteTable, add, rem);
			siteTable.setInput(m.getSites());
		}

		private void addSite() {
			var site = new EpdSite().withName("New facility");
			m.withSites().add(site);
			siteTable.setInput(m.getSites());
			editor.setDirty();
		}

		private void removeSites() {
			var list = m.getSites();
			if (list.isEmpty())
				return;
			for (var s : Viewers.getAllSelected(siteTable)) {
				if (!(s instanceof EpdSite site))
					continue;
				list.remove(site);
			}
			siteTable.setInput(list);
			editor.setDirty();
		}

		private void delete() {
			manufacturers.remove(m);
			section.dispose();
			parent.layout(true, true);
			form.reflow(true);
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

		private final int col;

		SiteModifier(int col) {
			this.col = col;
		}

		@Override
		protected String getText(EpdSite site) {
			if (site == null)
				return null;
			return switch (col) {
				case 0 -> site.getName();
				case 1 -> site.getFacilityIdentifier();
				case 2 -> site.getStreetAddress();
				case 3 -> site.getGeoCode();
				case 4 -> site.getOlc();
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
			switch (col) {
				case 0 -> site.withName(text);
				case 1 -> site.withFacilityIdentifier(text);
				case 2 -> site.withStreetAddress(text);
				case 3 -> site.withGeoCode(text);
				case 4 -> site.withOlc(text);
			}
			editor.setDirty();
		}
	}
}
