package epd.io.server;

import java.util.Objects;
import java.util.Set;

import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.io.FileStore;
import org.openlca.ilcd.io.SodaClient;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.ProcessInfo;
import org.openlca.ilcd.processes.QuantitativeReference;
import org.openlca.ilcd.sources.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import epd.io.EpdStore;
import epd.model.EpdDataSet;
import epd.model.EpdDescriptor;

class Download {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final SodaClient webStore;
	private final FileStore localStore;
	private final EpdStore epdStore;

	public Download(SodaClient webStore, EpdStore epdStore) {
		this.webStore = webStore;
		this.epdStore = epdStore;
		this.localStore = epdStore.ilcdStore;
	}

	public void doIt(EpdDescriptor descriptor) {
		try {
			log.trace("download EPD {}", descriptor);
			Process process = webStore
					.get(Process.class, descriptor.refId);
			localStore.put(process);
			syncFlow(getRefProductRef(process));
			EpdDataSet dataSet = epdStore.open(descriptor);
			syncGenericProduct(dataSet);
			syncActorsAndSources(dataSet);
		} catch (Exception e) {
			log.error("download failed", e);
		}
	}

	private Ref getRefProductRef(Process process) {
		if (process == null)
			return null;
		ProcessInfo info = process.processInfo;
		if (info == null)
			return null;
		QuantitativeReference qRef = info.quantitativeReference;
		if (qRef == null || qRef.referenceFlows.isEmpty())
			return null;
		Integer exchangeId = qRef.referenceFlows.get(0);
		if (exchangeId == null)
			return null;
		for (Exchange exchange : process.exchanges) {
			if (exchangeId == exchange.id)
				return exchange.flow;
		}
		return null;
	}

	private void syncActorsAndSources(EpdDataSet dataSet) {
		if (dataSet == null)
			return;
		Set<Ref> refs = new RefTraversal().traverse(dataSet);
		for (Ref ref : refs) {
			if (Objects.equals(ref.type, Contact.class))
				syncActor(ref);
			else if (Objects.equals(ref.type, Source.class))
				syncSource(ref);
		}
	}

	private void syncGenericProduct(EpdDataSet dataSet) {
		// TODO: sync products
		// if (dataSet == null || dataSet.declaredProduct == null)
		// return;
		// EpdProduct product = dataSet.declaredProduct;
		// if (product.genericFlow == null)
		// return;
		// syncFlow(product.genericFlow);
	}

	private void syncFlow(Ref ref) {
		if (ref == null || !ref.isValid())
			return;
		log.trace("download and import resources for flow {}", ref);
		try {
			DataStoreSync sync = new DataStoreSync(webStore, localStore);
			sync.run(ref);
		} catch (Exception e) {
			log.error("failed to download and import flow " + ref, e);
		}
	}

	private void syncActor(Ref ref) {
		if (ref == null || !ref.isValid())
			return;
		log.trace("download and import resources for contact {}", ref);
		try {
			DataStoreSync sync = new DataStoreSync(webStore, localStore);
			sync.run(ref);
		} catch (Exception e) {
			log.error("failed to download and import contact " + ref, e);
		}
	}

	private void syncSource(Ref ref) {
		if (ref == null || !ref.isValid())
			return;
		log.trace("download and import resources for source {}", ref);
		try {
			DataStoreSync sync = new DataStoreSync(webStore, localStore);
			sync.run(ref);
		} catch (Exception e) {
			log.error("failed to download and import source " + ref, e);
		}
	}

}
