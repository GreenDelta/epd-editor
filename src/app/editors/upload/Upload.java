package app.editors.upload;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.io.SodaClient;
import org.openlca.ilcd.sources.FileRef;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.util.Sources;

import app.App;
import epd.io.RefStatus;

class Upload {

	private final SodaClient client;
	private boolean cancelAll = false;

	Upload(SodaClient client) {
		this.client = client;
	}

	RefStatus next(Ref ref) {
		if (cancelAll)
			return new RefStatus(RefStatus.CANCEL, ref, "#Canceled");
		try {
			if (client.contains(ref))
				return new RefStatus(RefStatus.INFO, ref,
						"#Already on the server");
			IDataSet ds = App.store.get(ref.getDataSetClass(), ref.uuid);
			if (ds == null)
				return new RefStatus(RefStatus.ERROR, ref,
						"#Data set does not exist");
			if (ds instanceof Source)
				uploadSource((Source) ds);
			else
				client.put(ds);
			return new RefStatus(RefStatus.OK, ref, "#Uploaded");
		} catch (Exception e) {
			cancelAll = true;
			return new RefStatus(RefStatus.ERROR, ref,
					"#Upload failed: " + e.getMessage());
		}
	}

	private void uploadSource(Source source) throws Exception {
		List<FileRef> fileRefs = Sources.getFileRefs(source);
		List<File> files = new ArrayList<>();
		for (FileRef ref : fileRefs) {
			File file = App.store.getExternalDocument(ref);
			if (file == null || !file.exists())
				continue;
			files.add(file);
		}
		if (files.isEmpty())
			client.put(source);
		else {
			File[] array = files.toArray(
					new File[files.size()]);
			client.put(source, array);
		}
	}
}
