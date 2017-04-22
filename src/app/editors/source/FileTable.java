package app.editors.source;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.sources.FileRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.M;
import app.navi.Navigator;
import app.rcp.Icon;
import app.util.Actions;
import app.util.MsgBox;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;

class FileTable {

	private final SourceEditor editor;
	private final List<FileRef> fileRefs;
	private TableViewer table;

	FileTable(SourceEditor editor) {
		this.editor = editor;
		fileRefs = editor.source.sourceInfo.dataSetInfo.files;
	}

	public void render(Composite parent, FormToolkit tk) {
		Section section = UI.section(parent, tk,
				"#Links to external files");
		Composite comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		table = Tables.createViewer(comp, "File reference");
		table.setLabelProvider(new Label());
		Action[] actions = createActions(table);
		Actions.bind(section, actions);
		Actions.bind(table, actions);
		table.setInput(fileRefs);
		table.getTable().getColumn(0).setWidth(350);
	}

	private Action[] createActions(TableViewer table) {
		Action[] actions = new Action[2];
		actions[0] = Actions.create(M.Add,
				Icon.ADD.des(), this::add);
		actions[1] = Actions.create(M.Remove,
				Icon.DELETE.des(), this::remove);
		return actions;
	}

	private void add() {
		FileDialog dialog = new FileDialog(UI.shell(), SWT.OPEN);
		dialog.setText("#Open file ...");
		File dir = new File(App.store.getRootFolder(),
				"external_docs");
		if (!dir.exists())
			dir.mkdirs();
		dialog.setFilterPath(dir.getAbsolutePath());
		String path = dialog.open();
		if (path == null)
			return;
		File file = new File(path);
		checkCopy(dir, file);
		FileRef ref = new FileRef();
		ref.uri = "../external_docs/" + file.getName();
		fileRefs.add(ref);
		table.setInput(fileRefs);
		editor.setDirty();
	}

	private void checkCopy(File dir, File file) {
		if (!file.exists())
			return;
		File copy = new File(dir, file.getName());
		if (copy.exists())
			return;
		boolean b = MsgBox.ask("#Copy file?", "#The selected file is "
				+ "not located in the 'external_docs' folder. Should "
				+ "we make a copy there?");
		if (!b)
			return;
		try {
			Files.copy(file.toPath(), copy.toPath(),
					StandardCopyOption.REPLACE_EXISTING);
			Navigator.refresh();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to copy file " + file, e);
		}
	}

	private void remove() {
		FileRef ref = Viewers.getFirstSelected(table);
		if (ref == null)
			return;
		fileRefs.remove(ref);
		table.setInput(fileRefs);
		editor.setDirty();
	}

	private class Label extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			if (!(obj instanceof Ref))
				return null;
			return Icon.DOCUMENT.img();
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof FileRef))
				return null;
			FileRef ref = (FileRef) obj;
			return ref.uri;
		}
	}

	public static void main(String[] args) {
		File dir = new File("../epd_editor/libs");
		File file = new File("libs/gson-2.8.0.jar");
		System.out.println(Objects.equals(dir, file.getParentFile()));
	}
}
