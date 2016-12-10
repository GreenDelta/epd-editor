package app.editors.matprops;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import app.M;
import app.rcp.Icon;
import app.util.Actions;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;
import app.util.tables.ModifySupport;
import app.util.tables.TextCellModifier;
import epd.model.MaterialProperty;

class Table {

	private final static String NAME = M.Name;
	private final static String UNIT = M.Unit;
	private final static String DESCRIPTION = M.UnitDescription;

	private MaterialPropertyEditor editor;
	private List<MaterialProperty> properties;
	private TableViewer viewer;

	public Table(MaterialPropertyEditor editor, Section section,
			FormToolkit tk) {
		this.editor = editor;
		this.properties = editor.properties;
		Composite comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		viewer = Tables.createViewer(comp, NAME, UNIT, DESCRIPTION);
		viewer.setLabelProvider(new Label());
		Tables.bindColumnWidths(viewer, 0.35, 0.25, 0.40);
		ModifySupport<MaterialProperty> mf = new ModifySupport<>(viewer);
		mf.bind(NAME, new NameModifier());
		mf.bind(UNIT, new UnitModifier());
		mf.bind(DESCRIPTION, new DescriptionModifier());
		bindActions(viewer, section);
		viewer.setInput(properties);
	}

	private void bindActions(TableViewer table, Section section) {
		Action add = Actions.create("#Add", Icon.ADD.des(),
				this::onCreate);
		Action remove = Actions.create("#Remove", Icon.DELETE.des(),
				this::onRemove);
		Actions.bind(section, add, remove);
		Actions.bind(table, add, remove);
	}

	private void onCreate() {
		if (properties == null)
			return;
		MaterialProperty property = new MaterialProperty();
		property.id = UUID.randomUUID().toString().replace("", "");
		property.name = "new property";
		properties.add(property);
		viewer.setInput(properties);
		editor.setDirty();
	}

	private void onRemove() {
		MaterialProperty property = Viewers.getFirstSelected(viewer);
		if (property == null)
			return;
		properties.remove(property);
		viewer.setInput(properties);
		editor.setDirty();
	}

	private class Label extends BaseLabelProvider implements
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
