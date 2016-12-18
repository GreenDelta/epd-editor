package app.navi.actions;

import org.eclipse.jface.action.Action;

import app.navi.ConnectionFolder;
import app.rcp.Icon;

public class NewConnectionAction extends Action {

	private ConnectionFolder folder;

	public NewConnectionAction(ConnectionFolder folder) {
		setText("#New connection");
		setImageDescriptor(Icon.CONNECTION.des());
		this.folder = folder;
	}

	@Override
	public void run() {
	}

}
