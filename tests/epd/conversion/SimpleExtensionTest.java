package epd.conversion;

import epd.model.Xml;
import org.junit.Test;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.epd.EpdSubType;
import org.openlca.ilcd.util.Epds;
import org.openlca.ilcd.util.Processes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.junit.Assert.*;

public class SimpleExtensionTest {

	@Test
	public void testFormatVersion() {
		var epd = new Process();
		var id = UUID.randomUUID().toString();
		Processes.withUUID(epd, id);
		Tests.withStore(store -> {
			store.put(epd);
			var process = store.get(Process.class, id);
			assertEquals("1.2", process.getEpdVersion());
		});
	}

	@Test
	public void testLocalDate() {
		// test reading and writing XSD date instances using the new Java time
		// API
		// examples from https://www.w3schools.com/xml/schema_dtypes_date.asp
		var format = DateTimeFormatter.ISO_DATE;
		var dates = new String[]{
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
		var epd = new Process();
		var id = UUID.randomUUID().toString();
		Epds.withUUID(epd, id);
		Epds.withPublicationDate(epd, Xml.now());
		Tests.withStore(store -> {

			// insert and read
			store.put(epd);
			var copy = store.get(Process.class, id);
			var pubDate = Epds.getPublicationDate(copy);
			assertEquals(Epds.getPublicationDate(epd), pubDate);

			// update
			pubDate.setDay(pubDate.getDay() + 1);
			Epds.withPublicationDate(copy, pubDate);
			store.put(copy);
			copy = store.get(Process.class, id);
			assertEquals(pubDate, Epds.getPublicationDate(copy));

			// delete
			Epds.withPublicationDate(copy, null);
			store.put(copy);
			copy = store.get(Process.class, id);
			assertNull(Epds.getPublicationDate(copy));
		});
	}

	@Test
	public void testSubType() {
		var epd = new Process();
		var id = UUID.randomUUID().toString();
		Processes.withUUID(epd, id);
		Epds.withSubType(epd, EpdSubType.REPRESENTATIVE_DATASET);
		Tests.withStore(store -> {
			store.put(epd);
			var copy = store.get(Process.class, id);
			assertEquals(EpdSubType.REPRESENTATIVE_DATASET, Epds.getSubType(copy));
		});
	}

}
