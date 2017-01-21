package app.editors.unitgroup;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.openlca.ilcd.units.Unit;
import org.openlca.ilcd.units.UnitGroup;

import app.App;
import app.util.Tables;
import app.util.UI;

class UnitSection {

	private final UnitGroup group;

	UnitSection(UnitGroup group) {
		this.group = group;
	}

	void render(Composite parent, FormToolkit tk) {
		Section section = UI.section(parent, tk, "#Units");
		Composite composite = UI.sectionClient(section, tk);
		UI.gridLayout(composite, 1);
		TableViewer viewer = Tables.createViewer(composite,
				"#Unit",
				"#Factor",
				"#Comment");
		viewer.setLabelProvider(new RowLabel());
		if (group != null)
			viewer.setInput(group.units);
		Tables.bindColumnWidths(viewer, 0.2, 0.3, 0.5);

	}

	private class RowLabel extends LabelProvider implements
			ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int col) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int col) {
			if (!(element instanceof Unit))
				return null;
			Unit unit = (Unit) element;
			switch (col) {
			case 0:
				return unit.name;
			case 1:
				return String.valueOf(unit.factor);
			case 2:
				return App.s(unit.comment);
			default:
				return null;
			}
		}
	}

}