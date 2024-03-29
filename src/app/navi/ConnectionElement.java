package app.navi;

import org.eclipse.swt.graphics.Image;
import org.openlca.ilcd.io.SodaConnection;

import app.rcp.Icon;

public class ConnectionElement extends NavigationElement<SodaConnection> {

	private final ConnectionFolder parent;

	public ConnectionElement(ConnectionFolder parent, SodaConnection con) {
		this.parent = parent;
		this.content = con;
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
		return getContent().toString();
	}

	@Override
	public Image getImage() {
		return Icon.CONNECTION.img();
	}

	@Override
	public void update() {
	}
}
