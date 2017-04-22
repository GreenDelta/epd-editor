package app.editors.flowproperty;

import java.util.function.Supplier;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.flowproperties.AdminInfo;
import org.openlca.ilcd.flowproperties.DataSetInfo;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flowproperties.QuantitativeReference;

import app.App;
import app.M;
import app.editors.CategorySection;
import app.editors.RefLink;
import app.editors.VersionField;
import app.util.TextBuilder;
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
				+ App.s(property.getName());
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
		Composite comp = UI.formSection(body, tk, M.ContactInformation);
		DataSetInfo info = property.flowPropertyInfo.dataSetInfo;
		tb.text(comp, M.Name, info.name);
		tb.text(comp, M.Synonyms, info.synonyms);
		tb.text(comp, M.Description, info.generalComment);
	}

	private void categorySection(Composite body) {
		DataSetInfo info = property.flowPropertyInfo.dataSetInfo;
		CategorySection section = new CategorySection(editor,
				DataSetType.FLOW_PROPERTY, info.classifications);
		section.render(body, tk);
	}

	private void unitGroupSection(Composite body) {
		Composite comp = UI.formSection(body, tk, M.QuantitativeReference);
		QuantitativeReference qRef = property.flowPropertyInfo.quantitativeReference;
		UI.formLabel(comp, tk, M.UnitGroup);
		RefLink refText = new RefLink(comp, tk, DataSetType.UNIT_GROUP);
		refText.setRef(qRef.unitGroup);
		refText.onChange(ref -> {
			qRef.unitGroup = ref;
			editor.setDirty();
		});
	}

	private void adminSection(Composite body) {
		Composite comp = UI.formSection(body, tk, M.AdministrativeInformation);
		AdminInfo info = property.adminInfo;
		Text timeT = UI.formText(comp, tk, M.LastUpdate);
		timeT.setText(Xml.toString(info.dataEntry.timeStamp));
		Text uuidT = UI.formText(comp, tk, M.UUID);
		if (property.getUUID() != null)
			uuidT.setText(property.getUUID());
		VersionField vf = new VersionField(comp, tk);
		vf.setVersion(property.getVersion());
		vf.onChange(v -> {
			info.publication.version = v;
			editor.setDirty();
		});
		editor.onSaved(() -> {
			vf.setVersion(info.publication.version);
			timeT.setText(Xml.toString(info.dataEntry.timeStamp));
		});
	}
}
