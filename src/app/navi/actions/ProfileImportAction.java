package app.navi.actions;

import java.io.File;

import org.eclipse.jface.action.Action;

import app.M;
import app.navi.Navigator;
import app.rcp.Icon;
import app.store.EpdProfiles;
import app.store.Json;
import app.util.FileChooser;
import app.util.MsgBox;
import epd.model.EpdProfile;

public class ProfileImportAction extends Action {

	public ProfileImportAction() {
		setText(M.Import);
		setToolTipText(M.Import);
		setImageDescriptor(Icon.IMPORT.des());
	}

	@Override
	public void run() {
		File file = FileChooser.open("*.json");
		if (file == null)
			return;
		EpdProfile profile = Json.read(file, EpdProfile.class);
		if (profile == null) {
			MsgBox.error("#Could not read EPD profile from " + file.getName());
			return;
		}
		if (profile.id == null || profile.name == null) {
			MsgBox.error("#An EPD profile must have an ID or name");
			return;
		}
		EpdProfile other = EpdProfiles.get(profile.id);
		if (other != null) {
			boolean b = MsgBox.ask("#Overwrite profile?",
					"A profile with this ID already exists. "
							+ "Do you want to overwrite it?");
			if (!b)
				return;
		}
		EpdProfiles.save(profile);
		Navigator.refreshProfiles();
	}

}
