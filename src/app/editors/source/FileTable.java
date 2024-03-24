package app.editors.source;

import java.util.List;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.sources.FileRef;
import org.openlca.ilcd.util.Sources;

import app.M;
import app.Tooltips;
import app.rcp.Icon;
import app.util.Actions;
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

		var add = Actions.create(M.Add, Icon.ADD.des(), this::add);
		var del = Actions.create(M.Remove, Icon.DELETE.des(), this::remove);
		Actions.bind(section, add, del);
		Actions.bind(table, add, del);
		table.setInput(fileRefs);
		Tables.bindColumnWidths(table, 1.0);
	}

	private void add() {
		var ref = ResourceDialog.select().orElse(null);
		if (ref == null)
			return;
		fileRefs.add(ref);
		table.setInput(fileRefs);
		editor.setDirty();
	}

	private void remove() {
		FileRef ref = Viewers.getFirstSelected(table);
		if (ref == null)
			return;
		fileRefs.remove(ref);
		table.setInput(fileRefs);
		editor.setDirty();
	}


	private static class Label extends ColumnLabelProvider {

		@Override
		public Image getImage(Object obj) {
			if (!(obj instanceof FileRef ref))
				return null;
			return FileRefs.isNonAscii(ref)
				? Icon.WARNING.img()
				: Icon.DOCUMENT.img();
		}

		@Override
		public String getText(Object obj) {
			return obj instanceof FileRef ref
				? ref.getUri()
				: null;
		}

		@Override
		public String getToolTipText(Object obj) {
			if (!(obj instanceof FileRef ref))
				return null;
			if (FileRefs.isNonAscii(ref))
				return "The name of this file has non-ASCII characters"
					+ " which can cause upload problems. It is"
					+ " recommended to rename the file first"
					+ " using only latin letters, digits,"
					+ " underscores and dashes.";
			return null;
		}
	}
}
