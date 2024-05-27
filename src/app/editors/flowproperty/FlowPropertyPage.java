package app.editors.flowproperty;

import java.util.function.Supplier;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.util.DataSets;
import org.openlca.ilcd.util.FlowProperties;

import app.App;
import app.M;
import app.Tooltips;
import app.editors.CategorySection;
import app.editors.RefLink;
import app.editors.VersionField;
import app.util.LangText;
import app.util.UI;
import epd.model.Xml;

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
		var form = UI.formHeader(mform, title.get());
		editor.onSaved(() -> form.setText(title.get()));
		tk = mform.getToolkit();
		var body = UI.formBody(form, tk);
		infoSection(body);
		categorySection(body);
		unitGroupSection(body);
		adminSection(body);
	}

	private void infoSection(Composite body) {
		var comp = UI.infoSection(property, body, tk);
		var info = FlowProperties.withDataSetInfo(property);
		var tb = LangText.builder(editor, tk);

		tb.next(M.Name, Tooltips.FlowProperty_Name)
				.val(info.getName())
				.edit(info::withName)
				.draw(comp);

		tb.next(M.Synonyms, Tooltips.FlowProperty_Synonyms)
				.val(info.getSynonyms())
				.edit(info::withSynonyms)
				.draw(comp);

		tb.nextMulti(M.Description, Tooltips.FlowProperty_Description)
				.val(info.getComment())
				.edit(info::withComment)
				.draw(comp);

		UI.fileLink(property, comp, tk);
	}

	private void categorySection(Composite body) {
		var info = FlowProperties.withDataSetInfo(property);
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
