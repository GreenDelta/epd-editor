package app.store.indata;

import java.io.File;
import java.util.Optional;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.openlca.commons.Strings;

import app.M;
import app.util.Controls;
import app.util.FileChooser;
import app.util.UI;

/// A dialog for selecting the source from which the InData reference data
/// should be imported: either the zip file of the GitHub repository or a zip
/// file that was downloaded before.
public class InDataImportDialog extends FormDialog {

	private boolean fromGitHub = true;
	private File zipFile;
	private InDataSource source;

	private Text urlText;
	private Text fileText;
	private Button fileBtn;
	private Button okButton;

	private InDataImportDialog() {
		super(UI.shell());
		setBlockOnOpen(true);
	}

	/// Opens the dialog and returns the selected source. An empty result is
	/// returned when the dialog was closed with CANCEL.
	public static Optional<InDataSource> show() {
		var dialog = new InDataImportDialog();
		if (dialog.open() != OK)
			return Optional.empty();
		return Optional.ofNullable(dialog.source);
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(M.InDataImportTitle);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(640, 320);
	}

	@Override
	protected void createFormContent(IManagedForm mForm) {
		var tk = mForm.getToolkit();
		var form = UI.formHeader(mForm, M.InDataImportTitle);
		var body = UI.formBody(form, tk);
		UI.gridLayout(body, 1);

		// the GitHub option
		var githubCheck = tk.createButton(
			body, M.InDataDownloadOption, SWT.RADIO);
		githubCheck.setSelection(true);
		urlText = tk.createText(body, InDataSource.GITHUB_URL, SWT.BORDER);
		UI.stretchX(urlText).horizontalIndent = 20;
		urlText.addModifyListener(_ -> updateOk());

		// the local zip option
		tk.createButton(body, M.InDataLocalOption, SWT.RADIO);
		var fileComp = tk.createComposite(body);
		UI.stretchX(fileComp).horizontalIndent = 20;
		UI.gridLayout(fileComp, 2);
		fileText = tk.createText(fileComp, "", SWT.READ_ONLY | SWT.BORDER);
		UI.stretchX(fileText);
		fileBtn = tk.createButton(fileComp, M.Browse, SWT.NONE);
		Controls.onSelect(fileBtn, _ -> selectFile());

		Controls.onSelect(githubCheck, _ -> {
			fromGitHub = githubCheck.getSelection();
			updateState();
		});
		updateState();
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		var control = super.createButtonBar(parent);
		okButton = getButton(IDialogConstants.OK_ID);
		updateOk();
		return control;
	}

	@Override
	protected void okPressed() {
		source = fromGitHub
			? InDataSource.ofUrl(urlText.getText().strip())
			: InDataSource.ofFile(zipFile);
		super.okPressed();
	}

	private void selectFile() {
		var file = FileChooser.open("*.zip");
		if (file == null)
			return;
		zipFile = file;
		fileText.setText(file.getAbsolutePath());
		updateOk();
	}

	private void updateState() {
		urlText.setEnabled(fromGitHub);
		fileText.setEnabled(!fromGitHub);
		fileBtn.setEnabled(!fromGitHub);
		updateOk();
	}

	/// The OK button is only enabled when a valid source is selected.
	private void updateOk() {
		if (okButton == null || okButton.isDisposed())
			return;
		boolean valid = fromGitHub
			? Strings.isNotBlank(urlText.getText())
			: zipFile != null && zipFile.isFile();
		okButton.setEnabled(valid);
	}
}
