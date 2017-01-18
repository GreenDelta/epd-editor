package app.editors.upload;

import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.io.SodaClient;

import app.App;
import epd.io.RefStatus;

class Upload {

	private final SodaClient client;

	Upload(SodaClient client) {
		this.client = client;
	}

	RefStatus next(Ref ref) {
		try {
			if (client.contains(ref.getDataSetClass(), ref.uuid))
				return new RefStatus(RefStatus.INFO, ref,
						"#Already on the server");

			IDataSet ds = App.store.get(ref.getDataSetClass(), ref.uuid);
			client.put(ds);
			// TODO: upload external files ...
			return new RefStatus(RefStatus.OK, ref, "#Uploaded");
		} catch (Exception e) {
			return new RefStatus(RefStatus.ERROR, ref,
					"#Upload failed: " + e.getMessage());
		}
	}

}
