package app.store;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.io.FileStore;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.M;
import app.editors.Editors;

/**
 * Deletes all data sets and external documents from the editors
 */
public class CleanUp implements IRunnableWithProgress {

	private FileStore store = App.store;
	private Logger log = LoggerFactory.getLogger(getClass());

	public int deleted = 0;
	public int failors = 0;

	@Override
	public void run(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		try {
			monitor.beginTask("#Delete data sets", totalWork() + 1);
			for (File dir : folders()) {
				monitor.subTask(dir.getName());
				for (File f : dir.listFiles()) {
					if (monitor.isCanceled())
						break;
					if (f.delete()) {
						deleted++;
					} else {
						log.error("failed to delete data set {}", f);
						failors++;
					}
					monitor.worked(1);
				}
			}
			monitor.done();
			App.runInUI(M.ReloadNavigation, this::refreshUI);
		} catch (Exception e) {
			throw new InvocationTargetException(e, e.getMessage());
		}
	}

	private int totalWork() {
		int total = 0;
		for (File dir : folders()) {
			total += dir.list().length;
		}
		return total;
	}

	private List<File> folders() {
		List<File> folders = new ArrayList<>();
		for (Class<?> c : classes()) {
			File dir = store.getFolder(c);
			if (dir == null || !dir.exists())
				continue;
			folders.add(dir);
		}
		File docDir = new File(store.getRootFolder(), "external_docs");
		if (docDir.exists())
			folders.add(docDir);
		return folders;
	}

	private Class<?>[] classes() {
		return new Class<?>[] {
				LCIAMethod.class, Process.class, Flow.class,
				FlowProperty.class, UnitGroup.class,
				Contact.class, Source.class
		};
	}

	private void refreshUI() {
		Editors.closeAll();
		App.run(new IndexBuilder());
	}
}
