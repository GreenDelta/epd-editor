package app.util.tables;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ToDoubleFunction;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import app.util.MsgBox;
import app.util.tables.ICellModifier.CellEditingType;

/**
 * Provides an easy and type safe way to add cell editors to a table viewer. It
 * is important that the viewer is configured with column properties that are
 * used for the binding of cell modifiers. Thus, you have to call
 * <code>viewer.setColumnProperties(aStringArray)</code> <b>before</b> you
 * create the modify-support.
 */
public class ModifySupport<T> {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final ColumnViewer viewer;
	private final Map<String, ICellModifier<T>> cellModifiers;

	public ModifySupport(ColumnViewer viewer) {
		this.viewer = viewer;
		this.cellModifiers = new HashMap<>();
		var props = viewer.getColumnProperties();
		if (props != null) {
			viewer.setCellModifier(new CellModifier());
			var editors = new CellEditor[props.length];
			viewer.setCellEditors(editors);
		}
	}

	/// Directly binds a cell editor to a given property. The editor should
	/// then handle the value editing then completely. The activation of the
	/// editor can be controlled by setting a validator to the editor that
	/// returns non-null (the validation error) if the respective value is
	/// invalid, the editor is not activated then.
	public ModifySupport<T> bind(String property, CellEditor editor) {
		int idx = findIndex(property);
		if (idx == -1)
			return this;
		var editors = ensureEditors(idx);
		editors[idx] = editor;
		return this;
	}

	/**
	 * Binds the given getter and setter to the given table property. Null values
	 * for the getter are allowed. The setter is only called if text was changed.
	 */
	public ModifySupport<T> bind(String property, Function<T, String> getter,
			BiConsumer<T, String> setter) {
		var modifier = new TextModifier<T>() {
			@Override
			protected String getText(T element) {
				if (getter == null)
					return "";
				String val = getter.apply(element);
				return val == null ? "" : val;
			}

			@Override
			protected void setText(T element, String text) {
				if (getter == null || setter == null)
					return;
				String oldVal = getter.apply(element);
				if (Objects.equals(oldVal, text))
					return;
				setter.accept(element, text);
			}
		};
		return bind(property, modifier);
	}

	/**
	 * Binds the given getter and setter for double values. An error message is
	 * shown if the number format is not correct. No values are passed in this
	 * case to the setter.
	 */
	public ModifySupport<T> onDouble(String property, ToDoubleFunction<T> getter,
			ObjDoubleConsumer<T> setter) {
		var modifier = new TextModifier<T>() {
			@Override
			protected String getText(T elem) {
				if (getter == null)
					return "";
				double val = getter.applyAsDouble(elem);
				return Double.toString(val);
			}

			@Override
			protected void setText(T elem, String text) {
				if (getter == null || setter == null || elem == null)
					return;
				String oldVal = getText(elem);
				if (Objects.equals(oldVal, text))
					return;
				if (Strings.isNullOrEmpty(text)) {
					setter.accept(elem, 0);
					return;
				}
				try {
					double val = Double.parseDouble(text.trim());
					setter.accept(elem, val);
				} catch (Exception e) {
					MsgBox.error("Invalid number format",
							"Invalid number format: " + text);
				}
			}
		};
		return bind(property, modifier);
	}

	/**
	 * Binds the given modifier to the given property of the viewer.
	 */
	public ModifySupport<T> bind(String property, ICellModifier<T> modifier) {
		int index = findIndex(property);
		if (index == -1)
			return this;
		cellModifiers.put(property, modifier);
		setEditor(modifier, index);
		return this;
	}

	/**
	 * Removes a possible cell editor for the given property from this modifier.
	 *
	 * @param property the property which cell editor should be removed
	 */
	public void unbind(String property) {
		if (property == null)
			return;
		cellModifiers.remove(property);
		var index = findIndex(property);
		if (index < 0)
			return;
		var editors = viewer.getCellEditors();
		if (editors != null && editors.length > index) {
			editors[index] = null;
		}
	}

	private int findIndex(String property) {
		var props = viewer.getColumnProperties();
		if (props == null)
			return -1;
		for (int i = 0; i < props.length; i++) {
			if (Objects.equals(props[i], property))
				return i;
		}
		log.warn("Property {} is not a column property", property);
		return -1;
	}

	private Composite getComponent() {
		if (viewer instanceof TableViewer)
			return ((TableViewer) viewer).getTable();
		else if (viewer instanceof TreeViewer)
			return ((TreeViewer) viewer).getTree();
		return null;
	}

	private void setEditor(ICellModifier<T> modifier, int index) {
		var editors = ensureEditors(index);
		editors[index] = switch (modifier.getCellEditingType()) {
			case TEXTBOX -> modifier.getStyle() != SWT.NONE
					? new TextCellEditor(getComponent(), modifier.getStyle())
					: new TextCellEditor(getComponent());
			case COMBOBOX -> new ComboEditor(getComponent(), new String[0]);
			case CHECKBOX -> modifier.getStyle() != SWT.NONE
					? new CheckboxCellEditor(getComponent(), modifier.getStyle())
					: new CheckboxCellEditor(getComponent());
		};
	}

