package app.editors.io;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.io.SodaClient;
import org.openlca.ilcd.util.RefTree;

import app.App;
import epd.model.RefStatus;

class Download implements IRunnableWithProgress {

	private final SodaClient client;
	boolean withDependencies = false;
	boolean overwriteExisting = false;
	final List<RefStatus> status = new ArrayList<>();

	private final Queue<Ref> queue = new ArrayDeque<>();
	private final Set<Ref> handled = new HashSet<>();

	Download(SodaClient client, List<Ref> refs) {
		this.client = client;
		queue.addAll(refs);
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		monitor.beginTask("#Download data sets", IProgressMonitor.UNKNOWN);
		while (!queue.isEmpty()) {
			Ref ref = queue.poll();
			handled.add(ref);
			IDataSet ds = get(ref);
			if (ds == null)
				continue;
			monitor.subTask("#Save data set " + App.s(ds.getName()));
			save(ref, ds);
			if (!withDependencies)
				continue;
			for (Ref next : RefTree.create(ds).getRefs()) {
				if (handled.contains(next) || queue.contains(next))
					continue;
				queue.add(next);
			}
		}
		App.dumpIndex();
	}

	private IDataSet get(Ref ref) {
		if (!ref.isValid()) {
			status.add(RefStatus.error(ref, "#Invalid reference"));
			return null;
		}
		Class<? extends IDataSet> type = ref.getDataSetClass();
		try {
			if (!overwriteExisting && App.store.contains(type, ref.uuid)) {
				status.add(RefStatus.cancel(ref, "#Already exisits locally"));
				return null;
			}
			return client.get(type, ref.uuid);
		} catch (Exception e) {
			status.add(RefStatus.error(ref, "#Download failed: " + e.getMessage()));
			return null;
		}
	}

	private void save(Ref ref, IDataSet ds) {
		try {
			App.store.put(ds);
			App.index.remove(ref);
			App.index.add(ds);
			status.add(RefStatus.ok(ref, "Downloaded"));
		} catch (Exception e) {
			status.add(RefStatus.error(ref, "#Download failed: " + e.getMessage()));
		}
	}
}
