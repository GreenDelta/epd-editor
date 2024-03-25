package app.editors.connection;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.openlca.ilcd.descriptors.Descriptor;

import app.App;
import app.rcp.Icon;
import epd.util.Strings;

class TableLabel extends LabelProvider implements ITableLabelProvider {

	@Override
	public Image getColumnImage(Object obj, int col) {
		if (!(obj instanceof Descriptor<?> d))
			return null;
		if (col != 0)
			return null;
		return Icon.img(d.toRef().getType());
	}

	@Override
	public String getColumnText(Object obj, int col) {
		if (!(obj instanceof Descriptor<?> d))
			return null;
		return switch (col) {
			case 0 -> App.s(d.getName());
			case 1 -> d.getUUID();
			case 2 -> d.getVersion();
			case 3 -> Strings.cut(App.s(d.getComment()), 75);
			default -> null;
		};
	}
}
