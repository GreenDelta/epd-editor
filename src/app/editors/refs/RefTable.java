package app.editors.refs;

import java.util.List;
import java.util.function.Consumer;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.Ref;

import app.M;
import app.editors.Editors;
import app.editors.IEditor;
import app.rcp.Icon;
import app.rcp.Labels;
import app.util.Actions;
import app.util.Tables;
import app.util.Viewers;

public class RefTable {

	private final DataSetType type;
	private final List<Ref> refs;
	private final Action[] actions;

	private IEditor editor;
	private String tooltip;
	private TableViewer table;
	private Consumer<Ref> onAdd;
	private Consumer<Ref> onRemove;

	private RefTable(DataSetType type, List<Ref> refs) {
		this.type = type;
		this.refs = refs;
		this.actions = new Action[2];
		actions[0] = Actions.create(
			M.Add, Icon.ADD.des(), this::add);
		actions[1] = Actions.create(
			M.Remove, Icon.DELETE.des(), this::remove);
	}

	public static RefTable create(DataSetType type, List<Ref> refs) {
		return new RefTable(type, refs);
	}

	public RefTable withEditor(IEditor editor) {
		this.editor = editor;
		return this;
	}

	public RefTable withTooltip(String tooltip) {
		this.tooltip = tooltip;
		return this;
	}

	public RefTable onAdd(Consumer<Ref> fn) {
		this.onAdd = fn;
		return this;
	}

	public RefTable onRemove(Consumer<Ref> fn) {
		this.onRemove = fn;
		return this;
	}

	Action[] actions() {
		return actions;
	}

	public void render(Composite comp, FormToolkit tk) {
		table = Tables.createViewer(comp, Labels.get(type));
		table.setLabelProvider(new RefTableLabel());
		table.setComparator(RefComparator.get());

		Actions.bind(table, actions);
		table.setInput(refs);
		Tables.bindColumnWidths(table, 1.0);
		if (tooltip != null) {
			table.getTable().setToolTipText(tooltip);
		}

		Tables.onDoubleClick(table, _ -> {
			var item = Viewers.getFirstSelected(table);
			if (item instanceof Ref ref) {
				Editors.open(ref);
			}
		});

		// make ref-tables a bit smaller than other tables
		// because we typically only have only 1 or a few
		// refs to show here
		var layout = table.getTable().getLayoutData();
		if (layout instanceof GridData gd) {
			gd.minimumHeight = 85;
		}
	}

	private void add() {
		if (table == null)
			return;
		var ref = RefSelectionDialog.select(type);
		if (ref == null || refs.contains(ref))
			return;
		refs.add(ref);
		table.setInput(refs);
		if (onAdd != null) {
			onAdd.accept(ref);
		}
		if (editor != null) {
			editor.setDirty();
		}
	}

	private void remove() {
		if (table == null)
			return;
		Ref ref = Viewers.getFirstSelected(table);
		if (ref == null)
			return;
		refs.remove(ref);
		table.setInput(refs);
		if (onRemove != null) {
			onRemove.accept(ref);
		}
		if (editor != null) {
			editor.setDirty();
		}
	}

}
