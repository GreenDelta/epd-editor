package app.editors.matprops;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import app.M;
import app.util.Tables;
import app.util.tables.AbstractTableViewer;
import app.util.tables.TextCellModifier;
import epd.model.MaterialProperty;

class Table extends AbstractTableViewer<MaterialProperty> {

	private final static String NAME = M.Name;
	private final static String UNIT = M.Unit;
	private final static String DESCRIPTION = M.UnitDescription;

	private List<MaterialProperty> properties = new ArrayList<>();

	private MaterialPropertyEditor editor;

	public Table(MaterialPropertyEditor editor,
			Composite parent) {
		super(parent);
		this.editor = editor;
		getModifySupport().bind(NAME, new NameModifier());
		getModifySupport().bind(UNIT, new UnitModifier());
		getModifySupport().bind(DESCRIPTION, new DescriptionModifier());
		Tables.bindColumnWidths(getViewer(), 0.35, 0.25, 0.40);
		getViewer().refresh(true);
	}

	@Override
	protected String[] getColumnHeaders() {
		return new String[] { NAME, UNIT, DESCRIPTION };
	}

	@Override
	protected IBaseLabelProvider getLabelProvider() {
		return new LabelProvider();
	}

	public void setInput(List<MaterialProperty> properties) {
		this.properties = properties;
		if (properties == null)
			setInput(new MaterialProperty[0]);
		else
			setInput(properties
					.toArray(new MaterialProperty[properties.size()]));
		getViewer().refresh(true);
	}

	@OnAdd
	protected void onCreate() {
		if (properties == null)
			return;
		MaterialProperty property = new MaterialProperty();
		property.id = UUID.randomUUID().toString().replace("", "");
		property.name = "new property";
		properties.add(property);
		setInput(properties);
		editor.setDirty();
	}

	@OnRemove
	protected void onRemove() {
		if (properties == null)
			return;
		for (MaterialProperty property : getAllSelected())
			properties.remove(property);
		setInput(properties);
		editor.setDirty();
	}

	private class LabelProvider extends BaseLabelProvider implements
			ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (!(element instanceof MaterialProperty))
				return null;
			MaterialProperty property = (MaterialProperty) element;
			switch (columnIndex) {
			case 0:
				return property.name;
			case 1:
				return property.unit;
			case 2:
				return property.unitDescription;
			default:
				return null;
			}
		}
	}

	private class NameModifier extends TextCellModifier<MaterialProperty> {

		@Override
		protected String getText(MaterialProperty property) {
			return property.name;
		}

		@Override
		protected void setText(MaterialProperty property, String text) {
			if (Objects.equals(property.name, text))
				return;
			property.name = text;
			editor.setDirty();
		}
	}

	private class UnitModifier extends TextCellModifier<MaterialProperty> {

		@Override
		protected String getText(MaterialProperty property) {
			return property.unit;
		}

		@Override
		protected void setText(MaterialProperty property, String text) {
			if (Objects.equals(property.unit, text))
				return;
			property.unit = text;
			editor.setDirty();
		}
	}

	private class DescriptionModifier extends
			TextCellModifier<MaterialProperty> {

		@Override
		protected String getText(MaterialProperty property) {
			return property.unitDescription;
		}

		@Override
		protected void setText(MaterialProperty property, String text) {
			if (Objects.equals(property.unitDescription, text))
				return;
			property.unitDescription = text;
			editor.setDirty();
		}
	}
}
