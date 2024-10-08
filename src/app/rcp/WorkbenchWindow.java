package app.rcp;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.openlca.ilcd.epd.EpdProfiles;

import app.App;
import app.M;
import app.StatusView;
import app.navi.NaviSync;
import app.navi.Navigator;
import app.store.IndexBuilder;
import app.store.RefDataSync;
import app.util.UI;
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
		conf.setTitle("EPD Editor " + App.version());
	}

	@Override
	public void postWindowOpen() {
		var appVersion = App.version();
		var workspaceVersion = App.getWorkspace().version();
		if (Objects.equals(appVersion, workspaceVersion)) {
			if (App.settings().syncRefDataOnStartup) {
				syncRefData();
			}
			return;
		}

		// copy the files
		var data = new File(App.getInstallLocation(), "data");
		App.runWithProgress("Copy reference data",
				() -> App.getWorkspace().syncFilesFrom(data));
		App.run(new IndexBuilder());
		Navigator.refreshAll();

		// finally, tag the workspace with the current version
		App.getWorkspace().setVersion(appVersion);
	}

	private void syncRefData() {

		// collect the URLs from the EPD profiles
		var urls = new ArrayList<String>();
		for (var profile : EpdProfiles.getAll()) {
			if (Strings.nullOrEmpty(profile.getDataUrl()))
				continue;
			if (!urls.contains(profile.getDataUrl()))
				urls.add(profile.getDataUrl());
		}
		if (urls.isEmpty())
			return;

		// update the reference data from the collected URLs
		var sync = new RefDataSync(urls);
		App.run("Synchronize reference data ...", sync, () -> {
			if (sync.stats.isEmpty())
				return;

			// update the navigation and display possible updates
			new NaviSync(App.index()).run();
			boolean didUpdates = false;
			for (var stat : sync.stats) {
				if (stat.isDownloaded()) {
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
