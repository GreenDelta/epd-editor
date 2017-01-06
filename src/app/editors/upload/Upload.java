package app.editors.upload;

import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.io.SodaClient;

import app.App;

class Upload {

	private final SodaClient client;

	Upload(SodaClient client) {
		this.client = client;
	}

	void next(Ref ref) {
		try {
			IDataSet ds = App.store.get(ref.getDataSetClass(), ref.uuid);
			client.put(ds);
			// TODO: upload external files ...
		} catch (Exception e) {
			// TODO: log etc
			e.printStackTrace();
		}
	}

}
