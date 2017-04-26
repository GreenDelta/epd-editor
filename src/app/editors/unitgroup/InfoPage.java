package app.editors.unitgroup;

import java.util.function.Supplier;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openlca.ilcd.commons.DataEntry;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.units.DataSetInfo;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.util.UnitGroups;

import app.App;
import app.M;
import app.editors.CategorySection;
import app.editors.VersionField;
import app.util.TextBuilder;
import app.util.UI;
import epd.model.Xml;

class InfoPage extends FormPage {

	private final UnitGroup unitGroup;
	private final UnitGroupEditor editor;
	private FormToolkit tk;

	InfoPage(UnitGroupEditor editor) {
		super(editor, "UnitGroupPage", M.UnitGroup);
		this.editor = editor;
		this.unitGroup = editor.unitGroup;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		Supplier<String> title = () -> M.UnitGroup + ": "
				+ App.s(unitGroup.getName());
		ScrolledForm form = UI.formHeader(mform, title.get());
		editor.onSaved(() -> form.setText(title.get()));
		tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		TextBuilder tb = new TextBuilder(editor, this, tk);
		infoSection(body, tb);
		categorySection(body);
		new UnitSection(editor, unitGroup).render(body, tk);
		adminSection(body);
		form.reflow(true);
	}

	private void infoSection(Composite body, TextBuilder tb) {
		Composite comp = UI.infoSection(unitGroup, body, tk);
		DataSetInfo info = UnitGroups.dataSetInfo(unitGroup);
		tb.text(comp, M.Name, info.name);
		tb.text(comp, M.Description, info.generalComment);
		UI.fileLink(unitGroup, comp, tk);
	}

	private void categorySection(Composite body) {
		DataSetInfo info = UnitGroups.dataSetInfo(unitGroup);
		CategorySection section = new CategorySection(editor,
				DataSetType.UNIT_GROUP, info.classifications);
		section.render(body, tk);
	}

	private void adminSection(Composite body) {
		Composite comp = UI.formSection(body, tk, M.AdministrativeInformation);
		Publication pub = UnitGroups.publication(unitGroup);
		DataEntry entry = UnitGroups.dataEntry(unitGroup);
		Text timeT = UI.formText(comp, tk, M.LastUpdate);
		timeT.setText(Xml.toString(entry.timeStamp));
		Text uuidT = UI.formText(comp, tk, M.UUID);
		if (unitGroup.getUUID() != null)
			uuidT.setText(unitGroup.getUUID());
		VersionField vf = new VersionField(comp, tk);
		vf.setVersion(unitGroup.getVersion());
		vf.onChange(v -> {
			pub.version = v;
			editor.setDirty();
		});
		editor.onSaved(() -> {
			vf.setVersion(pub.version);
			timeT.setText(Xml.toString(entry.timeStamp));
		});
	}
}
