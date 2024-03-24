package app.editors.source;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.sources.FileRef;
import org.openlca.ilcd.util.Sources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.M;
import app.Tooltips;
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
		fileRefs = Sources.withDataSetInfo(editor.source).withFiles();
	}

	public void render(Composite parent, FormToolkit tk) {
		var section = UI.section(parent, tk, "Links to external files");
		section.setToolTipText(Tooltips.Source_LinksToExternalFiles);
		var comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		table = Tables.createViewer(comp, "File reference");
		table.getTable().setToolTipText(Tooltips.Source_LinksToExternalFiles);
		table.setLabelProvider(new Label());
		ColumnViewerToolTipSupport.enableFor(table);
		var actions = createActions();
		Actions.bind(section, actions);
		Actions.bind(table, actions);
		table.setInput(fileRefs);
		Tables.bindColumnWidths(table, 1.0);
	}

	private Action[] createActions() {
		Action[] actions = new Action[2];
		actions[0] = Actions.create(M.Add,
			Icon.ADD.des(), ResourceDialog::show);
		actions[1] = Actions.create(M.Remove,
			Icon.DELETE.des(), this::remove);
		return actions;
	}

	private void add() {
		var dialog = new FileDialog(UI.shell(), SWT.OPEN);
		dialog.setText("Open file ...");
		File dir = new File(App.store().getRootFolder(),
			"external_docs");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		dialog.setFilterPath(dir.getAbsolutePath());
		String path = dialog.open();
		if (path == null)
			return;

		File file = new File(path);
		if (hasNonAsciiChars(file.getName())) {
			boolean b = MsgBox.ask("File name has non-ASCII characters",
				"The name of the selected file has non-ASCII characters"
					+ " which can cause upload problems. It is"
					+ " recommended to rename the file first using only"
					+ " latin letters, digits, underscores and dashes."
					+ " Continue anyway?");
			if (!b)
				return;
		}

		checkCopy(dir, file);

		FileRef ref = new FileRef();
		ref.withUri("../external_docs/" + file.getName());
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
		boolean b = MsgBox.ask("Copy file?", "The selected file is "
			+ "not located in the 'external_docs' folder. Should "
			+ "we make a copy there?");
		if (!b)
			return;
		try {
			Files.copy(file.toPath(), copy.toPath(),
				StandardCopyOption.REPLACE_EXISTING);
			Navigator.refreshFolders();
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

	// see https://github.com/GreenDelta/epd-editor/issues/39
	private boolean hasNonAsciiChars(String fileName) {
		if (fileName == null)
			return false;
		return !StandardCharsets.US_ASCII
			.newEncoder()
			.canEncode(fileName);
	}

	private class Label extends ColumnLabelProvider {

		@Override
		public Image getImage(Object obj) {
			if (!(obj instanceof FileRef ref))
				return null;
			if (ref.getUri() == null)
				return null;
			File file = new File(ref.getUri());
			if (hasNonAsciiChars(file.getName()))
				return Icon.WARNING.img();
			return Icon.DOCUMENT.img();
		}

		@Override
		public String getText(Object obj) {
			if (!(obj instanceof FileRef ref))
				return null;
			return ref.getUri();
		}

		@Override
		public String getToolTipText(Object obj) {
			if (!(obj instanceof FileRef ref))
				return null;
			if (ref.getUri() == null)
				return null;
			File file = new File(ref.getUri());
			if (!hasNonAsciiChars(file.getName()))
				return null;
			return "The name of this file has non-ASCII characters"
				+ " which can cause upload problems. It is"
				+ " recommended to rename the file first"
				+ " using only latin letters, digits,"
				+ " underscores and dashes.";
		}
	}

}
