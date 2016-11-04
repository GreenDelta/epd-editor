package app.rcp;

import java.io.File;

import org.eclipse.jface.action.Action;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.io.ZipStore;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.navi.Navigator;
import app.util.FileChooser;
import app.util.MsgBox;

class ImportAction extends Action {

	private Logger log = LoggerFactory.getLogger(getClass());

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
			run(Contact.class, zip);
			run(Source.class, zip);
			run(Flow.class, zip);
			run(FlowProperty.class, zip);
			run(UnitGroup.class, zip);
			App.dumpIndex();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to import data sets", e);
		}
	}

	private void run(Class<? extends IDataSet> type, ZipStore zip)
			throws Exception {
		zip.iterator(type).forEachRemaining(d -> {
			Ref ref = Ref.of(d);
			if (ref == null || !ref.isValid())
				return;
			log.info("import {}", ref);
			try {
				App.store.put(d, ref.uuid);
				App.index.add(d);
			} catch (Exception e) {
				throw new RuntimeException("failed to save " + ref, e);
			}
		});
	}
}
