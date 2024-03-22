package app.editors.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.io.SodaClient;
import org.openlca.ilcd.sources.FileRef;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.util.Sources;
import org.slf4j.LoggerFactory;

import app.App;
import epd.model.RefStatus;

class Upload {

	private final SodaClient client;
	private boolean cancelAll = false;

	Upload(SodaClient client) {
		this.client = client;
	}

	RefStatus next(Ref ref) {
		if (cancelAll)
			return RefStatus.cancel(ref, "Canceled");
		try {
			if (client.contains(ref))
				return RefStatus.info(ref, "Already on the server");
			var ds = App.store().get(ref.getDataSetClass(), ref.getUUID());
			if (ds == null)
				return RefStatus.error(ref, "Data set does not exist");
			if (ds instanceof Source)
				uploadSource((Source) ds);
			else
				client.put(ds);
			return RefStatus.ok(ref, "Uploaded");
		} catch (Exception e) {
			LoggerFactory.getLogger(getClass())
				.error("upload of dataset failed: " + ref, e);
			cancelAll = true;
			return RefStatus.error(ref, "Upload failed: " + e.getMessage());
		}
	}

	private void uploadSource(Source source) {
		List<FileRef> fileRefs = Sources.getFileRefs(source);
		List<File> files = new ArrayList<>();
		for (FileRef ref : fileRefs) {
			File file = App.store().getExternalDocument(ref);
			if (file == null || !file.exists())
				continue;
			files.add(file);
		}
		if (files.isEmpty())
			client.put(source);
		else {
			client.put(source, files.toArray(new File[0]));
		}
	}
}
