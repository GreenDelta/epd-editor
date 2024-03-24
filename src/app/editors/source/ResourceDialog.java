package app.editors.source;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;

import app.util.Controls;
import app.util.UI;

class ResourceDialog extends FormDialog {

	static void show() {
		var dialog = new ResourceDialog();
		dialog.open();
	}

	private ResourceDialog() {
		super(UI.shell());
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Select a local file or web resource");
	}

	@Override
	protected void createFormContent(IManagedForm mForm) {
		var tk = mForm.getToolkit();
		var body = UI.formBody(mForm.getForm(), tk);

		// local file
		var fileCheck = tk.createButton(body, "Local file", SWT.RADIO);
		fileCheck.setSelection(true);
		var fileComp = tk.createComposite(body);
		UI.gridData(fileComp, true, false);
		UI.gridLayout(fileComp, 2);
		var fileText = tk.createText(
			fileComp, "", SWT.READ_ONLY | SWT.BORDER);
		UI.gridData(fileText, true, false);
		var fileBtn = tk.createButton(fileComp, "Select", SWT.NONE);

		// URL
		var urlCheck = tk.createButton(body, "URL to web resource", SWT.RADIO);
		urlCheck.setSelection(false);
		var urlComp = tk.createComposite(body);
		UI.gridData(urlComp, true, false);
		UI.gridLayout(urlComp, 1);
		var urlText = tk.createText(urlComp, "https://...", SWT.BORDER);
		UI.gridData(urlText, true, false).minimumWidth = 400;
		urlText.setEnabled(false);

		Controls.onSelect(fileCheck, $ -> {
			boolean isFile = fileCheck.getSelection();
			fileText.setEnabled(isFile);
			fileBtn.setEnabled(isFile);
			urlText.setEnabled(!isFile);
		});

	}
}
