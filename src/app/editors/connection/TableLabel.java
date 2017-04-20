package app.editors.connection;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.descriptors.Descriptor;

import app.App;
import app.rcp.Icon;
import epd.util.Strings;

class TableLabel extends LabelProvider implements ITableLabelProvider {

	@Override
	public Image getColumnImage(Object obj, int col) {
		if (!(obj instanceof Descriptor))
			return null;
		if (col != 0)
			return null;
		Descriptor d = (Descriptor) obj;
		return Icon.img(d.toRef().type);
	}

	@Override
	public String getColumnText(Object obj, int col) {
		if (!(obj instanceof Descriptor))
			return null;
		Descriptor d = (Descriptor) obj;
		switch (col) {
		case 0:
			return LangString.getFirst(d.name, App.lang());
		case 1:
			return d.uuid;
		case 2:
			return d.version;
		case 3:
			String val = LangString.getFirst(d.comment, App.lang());
			return Strings.cut(val, 75);
		default:
			return null;
		}
	}
}