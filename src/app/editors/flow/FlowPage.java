package app.editors.flow;

import java.util.function.Supplier;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.flows.DataSetInfo;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.FlowName;
import org.openlca.ilcd.util.Flows;

import app.App;
import app.M;
import app.editors.CategorySection;
import app.editors.RefLink;
import app.editors.VersionField;
import app.util.TextBuilder;
import app.util.UI;
import epd.model.EpdProduct;
import epd.model.Xml;

class FlowPage extends FormPage {

	private final EpdProduct product;
	private final FlowEditor editor;
	private FormToolkit tk;

	FlowPage(FlowEditor editor) {
		super(editor, "FlowPage", M.Flow);
		this.editor = editor;
		this.product = editor.product;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		Supplier<String> title = () -> M.Flow + ": "
				+ App.s(product.flow.getName());
		ScrolledForm form = UI.formHeader(mform, title.get());
		editor.onSaved(() -> form.setText(title.get()));
		tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		TextBuilder tb = new TextBuilder(editor, this, tk);
		infoSection(body, tb);
		categorySection(body);
		VendorSection.create(body, tk, editor);
		propertySections(body);
		adminSection(body);
		form.reflow(true);
	}

	private void infoSection(Composite body, TextBuilder tb) {
		Composite comp = UI.formSection(body, tk, M.FlowInformation);
		FlowName fName = Flows.flowName(product.flow);
		tb.text(comp, M.Name, fName.baseName);
		DataSetInfo info = Flows.dataSetInfo(product.flow);
		tb.text(comp, M.Synonyms, info.synonyms);
		tb.text(comp, M.Description, info.generalComment);

		UI.formLabel(comp, tk, M.GenericProduct);
		RefLink rt = new RefLink(comp, tk, DataSetType.FLOW);
		rt.setRef(product.genericFlow);
		rt.onChange(ref -> {
			product.genericFlow = ref;
			editor.setDirty();
		});
	}

	private void categorySection(Composite body) {
		CategorySection section = new CategorySection(editor,
				DataSetType.FLOW, Flows.classifications(product.flow));
		section.render(body, tk);
	}

	private void propertySections(Composite body) {
		FlowPropertySection section = new FlowPropertySection(editor,
				product.flow);
		section.render(body, tk);
		Section s = UI.section(body, tk, M.MaterialProperties);
		UI.gridData(s, true, false);
		new MaterialPropertyTable(editor, s, tk);
	}

	private void adminSection(Composite body) {
		Flow f = product.flow;
		Composite comp = UI.formSection(body, tk,
				M.AdministrativeInformation);
		Text timeT = UI.formText(comp, tk, M.LastUpdate);
		timeT.setText(Xml.toString(Flows.dataEntry(f).timeStamp));
		Text uuidT = UI.formText(comp, tk, M.UUID);
		if (f.getUUID() != null)
			uuidT.setText(f.getUUID());
		VersionField vf = new VersionField(comp, tk);
		vf.setVersion(f.getVersion());
		vf.onChange(v -> {
			Flows.publication(f).version = v;
			editor.setDirty();
		});
		editor.onSaved(() -> {
			vf.setVersion(f.getVersion());
			timeT.setText(Xml.toString(Flows.dataEntry(f).timeStamp));
		});
	}
}
