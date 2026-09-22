package app.navi.actions;

import org.eclipse.jface.action.Action;
import org.openlca.ilcd.epd.EpdProfile;

import app.M;
import app.editors.profiles.ProfileEditor;
import app.rcp.Icon;
import app.store.Profiles;
import app.util.MsgBox;

/// Creates an editable copy of the given profile and opens it in the profile
/// editor. This is the way to create a user defined profile that is based on
/// a built-in profile.
public class DuplicateProfileAction extends Action {

	private final EpdProfile profile;

	public DuplicateProfileAction(EpdProfile profile) {
		this.profile = profile;
		setText(M.DuplicateProfile);
		setToolTipText(M.DuplicateProfile);
		setImageDescriptor(Icon.COPY.des());
	}

	@Override
	public void run() {
		var copy = Profiles.copyOf(profile);
		if (copy == null) {
			MsgBox.error(M.FailedToSaveProfile);
			return;
		}
		ProfileEditor.open(copy);
	}
}
