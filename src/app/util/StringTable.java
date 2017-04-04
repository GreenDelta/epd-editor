package app.util;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import app.editors.BaseEditor;
import app.rcp.Icon;
import app.util.tables.ModifySupport;
import epd.util.Strings;

public class StringTable {

	private final BaseEditor editor;
	private final String property;
	private final List<String> values;
	private TableViewer viewer;

	public StringTable(BaseEditor editor, String property,
			List<String> values) {
		this.editor = editor;
		this.property = property;
		this.values = values;
	}

	public void render(Composite comp, FormToolkit tk) {
		viewer = Tables.createViewer(comp, property);
		viewer.getTable().setHeaderVisible(false);
		viewer.setLabelProvider(new LabelProvider());
		Tables.bindColumnWidths(viewer, 1.0);
		bindActions(viewer);
		viewer.setInput(values);
		ModifySupport<String> ms = new ModifySupport<>(viewer);
		ms.bind(property, s -> s, (oldVal, newVal) -> {
			int idx = viewer.getTable().getSelectionIndex();
			if (idx < values.size()
					&& Strings.nullOrEqual(values.get(idx), oldVal)) {
				values.set(idx, newVal);
				editor.setDirty();
			}
			viewer.refresh();
		});
	}

	private void bindActions(TableViewer table) {
		Action add = Actions.create("#Add", Icon.ADD.des(), () -> {
			values.add("#New: " + property);
			viewer.setInput(values);
			editor.setDirty();
		});
		Action remove = Actions.create("#Remove", Icon.DELETE.des(), () -> {
			String v = Viewers.getFirstSelected(viewer);
			if (v == null)
				return;
			values.remove(v);
			viewer.setInput(values);
			editor.setDirty();
		});
		Actions.bind(table, add, remove);
	}
}
