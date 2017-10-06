package app.editors.matprops;

import java.util.List;
import java.util.Objects;

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
import app.util.MsgBox;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;
import app.util.tables.ModifySupport;
import app.util.tables.TextCellModifier;
import epd.model.MaterialProperty;
import epd.util.Strings;

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
		viewer = Tables.createViewer(comp, "ID", NAME, UNIT, DESCRIPTION);
		viewer.setLabelProvider(new Label());
		Tables.bindColumnWidths(viewer, 0.1, 0.25, 0.25, 0.40);
		ModifySupport<MaterialProperty> mf = new ModifySupport<>(viewer);
		mf.bind("ID", new IdModifier());
		mf.bind(NAME, new NameModifier());
		mf.bind(UNIT, new UnitModifier());
		mf.bind(DESCRIPTION, new DescriptionModifier());
		bindActions(viewer, section);
		viewer.setInput(properties);
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
		if (properties == null)
			return;
		MaterialProperty p = new MaterialProperty();
		int i = properties.size() + 1;
		while (true) {
			String id = "pr" + i;
			if (!idExists(id)) {
				p.id = id;
				break;
			}
			i++;
		}
		p.name = "new property";
		properties.add(p);
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

	private boolean idExists(String id) {
		for (MaterialProperty p : properties) {
			if (Strings.nullOrEqual(id, p.id))
				return true;
		}
		return false;
	}

	private class Label extends BaseLabelProvider implements
			ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof MaterialProperty))
				return null;
			MaterialProperty p = (MaterialProperty) obj;
			switch (col) {
			case 0:
				return p.id;
			case 1:
				return p.name;
			case 2:
				return p.unit;
			case 3:
				return p.unitDescription;
			default:
				return null;
			}
		}
	}

	private class IdModifier extends TextCellModifier<MaterialProperty> {

		@Override
		protected String getText(MaterialProperty p) {
			return p.id;
		}

		@Override
		protected void setText(MaterialProperty p, String text) {
			if (Objects.equals(p.id, text))
				return;
			if (Strings.nullOrEmpty(text)) {
				MsgBox.error("#Invalid ID", "The ID cannot be empty.");
				return;
			}
			String id = text.trim();
			if (idExists(id)) {
				MsgBox.error("#ID already exists",
						"#A material property with the given ID already exists.");
				return;
			}
			if (!isValid(id)) {
				MsgBox.error("#Invalid ID",
						"#It must start with a letter followed by letters and digits only.");
				return;
			}
			p.id = id;
			editor.setDirty();
		}

		private boolean isValid(String id) {
			char[] chars = id.toCharArray();
			for (int i = 0; i < chars.length; i++) {
				char c = chars[i];
				if (i == 0) {
					if (!Character.isLetter(c))
						return false;
					continue;
				}
				if (!Character.isLetterOrDigit(c))
					return false;
			}
			return true;
		}
	}

	private class NameModifier extends TextCellModifier<MaterialProperty> {

		@Override
		protected String getText(MaterialProperty p) {
			return p.name;
		}

		@Override
		protected void setText(MaterialProperty p, String text) {
			if (Objects.equals(p.name, text))
				return;
			p.name = text;
			editor.setDirty();
		}
	}

	private class UnitModifier extends TextCellModifier<MaterialProperty> {

		@Override
		protected String getText(MaterialProperty p) {
			return p.unit;
		}

		@Override
		protected void setText(MaterialProperty p, String text) {
			if (Objects.equals(p.unit, text))
				return;
			p.unit = text;
			editor.setDirty();
		}
	}

	private class DescriptionModifier extends
			TextCellModifier<MaterialProperty> {

		@Override
		protected String getText(MaterialProperty p) {
			return p.unitDescription;
		}

		@Override
		protected void setText(MaterialProperty p, String text) {
			if (Objects.equals(p.unitDescription, text))
				return;
			p.unitDescription = text;
			editor.setDirty();
		}
	}
}
