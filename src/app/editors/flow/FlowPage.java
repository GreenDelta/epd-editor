package app.editors.flow;

import java.util.function.Supplier;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.FlowType;
import org.openlca.ilcd.util.Flows;

import app.App;
import app.M;
import app.Tooltips;
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
		var form = UI.formHeader(mform, title.get());
		editor.onSaved(() -> form.setText(title.get()));
		tk = mform.getToolkit();
		var body = UI.formBody(form, tk);
		var tb = new TextBuilder(editor, this, tk);
		infoSection(body, tb);
		new CategorySection(editor, DataSetType.FLOW,
				Flows.classifications(product.flow)).render(body, tk);
		if (Flows.getType(product.flow) == FlowType.PRODUCT_FLOW) {
			VendorSection.create(body, tk, editor);
		}
		propertySections(body);
		adminSection(body);
		form.reflow(true);
	}

	private void infoSection(Composite body, TextBuilder tb) {
		var comp = UI.infoSection(product.flow, body, tk);
		var fName = Flows.flowName(product.flow);
		tb.text(comp, M.Name, Tooltips.Flow_Name, fName.baseName);
		var info = Flows.dataSetInfo(product.flow);
		tb.text(comp, M.Synonyms, Tooltips.Flow_Synonyms, info.synonyms);
		tb.text(comp, M.Description,
				Tooltips.Flow_Description, info.generalComment);
		if (Flows.getType(product.flow) == FlowType.PRODUCT_FLOW) {
			genericProductLink(comp);
		}
		UI.fileLink(product.flow, comp, tk);
	}

	private void genericProductLink(Composite comp) {
		UI.formLabel(comp, tk, M.GenericProduct, Tooltips.Flow_GenericProduct);
		var link = new RefLink(comp, tk, DataSetType.FLOW);
		link.setRef(product.genericFlow);
		link.onChange(ref -> {
			product.genericFlow = ref;
			editor.setDirty();
		});
	}

	private void propertySections(Composite body) {
		var flowProps = new FlowPropertySection(editor);
		flowProps.render(body, tk);
		if (Flows.getType(product.flow) != FlowType.PRODUCT_FLOW)
			return;
		var matProps = new MaterialPropertySection(editor);
		matProps.render(body, tk);
		flowProps.materialPropertySection = matProps;
	}

	private void adminSection(Composite body) {
		var flow = product.flow;
		var comp = UI.formSection(body, tk, M.AdministrativeInformation);
		var time = UI.formText(comp, tk,
				M.LastUpdate, Tooltips.All_LastUpdate);
		time.setText(Xml.toString(Flows.dataEntry(flow).timeStamp));
		var uuid = UI.formText(comp, tk, M.UUID, Tooltips.All_UUID);
		if (flow.getUUID() != null) {
			uuid.setText(flow.getUUID());
		}
		var version = new VersionField(comp, tk);
		version.setVersion(flow.getVersion());
		version.onChange(v -> {
			Flows.publication(flow).version = v;
			editor.setDirty();
		});
		editor.onSaved(() -> {
			version.setVersion(flow.getVersion());
			time.setText(Xml.toString(Flows.dataEntry(flow).timeStamp));
		});
	}
}
