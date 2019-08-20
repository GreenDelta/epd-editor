package app.editors.flow;

import java.util.ArrayList;

import org.eclipse.jface.action.Action;
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
import epd.model.MaterialProperty;
import epd.model.MaterialPropertyValue;

class MaterialPropertyTable {

	private final static String PROPERTY = M.Property;
	private final static String VALUE = M.Value;
	private final static String UNIT = M.Unit;

	private ArrayList<MaterialPropertyValue> values;
	private FlowEditor editor;
	private TableViewer viewer;

	public MaterialPropertyTable(FlowEditor editor, Section section,
			FormToolkit tk) {
		this.editor = editor;
		this.values = editor.product.properties;
		Composite comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		viewer = Tables.createViewer(comp, PROPERTY, VALUE, UNIT);
		viewer.getTable().setToolTipText(Tooltips.Flow_MaterialProperties);
		viewer.setLabelProvider(new Label());
		Tables.bindColumnWidths(viewer, 0.34, 0.33, 0.33);
		ModifySupport<MaterialPropertyValue> mf = new ModifySupport<>(viewer);
		mf.bind(VALUE, new ValueModifier());
		bindActions(viewer, section);
		viewer.setInput(values);
	}

	private void bindActions(TableViewer table, Section section) {
		Action add = Actions.create(M.Add, Icon.ADD.des(),
				this::onCreate);
		Action remove = Actions.create(M.Remove, Icon.DELETE.des(),
				this::onRemove);
		Actions.bind(section, add, remove);
		Actions.bind(table, add, remove);
	}

	private void onCreate() {
		if (values == null)
			return;
		MaterialPropertyDialog dialog = new MaterialPropertyDialog(UI.shell());
		if (dialog.open() != Window.OK)
			return;
		MaterialProperty property = dialog.getSelectedProperty();
		if (property == null)
			return;
		MaterialPropertyValue value = new MaterialPropertyValue();
		value.property = property;
		value.value = (double) 1;
		values.add(value);
		viewer.setInput(values);
		editor.setDirty();
	}

	private void onRemove() {
		MaterialPropertyValue v = Viewers.getFirstSelected(viewer);
		if (v == null)
			return;
		values.remove(v);
		viewer.setInput(values);
		editor.setDirty();
	}

	private class ValueModifier
			extends TextCellModifier<MaterialPropertyValue> {

		@Override
		protected String getText(MaterialPropertyValue element) {
			return String.valueOf(element.value);
		}

		@Override
		protected void setText(MaterialPropertyValue element, String text) {
			try {
				if (element == null)
					return;
				double val = Double.valueOf(text);
				if (element.value != val) {
					element.value = val;
					editor.setDirty();
				}
			} catch (NumberFormatException e) {
			}
		}
	}

	private class Label extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int col) {
			if (!(element instanceof MaterialPropertyValue))
				return null;
			MaterialPropertyValue val = (MaterialPropertyValue) element;
			MaterialProperty prop = val.property;
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
