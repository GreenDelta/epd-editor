package app.editors.epd;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.epd.EpdProductId;
import org.openlca.ilcd.util.Epds;

import app.M;
import app.Tooltips;
import app.rcp.Icon;
import app.util.Actions;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;
import app.util.tables.ModifySupport;
import app.util.tables.TextModifier;

class ProductIdTable {

	private final EpdEditor editor;
	private final TableViewer table;
	private final Process epd;

	private ProductIdTable(EpdEditor editor, Composite parent, FormToolkit tk) {
		this.editor = editor;
		this.epd = editor.epd;
		UI.formLabel(parent, tk, M.ProductIds, Tooltips.EPD_ProductIds);
		table = Tables.createViewer(parent, M.Type, M.Value);
		UI.gridData(table.getControl(), true, true).heightHint = 100;
		table.setLabelProvider(new LabelProvider());
		Tables.bindColumnWidths(table, 0.4, 0.6);

		var ms = new ModifySupport<EpdProductId>(table);
		ms.bind(M.Type, new TypeModifier());
		ms.bind(M.Value, new ValueModifier());

		var add = Actions.create(M.Add, Icon.ADD.des(), this::onCreate);
		var rem = Actions.create(M.Remove, Icon.DELETE.des(), this::onRemove);
		Actions.bind(table, add, rem);
		setInput();
	}

	static void create(EpdEditor editor, Composite parent, FormToolkit tk) {
		new ProductIdTable(editor, parent, tk);
	}

	private void setInput() {
		table.setInput(Epds.getProductIds(epd));
	}

	protected void onCreate() {
		var id = new EpdProductId();
		Epds.withProductIds(epd).add(id);
		setInput();
		editor.setDirty();
	}

	protected void onRemove() {
		List<EpdProductId> selection = Viewers.getAllSelected(table);
		for (var id : selection) {
			Epds.withProductIds(epd).remove(id);
		}
		setInput();
		editor.setDirty();
	}

	private static class LabelProvider extends BaseLabelProvider implements
		ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof EpdProductId id))
				return null;
			return switch (col) {
				case 0 -> id.getType();
				case 1 -> id.getValue();
				default -> null;
			};
		}
	}

	private class TypeModifier extends TextModifier<EpdProductId> {

		@Override
		protected String getText(EpdProductId id) {
			return id.getType();
		}

		@Override
		protected void setText(EpdProductId id, String newText) {
			if (id == null)
				return;
			if (Objects.equals(id.getType(), newText))
				return;
			id.withType(newText);
			editor.setDirty();
		}
	}

	private class ValueModifier extends TextModifier<EpdProductId> {

		@Override
		protected String getText(EpdProductId id) {
			return id.getValue();
		}

		@Override
		protected void setText(EpdProductId id, String newText) {
			if (id == null)
				return;
			if (Objects.equals(id.getValue(), newText))
				return;
			id.withValue(newText);
			editor.setDirty();
		}
	}
}
