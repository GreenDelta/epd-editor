package app.editors.io;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
import org.openlca.ilcd.sources.FileRef;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.util.RefTree;
import org.openlca.ilcd.util.Sources;

import app.App;
import app.M;
import app.store.RefTrees;
import epd.model.RefStatus;

public class Download implements IRunnableWithProgress {

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
	public void run(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		monitor.beginTask("#Download data sets", IProgressMonitor.UNKNOWN);
		while (!queue.isEmpty()) {
			Ref ref = queue.poll();
			handled.add(ref);
			IDataSet ds = get(ref);
			if (ds == null)
				continue;
			monitor.subTask(M.SaveDataSet + ": " + App.s(ds.getName()));
			save(ref, ds, status);
			if (ds instanceof Source) {
				extDocs((Source) ds, client, status);
			}
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

	/**
	 * Download the external documents of the given source from the soda4LCA
	 * server. For each file where the download failed, an error message is
	 * written to the status log.
	 */
	public static void extDocs(Source source, SodaClient client,
			List<RefStatus> stats) {
		for (FileRef ref : Sources.getFileRefs(source)) {
			String fileName = Sources.getFileName(ref);
			if (fileName == null || fileName.isEmpty())
				continue;
			try {
				InputStream is = client.getExternalDocument(
						source.getUUID(), fileName);
				File target = App.store().getExternalDocument(ref);
				Files.copy(is, target.toPath(),
						StandardCopyOption.REPLACE_EXISTING);
			} catch (Exception e) {
				stats.add(RefStatus.error(Ref.of(source),
						"#Failed to get external doc.: " + fileName));
			}
		}
	}

	/**
	 * Saves the given data set locally and updates the application index.
	 */
	public static void save(Ref ref, IDataSet ds, List<RefStatus> stats) {
		try {
			App.store().put(ds);
			App.index().remove(ref);
			App.index().add(ds);
			RefTrees.cache(ds);
			stats.add(RefStatus.downloaded(ref, "Downloaded/Updated"));
		} catch (Exception e) {
			stats.add(RefStatus.error(ref,
					"Failed to store data set: " + e.getMessage()));
		}
	}

	private IDataSet get(Ref ref) {
		if (!ref.isValid()) {
			status.add(RefStatus.error(ref, M.InvalidReference));
			return null;
		}
		Class<? extends IDataSet> type = ref.getDataSetClass();
		try {
			if (!overwriteExisting && App.store().contains(type, ref.uuid)) {
				status.add(RefStatus.info(ref, M.AlreadyExists));
				return null;
			}
			return client.get(type, ref.uuid);
		} catch (Exception e) {
			status.add(RefStatus.error(ref,
					M.DownloadFailed + ": " + e.getMessage()));
			return null;
		}
	}
}
