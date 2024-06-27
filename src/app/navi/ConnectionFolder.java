package app.navi;

import org.eclipse.swt.graphics.Image;
import org.openlca.ilcd.io.SodaConnection;

import app.M;
import app.rcp.Icon;
import app.store.Connections;

public class ConnectionFolder extends NavigationElement<Void> {

	private final NavigationElement<?> parent;

	public ConnectionFolder(NavigationElement<?> parent) {
		this.parent = parent;
	}

	@Override
	public NavigationElement<?> getParent() {
		return parent;
	}

	@Override
	public int compareTo(NavigationElement<?> other) {
		return 0;
	}

	@Override
	public String getLabel() {
		return M.ServerConnections;
	}

	@Override
	public Image getImage() {
		return Icon.FOLDER.img();
	}

	@Override
	public void update() {
		if (childs == null)
			return;
		childs.clear();
		for (SodaConnection con : Connections.get()) {
			childs.add(new ConnectionElement(this, con));
		}
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ConnectionFolder;
	}
}
