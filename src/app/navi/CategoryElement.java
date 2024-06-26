package app.navi;

import java.util.Objects;

import org.eclipse.swt.graphics.Image;
import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.commons.Ref;

import app.rcp.Icon;
import app.rcp.Labels;
import epd.index.CategoryNode;
import epd.util.Strings;

public class CategoryElement extends NavigationElement {

	private final NavigationElement parent;
	private final CategoryNode node;

	public CategoryElement(NavigationElement parent, CategoryNode node) {
		this.parent = parent;
		this.node = node;
	}

	public Category getCategory() {
		return node != null
				? node.category
				: null;
	}

	@Override
	public void update() {
		if (childs == null)
			return;
		childs.clear();
		if (node == null)
			return;
		for (var catNode : node.categories) {
			var e = new CategoryElement(this, catNode);
			childs.add(e);
		}
		for (Ref ref : node.refs) {
			var e = new RefElement(parent, ref);
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
		if (!(other instanceof CategoryElement o))
			return -1;
		return Strings.compare(getLabel(), o.getLabel());
	}

	@Override
	public String getLabel() {
		return node != null
			? Labels.get(node.category)
			: null;
	}

	@Override
	public Image getImage() {
		return Icon.FOLDER.img();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CategoryElement other))
			return false;
		return Objects.equals(this.node, other.node);
	}
}
