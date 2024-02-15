package app.editors.classifications;

import app.M;
import app.rcp.Icon;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.openlca.ilcd.lists.Category;
import org.openlca.ilcd.lists.CategoryList;
import org.openlca.ilcd.lists.ContentType;

class TreeLabel extends LabelProvider {

	@Override
	public Image getImage(Object obj) {
		return Icon.FOLDER.img();
	}

	@Override
	public String getText(Object obj) {
		if (obj instanceof CategoryList cl) {
			return s(cl.type);
		}
		if (obj instanceof Category c) {
			return s(c);
		}
		return super.getText(obj);
	}

	private String s(ContentType t) {
		if (t == null)
			return null;
		return switch (t) {
			case CONTACT -> M.Contacts;
			case FLOW -> M.Flows;
			case FLOW_PROPERTY -> M.FlowProperties;
			case LCIA_METHOD -> M.LCIAMethods;
			case PROCESS -> M.EPDs;
			case SOURCE -> M.Sources;
			case UNIT_GROUP -> M.UnitGroups;
		};
	}

	private String s(Category c) {
		if (c == null)
			return null;
		String s = c.name;
		if (c.id != null && c.id.length() < 6)
			s = c.id + " " + s;
		return s;
	}
}
