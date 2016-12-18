package epd.io.server;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.ilcd.descriptors.DescriptorList;
import org.openlca.ilcd.descriptors.ProcessDescriptor;
import org.openlca.ilcd.io.SodaClient;
import org.openlca.ilcd.io.SodaConnection;
import org.openlca.ilcd.processes.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import epd.io.EpdStore;
import epd.model.EpdDataSet;
import epd.model.EpdDescriptor;

public class Connection implements Closeable {

	private Logger log = LoggerFactory.getLogger(getClass());
	private final SodaClient client;

	private Connection(SodaConnection con) throws Exception {
		log.trace("create new network adapter {}", con);
		client = new SodaClient(con);
		client.connect();
	}

	public static Connection create(SodaConnection con)
			throws Exception {
		return new Connection(con);
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
