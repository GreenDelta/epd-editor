package app.navi.actions;

import org.eclipse.jface.action.Action;

import app.M;
import app.navi.Navigator;
import app.rcp.Icon;
import app.util.MsgBox;
import epd.profiles.EpdProfile;
import epd.profiles.EpdProfiles;

public class ProfileDeleteAction extends Action {

	private final EpdProfile profile;

	public ProfileDeleteAction(EpdProfile profile) {
		this.profile = profile;
		setText(M.Delete);
		setToolTipText(M.Delete);
		setImageDescriptor(Icon.DELETE.des());
	}

	@Override
	public void run() {
		if (profile == null)
			return;
		if (EpdProfiles.isDefault(profile)) {
			MsgBox.info("#The default EPD profile cannot be deleted.");
			return;
		}
		EpdProfiles.delete(profile.id);
		Navigator.refreshProfiles();
	}

}
