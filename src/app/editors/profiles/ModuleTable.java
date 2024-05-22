package app.editors.profiles;

import java.util.Comparator;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.openlca.ilcd.epd.EpdProfile;
import org.openlca.ilcd.epd.EpdProfileModule;

import app.M;
import app.util.Tables;
import app.util.UI;

class ModuleTable {
	private final EpdProfile profile;

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
		TableViewer table = Tables.createViewer(comp, M.Index,
			M.Name, M.Description);
		Tables.bindColumnWidths(table, 0.2, 0.3, 0.5);
		table.setLabelProvider(new Label());
		profile.getModules().sort(
			Comparator.comparingInt(EpdProfileModule::getIndex));
		table.setInput(profile.getModules());
	}

	private static class Label extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof EpdProfileModule module))
				return null;
			return switch (col) {
				case 0 -> Integer.toString(module.getIndex());
				case 1 -> module.getName();
				case 2 -> module.getDescription();
				default -> null;
			};
		}

	}
}
