package app.editors.methods;

import java.util.function.Supplier;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.methods.DataSetInfo;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.ilcd.util.Methods;

import app.App;
import app.editors.CategorySection;
import app.editors.VersionField;
import app.util.StringTable;
import app.util.TextBuilder;
import app.util.UI;
import epd.model.Xml;

class InfoPage extends FormPage {

	private final LCIAMethod method;
	private final MethodEditor editor;
	private FormToolkit tk;

	InfoPage(MethodEditor editor) {
		super(editor, "#MethodInfoPage", "LCIA Method");
		this.editor = editor;
		this.method = editor.method;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		Supplier<String> title = () -> "#LCIA Method: "
				+ App.s(method.getName());
		ScrolledForm form = UI.formHeader(mform, title.get());
		editor.onSaved(() -> form.setText(title.get()));
		tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		TextBuilder tb = new TextBuilder(editor, this, tk);
		infoSection(body, tb);
		categorySection(body);
		adminSection(body);
		form.reflow(true);
	}

	private void infoSection(Composite body, TextBuilder tb) {
		DataSetInfo info = Methods.dataSetInfo(method);
		Composite comp = UI.formSection(body, tk, "#LCIA Method information");
		tb.text(comp, "#Name", info.name);
		UI.formLabel(comp, "#Methodologies");
		new StringTable(editor, "#Methodology", info.methods).render(comp, tk);
		UI.formLabel(comp, "#Impact Categories");
		new StringTable(editor, "#Impact Category", info.impactCategories)
				.render(comp, tk);
		tb.text(comp, "#Impact Indicator", info.indicator, val -> {
			info.indicator = val;
			editor.setDirty();
		});
		tb.text(comp, "#Comment", info.comment);
	}

	private void categorySection(Composite body) {
		CategorySection section = new CategorySection(editor,
				DataSetType.LCIA_METHOD, method.getClassifications());
		section.render(body, tk);
	}

	private void adminSection(Composite body) {
		Composite comp = UI.formSection(body, tk,
				"#Administrative information");
		Text timeT = UI.formText(comp, tk, "#Last change");
		timeT.setText(Xml.toString(Methods.dataEntry(method).timeStamp));
		Text uuidT = UI.formText(comp, tk, "#UUID");
		if (method.getUUID() != null)
			uuidT.setText(method.getUUID());
		VersionField vf = new VersionField(comp, tk);
		vf.setVersion(method.getVersion());
		vf.onChange(v -> Methods.publication(method).version = v);
		editor.onSaved(() -> {
			vf.setVersion(method.getVersion());
			timeT.setText(Xml.toString(Methods.dataEntry(method).timeStamp));
		});
	}

}