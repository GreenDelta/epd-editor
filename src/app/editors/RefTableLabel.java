package app.editors;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;

import app.App;
import app.rcp.Icon;

public class RefTableLabel extends LabelProvider implements ITableLabelProvider {

	@Override
	public Image getColumnImage(Object obj, int col) {
		if (col != 0 || !(obj instanceof Ref))
			return null;
		Ref ref = (Ref) obj;
		return Icon.img(ref.type);
	}

	@Override
	public String getColumnText(Object obj, int col) {
		if (!(obj instanceof Ref))
			return null;
		Ref ref = (Ref) obj;
		switch (col) {
		case 0:
			return LangString.getFirst(ref.name, App.lang());
		case 1:
			return ref.uuid;
		case 2:
			return ref.version;
		default:
			return null;
		}
	}
}