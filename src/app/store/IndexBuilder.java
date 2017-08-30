package app.store;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.io.FileStore;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.util.ClassList;
import org.openlca.ilcd.util.Refs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.navi.Sync;
import epd.index.Index;

/**
 * Creates a new data set index from the data sets in the local data store.
 */
public class IndexBuilder implements IRunnableWithProgress {

	private final FileStore store = App.store;
	private final Logger log = LoggerFactory.getLogger(getClass());

	private Index index;

	@Override
	public void run(IProgressMonitor m)
			throws InvocationTargetException, InterruptedException {
		index = new Index();
		List<File> dirs = folders();
		m.beginTask("#Build data set index", totalWork(dirs));
		for (File folder : dirs) {
			m.subTask("#Scan folder: " + folder.getName());
			for (File file : folder.listFiles()) {
				add(file);
				m.worked(1);
			}
		}
		App.index = index;
		App.dumpIndex();
		App.runInUI("Refresh navigation", new Sync(App.index));
	}

	private List<File> folders() {
		Class<?>[] classes = new Class<?>[] {
				LCIAMethod.class, Process.class, Flow.class,
				FlowProperty.class, UnitGroup.class,
				Contact.class, Source.class };
		List<File> folders = new ArrayList<>();
		for (Class<?> c : classes) {
			File dir = store.getFolder(c);
			if (dir == null || !dir.exists())
				continue;
			folders.add(dir);
		}
		return folders;
	}

	private int totalWork(List<File> folders) {
		int total = 0;
		for (File folder : folders) {
			total += folder.list().length;
		}
		return total;
	}

	private void add(File f) {
		try {
			Ref ref;
			try (FileInputStream is = new FileInputStream(f)) {
				ref = Refs.fetch(is);
			}
			List<Classification> classes;
			try (FileInputStream is = new FileInputStream(f)) {
				classes = ClassList.read(is);
			}
			index.add(ref, classes);
		} catch (Exception e) {
			log.error("failed to read reference and class from file " + f, e);
		}
	}
}
