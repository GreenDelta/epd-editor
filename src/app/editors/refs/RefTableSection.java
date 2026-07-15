package app.editors.refs;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.Ref;

import app.editors.IEditor;
import app.util.Actions;
import app.util.UI;

public class RefTableSection {

	private final RefTable table; 
	
	private String title = "?";
	private String tooltip;
	
	private RefTableSection(DataSetType type) {
		this.table = RefTable.create(type);
	}

	public static RefTableSection create(DataSetType type) {
		return new RefTableSection(type);
	}

	public static RefTableSection create(DataSetType type, List<Ref> refs) {
		return new RefTableSection(type).withInitial(refs);
	}

	public RefTableSection withInitial(List<Ref> refs) {
		table.withInitial(refs);
		return this;
	}

	public RefTableSection withSupplier(Supplier<List<Ref>> supplier) {
		table.withSupplier(supplier);
		return this;
	}

	public RefTableSection withEditor(IEditor editor) {
		table.withEditor(editor);
		return this;
	}

	public RefTableSection withTitle(String title) {
		this.title = title;
		return this;
	}

	public RefTableSection withTooltip(String tooltip) {
		table.withTooltip(tooltip);
		this.tooltip = tooltip;
		return this;
	}

	public RefTableSection onAdd(Consumer<Ref> fn) {
		table.onAdd(fn);
		return this;
	}

	public RefTableSection onRemove(Consumer<Ref> fn) {
		table.onRemove(fn);
		return this;
	}

	public void render(Composite parent, FormToolkit tk) {
		var section = UI.section(parent, tk, title);
		if (tooltip != null) {
			section.setToolTipText(tooltip);
		}
		var comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		table.render(comp, tk);		
		Actions.bind(section, table.actions());
	}
}
