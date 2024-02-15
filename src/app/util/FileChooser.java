package app.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import java.io.File;

public class FileChooser {

	/**
	 * Selects a file for an export. Returns null if the user cancelled the
	 * dialog.
	 */
	public static File save(String fileName, String... extensions) {
		FileDialog dialog = new FileDialog(UI.shell(), SWT.SAVE);
		dialog.setText("#Save as ...");
		if (extensions.length > 0)
			dialog.setFilterExtensions(extensions);
		dialog.setFileName(fileName);
		String path = dialog.open();
		if (path == null)
			return null;
		File file = new File(path);
		if (!file.exists())
			return file;
		boolean b = MsgBox.ask("#Overwrite existing file?",
			"#The selected file already exists. "
				+ "Should we overwrite it?");
		return b ? file : null;
	}

	public static File open(String... extensions) {
		FileDialog dialog = new FileDialog(UI.shell(), SWT.OPEN);
		dialog.setText("#Open file ...");
		if (extensions.length > 0)
			dialog.setFilterExtensions(extensions);
		String path = dialog.open();
		if (path == null)
			return null;
		return new File(path);
	}

}
