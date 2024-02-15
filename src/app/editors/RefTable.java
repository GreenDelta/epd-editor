package app.editors;

import app.M;
import app.rcp.Icon;
import app.rcp.Labels;
import app.util.Actions;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.Ref;

import java.util.List;
import java.util.function.Consumer;

public class RefTable {

	private final DataSetType type;
	private final List<Ref> refs;

	private IEditor editor;
	private String title = "?";
	private String tooltip;

	private Consumer<Ref> onAdd;
	private Consumer<Ref> onRemove;

	private RefTable(DataSetType type, List<Ref> refs) {
		this.type = type;
		this.refs = refs;
	}

	public static RefTable create(DataSetType type, List<Ref> refs) {
		return new RefTable(type, refs);
	}

	public RefTable withEditor(IEditor editor) {
		this.editor = editor;
		return this;
	}

	public RefTable withTitle(String title) {
		this.title = title;
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

	public void render(Composite parent, FormToolkit tk) {
		Section section = UI.section(parent, tk, title);
		if (tooltip != null) {
			section.setToolTipText(tooltip);
		}
		var comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		var table = Tables.createViewer(comp, Labels.get(type));
		table.setLabelProvider(new RefTableLabel());
		table.setComparator(comparator());
		Action[] actions = createActions(table);
		Actions.bind(section, actions);
		Actions.bind(table, actions);
		table.setInput(refs);
		Tables.bindColumnWidths(table, 1.0);
		if (tooltip != null) {
			table.getTable().setToolTipText(tooltip);
		}
		Tables.onDoubleClick(table, _e -> {
			var item = Viewers.getFirstSelected(table);
			if (item instanceof Ref) {
				Editors.open((Ref) item);
			}
		});

		// make ref-tables a bit smaller than other tables
		// because we typically only have only 1 or a few
		// refs to show here
		var layout = table.getTable().getLayoutData();
		if (layout instanceof GridData) {
			((GridData) layout).minimumHeight = 85;
		}

	}

	private Action[] createActions(TableViewer table) {
		Action[] actions = new Action[2];
		actions[0] = Actions.create(M.Add, Icon.ADD.des(), () -> add(table));
		actions[1] = Actions.create(M.Remove, Icon.DELETE.des(),
			() -> remove(table));
		return actions;
	}

	private void add(TableViewer table) {
		Ref ref = RefSelectionDialog.select(type);
		if (ref == null || refs.contains(ref))
			return;
		refs.add(ref);
		table.setInput(refs);
		if (onAdd != null)
			onAdd.accept(ref);
		if (editor != null)
			editor.setDirty();
	}

	private void remove(TableViewer table) {
		Ref ref = Viewers.getFirstSelected(table);
		if (ref == null)
			return;
		refs.remove(ref);
		table.setInput(refs);
		if (onRemove != null)
			onRemove.accept(ref);
		if (editor != null)
			editor.setDirty();
	}


	public static ViewerComparator comparator() {
		return Comparator.instance;
	}

	private static class Comparator extends ViewerComparator {

		static final Comparator instance = new Comparator();

		@Override
		public int category(Object obj) {
			if (!(obj instanceof Ref ref))
				return -1;
			if (ref.type == null)
				return -1;
			return switch (ref.type) {
				case MODEL -> 0;
				case PROCESS -> 1;
				case FLOW -> 2;
				case LCIA_METHOD -> 3;
				case SOURCE -> 4;
				case CONTACT -> 5;
				case FLOW_PROPERTY -> 6;
				case UNIT_GROUP -> 7;
				case EXTERNAL_FILE -> 8;
			};
		}
	}

}
