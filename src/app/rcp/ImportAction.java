package app.rcp;

import java.io.File;

import org.eclipse.jface.action.Action;
import org.openlca.ilcd.io.ZipStore;
import org.openlca.ilcd.processes.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.navi.Navigator;
import app.util.FileChooser;
import app.util.MsgBox;
import epd.model.Ref;

class ImportAction extends Action {

	ImportAction() {
		setText("#Import Data Package");
		setImageDescriptor(Icon.IMPORT.des());
	}

	@Override
	public void run() {
		File zipFile = FileChooser.open("*.zip");
		if (zipFile == null)
			return;
		boolean b = MsgBox.ask("#Import data sets?", "#Should we import all "
				+ "data sets from the selected file?");
		if (!b)
			return;
		App.run("Import...",
				() -> doIt(zipFile),
				Navigator::refresh);
	}

	private void doIt(File zipFile) {
		try (ZipStore zip = new ZipStore(zipFile)) {
			run(Process.class, zip);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to import data sets", e);
		}
	}

	private void run(Class<?> type, ZipStore zip) throws Exception {
		zip.iterator(type).forEachRemaining(d -> {
			Ref ref = Ref.of(d, App.lang);
			if (ref == null || !ref.isValid())
				return;
			try {
				App.store.put(d, ref.uuid);
			} catch (Exception e) {
				throw new RuntimeException("failed to save " + ref, e);
			}
		});
	}
}
