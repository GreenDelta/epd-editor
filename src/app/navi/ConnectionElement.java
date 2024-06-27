package app.navi;

import java.util.Objects;

import org.eclipse.swt.graphics.Image;
import org.openlca.ilcd.io.SodaConnection;

import app.rcp.Icon;

public class ConnectionElement extends NavigationElement {

	private final ConnectionFolder parent;
	private final SodaConnection con;

	public ConnectionElement(ConnectionFolder parent, SodaConnection con) {
		this.parent = parent;
		this.con = con;
	}

	public SodaConnection connection() {
		return con;
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
		return con.toString();
	}

	@Override
	public Image getImage() {
		return Icon.CONNECTION.img();
	}

	@Override
	public void update() {
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ConnectionElement other))
			return false;
		return Objects.equals(this.con, other.con);
	}
}
