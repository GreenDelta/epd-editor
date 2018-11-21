package app.store;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.io.ZipStore;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.util.ClassList;
import org.openlca.ilcd.util.Refs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.M;
import app.StatusView;
import app.navi.Sync;
import epd.model.RefStatus;

public class ZipImport implements IRunnableWithProgress {

	private Logger log = LoggerFactory.getLogger(getClass());
	private final ZipStore zip;
	private final List<RefStatus> status = new ArrayList<>();

	public ZipImport(ZipStore zip) {
		this.zip = zip;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void run(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		try {
			monitor.beginTask("Import", getTotalWork());
			for (Class<?> c : classes()) {
				if (monitor.isCanceled())
					break;
				run((Class<? extends IDataSet>) c, monitor);
			}
			extDocs(monitor);
			zip.close();
			App.dumpIndex();
			monitor.done();
			App.runInUI("Refresh...", () -> new Sync(App.index).run());
			StatusView.open(M.Import, status);
		} catch (Exception e) {
			throw new InvocationTargetException(e, e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	private int getTotalWork() {
		int total = 0;
		for (Class<?> c : classes()) {
			total += zip.getEntries((Class<? extends IDataSet>) c).size();
		}
		total += zip.getEntries("external_docs").size();
		return total;
	}

	private void run(Class<? extends IDataSet> type, IProgressMonitor monitor)
			throws Exception {
		File dir = App.store.getFolder(type);
		if (!dir.exists())
			dir.mkdirs();
		monitor.subTask(dir.getName());
		for (Path p : zip.getEntries(type)) {
			if (monitor.isCanceled())
				break;
			byte[] data = Files.readAllBytes(p);
			ByteArrayInputStream is = new ByteArrayInputStream(data);
			Ref ref = Refs.fetch(is);
			if (ref == null || !ref.isValid()) {
				log.warn("invalid data set {} not imported", p);
				continue;
			}
			is.reset();
			List<Classification> classes = ClassList.read(is);
			File f = new File(dir, getFileName(ref));
			Files.write(f.toPath(), data, StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
			RefTrees.remove(ref);
			App.index.add(ref, classes);
			status.add(RefStatus.ok(ref, M.Imported));
			monitor.worked(1);
		}
	}

	private void extDocs(IProgressMonitor monitor) throws Exception {
		monitor.subTask("external_docs");
		File targetDir = new File(App.store.getRootFolder(), "external_docs");
		if (!targetDir.exists())
			targetDir.mkdirs();
		for (Path doc : zip.getEntries("external_docs")) {
			if (monitor.isCanceled())
				break;
			String name = doc.getFileName().toString();
			File target = new File(targetDir, name);
			Files.copy(doc, target.toPath(),
					StandardCopyOption.REPLACE_EXISTING);
			monitor.worked(1);
		}
	}

	private Class<?>[] classes() {
		return new Class<?>[] { Contact.class, Source.class, UnitGroup.class,
				FlowProperty.class, Flow.class,
				LCIAMethod.class, Process.class };
	}

	private String getFileName(Ref ref) {
		String n = ref.uuid;
		// TODO: add version to XML file when we support multiple versions
		// if (ref.version != null) {
		// n += "_" + ref.version;
		// }
		return n + ".xml";
	}

}
