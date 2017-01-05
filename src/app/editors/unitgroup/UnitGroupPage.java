package app.editors.unitgroup;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.units.AdminInfo;
import org.openlca.ilcd.units.DataSetInfo;
import org.openlca.ilcd.units.UnitGroup;

import app.App;
import app.editors.CategorySection;
import app.editors.VersionField;
import app.util.TextBuilder;
import app.util.UI;
import epd.model.Xml;

class UnitGroupPage extends FormPage {

	private final UnitGroup property;
	private final UnitGroupEditor editor;
	private FormToolkit tk;

	UnitGroupPage(UnitGroupEditor editor) {
		super(editor, "#UnitGroupPage", "#Unit Group Data Set");
		this.editor = editor;
		this.property = editor.unitGroup;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform,
				"#Unit Group: " + App.s(property.getName()));
		tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		TextBuilder tb = new TextBuilder(editor, this, tk);
		infoSection(body, tb);
		categorySection(body);
		unitSection(body);
		adminSection(body);
		form.reflow(true);
	}

	private void infoSection(Composite body, TextBuilder tb) {
		Composite comp = UI.formSection(body, tk, "#Contact information");
		DataSetInfo info = property.unitGroupInfo.dataSetInfo;
		tb.text(comp, "#Name", info.name);
		tb.text(comp, "#Description", info.generalComment);
	}

	private void categorySection(Composite body) {
		DataSetInfo info = property.unitGroupInfo.dataSetInfo;
		CategorySection section = new CategorySection(editor,
				DataSetType.UNIT_GROUP, info.classifications);
		section.render(body, tk);
	}

	private void unitSection(Composite body) {
		UnitSection section = new UnitSection(property.units.unit);
		section.render(body, tk);
	}

	private void adminSection(Composite body) {
		Composite comp = UI.formSection(body, tk,
				"#Administrative information");
		AdminInfo info = property.adminInfo;
		Text timeT = UI.formText(comp, tk, "#Last change");
		timeT.setText(Xml.toString(info.dataEntry.timeStamp));
		Text uuidT = UI.formText(comp, tk, "#UUID");
		if (property.getUUID() != null)
			uuidT.setText(property.getUUID());
		VersionField vf = new VersionField(comp, tk);
		vf.setVersion(property.getVersion());
		vf.onChange(v -> info.publication.version = v);
		editor.onSaved(() -> {
			vf.setVersion(info.publication.version);
			timeT.setText(Xml.toString(info.dataEntry.timeStamp));
		});
	}
}