	private CellEditor getCellEditor(String property) {
		int idx = findIndex(property);
		if (idx < 0)
			return null;
		var editors = ensureEditors(idx);
		return editors[idx];
	}

	private void refresh(T value) {
		for (var property : cellModifiers.keySet()) {
			var modifier = cellModifiers.get(property);
			if (modifier.getCellEditingType() == CellEditingType.COMBOBOX) {
				var editor = getCellEditor(property);
				if (editor instanceof ComboBoxCellEditor combo) {
					combo.setItems(modifier.getStringValues(value));
				}
			}
		}
	}

	private CellEditor[] ensureEditors(int withIdx) {
		var editors = viewer.getCellEditors();
		if (editors == null) {
			editors = new CellEditor[withIdx + 1];
			viewer.setCellEditors(editors);
			return editors;
		}
		if (editors.length <= withIdx) {
			editors = Arrays.copyOf(editors, withIdx + 1);
			viewer.setCellEditors(editors);
		}
		return editors;
	}

	private class CellModifier implements
			org.eclipse.jface.viewers.ICellModifier {

		@Override
		@SuppressWarnings("unchecked")
		public boolean canModify(Object element, String property) {
			if (element == null || property == null)
				return false;

			// check if there is a registered modifier
			var modifier = cellModifiers.get(property);
			if (modifier != null)
				return modifier.canModify((T) element);

			// check if there is a cell editor with a validator
			// registered
			var editor = getCellEditor(property);
			if (editor == null)
				return false;
			var validator = editor.getValidator();
			if (validator == null)
				return true;
			return validator.isValid(element) == null;
		}

		@Override
		@SuppressWarnings("unchecked")
		public Object getValue(Object element, String property) {
			var modifier = cellModifiers.get(property);
			if (modifier != null) {
				var elem = (T) element;
				var value = modifier.getValue(elem);
				return switch (modifier.getCellEditingType()) {
					case TEXTBOX -> value != null ? value.toString() : "";
					case COMBOBOX -> getComboIndex(modifier, elem, value);
					case CHECKBOX -> value instanceof Boolean ? value : false;
				};
			}
			var editor = getCellEditor(property);
			return editor != null ? element : null;
		}

		private Object getComboIndex(
				ICellModifier<T> modifier, T elem, Object value) {
			refresh(elem);
			Object[] values = modifier.getValues(elem);
			if (values == null)
				return -1;
			for (int i = 0; i < values.length; i++) {
				if (Objects.equals(values[i], value))
					return i;
			}
			return -1;
		}

		@Override
		public void modify(Object element, String property, Object value) {
			var widget = element instanceof Item
					? ((Item) element).getData()
					: element;
			var modifier = cellModifiers.get(property);
			if (modifier != null) {
				T elem = setModifierValue(widget, value, modifier);
				refresh(elem);
			}
			// update the viewer
			if (viewer.getControl().isDisposed())
				return;
			if (modifier != null && modifier.affectsOtherElements()) {
				viewer.refresh(true);
			} else {
				viewer.refresh(widget, true);
			}
		}

		@SuppressWarnings("unchecked")
		private T setModifierValue(Object element, Object value,
				ICellModifier<T> modifier) {
			T elem = (T) element;
			switch (modifier.getCellEditingType()) {
				case TEXTBOX -> modifier.modify(elem, value.toString());
				case COMBOBOX -> setComboValue(modifier, elem, value);
				case CHECKBOX -> modifier.modify(elem, value);
			}
			return elem;
		}

		private void setComboValue(ICellModifier<T> modifier, T elem,
				Object value) {
			if (value instanceof Integer) {
				int index = (int) value;
				if (index == -1)
					return;
				Object[] values = modifier.getValues(elem);
				if (values == null || index >= values.length)
					return;
				modifier.modify(elem, values[index]);
			}
		}
	}

	/**
	 * Overwrites the getValue method from the JFace combo editor so that also
	 * entered strings that are elements of the respective combo-items are accepted
	 * as user input.
	 */
	private static class ComboEditor extends ComboBoxCellEditor {

		public ComboEditor(Composite parent, String[] items) {
			super(parent, items);
		}

		@Override
		protected Object doGetValue() {
			Object val = super.doGetValue();
			if (!(val instanceof Integer))
				return val;
			int idx = (Integer) val;
			if (idx > -1)
				return idx;
			String cellText = getCellText();
			return getIndexForText(cellText);
		}

		private String getCellText() {
			Control control = getControl();
			if (!(control instanceof CCombo))
				return null;
			CCombo combo = (CCombo) getControl();
			return combo.getText();
		}

		private Integer getIndexForText(String cellText) {
			if (cellText == null)
				return -1;
			String term = cellText.trim();
			String[] items = getItems();
			for (int i = 0; i < items.length; i++) {
				if (term.equals(items[i]))
					return i;
			}
			return -1;
		}
	}
}
