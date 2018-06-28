package app.editors.connection;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;

import app.M;
import app.navi.Navigator;
import app.store.EpdProfiles;
import app.util.MsgBox;
import epd.model.EpdProfile;

public class EpdProfileDownload {

	private EpdProfileDownload() {
	}

	public static void runInUI(String baseURL) {
		List<EpdProfile> results = new ArrayList<>();
		BusyIndicator.showWhile(Display.getDefault(), () -> {
			EpdProfiles.downloadAll(baseURL, results::add);
			Navigator.refreshProfiles();
		});
		if (results.isEmpty()) {
			MsgBox.info(M.NoEPDProfilesOnServer_Message);
			return;
		}
		String text = M.DownloadedEPDProfiles_Message + ": ";
		for (int i = 0; i < results.size(); i++) {
			text += results.get(i).name;
			if (i < results.size() - 1) {
				text += ", ";
			}
		}
		MsgBox.info(M.DownloadedEPDProfiles, text);
	}

}
