package epd.io.server;

import java.util.Objects;
import java.util.Set;

import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.FlowPropertyRef;
import org.openlca.ilcd.io.FileStore;
import org.openlca.ilcd.io.NetworkClient;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.sources.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import epd.io.Configs;
import epd.io.EpdStore;
import epd.io.MappingConfig;
import epd.io.conversion.Converter;
import epd.model.DeclaredProduct;
import epd.model.EpdDataSet;
import epd.model.Ref;

class Upload {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final NetworkClient webStore;
	private final FileStore fileStore;
	private final EpdStore store;

	public Upload(NetworkClient client, EpdStore store) {
		this.webStore = client;
		this.store = store;
		fileStore = store.ilcdStore;
	}

	public void doIt(EpdDataSet dataSet) {
		uploadProducts(dataSet);
		uploadEpd(dataSet);
		uploadActorsAndSources(dataSet);
	}

	private void uploadProducts(EpdDataSet dataSet) {
		try {
			DeclaredProduct product = dataSet.declaredProduct;
			if (product != null) {
				uploadFlow(product.flow);
				uploadFlow(product.genericFlow);
			}
		} catch (Exception e) {
			log.error("failed to upload data set", e);
		}
	}

	private void uploadEpd(EpdDataSet dataSet) {
		log.trace("upload EPD: {}", dataSet);
		try {
			MappingConfig config = Configs.getMappingConfig(store);
			Process process = Converter.convert(dataSet, config);
			webStore.put(process, process.getUUID());
		} catch (Exception e) {
			log.error("failed to upload EPD data set", e);
		}
	}

	private void uploadFlow(Ref flowDescriptor) {
		if (flowDescriptor == null)
			return;
		String id = flowDescriptor.uuid;
		try {
			log.trace("upload flow {}", id);
			if (fileStore.contains(Flow.class, id)) {
				Flow flow = fileStore.get(Flow.class, id);
				webStore.put(flow, id); // considers updated versions
				syncFlow(flow);
			}
		} catch (Exception e) {
			log.error("failed to upload flow " + id, e);
		}
	}

	private void syncFlow(Flow flow) {
		if (flow == null || flow.flowProperties == null)
			return;
		DataStoreSync sync = new DataStoreSync(fileStore, webStore);
		for (FlowPropertyRef ref : flow.flowProperties.flowProperty) {
			DataSetReference propRef = ref.flowProperty;
			if (propRef == null)
				continue;
			sync.run(Ref.of(propRef));
		}
	}

	private void uploadActorsAndSources(EpdDataSet dataSet) {
		if (dataSet == null)
			return;
		Set<Ref> refs = new RefTraversal().traverse(dataSet);
		for (Ref ref : refs) {
			if (Objects.equals(ref.type, Contact.class))
				uploadActor(ref);
			else if (Objects.equals(ref.type, Source.class))
				uploadSource(ref);
		}
	}

	private void uploadActor(Ref ref) {
		if (ref == null || !ref.isValid())
			return;
		try {
			if (webStore.contains(Contact.class, ref.uuid))
				return;
			log.trace("upload actor {}", ref);
			if (fileStore.contains(Contact.class, ref.uuid)) {
				DataStoreSync sync = new DataStoreSync(fileStore, webStore);
				sync.run(ref);
			}
		} catch (Exception e) {
			log.error("failed to upload actor " + ref, e);
		}
	}

	private void uploadSource(Ref ref) {
		if (ref == null || !ref.isValid())
			return;
		try {
			if (webStore.contains(Source.class, ref.uuid))
				return;
			log.trace("upload source {}", ref);
			if (fileStore.contains(Source.class, ref.uuid)) {
				DataStoreSync sync = new DataStoreSync(fileStore, webStore);
				sync.run(ref);
			}
		} catch (Exception e) {
			log.error("failed to upload source " + ref, e);
		}
	}
}
