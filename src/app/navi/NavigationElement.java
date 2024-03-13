package app.navi;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;

public abstract class NavigationElement<T> {

	/**
	 * The list of child elements which is null initially and will be created on
	 * demand.
	 */
	protected List<NavigationElement<?>> childs;
	protected T content;

	public final List<NavigationElement<?>> getChilds() {
		if (childs == null) {
			childs = new ArrayList<NavigationElement<?>>();
			update();
		}
		return childs;
	}

	public abstract void update();

	public abstract NavigationElement<?> getParent();

	public abstract int compareTo(NavigationElement<?> other);

	public abstract String getLabel();

	public abstract Image getImage();
	
	public T getContent() {
		return content;
	}
}
