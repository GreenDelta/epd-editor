package app.store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.descriptors.Descriptor;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.io.SodaClient;
import org.openlca.ilcd.io.SodaConnection;
import org.openlca.ilcd.methods.ImpactMethod;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.editors.io.Download;
import epd.model.RefStatus;
import epd.model.Version;
import epd.util.Strings;

/**
 * Synchronize reference data from a data stock URL with the local data store.
 */
public class RefDataSync implements Runnable {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final List<String> urls;
	public List<String> errors = new ArrayList<>();
	public List<RefStatus> stats = new ArrayList<>();

	public RefDataSync(String url) {
		urls = Collections.singletonList(url);
	}

	public RefDataSync(List<String> urls) {
		this.urls = urls;
	}

	@Override
	public void run() {
		if (urls == null || urls.isEmpty())
			return;
		for (String url : urls) {
			var con = makeConnection(url);
			if (con != null) {
				doSync(con);
			}
		}
		App.getWorkspace().saveIndex();
	}

	private SodaConnection makeConnection(String url) {
		if (Strings.nullOrEmpty(url))
			return null;
		String baseUrl = url;
		String dataStock = null;
		if (url.contains("/datastocks/")) {
			String[] parts = url.split("/datastocks/");
			baseUrl = parts[0];
			dataStock = parts[1].replace("/", "");
		}
		var con = new SodaConnection();
		con.dataStockId = dataStock;
		con.url = baseUrl;
		return con;
	}

	@SuppressWarnings("unchecked")
	private void doSync(SodaConnection con) {
		try (var client = SodaClient.of(con)) {
			log.info("Connected to {} (datastock={})", con.url,
					con.dataStockId);
			Class<?>[] types = new Class<?>[] {
					Contact.class,
					Source.class,
					UnitGroup.class,
					FlowProperty.class,
					Flow.class,
					Process.class,
					ImpactMethod.class,
			};
			for (Class<?> type : types) {
				log.info("Fetch descriptors for type {}", type);
				var descriptors = client.getDescriptors(type);
				log.info("Fetch {} descriptors", descriptors.size());
				sync(client, (Class<? extends IDataSet>) type, descriptors);
			}
		} catch (Exception e) {
			log.error("Ref. data download failed", e);
			errors.add("Download failed for " + con.url
					+ "; datastock=" + con.dataStockId);
		}
	}

	private void sync(SodaClient client, Class<? extends IDataSet> type,
			List<Descriptor<?>> descriptors) {
		for (var d : descriptors) {
			Ref newRef = d.toRef();
			Ref oldRef = App.index().find(newRef);
			if (!shouldDownload(newRef, oldRef)) {
				stats.add(RefStatus.ok(oldRef, "No newer version on server"));
				continue;
			}
			try {
				var ds = client.get(type, newRef.getUUID());
				if (ds == null) {
					stats.add(RefStatus.error(newRef,
							"The downloaded thing was not a data set"));
					continue;
				}
				Download.save(newRef, ds, stats);
				if (ds instanceof Source) {
					Download.extDocs((Source) ds, client, stats);
				}
			} catch (Exception e) {
				stats.add(RefStatus.error(newRef,
						"Download failed: " + e.getMessage()));
			}
		}
	}

	private boolean shouldDownload(Ref newRef, Ref oldRef) {
		if (newRef == null)
			return false;
		if (oldRef == null)
			return true;
		Version newV = Version.fromString(newRef.getVersion());
		Version oldV = Version.fromString(oldRef.getVersion());
		if (newV.getMajor() != oldV.getMajor())
			return newV.getMajor() > oldV.getMajor();
		if (newV.getMinor() != oldV.getMinor())
			return newV.getMinor() > oldV.getMinor();
		return newV.getUpdate() > oldV.getUpdate();
	}
}
