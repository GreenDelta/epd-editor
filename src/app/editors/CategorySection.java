package app.editors;

import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.DataSetType;

import app.M;
import app.Tooltips;
import app.rcp.Icon;
import app.rcp.Labels;
import app.util.Actions;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;

public class CategorySection {

	private final IEditor editor;
	private final DataSetType type;
	private final List<Classification> classifications;

	public CategorySection(
		IEditor editor, DataSetType type, List<Classification> list
	) {
		this.editor = editor;
		this.type = type;
		classifications = list;
	}

	public void render(Composite parent, FormToolkit tk) {
		var section = UI.section(parent, tk, M.Classification);
		section.setToolTipText(Tooltips.All_Classification);
		var comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		var table = Tables.createViewer(comp,
			M.ClassificationSystem,
			M.CategoryPath);
		table.getTable().setToolTipText(Tooltips.All_Classification);
		table.setLabelProvider(new RowLabel());
		table.setInput(classifications);
		Tables.bindColumnWidths(table, 0.3, 0.7);
		bindActions(section, table);
	}

	private void bindActions(Section section, TableViewer viewer) {
		var add = Actions.create(M.Add, Icon.ADD.des(),
			() -> addRow(viewer));
		var delete = Actions.create(M.Remove, Icon.DELETE.des(),
			() -> deleteRow(viewer));
		Actions.bind(section, add, delete);
		Actions.bind(viewer, add, delete);
	}

	private void addRow(TableViewer viewer) {
		var dialog = new CategoryDialog(type);
		if (dialog.open() != Window.OK)
			return;
		var classification = dialog.getSelection();
		if (classification == null)
			return;
		classifications.add(classification);
		viewer.setInput(classifications);
		editor.setDirty();
	}

	private void deleteRow(TableViewer viewer) {
		Classification classification = Viewers.getFirstSelected(viewer);
		if (classification == null)
			return;
		classifications.remove(classification);
		viewer.setInput(classifications);
		editor.setDirty();
	}

	private static class RowLabel extends LabelProvider implements
		ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int col) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int col) {
			if (!(element instanceof Classification classification))
				return null;
			return switch (col) {
				case 0 -> classification.getName();
				case 1 -> getPath(classification);
				default -> null;
			};
		}

		private String getPath(Classification classification) {
			var classes = classification.getCategories();
			classes.sort(Comparator.comparingInt(Category::getLevel));
			var path = new StringBuilder();
			for (int i = 0; i < classes.size(); i++) {
				if (i > 0) {
					path.append(" / ");
				}
				var clazz = classes.get(i);
				var name = Labels.get(clazz);
				if (name != null) {
					path.append(name);
				}
			}
			return path.toString();
		}
	}

}
