package app.editors.flow;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.flows.AdminInfo;
import org.openlca.ilcd.flows.DataSetInfo;
import org.openlca.ilcd.flows.Flow;

import app.App;
import app.editors.CategorySection;
import app.editors.VersionField;
import app.util.TextBuilder;
import app.util.UI;
import epd.model.Xml;

class FlowPage extends FormPage {

	private final Flow flow;
	private final FlowEditor editor;
	private FormToolkit tk;

	FlowPage(FlowEditor editor) {
		super(editor, "#FlowPage", "#Flow");
		this.editor = editor;
		this.flow = editor.flow;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform,
				"#Flow: " + App.s(flow.getName()));
		tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		TextBuilder tb = new TextBuilder(editor, this, tk);
		infoSection(body, tb);
		categorySection(body);
		flowSection(body);
		adminSection(body);
	}

	private void infoSection(Composite body, TextBuilder tb) {
		Composite comp = UI.formSection(body, tk, "#Flow information");
		DataSetInfo info = flow.flowInfo.dataSetInfo;
		// tb.text(comp, "#Name", (List<LangString>) info.name);
		tb.text(comp, "#Synonyms", info.synonyms);
		tb.text(comp, "#Description", info.generalComment);
	}

	private void categorySection(Composite body) {
		DataSetInfo info = flow.flowInfo.dataSetInfo;
		CategorySection section = new CategorySection(editor,
				org.openlca.ilcd.commons.DataSetType.FLOW,
				info.classificationInformation.classifications);
		section.render(body, tk);
	}

	private void flowSection(Composite body) {
		FlowPropertySection section = new FlowPropertySection(editor,
				DataSetType.FLOW, flow.flowProperties);
		section.render(body, tk);
	}

	private void adminSection(Composite body) {
		Composite comp = UI.formSection(body, tk,
				"#Administrative information");
		AdminInfo info = flow.adminInfo;
		Text timeT = UI.formText(comp, tk, "#Last change");
		timeT.setText(Xml.toString(info.dataEntry.timeStamp));
		Text uuidT = UI.formText(comp, tk, "#UUID");
		if (flow.getUUID() != null)
			uuidT.setText(flow.getUUID());
		VersionField vf = new VersionField(comp, tk);
		vf.setVersion(flow.getVersion());
		vf.onChange(v -> info.publication.version = v);
		editor.onSaved(() -> {
			vf.setVersion(info.publication.version);
			timeT.setText(Xml.toString(info.dataEntry.timeStamp));
		});
	}
}
