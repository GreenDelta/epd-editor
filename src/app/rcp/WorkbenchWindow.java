package app.rcp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import app.App;
import app.M;
import app.StatusView;
import app.navi.Sync;
import app.store.EpdProfiles;
import app.store.RefDataSync;
import app.util.UI;
import epd.model.EpdProfile;
import epd.model.RefStatus;
import epd.util.Strings;

public class WorkbenchWindow extends WorkbenchWindowAdvisor {

	public WorkbenchWindow(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	@Override
	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer conf) {
		return new ActionBar(conf);
	}

	@Override
	public void preWindowOpen() {
		var conf = getWindowConfigurer();
		conf.setInitialSize(new Point(800, 600));
		conf.setShowCoolBar(true);
		conf.setShowStatusLine(true);
		conf.setShowProgressIndicator(true);
		conf.setShowMenuBar(true);
		conf.setTitle("EPD - Editor " + App.version());
	}

	@Override
	public void postWindowOpen() {
		if (!App.settings().syncRefDataOnStartup)
			return;
		List<String> urls = new ArrayList<>();
		for (EpdProfile p : EpdProfiles.getAll()) {
			if (Strings.nullOrEmpty(p.referenceDataUrl))
				continue;
			if (!urls.contains(p.referenceDataUrl))
				urls.add(p.referenceDataUrl);
		}
		if (urls.isEmpty())
			return;
		RefDataSync sync = new RefDataSync(urls);
		App.run("Synchronize reference data ...", sync, () -> {
			if (sync.stats.isEmpty())
				return;
			new Sync(App.index()).run();
			boolean didUpdates = false;
			for (RefStatus stat : sync.stats) {
				if (stat.value == RefStatus.DOWNLOADED) {
					didUpdates = true;
					break;
				}
			}
			if (didUpdates) {
				int code = MessageDialog.open(
						MessageDialog.INFORMATION, UI.shell(),
						M.Information, M.UpdatedReferenceData, SWT.NONE,
						"OK", M.ShowDetails);
				if (code == 1) {
					StatusView.open(M.UpdatedReferenceData, sync.stats);
				}
			}
		});
	}

	@Override
	public void postWindowClose() {
		ImageManager.dispose();
	}
}
