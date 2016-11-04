package app.navi;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.Ref;

import app.App;
import app.rcp.Icon;
import epd.index.CategoryNode;
import epd.index.TypeNode;

public class TypeElement implements NavigationElement {

	private final DataSetType type;
	private final NavigationRoot parent;
	private List<NavigationElement> childs;

	public TypeElement(NavigationRoot parent, DataSetType type) {
		this.parent = parent;
		this.type = type;
	}

	@Override
	public List<NavigationElement> getChilds() {
		if (childs == null) {
			childs = new ArrayList<>();
			update();
		}
		return childs;
	}

	@Override
	public void update() {
		if (childs == null)
			return;
		childs.clear();
		if (App.index == null)
			return;
		TypeNode node = App.index.getNode(type);
		if (node == null)
			return;
		for (CategoryNode catNode : node.categories) {
			CategoryElement e = new CategoryElement(this, catNode);
			childs.add(e);
		}
		for (Ref ref : node.refs) {
			RefElement e = new RefElement(parent, ref);
			childs.add(e);
		}
	}

	@Override
	public int compareTo(NavigationElement other) {
		return 0;
	}

	@Override
	public String getLabel() {
		if (type == null)
			return "";
		switch (type) {
		case CONTACT:
			return "#Contacts";
		case EXTERNAL_FILE:
			return "#External Files";
		case FLOW:
			return "#Products";
		case FLOW_PROPERTY:
			return "#Flow Properties";
		case LCIA_METHOD:
			return "#LCIA Methods";
		case PROCESS:
			return "#EPD Data Sets";
		case SOURCE:
			return "#Sources";
		case UNIT_GROUP:
			return "#Unit Groups";
		default:
			return "#Unknown";
		}
	}

	@Override
	public Image getImage() {
		return Icon.FOLDER.img();
	}

	@Override
	public NavigationElement getParent() {
		return parent;
	}

}
