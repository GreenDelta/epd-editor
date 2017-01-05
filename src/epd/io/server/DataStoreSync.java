package epd.io.server;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.util.SourceBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import epd.util.Dirs;

/**
 * Synchronizes a data set and its dependencies between two ILCD data stores.
 */
public class DataStoreSync {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final DataStore source;
	private final DataStore target;

	private final Queue<Ref> queue = new ArrayDeque<>();
	private final Set<Ref> handled = new HashSet<>();

	public DataStoreSync(DataStore source, DataStore target) {
		this.source = source;
		this.target = target;
	}

	public void run(Ref ref) {
		log.trace("synchronize resource tree for {}", ref);
		try {
			queue.add(ref);
			exec();
		} catch (Exception e) {
			log.error("failed to sync resources of " + ref, e);
		} finally {
			queue.clear();
			handled.clear();
		}
	}

	private void exec() {
		while (!queue.isEmpty()) {
			Ref ref = queue.poll();
			handled.add(ref);
			Object model = sync(ref);
			if (model == null)
				continue;
			Set<Ref> nextRefs = new RefTraversal().traverse(model);
			for (Ref nextRef : nextRefs) {
				if (!handled.contains(nextRef) && !queue.contains(nextRef))
					queue.add(nextRef);
			}
		}
	}

	private Object sync(Ref ref) {
		try {
			Class<? extends IDataSet> type = ref.getDataSetClass();
			if (target.contains(type, ref.uuid))
				return null;
			IDataSet model = source.get(type, ref.uuid);
			if (model == null)
				return null;
			if (model instanceof Source)
				syncSource((Source) model, ref);
			target.put(model);
			return model;
		} catch (Exception e) {
			log.error("failed to sync " + ref, e);
			return null;
		}
	}

	private void syncSource(Source model, Ref ref) throws Exception {
		File file = downloadFile(model);
		if (file == null)
			target.put(model);
		else {
			target.put(model, file);
			log.trace("delete directory {}", file.getParentFile());
			Dirs.delete(file.getParentFile().toPath());
		}
	}

	private File downloadFile(Source sourceDataSet) {
		try {
			SourceBag bag = new SourceBag(sourceDataSet, "en");
			if (bag.getExternalFileURIs().isEmpty())
				return null;
			String uri = bag.getExternalFileURIs().get(0);
			String fileName = new File(uri).getName();
			File dir = Files.createTempDirectory("epd_downloads").toFile();
			File tempFile = new File(dir, fileName);
			log.trace("download file to {}", tempFile);
			try (InputStream in = source.getExternalDocument(bag.getId(),
					fileName)) {
				Files.copy(in, tempFile.toPath(),
						StandardCopyOption.REPLACE_EXISTING);
			}
			return tempFile;
		} catch (Exception e) {
			log.warn("Failed to download external file", e);
			return null;
		}
	}
}
