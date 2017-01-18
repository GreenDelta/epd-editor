package epd.io.server;

import java.io.Closeable;
import java.io.IOException;

import org.openlca.ilcd.io.SodaClient;
import org.openlca.ilcd.io.SodaConnection;
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
