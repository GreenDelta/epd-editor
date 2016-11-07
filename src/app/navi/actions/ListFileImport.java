package app.navi.actions;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.eclipse.jface.action.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.navi.ListFolderElement;
import app.navi.Navigator;
import app.rcp.Icon;
import app.util.FileChooser;

public class ListFileImport extends Action {

	private final ListFolderElement e;

	public ListFileImport(ListFolderElement e) {
		this.e = e;
		setText("#Import File");
		setImageDescriptor(Icon.IMPORT.des());
	}

	@Override
	public void run() {
		if (e == null)
			return;
		File source = FileChooser.open("*.xml");
		if (source == null || !source.exists())
			return;
		File folder = e.getFolder();
		if (folder == null)
			return;
		if (!folder.exists())
			folder.mkdirs();
		File target = new File(folder, source.getName());
		try {
			Files.copy(source.toPath(), target.toPath(),
					StandardCopyOption.REPLACE_EXISTING);
			Navigator.refresh(e);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("filed to import " + source + " to " + target, e);
		}
	}
}
