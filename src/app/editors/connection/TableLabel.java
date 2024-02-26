package app.editors.connection;

import app.App;
import app.rcp.Icon;
import epd.util.Strings;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.descriptors.Descriptor;

class TableLabel extends LabelProvider implements ITableLabelProvider {

	@Override
	public Image getColumnImage(Object obj, int col) {
		if (!(obj instanceof Descriptor d))
			return null;
		if (col != 0)
			return null;
		return Icon.img(d.toRef().getType());
	}

	@Override
	public String getColumnText(Object obj, int col) {
		if (!(obj instanceof Descriptor d))
			return null;
		return switch (col) {
			case 0 -> LangString.getFirst(d.getName(), App.lang());
			case 1 -> d.getUUID();
			case 2 -> d.getVersion();
			case 3 -> {
				String val = LangString.getFirst(d.getComment(), App.lang());
				yield Strings.cut(val, 75);
			}
			default -> null;
		};
	}
}
