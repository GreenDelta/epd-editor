package app.navi;

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import app.rcp.Icon;
import epd.model.Ref;

public class DataRefElement implements NavigationElement {

	private final Ref ref;

	public DataRefElement(Ref ref) {
		this.ref = ref;
	}

	@Override
	public List<NavigationElement> getChilds() {
		return Collections.emptyList();
	}

	@Override
	public NavigationElement getParent() {
		return null;
	}

	@Override
	public int compareTo(NavigationElement other) {
		return 0;
	}

	@Override
	public String getLabel() {
		return ref.name;
	}

	@Override
	public Image getImage() {
		return Icon.img(ref.type);
	}

	@Override
	public void update() {
	}
}
