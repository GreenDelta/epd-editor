package app.editors.methods;

import java.util.function.Supplier;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openlca.ilcd.methods.LCIAMethod;

import app.App;
import app.util.TextBuilder;
import app.util.UI;

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
		// infoSection(body, tb);
		// categorySection(body);
		// adminSection(body);
		form.reflow(true);
	}

}