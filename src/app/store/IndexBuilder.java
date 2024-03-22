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
import org.openlca.ilcd.methods.ImpactMethod;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.util.Categories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.navi.NaviSync;
import epd.refs.RefFetch;
import epd.index.Index;

/**
 * Creates a new data set index from the data sets in the local data store.
 */
public class IndexBuilder implements IRunnableWithProgress {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public void run(IProgressMonitor m)
		throws InvocationTargetException, InterruptedException {
		var index = new Index();
		List<File> dirs = folders();
		m.beginTask("Build data set index", totalWork(dirs));
		for (File folder : dirs) {
			m.subTask("Scan folder: " + folder.getName());
			var files = folder.listFiles();
			if (files == null)
				continue;
			for (File file : files) {
				add(file, index);
				m.worked(1);
			}
		}
		App.updateIndex(index);
		App.runInUI("Refresh navigation", new NaviSync(App.index()));
	}

	private List<File> folders() {
		Class<?>[] classes = new Class<?>[]{
			ImpactMethod.class,
			Process.class,
			Flow.class,
			FlowProperty.class,
			UnitGroup.class,
			Contact.class,
			Source.class};
		List<File> folders = new ArrayList<>();
		for (Class<?> c : classes) {
			File dir = App.store().getFolder(c);
			if (dir == null || !dir.exists())
				continue;
			folders.add(dir);
		}
		return folders;
	}

	private int totalWork(List<File> folders) {
		int total = 0;
		for (File folder : folders) {
			var files = folder.list();
			if (files != null) {
				total += files.length;
			}
		}
		return total;
	}

	private void add(File f, Index index) {
		try {
			Ref ref;
			try (FileInputStream is = new FileInputStream(f)) {
				ref = RefFetch.get(is).orElse(null);
			}
			List<Classification> classes;
			try (FileInputStream is = new FileInputStream(f)) {
				classes = Categories.read(is);
			}
			index.add(ref, classes);
		} catch (Exception e) {
			log.error("failed to read reference and class from file " + f, e);
		}
	}
}
