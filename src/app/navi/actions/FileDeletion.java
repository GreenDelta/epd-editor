package app.navi.actions;

import java.nio.file.Files;

import org.eclipse.jface.action.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.M;
import app.navi.FileElement;
import app.navi.Navigator;
import app.rcp.Icon;
import app.util.MsgBox;

public class FileDeletion extends Action {

	private final FileElement e;

	public FileDeletion(FileElement e) {
		this.e = e;
		setText(M.DeleteFile);
		setImageDescriptor(Icon.DELETE.des());
	}

	@Override
	public void run() {
		if (e == null || e.getContent() == null || !e.getContent().exists())
			return;
		boolean b = MsgBox.ask(M.DeleteFile, M.DeleteFileQuestion);
		if (!b)
			return;
		try {
			Files.delete(e.getContent().toPath());
			Navigator.refresh(e.getParent());
		} catch (Exception ex) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to delete file " + e.getContent(), ex);
		}
	}
}
