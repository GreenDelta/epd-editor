package app.navi.actions;

import java.io.File;

import org.eclipse.jface.action.Action;

import app.M;
import app.rcp.Icon;
import app.store.Json;
import app.util.FileChooser;
import epd.profiles.EpdProfile;

public class ProfileExportAction extends Action {

	private final EpdProfile profile;

	public ProfileExportAction(EpdProfile profile) {
		this.profile = profile;
		setText(M.Export);
		setToolTipText(M.Export);
		setImageDescriptor(Icon.EXPORT.des());
	}

	@Override
	public void run() {
		if (profile == null)
			return;
		File file = FileChooser.save(profile.id + ".json", "*.json");
		if (file == null)
			return;
		Json.write(profile, file);
	}
}
