package app.editors.flow;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import app.M;
import app.Tooltips;
import app.rcp.Icon;
import app.util.Actions;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;
import app.util.tables.ModifySupport;
import app.util.tables.TextCellModifier;
import epd.model.MaterialPropertyValue;

class MaterialPropertySection {

	private final static String PROPERTY = M.Property;
	private final static String VALUE = M.Value;
	private final static String UNIT = M.Unit;

	private FlowEditor editor;
	private TableViewer table;

	MaterialPropertySection(FlowEditor editor) {
		this.editor = editor;
	}

	void render(Composite body, FormToolkit tk) {
		var section = UI.section(body, tk, M.MaterialProperties);
		section.setToolTipText(Tooltips.Flow_MaterialProperties);
		UI.gridData(section, true, false);
		var comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		table = Tables.createViewer(comp, PROPERTY, VALUE, UNIT);
		table.getTable().setToolTipText(Tooltips.Flow_MaterialProperties);
		table.setLabelProvider(new Label());
		Tables.bindColumnWidths(table, 0.34, 0.33, 0.33);
		var mf = new ModifySupport<MaterialPropertyValue>(table);
		mf.bind(VALUE, new ValueModifier());
		bindActions(table, section);
		table.setInput(properties());
	}

	private List<MaterialPropertyValue> properties() {
		return editor.product == null
				? new ArrayList<>()
				: editor.product.properties;
	}

	void refresh() {
		table.setInput(properties());
	}

	private void bindActions(TableViewer table, Section section) {
		var add = Actions.create(
				M.Add, Icon.ADD.des(), this::onAdd);
		var remove = Actions.create(
				M.Remove, Icon.DELETE.des(), this::onRemove);
		Actions.bind(section, add, remove);
		Actions.bind(table, add, remove);
	}

	private void onAdd() {
		if (editor.product == null)
			return;
		var dialog = new MaterialPropertyDialog(UI.shell());
		if (dialog.open() != Window.OK)
			return;
		var property = dialog.getSelectedProperty();
		if (property == null)
			return;
		var value = new MaterialPropertyValue();
		value.property = property;
		value.value = (double) 1;
		editor.product.properties.add(value);
		table.setInput(editor.product.properties);
		editor.setDirty();
	}

	private void onRemove() {
		MaterialPropertyValue v = Viewers.getFirstSelected(table);
		if (v == null)
			return;
		var props = properties();
		props.remove(v);
		table.setInput(props);
		editor.setDirty();
	}

	private class ValueModifier
			extends TextCellModifier<MaterialPropertyValue> {

		@Override
		protected String getText(MaterialPropertyValue v) {
			return String.valueOf(v.value);
		}

		@Override
		protected void setText(MaterialPropertyValue v, String text) {
			try {
				if (v == null)
					return;
				double val = Double.valueOf(text);
				if (v.value != val) {
					v.value = val;
					editor.setDirty();
				}
			} catch (NumberFormatException e) {
			}
		}
	}

	private class Label extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof MaterialPropertyValue))
				return null;
			var val = (MaterialPropertyValue) obj;
			var prop = val.property;
			switch (col) {
			case 0:
				return prop != null ? prop.name : null;
			case 1:
				return Double.toString(val.value);
			case 2:
				return prop != null ? prop.unit : null;
			default:
				return null;
			}
		}
	}
}
