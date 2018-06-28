package app.editors.profiles;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import app.M;
import app.util.Tables;
import app.util.UI;
import epd.model.EpdProfile;
import epd.model.Module;

class ModuleTable {
	private final EpdProfile profile;
	private TableViewer table;

	private ModuleTable(EpdProfile profile) {
		this.profile = profile;
	}

	static ModuleTable of(EpdProfile profile) {
		return new ModuleTable(profile);
	}

	void render(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, M.Modules);
		Composite comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		table = Tables.createViewer(comp, M.Index,
				M.Name, M.Description);
		Tables.bindColumnWidths(table, 0.2, 0.3, 0.5);
		table.setLabelProvider(new Label());
		profile.modules.sort((m1, m2) -> m1.index - m2.index);
		table.setInput(profile.modules);
	}

	private class Label extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof Module))
				return null;
			Module module = (Module) obj;
			switch (col) {
			case 0:
				return Integer.toString(module.index);
			case 1:
				return module.name;
			case 2:
				return module.description;
			default:
				return null;
			}
		}

	}
}
