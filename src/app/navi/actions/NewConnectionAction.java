package app.navi.actions;

import java.util.UUID;

import org.eclipse.jface.action.Action;
import org.openlca.ilcd.io.SodaConnection;

import app.editors.connection.ConnectionEditor;
import app.navi.ConnectionFolder;
import app.navi.Navigator;
import app.rcp.Icon;
import app.store.Connections;

public class NewConnectionAction extends Action {

	private ConnectionFolder folder;

	public NewConnectionAction(ConnectionFolder folder) {
		setText("#New connection");
		setImageDescriptor(Icon.CONNECTION.des());
		this.folder = folder;
	}

	@Override
	public void run() {
		SodaConnection con = new SodaConnection();
		con.uuid = UUID.randomUUID().toString();
		con.url = "http://localhost:8080/soda/resource";
		con.user = "admin";
		con.password = "default";
		Connections.save(con);
		Navigator.refresh(folder);
		ConnectionEditor.open(con);
	}
}
