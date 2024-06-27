package app.editors.methods;

import java.util.function.Supplier;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.methods.ImpactMethod;
import org.openlca.ilcd.util.ImpactMethods;

import app.App;
import app.M;
import app.Tooltips;
import app.editors.CategorySection;
import app.editors.CommonAdminSection;
import app.util.LangText;
import app.util.StringTable;
import app.util.TextBuilder;
import app.util.UI;

class InfoPage extends FormPage {

	private final ImpactMethod method;
	private final MethodEditor editor;
	private FormToolkit tk;

	InfoPage(MethodEditor editor) {
		super(editor, "MethodInfoPage", M.LCIAMethod);
		this.editor = editor;
		this.method = editor.method;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		Supplier<String> title = () -> M.LCIAMethod + ": "
				+ App.s(ImpactMethods.getName(method));
		var form = UI.formHeader(mform, title.get());
		editor.onSaved(() -> form.setText(title.get()));
		tk = mform.getToolkit();
		var body = UI.formBody(form, tk);
		infoSection(body);
		categorySection(body);
		CommonAdminSection.of(editor, method).render(body, tk);
		form.reflow(true);
	}

	private void infoSection(Composite body) {
		var info = method.withMethodInfo().withDataSetInfo();
		var comp = UI.infoSection(method, body, tk);
		var tb = LangText.builder(editor, tk);

		tb.next(M.Name, Tooltips.LCIAMethod_Name)
				.val(info.getName())
				.edit(info::withName)
				.draw(comp);

		UI.formLabel(comp, tk, "Methodologies", Tooltips.LCIAMethod_Methodologies);
		new StringTable(editor, "Methodology", info.withMethods()).render(comp, tk);

		UI.formLabel(comp, tk, "Impact Categories",
				Tooltips.LCIAMethod_ImpactCategories);
		new StringTable(editor, "Impact Category", info.withImpactCategories())
				.render(comp, tk);

		new TextBuilder(editor, tk)
				.text(comp, "Impact Indicator", Tooltips.LCIAMethod_ImpactIndicator,
						info.getIndicator(), val -> {
							info.withIndicator(val);
							editor.setDirty();
						});

		tb.next(M.Description, Tooltips.LCIAMethod_Description)
				.val(info.getComment())
				.edit(info::withComment)
				.draw(comp);

		UI.fileLink(method, comp, tk);
	}

	private void categorySection(Composite body) {
		new CategorySection(
				editor,
				DataSetType.IMPACT_METHOD,
				method.withMethodInfo().withDataSetInfo().withClassifications()
		).render(body, tk);
	}
}
