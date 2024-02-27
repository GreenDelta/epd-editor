package app.editors;

import app.App;
import app.rcp.Icon;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;

public class RefTableLabel extends LabelProvider implements ITableLabelProvider {

	@Override
	public Image getColumnImage(Object obj, int col) {
		if (col != 0 || !(obj instanceof Ref ref))
			return null;
		return Icon.img(ref.getType());
	}

	@Override
	public String getColumnText(Object obj, int col) {
		if (!(obj instanceof Ref ref))
			return null;
		return switch (col) {
			case 0 -> LangString.getFirst(ref.getName(), App.lang());
			case 1 -> ref.getUUID();
			case 2 -> ref.getVersion();
			default -> null;
		};
	}
}
