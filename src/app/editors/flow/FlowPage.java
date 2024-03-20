package app.editors.flow;

import java.util.function.Supplier;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.FlowType;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.util.Flows;

import app.App;
import app.M;
import app.Tooltips;
import app.editors.CategorySection;
import app.editors.RefLink;
import app.editors.VersionField;
import app.util.TextBuilder;
import app.util.UI;
import epd.model.Xml;

class FlowPage extends FormPage {

	private final Flow flow;
	private final FlowEditor editor;
	private FormToolkit tk;

	FlowPage(FlowEditor editor) {
		super(editor, "FlowPage", M.Flow);
		this.editor = editor;
		this.flow = editor.product;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		Supplier<String> title = () -> M.Flow + ": "
			+ App.s(Flows.getBaseName(flow));
		var form = UI.formHeader(mform, title.get());
		editor.onSaved(() -> form.setText(title.get()));
		tk = mform.getToolkit();
		var body = UI.formBody(form, tk);
		var tb = new TextBuilder(editor, this, tk);
		infoSection(body, tb);
		new CategorySection(editor, DataSetType.FLOW,
			Flows.withClassifications(flow)).render(body, tk);
		if (Flows.getType(flow) == FlowType.PRODUCT_FLOW) {
			VendorSection.create(body, tk, editor);
		}
		propertySections(body);
		adminSection(body);
		form.reflow(true);
	}

	private void infoSection(Composite body, TextBuilder tb) {
		var comp = UI.infoSection(flow, body, tk);
		var fName = Flows.withFlowName(flow);
		tb.text(comp, M.Name, Tooltips.Flow_Name, fName.withBaseName());
		var info = Flows.withDataSetInfo(flow);
		tb.text(comp, M.Synonyms, Tooltips.Flow_Synonyms, info.withSynonyms());
		tb.text(comp, M.Description,
			Tooltips.Flow_Description, info.withComment());
		if (Flows.getType(flow) == FlowType.PRODUCT_FLOW) {
			genericProductLink(comp);
		}
		UI.fileLink(flow, comp, tk);
	}

	private void genericProductLink(Composite comp) {
		UI.formLabel(comp, tk, M.GenericProduct, Tooltips.Flow_GenericProduct);
		var link = new RefLink(comp, tk, DataSetType.FLOW);
		var info = Flows.getDataSetInfo(flow);
		if (info != null && info.getEpdExtension() != null) {
			link.setRef(info.getEpdExtension().getGenericFlow());
		}
		link.onChange(ref -> {
			Flows.withDataSetInfo(flow)
				.withEpdExtension()
				.withGenericFlow(ref);
			editor.setDirty();
		});
	}

	private void propertySections(Composite body) {
		var flowProps = new FlowPropertySection(editor);
		flowProps.render(body, tk);
		if (Flows.getType(flow) != FlowType.PRODUCT_FLOW)
			return;
		var matProps = new MaterialPropertySection(editor);
		matProps.render(body, tk);
		flowProps.materialPropertySection = matProps;
	}

	private void adminSection(Composite body) {
		var comp = UI.formSection(body, tk, M.AdministrativeInformation);
		var time = UI.formText(comp, tk,
			M.LastUpdate, Tooltips.All_LastUpdate);
		time.setText(Xml.toString(Flows.getTimeStamp(flow)));

		var uuidT = UI.formText(comp, tk, M.UUID, Tooltips.All_UUID);
		var uuid = Flows.getUUID(flow);
		if (uuid != null) {
			uuidT.setText(uuid);
		}

		var version = new VersionField(comp, tk);
		version.setVersion(Flows.getVersion(flow));
		version.onChange(v -> {
			Flows.withVersion(flow, v);
			editor.setDirty();
		});

		editor.onSaved(() -> {
			version.setVersion(Flows.getVersion(flow));
			time.setText(Xml.toString(Flows.getTimeStamp(flow)));
		});
	}
}
