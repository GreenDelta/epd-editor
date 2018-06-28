package app.editors.profiles;

import java.util.ArrayList;
import java.util.List;

import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.descriptors.Descriptor;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.io.SodaClient;
import org.openlca.ilcd.io.SodaConnection;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.StatusView;
import app.editors.io.Download;
import app.util.MsgBox;
import epd.model.RefStatus;
import epd.model.Version;
import epd.util.Strings;

class RefDataDownload implements Runnable {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final SodaConnection con;
	private String error;
	private List<RefStatus> stats = new ArrayList<>();

	private RefDataDownload(SodaConnection con) {
		this.con = con;
	}

	static void run(String url) {
		if (Strings.nullOrEmpty(url)) {
			MsgBox.error("#Empty URL", "#The URL is empty.");
			return;
		}
		String baseUrl = url;
		String dataStock = null;
		if (url.contains("/datastocks/")) {
			String[] parts = url.split("/datastocks/");
			baseUrl = parts[0];
			dataStock = parts[1].replace("/", "");
		}
		boolean b = MsgBox.ask("#Download reference data",
				"#Do you want to download / update the "
						+ "reference data of this profile?");
		if (!b)
			return;
		SodaConnection con = new SodaConnection();
		con.dataStockId = dataStock;
		con.url = baseUrl;
		RefDataDownload download = new RefDataDownload(con);
		App.run("#Download reference data", download, download::doAfter);
	}

	@Override
	public void run() {
		try (SodaClient client = new SodaClient(con)) {
			log.info("Connected to {} (datastock={})", con.url,
					con.dataStockId);
			Class<?>[] types = new Class<?>[] {
					Contact.class, Source.class, UnitGroup.class,
					FlowProperty.class, Flow.class, Process.class,
					LCIAMethod.class };
			for (Class<?> type : types) {
				log.info("Fetch descriptors for type {}", type);
				List<Descriptor> descriptors = client.getDescriptors(type);
				log.info("Fetch {} descriptors", descriptors.size());
				sync(client, type, descriptors);
			}
		} catch (Exception e) {
			log.error("Ref. data download failed", e);
			error = "#Download failed: " + e.getMessage();
		}
	}

	private void sync(SodaClient client, Class<?> type,
			List<Descriptor> descriptors) {
		for (Descriptor d : descriptors) {
			Ref newRef = d.toRef();
			Ref oldRef = App.index.find(newRef);
			if (!shouldDownload(newRef, oldRef)) {
				stats.add(RefStatus.ok(oldRef, "No newer version on server"));
				continue;
			}
			try {
				Object obj = client.get(type, newRef.uuid);
				if (!(obj instanceof IDataSet)) {
					stats.add(RefStatus.error(newRef,
							"The downloaded thing was not a data set"));
					continue;
				}
				IDataSet ds = (IDataSet) obj;
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
		Version newV = Version.fromString(newRef.version);
		Version oldV = Version.fromString(oldRef.version);
		if (newV.getMajor() != oldV.getMajor())
			return newV.getMajor() > oldV.getMajor();
		if (newV.getMinor() != oldV.getMinor())
			return newV.getMinor() > oldV.getMinor();
		return newV.getUpdate() > oldV.getUpdate();
	}

	private void doAfter() {
		if (error != null) {
			MsgBox.error(error);
		} else if (stats.size() == 0) {
			MsgBox.info("#No data found on server");
		}
		if (stats.size() > 0) {
			StatusView.open("#Synchronized data sets from " + con.url, stats);
		}
	}

}
