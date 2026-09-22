package app.navi.actions;

import org.eclipse.jface.action.Action;

import app.M;
import app.editors.profiles.ProfileEditor;
import app.rcp.Icon;
import app.store.Profiles;
import app.util.MsgBox;

/// Creates a new EPD profile, saves it in the workspace and opens it in the
/// profile editor.
public class NewProfileAction extends Action {

	public NewProfileAction() {
		setText(M.NewProfile);
		setToolTipText(M.NewProfile);
		setImageDescriptor(Icon.ADD.des());
	}

	@Override
	public void run() {
		var profile = Profiles.create();
		if (profile == null) {
			MsgBox.error(M.FailedToSaveProfile);
			return;
		}
		ProfileEditor.open(profile);
	}
}
