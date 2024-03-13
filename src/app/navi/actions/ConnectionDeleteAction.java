package app.navi.actions;

import org.eclipse.jface.action.Action;

import app.M;
import app.navi.ConnectionElement;
import app.rcp.Icon;
import app.store.Connections;
import app.util.MsgBox;

public class ConnectionDeleteAction extends Action {

	private final ConnectionElement e;

	public ConnectionDeleteAction(ConnectionElement e) {
		this.e = e;
		setText(M.Delete);
		setImageDescriptor(Icon.DELETE.des());
	}

	@Override
	public void run() {
		if (e == null || e.getContent() == null)
			return;
		boolean b = MsgBox.ask("#Delete connection?",
				"#Do you really want to delete the"
						+ " selected connection?");
		if (!b)
			return;
		Connections.delete(e.getContent());
	}

}
