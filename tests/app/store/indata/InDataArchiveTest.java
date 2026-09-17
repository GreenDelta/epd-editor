package app.store.indata;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.ilcd.commons.DataSetType;

public class InDataArchiveTest {

	private static final String CONTACT_UUID =
		"c50ec8b7-691d-458b-8379-a8d787e4e4b4";
	private static final String FLOW_UUID =
		"3a1f0f2e-7c5b-4e0e-9f7a-2b1c8d5e6f70";

	private static final String CONTACT_XML = """
		<?xml version="1.0" encoding="UTF-8"?>
		<contactDataSet xmlns="http://lca.jrc.it/ILCD/Contact">
		  <dataSetInformation>
		    <UUID>%s</UUID>
		    <dataSetVersion>01.02.000</dataSetVersion>
		  </dataSetInformation>
		</contactDataSet>
		""";

	private static final String FLOW_XML = """
		<?xml version="1.0" encoding="UTF-8"?>
		<flowDataSet xmlns="http://lca.jrc.it/ILCD/Flow">
		  <dataSetInformation>
		    <UUID>%s</UUID>
		  </dataSetInformation>
		</flowDataSet>
		""";

	private static final String OTHER_XML = """
		<?xml version="1.0" encoding="UTF-8"?>
		<categorySystem xmlns="http://lca.jrc.it/ILCD/Categories">
		  <dataSetInformation>
		    <UUID>d8f0b1f8-1c1e-4a5d-8a2b-2c3d4e5f6a7b</UUID>
		  </dataSetInformation>
		</categorySystem>
		""";

	@Test
	public void testDataSets() throws Exception {
		var zip = createZip();
		try (var archive = InDataArchive.open(zip)) {
			var entries = archive.dataSets();
			Assert.assertEquals(2, entries.size());

			var contact = find(entries, DataSetType.CONTACT);
			Assert.assertNotNull(contact);
			Assert.assertEquals(CONTACT_UUID, contact.ref().getUUID());
			Assert.assertEquals("01.02.000", contact.ref().getVersion());

			var flow = find(entries, DataSetType.FLOW);
			Assert.assertNotNull(flow);
			Assert.assertEquals(FLOW_UUID, flow.ref().getUUID());

			var data = new String(
				archive.read(contact), StandardCharsets.UTF_8);
			Assert.assertTrue(data.contains(CONTACT_UUID));
		}
	}

	private InDataArchive.Entry find(
			List<InDataArchive.Entry> entries, DataSetType type) {
		for (var entry : entries) {
			if (entry.ref().getType() == type)
				return entry;
		}
		return null;
	}

	private File createZip() throws Exception {
		var file = File.createTempFile("indata-archive-test-", ".zip");
		file.deleteOnExit();
		try (var out = new ZipOutputStream(new FileOutputStream(file))) {
			write(out, "ILCD-EPD-Master-Data-main/contacts/contact.xml",
				CONTACT_XML.formatted(CONTACT_UUID));
			write(out, "ILCD-EPD-Master-Data-main/flows/flow.xml",
				FLOW_XML.formatted(FLOW_UUID));
			write(out, "ILCD-EPD-Master-Data-main/categories.xml", OTHER_XML);
			write(out, "ILCD-EPD-Master-Data-main/README.md", "# master data");
		}
		return file;
	}

	private void write(ZipOutputStream out, String name, String content)
		throws Exception {
		out.putNextEntry(new ZipEntry(name));
		out.write(content.getBytes(StandardCharsets.UTF_8));
		out.closeEntry();
	}
}
