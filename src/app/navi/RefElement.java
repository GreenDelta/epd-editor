package app.navi;

import org.eclipse.swt.graphics.Image;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;

import app.App;
import app.rcp.Icon;
import epd.util.Strings;

public class RefElement extends NavigationElement<Ref> {

	private final NavigationElement<?> parent;

	public RefElement(NavigationElement<?> parent, Ref ref) {
		this.parent = parent;
		this.content = ref;
	}

	@Override
	public NavigationElement<?> getParent() {
		return parent;
	}

	@Override
	public int compareTo(NavigationElement other) {
		if (!(other instanceof RefElement))
			return 1;
		return Strings.compare(this.getLabel(), other.getLabel());
	}

	@Override
	public String getLabel() {
		return LangString.getFirst(getContent().getName(), App.lang());
	}

	@Override
	public Image getImage() {
		return Icon.img(getContent().getType());
	}

	@Override
	public void update() {
	}
}
