package app.navi.actions;

import org.eclipse.jface.action.Action;
import org.openlca.ilcd.epd.EpdProfile;

import app.M;
import app.rcp.Icon;
import app.store.Profiles;
import app.util.MsgBox;

/// Deletes a user defined EPD profile. Built-in profiles cannot be deleted;
/// the action is disabled for them.
public class ProfileDeleteAction extends Action {

	private final EpdProfile profile;

	public ProfileDeleteAction(EpdProfile profile) {
		this.profile = profile;
		setText(M.DeleteProfile);
		setToolTipText(M.DeleteProfile);
		setImageDescriptor(Icon.DELETE.des());
		setEnabled(profile != null && !Profiles.isBuiltIn(profile));
	}

	@Override
	public void run() {
		if (profile == null || Profiles.isBuiltIn(profile))
			return;
		if (!MsgBox.ask(M.DeleteProfile, M.DeleteProfileQuestion))
			return;
		if (!Profiles.delete(profile)) {
			MsgBox.error(M.FailedToDeleteProfile);
		}
	}
}
