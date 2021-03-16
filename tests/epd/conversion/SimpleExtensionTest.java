package epd.conversion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.function.Consumer;

import javax.xml.namespace.QName;

import app.store.EpdProfiles;
import epd.model.EpdProfile;
import epd.model.SubType;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.io.FileStore;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.util.Processes;

import epd.io.conversion.Extensions;
import epd.io.conversion.Vocab;
import epd.model.EpdDataSet;

public class SimpleExtensionTest {

	@Test
	public void testFormatVersion() {
		var epd = new EpdDataSet();
		var info = Processes.dataSetInfo(epd.process);
		info.uuid = UUID.randomUUID().toString();
		Extensions.write(epd);
		withStore(store -> {
			store.put(epd.process);
			var process = store.get(Process.class, info.uuid);
			var qName = "{" + Vocab.NS_EPDv2 + "}epd-version";
			var version = process.otherAttributes.get(QName.valueOf(qName));
			assertEquals("1.2", version);
		});
	}

	@Test
	public void testLocalDate() {
		// test reading and writing XSD date instances using the new Java time API
		// examples from https://www.w3schools.com/xml/schema_dtypes_date.asp
		var format = DateTimeFormatter.ISO_DATE;
		var dates = new String[] {
			"2002-09-24",
			"2002-09-24Z",
			"2002-09-24-06:00",
			"2002-09-24+06:00"
		};
		for (var s : dates) {
			var date = LocalDate.parse(s, format);
			assertEquals(24, date.getDayOfMonth());
			assertEquals(9, date.getMonthValue());
			assertEquals(2002, date.getYear());
			assertEquals("2002-09-24", date.toString());
		}
	}

	@Test
	public void testPublicationDate() {
		var profile = EpdProfiles.getDefault();

		var epd = new EpdDataSet();
		var id = UUID.randomUUID().toString();
		Processes.dataSetInfo(epd.process).uuid = id;
		epd.publicationDate = LocalDate.now();
		Extensions.write(epd);
		withStore(store -> {

			// insert and read
			store.put(epd.process);
			var copy = Extensions.read(store.get(Process.class, id), profile);
			assertEquals(epd.publicationDate, copy.publicationDate);

			// update
			var next = copy.publicationDate.plusDays(1);
			copy.publicationDate = next;
			Extensions.write(copy);
			store.put(copy.process);
			copy = Extensions.read(store.get(Process.class, id), profile);
			assertEquals(next, copy.publicationDate);

			// delete
			copy.publicationDate = null;
			Extensions.write(copy);
			store.put(copy.process);
			copy = Extensions.read(store.get(Process.class, id), profile);
			assertNull(copy.publicationDate);
		});
	}

	@Test
	public void testSubType() {
		var profile = EpdProfiles.getDefault();

		var epd = new EpdDataSet();
		var id = UUID.randomUUID().toString();
		Processes.dataSetInfo(epd.process).uuid = id;
		epd.subType = SubType.REPRESENTATIVE;
		Extensions.write(epd);

		withStore(store -> {
			store.put(epd.process);
			var copy = Extensions.read(store.get(Process.class, id), profile);
			assertEquals(SubType.REPRESENTATIVE, copy.subType);
		});
	}

	private void withStore(Consumer<DataStore> fn) {
		try {
			var dir = Files.createTempDirectory("_epd_test").toFile();
			try (var store = new FileStore(dir)) {
				fn.accept(store);
			}
			FileUtils.deleteDirectory(dir);
			// System.out.println(dir.getAbsolutePath());
		} catch (Exception e) {
			throw new RuntimeException("failed to test with file store", e);
		}
	}

}
