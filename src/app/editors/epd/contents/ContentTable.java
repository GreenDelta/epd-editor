package app.editors.epd.contents;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import app.util.Tables;
import app.util.UI;

class ContentTable {

	boolean forPackaging;

	void render(FormToolkit tk, Composite body) {
		String title = forPackaging
				? "Packaging materials"
				: "Components and materials";
		Section section = UI.section(body, tk, title);
		UI.gridData(section, true, true);
		Composite comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);

		TableViewer table = Tables.createViewer(comp,
				"Component/material",
				"weight-%",
				"kg",
				"CAS No",
				"EC No",
				"GUUID",
				"Renewable resource",
				"Recycled content",
				"Recyclable content",
				"Comment");
		table.getTable().getColumn(5).setToolTipText(
				"Data dictionary GUUID");
		table.getTable().getColumn(7).setToolTipText(
				"Post cosumer material recycled content");
		table.getTable().getColumn(8).setToolTipText(
				"Material recyclable content");
		double w = 0.8 / 9;
		Tables.bindColumnWidths(table, 0.2, w, w, w, w, w, w, w, w, w);

	}

}
