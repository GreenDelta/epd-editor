package app.editors.classifications;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.openlca.ilcd.lists.Category;
import org.openlca.ilcd.lists.CategoryList;
import org.openlca.ilcd.lists.ContentType;

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
			return "#Contacts";
		case FLOW:
			return "#Flows";
		case FLOW_PROPERTY:
			return "#Flow properties";
		case LCIA_METHOD:
			return "#LCIA methods";
		case PROCESS:
			return "#Processes";
		case SOURCE:
			return "#Sources";
		case UNIT_GROUP:
			return "#Unit groups";
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
