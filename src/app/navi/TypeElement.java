package app.navi;

import org.eclipse.swt.graphics.Image;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.Ref;

import app.App;
import app.M;
import app.rcp.Icon;
import epd.index.CategoryNode;
import epd.index.TypeNode;

public class TypeElement extends NavigationElement {

	private final NavigationRoot parent;
	private final DataSetType type;

	public TypeElement(NavigationRoot parent, DataSetType type) {
		this.parent = parent;
		this.type = type;
	}

	public DataSetType type() {
		return type;
	}

	@Override
	public void update() {
		if (childs == null)
			return;
		childs.clear();
		if (App.index() == null)
			return;
		TypeNode node = App.index().getNode(type);
		if (node == null)
			return;
		for (CategoryNode catNode : node.categories) {
			CategoryElement e = new CategoryElement(this, catNode);
			childs.add(e);
		}
		for (Ref ref : node.refs) {
			RefElement e = new RefElement(this, ref);
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
		return switch (type) {
			case CONTACT -> M.Contacts;
			case EXTERNAL_FILE -> M.ExternalFiles;
			case FLOW -> M.Flows;
			case FLOW_PROPERTY -> M.FlowProperties;
			case IMPACT_METHOD -> M.LCIAMethods;
			case PROCESS -> M.EPDs;
			case SOURCE -> M.Sources;
			case UNIT_GROUP -> M.UnitGroups;
			default -> M.None;
		};
	}

	@Override
	public Image getImage() {
		return Icon.FOLDER.img();
	}

	@Override
	public NavigationElement getParent() {
		return parent;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TypeElement other))
			return false;
		return this.type == other.type;
	}

}
