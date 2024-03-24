package app.editors.source;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.openlca.ilcd.sources.FileRef;
import org.openlca.ilcd.util.Strings;
import org.slf4j.LoggerFactory;

import app.App;
import app.navi.Navigator;
import app.util.Controls;
import app.util.MsgBox;
import app.util.UI;

class ResourceDialog extends FormDialog {

	private File file;
	private String url;
	private boolean isFile = true;

	static Optional<FileRef> select() {
		var dialog = new ResourceDialog();
		if (dialog.open() != OK)
			return Optional.empty();
		if (dialog.isFile) {
			if (dialog.file == null)
				return Optional.empty();
			var ref = new FileRef()
				.withUri(dialog.file.getName());
			return Optional.of(ref);
		} else {
			return Strings.notEmpty(dialog.url)
				? Optional.of(new FileRef().withUri(dialog.url))
				: Optional.empty();
		}
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
			isFile = fileCheck.getSelection();
			fileText.setEnabled(isFile);
			fileBtn.setEnabled(isFile);
			urlText.setEnabled(!isFile);
		});

		urlText.addModifyListener($ -> url = urlText.getText().strip());
		Controls.onSelect(fileBtn, $ -> {
			selectFile();
			if (file != null) {
				fileText.setText(file.getName());
			}
		});
	}

	private void selectFile() {
		var docDir = new File(App.store().getRootFolder(), "external_docs");
		if (!docDir.exists()) {
			try {
				Files.createDirectories(docDir.toPath());
			} catch (Exception e) {
				LoggerFactory.getLogger(getClass())
					.error("failed to create external_docs folder", e);
			}
		}

		var dialog = new FileDialog(UI.shell(), SWT.OPEN);
		dialog.setText("Open file ...");
		dialog.setFilterPath(docDir.getAbsolutePath());
		var path = dialog.open();
		if (path == null)
			return;

		this.file = new File(path);
		if (FileRefs.isNonAscii(file)) {
			boolean b = MsgBox.ask("File name has non-ASCII characters",
				"The name of the selected file has non-ASCII characters"
					+ " which can cause upload problems. It is"
					+ " recommended to rename the file first using only"
					+ " latin letters, digits, underscores and dashes."
					+ " Continue anyway?");
			if (!b)
				return;
		}

		this.file = checkCopy(docDir, file);
	}

	private File checkCopy(File dir, File file) {
		if (!file.exists())
			return file;
		var copy = new File(dir, file.getName());
		if (copy.exists())
			return copy;
		boolean b = MsgBox.ask("Copy file?", "The selected file is "
			+ "not located in the 'external_docs' folder but for data "
			+ "exchange it should be in this folder. Should we make a "
			+	"copy there?");
		if (!b)
			return file;
		try {
			Files.copy(file.toPath(), copy.toPath(),
				StandardCopyOption.REPLACE_EXISTING);
			Navigator.refreshFolders();
			return copy;
		} catch (Exception e) {
			LoggerFactory.getLogger(getClass())
				.error("failed to copy file " + file, e);
			return file;
		}
	}


}
