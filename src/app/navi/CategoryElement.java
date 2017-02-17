package app.navi;

import org.eclipse.swt.graphics.Image;
import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.commons.Ref;

import app.rcp.Icon;
import epd.index.CategoryNode;
import epd.util.Strings;

public class CategoryElement extends NavigationElement {

	private NavigationElement parent;
	private CategoryNode node;

	public CategoryElement(NavigationElement parent, CategoryNode node) {
		this.parent = parent;
		this.node = node;
	}

	public Category getCategory() {
		if (node == null)
			return null;
		return node.category;
	}

	@Override
	public void update() {
		if (childs == null)
			return;
		childs.clear();
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
	public NavigationElement getParent() {
		return parent;
	}

	@Override
	public int compareTo(NavigationElement other) {
		if (other == null)
			return 1;
		if (!(other instanceof CategoryElement))
			return -1;
		CategoryElement o = (CategoryElement) other;
		return Strings.compare(getLabel(), o.getLabel());
	}

	@Override
	public String getLabel() {
		if (node == null || node.category == null)
			return null;
		Category cat = node.category;
		if (cat.classId != null && cat.classId.length() < 8)
			return cat.classId + " " + cat.value;
		return cat.value;
	}

	@Override
	public Image getImage() {
		return Icon.FOLDER.img();
	}

}
