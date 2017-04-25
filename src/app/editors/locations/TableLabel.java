package app.editors.locations;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.openlca.ilcd.lists.Location;

import app.rcp.Icon;

class TableLabel extends LabelProvider implements ITableLabelProvider {

	@Override
	public Image getColumnImage(Object obj, int col) {
		if (col == 0)
			return Icon.LOCATION.img();
		return null;
	}

	@Override
	public String getColumnText(Object obj, int col) {
		if (!(obj instanceof Location))
			return null;
		Location loc = (Location) obj;
		if (col == 0)
			return loc.code;
		if (col == 1)
			return loc.name;
		return null;
	}
}
