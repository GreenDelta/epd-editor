package app.editors.refs;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
	private List<Ref> refs;
	private Supplier<List<Ref>> supplier;
	private final Action[] actions;

	private IEditor editor;
	private String tooltip;
	private TableViewer table;
	private Consumer<Ref> onAdd;
	private Consumer<Ref> onRemove;

	private RefTable(DataSetType type) {
		this.type = type;
		this.actions = new Action[2];
		actions[0] = Actions.create(
			M.Add, Icon.ADD.des(), this::add);
		actions[1] = Actions.create(
			M.Remove, Icon.DELETE.des(), this::remove);
	}

	public RefTable withInitial(List<Ref> refs) {
		this.refs = refs;
		return this;
	}

	public RefTable withSupplier(Supplier<List<Ref>> supplier) {
		this.supplier = supplier;
		return this;
	}

	public static RefTable create(DataSetType type) {
		return new RefTable(type);
	}

	public static RefTable create(DataSetType type, List<Ref> refs) {
		return new RefTable(type).withInitial(refs);
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
		if (refs != null) {
			table.setInput(refs);
		}
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
		if (ref == null)
			return;
		var list = withRefs();
		if (list.contains(ref))
			return;
		list.add(ref);
		table.setInput(list);
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
		var list = withRefs();
		list.remove(ref);
		table.setInput(list);
		if (onRemove != null) {
			onRemove.accept(ref);
		}
		if (editor != null) {
			editor.setDirty();
		}
	}

	private List<Ref> withRefs() {
		if (supplier != null)
			return supplier.get();

		if (isImmutable(refs)) {
			refs = new ArrayList<>();
			return refs;
		}
		return refs;
	}

	private boolean isImmutable(List<Ref> list) {
		if (list == null)
			return true;
		return list.isEmpty()
			&& !(list instanceof ArrayList)
			&& !(list instanceof LinkedList);
	}

}
