package app.editors.classifications;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.openlca.ilcd.lists.Category;
import org.openlca.ilcd.lists.CategoryList;
import org.openlca.ilcd.lists.ContentType;

import app.M;
import app.rcp.Icon;

class TreeLabel extends LabelProvider {

	@Override
	public Image getImage(Object obj) {
		return Icon.FOLDER.img();
	}

	@Override
	public String getText(Object obj) {
		if (obj instanceof CategoryList) {
			CategoryList cl = (CategoryList) obj;
			return s(cl.type);
		}
		if (obj instanceof Category) {
			Category c = (Category) obj;
			return s(c);
		}
		return super.getText(obj);
	}

	private String s(ContentType t) {
		if (t == null)
			return null;
		switch (t) {
		case CONTACT:
			return M.Contacts;
		case FLOW:
			return M.Flows;
		case FLOW_PROPERTY:
			return M.FlowProperties;
		case LCIA_METHOD:
			return M.LCIAMethods;
		case PROCESS:
			return M.EPDs;
		case SOURCE:
			return M.Sources;
		case UNIT_GROUP:
			return M.UnitGroups;
		default:
			return null;
		}
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
