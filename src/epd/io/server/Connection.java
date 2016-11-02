package epd.io.server;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.ilcd.descriptors.DataStock;
import org.openlca.ilcd.descriptors.DescriptorList;
import org.openlca.ilcd.descriptors.ProcessDescriptor;
import org.openlca.ilcd.io.NetworkClient;
import org.openlca.ilcd.processes.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import epd.io.EpdStore;
import epd.io.ServerCredentials;
import epd.model.EpdDataSet;
import epd.model.EpdDescriptor;

public class Connection implements Closeable {

	private Logger log = LoggerFactory.getLogger(getClass());
	private final NetworkClient client;
	public final ServerCredentials credentials;

	private Connection(ServerCredentials credentials) throws Exception {
		log.trace("create new network adapter {}", credentials);
		client = new NetworkClient(credentials.url, credentials.user,
				credentials.password);
		if (credentials.dataStockUuid != null) {
			DataStock dataStock = new DataStock();
			dataStock.uuid = credentials.dataStockUuid;
			dataStock.shortName = credentials.dataStockName;
			client.setDataStock(dataStock);
		}
		client.connect();
		this.credentials = credentials;
	}

	public static Connection create(ServerCredentials credentials)
			throws Exception {
		return new Connection(credentials);
	}

	public List<EpdDescriptor> search(String term) {
		try {
			String t = term == null ? "" : term.trim();
			DescriptorList results = client.search(Process.class, t);
			if (results == null)
				return Collections.emptyList();
			List<EpdDescriptor> descriptors = new ArrayList<>();
			for (Object o : results.descriptors) {
				if (!(o instanceof ProcessDescriptor))
					continue;
				ProcessDescriptor d = (ProcessDescriptor) o;
				EpdDescriptor descriptor = new EpdDescriptor();
				if (d.name != null)
					descriptor.name = d.name.value;
				descriptor.refId = d.uuid;
				descriptors.add(descriptor);
			}
			return descriptors;
		} catch (Exception e) {
			log.error("Online search failed", e);
			return Collections.emptyList();
		}
	}

	public void upload(EpdDataSet dataSet, EpdStore store) {
		new Upload(client, store).doIt(dataSet);
	}

	public void download(EpdDescriptor epd, EpdStore store) {
		new Download(client, store).doIt(epd);
	}

	@Override
	public void close() throws IOException {
		client.close();
	}

}
