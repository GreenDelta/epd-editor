package epd.conversion;

import static org.junit.Assert.assertEquals;

import java.nio.file.Files;
import java.util.UUID;

import javax.xml.namespace.QName;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.openlca.ilcd.io.FileStore;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.util.Processes;

import epd.io.conversion.Extensions;
import epd.io.conversion.Vocab;
import epd.model.EpdDataSet;

public class FormatVersionTest {

	@Test
	public void testFormatVersion() throws Exception {
		var epd = new EpdDataSet();
		var info = Processes.dataSetInfo(epd.process);
		info.uuid = UUID.randomUUID().toString();
		Extensions.write(epd);

		var dir = Files.createTempDirectory("_epd_test").toFile();
		try (var store = new FileStore(dir)) {
			store.put(epd.process);
			var process = store.get(Process.class, info.uuid);
			var qName = "{" + Vocab.NS_EPDv2 + "}epd-version";
			var version = process.otherAttributes.get(QName.valueOf(qName));
			assertEquals("1.2", version);
		}
		FileUtils.deleteDirectory(dir);
	}

}
