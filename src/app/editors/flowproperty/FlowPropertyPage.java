package app.editors.flowproperty;

import app.App;
import app.M;
import app.Tooltips;
import app.editors.CategorySection;
import app.editors.RefLink;
import app.editors.VersionField;
import app.util.TextBuilder;
import app.util.UI;
import epd.model.Xml;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.util.DataSets;

import java.util.function.Supplier;

class FlowPropertyPage extends FormPage {

	private final FlowProperty property;
	private final FlowPropertyEditor editor;
	private FormToolkit tk;

	FlowPropertyPage(FlowPropertyEditor editor) {
		super(editor, "FlowPropertyPage", M.FlowProperty);
		this.editor = editor;
		this.property = editor.property;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		Supplier<String> title = () -> M.FlowProperty + ": "
				+ App.s(DataSets.getBaseName(property));
		ScrolledForm form = UI.formHeader(mform, title.get());
		editor.onSaved(() -> form.setText(title.get()));
		tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		TextBuilder tb = new TextBuilder(editor, this, tk);
		infoSection(body, tb);
		categorySection(body);
		unitGroupSection(body);
		adminSection(body);
	}

	private void infoSection(Composite body, TextBuilder tb) {
		Composite comp = UI.infoSection(property, body, tk);
		var info = property.withFlowPropertyInfo().withDataSetInfo();
		tb.text(comp, M.Name, Tooltips.FlowProperty_Name, info.withName());
		tb.text(comp, M.Synonyms,
				Tooltips.FlowProperty_Synonyms, info.withSynonyms());
		tb.text(comp, M.Description,
				Tooltips.FlowProperty_Description, info.withGeneralComment());
		UI.fileLink(property, comp, tk);
	}

	private void categorySection(Composite body) {
		var info = property.withFlowPropertyInfo().withDataSetInfo();
		var section = new CategorySection(editor,
				DataSetType.FLOW_PROPERTY, info.withClassifications());
		section.render(body, tk);
	}

	private void unitGroupSection(Composite body) {
		var comp = UI.formSection(body, tk, M.QuantitativeReference);
		var qRef = property
			.withFlowPropertyInfo()
			.withQuantitativeReference();
		UI.formLabel(comp, tk, M.UnitGroup, Tooltips.FlowProperty_UnitGroup);
		RefLink refText = new RefLink(comp, tk, DataSetType.UNIT_GROUP);
		refText.setRef(qRef.getUnitGroup());
		refText.onChange(ref -> {
			qRef.withUnitGroup(ref);
			editor.setDirty();
		});
	}

	private void adminSection(Composite body) {
		var comp = UI.formSection(body, tk, M.AdministrativeInformation);
		var timeT = UI.formText(comp, tk,
				M.LastUpdate, Tooltips.All_LastUpdate);
		timeT.setText(Xml.toString(DataSets.getTimeStamp(property)));

		var uuidT = UI.formText(comp, tk, M.UUID, Tooltips.All_UUID);
		var uuid = DataSets.getUUID(property);
		if (uuid != null) {
			uuidT.setText(uuid);
		}

		var vf = new VersionField(comp, tk);
		vf.setVersion(DataSets.getVersion(property));
		vf.onChange(v -> {
			property
				.withAdminInfo()
				.withPublication()
				.withVersion(v);
			editor.setDirty();
		});

		editor.onSaved(() -> {
			vf.setVersion(DataSets.getVersion(property));
			timeT.setText(Xml.toString(DataSets.getTimeStamp(property)));
		});
	}
}
