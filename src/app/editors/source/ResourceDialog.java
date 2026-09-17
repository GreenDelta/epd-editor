package app.editors.source;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.openlca.commons.Strings;
import org.openlca.ilcd.sources.FileRef;
import org.slf4j.LoggerFactory;

import app.App;
import app.M;
import app.navi.Navigator;
import app.util.Controls;
import app.util.MsgBox;
import app.util.UI;

class ResourceDialog extends FormDialog {

	private final String sourceUuid;

	/// The file that was selected by the user. It is copied into the
	/// `external_docs` folder only when the dialog is closed with OK.
	private File selectedFile;

	/// The file to which the source data set links after the dialog was closed
	/// with OK. This is either the selected file or the file into which it was
	/// copied.
	private File file;

	/// Indicates whether the selected file should be copied into the
	/// `external_docs` folder when the dialog is closed with OK. This is false
	/// when the user declined this copy.
	private boolean copyToDocs = true;

	private String url;
	private boolean isFile = true;

	private Button uuidCheck;
	private Text fileText;

	static Optional<FileRef> select(String sourceUuid) {
		var dialog = new ResourceDialog(sourceUuid);
		if (dialog.open() != OK)
			return Optional.empty();
		if (dialog.isFile) {
			if (dialog.file == null)
				return Optional.empty();
			var ref = new FileRef()
				.withUri(dialog.file.getName());
			return Optional.of(ref);
		} else {
			return Strings.isNotBlank(dialog.url)
				? Optional.of(new FileRef().withUri(dialog.url))
				: Optional.empty();
		}
	}

	private ResourceDialog(String sourceUuid) {
		super(UI.shell());
		this.sourceUuid = sourceUuid;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Select a local file or web resource");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(500, 350);
	}

	@Override
	protected void createFormContent(IManagedForm mForm) {
		var tk = mForm.getToolkit();
		var body = UI.formBody(mForm.getForm(), tk);

		// local file
		var fileCheck = tk.createButton(body, "Local file", SWT.RADIO);
		fileCheck.setSelection(true);
		var fileComp = tk.createComposite(body);
		UI.stretchX(fileComp);
		UI.gridLayout(fileComp, 2);
		fileText = tk.createText(
			fileComp, "", SWT.READ_ONLY | SWT.BORDER);
		UI.stretchX(fileText);
		var fileBtn = tk.createButton(fileComp, "Select", SWT.NONE);
		uuidCheck = tk.createButton(fileComp,
			M.AppendUuidToFileName, SWT.CHECK);
		uuidCheck.setSelection(true);
		UI.stretchX(uuidCheck).horizontalSpan = 2;
		Controls.onSelect(uuidCheck, _ -> updateFileText());

		// URL
		var urlCheck = tk.createButton(body, "URL to web resource", SWT.RADIO);
		urlCheck.setSelection(false);
		var urlComp = tk.createComposite(body);
		UI.stretchX(urlComp);
		UI.gridLayout(urlComp, 1);
		var urlText = tk.createText(urlComp, "https://...", SWT.BORDER);
		UI.stretchX(urlText).minimumWidth = 400;
		urlText.setEnabled(false);

		Controls.onSelect(fileCheck, _ -> {
			isFile = fileCheck.getSelection();
			fileText.setEnabled(isFile);
			fileBtn.setEnabled(isFile);
			uuidCheck.setEnabled(isFile);
			urlText.setEnabled(!isFile);
		});

		urlText.addModifyListener(_ -> url = urlText.getText().strip());
		Controls.onSelect(fileBtn, _ -> selectFile());
	}

	/// Copies the selected file into the `external_docs` folder when this is
	/// needed. Nothing is written to disk when the dialog is closed with
	/// CANCEL.
	@Override
	protected void okPressed() {
		if (isFile && selectedFile != null) {
			var target = targetFile();
			if (target == null)
				return; // invalid state
			if (target.equals(selectedFile)) {
				file = selectedFile;
			} else if (copy(target)) {
				file = target;
			} else {
				return; // keep the dialog open
			}
		}
		super.okPressed();
	}

	private void selectFile() {
		var docDir = docsFolder();
		var dialog = new FileDialog(UI.shell(), SWT.OPEN);
		dialog.setText("Open file ...");
		dialog.setFilterPath(docDir.getAbsolutePath());
		var path = dialog.open();
		if (path == null)
			return;

		var picked = new File(path);
		if (FileRefs.isNonAscii(picked)) {
			boolean b = MsgBox.ask("File name has non-ASCII characters",
				"The name of the selected file has non-ASCII characters"
					+ " which can cause upload problems. It is"
					+ " recommended to rename the file first using only"
					+ " latin letters, digits, underscores and dashes."
					+ " Continue anyway?");
			if (!b)
				return;
		}

		selectedFile = picked;
		var parent = picked.getParentFile();
		boolean inDocs = parent != null && parent.equals(docDir);
		copyToDocs = inDocs || MsgBox.ask(M.CopyFile, M.CopyFileQuestion);
		updateFileText();
	}

	/// Returns the file to which the source data set should link. This is the
	/// selected file itself when it is already stored in the `external_docs`
	/// folder under the correct name or when the user did not want a copy of
	/// it. Otherwise, it is the file in the `external_docs` folder into which
	/// the selected file is copied when the dialog is closed with OK. With the
	/// default options, the UUID of the source is inserted into the file name
	/// so that the names of external documents are unique.
	private File targetFile() {
		if (selectedFile == null)
			return null;
		if (!copyToDocs)
			return selectedFile;
		var name = uuidCheck.getSelection()
			? FileRefs.withUuid(selectedFile.getName(), sourceUuid)
			: selectedFile.getName();
		return new File(docsFolder(), name);
	}

	/// Shows the name of the file under which the selected file will be linked
	/// in the source data set.
	private void updateFileText() {
		if (fileText == null || fileText.isDisposed())
			return;
		var target = targetFile();
		fileText.setText(target == null ? "" : target.getName());
	}

	/// Copies the selected file to the given target file in the
	/// `external_docs` folder. Returns false when this failed.
	private boolean copy(File target) {
		try {
			var dir = target.getParentFile();
			if (dir != null) {
				Files.createDirectories(dir.toPath());
			}
			Files.copy(selectedFile.toPath(), target.toPath(),
				StandardCopyOption.REPLACE_EXISTING);
			Navigator.refreshFolders();
			return true;
		} catch (Exception e) {
			LoggerFactory.getLogger(getClass())
				.error("failed to copy file {} to {}", selectedFile, target, e);
			MsgBox.error(M.FailedToCopyFile, target.getName());
			return false;
		}
	}

	private static File docsFolder() {
		return new File(App.store().getRootFolder(), "external_docs");
	}
}
