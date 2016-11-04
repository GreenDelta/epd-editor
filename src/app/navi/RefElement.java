package app.navi;

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;

import app.App;
import app.rcp.Icon;

public class RefElement implements NavigationElement {

	private final Ref ref;
	private NavigationElement parent;

	public RefElement(NavigationElement parent, Ref ref) {
		this.parent = parent;
		this.ref = ref;
	}

	@Override
	public List<NavigationElement> getChilds() {
		return Collections.emptyList();
	}

	@Override
	public NavigationElement getParent() {
		return parent;
	}

	@Override
	public int compareTo(NavigationElement other) {
		return 0;
	}

	@Override
	public String getLabel() {
		return LangString.getFirst(ref.name, App.lang);
	}

	@Override
	public Image getImage() {
		return Icon.img(ref.type);
	}

	@Override
	public void update() {
	}
}
