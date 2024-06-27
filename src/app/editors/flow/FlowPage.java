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
import app.editors.CommonAdminSection;
import app.editors.RefLink;
import app.util.LangText;
import app.util.UI;

class FlowPage extends FormPage {

	private final Flow flow;
	private final FlowEditor editor;
	private FormToolkit tk;

	FlowPage(FlowEditor editor) {
		super(editor, "FlowPage", M.Flow);
		this.editor = editor;
		this.flow = editor.flow;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		Supplier<String> title = () -> M.Flow + ": "
				+ App.s(Flows.getBaseName(flow));
		var form = UI.formHeader(mform, title.get());
		editor.onSaved(() -> form.setText(title.get()));
		tk = mform.getToolkit();
		var body = UI.formBody(form, tk);

		infoSection(body);
		new CategorySection(editor, DataSetType.FLOW,
				Flows.withClassifications(flow)).render(body, tk);
		if (Flows.getFlowType(flow) == FlowType.PRODUCT_FLOW) {
			VendorSection.create(body, tk, editor);
		}
		propertySections(body);
		CommonAdminSection.of(editor, flow).render(body, tk);
		form.reflow(true);
	}

	private void infoSection(Composite body) {
		var comp = UI.infoSection(flow, body, tk);
		var name = Flows.withFlowName(flow);
		var tb = LangText.builder(editor, tk);

		tb.next(M.Name, Tooltips.Flow_Name)
				.val(name.getBaseName())
				.edit(name::withBaseName)
				.draw(comp);

		var info = Flows.withDataSetInfo(flow);
		tb.next(M.Synonyms, Tooltips.Flow_Synonyms)
				.val(info.getSynonyms())
				.edit(info::withSynonyms)
				.draw(comp);

		tb.nextMulti(M.Description, Tooltips.Flow_Description)
				.val(info.getComment())
				.edit(info::withComment)
				.draw(comp);

		if (Flows.getFlowType(flow) == FlowType.PRODUCT_FLOW) {
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
		if (Flows.getFlowType(flow) != FlowType.PRODUCT_FLOW)
			return;
		var matProps = new MaterialPropertySection(editor);
		matProps.render(body, tk);
		flowProps.materialPropertySection = matProps;
	}
}
