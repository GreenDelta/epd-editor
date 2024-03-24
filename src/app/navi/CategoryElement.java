package app.navi;

import org.eclipse.swt.graphics.Image;
import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.commons.Ref;

import app.rcp.Icon;
import app.rcp.Labels;
import epd.index.CategoryNode;
import epd.util.Strings;

public class CategoryElement extends NavigationElement<CategoryNode> {

	private final NavigationElement<?> parent;

	public CategoryElement(NavigationElement<?> parent, CategoryNode node) {
		this.parent = parent;
		this.content = node;
	}

	public Category getCategory() {
		var node = getContent();
		if (node == null)
			return null;
		return node.category;
	}

	@Override
	public void update() {
		var node = getContent();
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
	public NavigationElement<?> getParent() {
		return parent;
	}

	@Override
	public int compareTo(NavigationElement<?> other) {
		if (other == null)
			return 1;
		if (!(other instanceof CategoryElement o))
			return -1;
		return Strings.compare(getLabel(), o.getLabel());
	}

	@Override
	public String getLabel() {
		var node = getContent();
		return node != null
			? Labels.get(node.category)
			: null;
	}

	@Override
	public Image getImage() {
		return Icon.FOLDER.img();
	}

}
