package app.navi.actions;

import org.eclipse.jface.action.Action;

import app.navi.ConnectionElement;
import app.navi.Navigator;
import app.rcp.Icon;
import app.store.Connections;
import app.util.MsgBox;

public class ConnectionDeleteAction extends Action {

	private final ConnectionElement e;

	public ConnectionDeleteAction(ConnectionElement e) {
		this.e = e;
		setText("#Delete connection");
		setImageDescriptor(Icon.DELETE.des());
	}

	@Override
	public void run() {
		if (e == null || e.con == null)
			return;
		boolean b = MsgBox.ask("#Delete connection?",
				"#Do you really want to delete the"
						+ " selected connection?");
		if (!b)
			return;
		Connections.delete(e.con);
		Navigator.refresh(e.getParent());
	}

}
