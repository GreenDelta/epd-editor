package app.navi;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.openlca.ilcd.io.SodaConnection;

import app.rcp.Icon;
import app.store.Connections;

public class ConnectionFolder implements NavigationElement {

	private final NavigationElement parent;
	private List<NavigationElement> childs;

	public ConnectionFolder(NavigationElement parent) {
		this.parent = parent;
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
	public NavigationElement getParent() {
		return parent;
	}

	@Override
	public int compareTo(NavigationElement other) {
		return 0;
	}

	@Override
	public String getLabel() {
		return "#Connections";
	}

	@Override
	public Image getImage() {
		return Icon.FOLDER.img();
	}

	@Override
	public void update() {
		childs.clear();
		for (SodaConnection con : Connections.get()) {
			childs.add(new ConnectionElement(this, con));
		}
	}

}
