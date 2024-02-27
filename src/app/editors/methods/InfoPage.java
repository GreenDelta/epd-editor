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
import org.openlca.ilcd.methods.ImpactMethod;
import org.openlca.ilcd.util.ImpactMethods;

import app.App;
import app.M;
import app.Tooltips;
import app.editors.CategorySection;
import app.editors.VersionField;
import app.util.StringTable;
import app.util.TextBuilder;
import app.util.UI;
import epd.model.Xml;

class InfoPage extends FormPage {

	private final ImpactMethod method;
	private final MethodEditor editor;
	private FormToolkit tk;

	InfoPage(MethodEditor editor) {
		super(editor, "#MethodInfoPage", M.LCIAMethod);
		this.editor = editor;
		this.method = editor.method;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		Supplier<String> title = () -> M.LCIAMethod + ": "
				+ App.s(ImpactMethods.getName(method));
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
		DataSetInfo info = method.withMethodInfo().withDataSetInfo();
		Composite comp = UI.infoSection(method, body, tk);
		tb.text(comp, M.Name, Tooltips.LCIAMethod_Name, info.getName());
		UI.formLabel(comp, tk, "#Methodologies",
				Tooltips.LCIAMethod_Methodologies);
		new StringTable(editor, "#Methodology", info.getMethods()).render(comp, tk);
		UI.formLabel(comp, tk, "#Impact Categories",
				Tooltips.LCIAMethod_ImpactCategories);
		new StringTable(editor, "#Impact Category", info.getImpactCategories())
				.render(comp, tk);
		tb.text(comp, "#Impact Indicator", Tooltips.LCIAMethod_ImpactIndicator,
				info.getIndicator(), val -> {
					info.withIndicator(val);
					editor.setDirty();
				});
		tb.text(comp, M.Description,
				Tooltips.LCIAMethod_Description, info.getComment());
		UI.fileLink(method, comp, tk);
	}

	private void categorySection(Composite body) {
		CategorySection section = new CategorySection(editor,
				DataSetType.IMPACT_METHOD, ImpactMethods.getClassifications(method));
		section.render(body, tk);
	}

	private void adminSection(Composite body) {
		Composite comp = UI.formSection(body, tk, M.AdministrativeInformation);
		Text timeT = UI.formText(comp, tk,
				M.LastUpdate, Tooltips.All_LastUpdate);
		timeT.setText(Xml.toString(method.withAdminInfo().withDataEntry().getTimeStamp()));
		Text uuidT = UI.formText(comp, tk, M.UUID, Tooltips.All_UUID);
		if (ImpactMethods.getUUID(method) != null)
			uuidT.setText(ImpactMethods.getUUID(method));
		VersionField vf = new VersionField(comp, tk);
		vf.setVersion(method.getVersion());
		vf.onChange(v -> {
			method.withAdminInfo().withPublication().withVersion(v);
			editor.setDirty();
		});
		editor.onSaved(() -> {
			vf.setVersion(method.getVersion());
			timeT.setText(Xml.toString(method.withAdminInfo().withDataEntry().getTimeStamp()));
		});
	}

}
