package epd.conversion;

import java.util.UUID;

import app.store.EpdProfiles;
import epd.io.conversion.Extensions;
import epd.model.EpdDataSet;
import org.junit.Test;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.util.Processes;

import static org.junit.Assert.assertEquals;

public class RefExtensionTest {

	@Test
	public void testPublisherRefs() {
		var epd = new EpdDataSet();
		var id = UUID.randomUUID().toString();
		Processes.dataSetInfo(epd.process).uuid = id;

		var ref = new Ref();
		ref.name.add(LangString.of("test ref", "en"));
		ref.uuid = UUID.randomUUID().toString();
		ref.type = DataSetType.CONTACT;
		ref.version = "01.00.000";
		ref.uri = "../" + ref.uuid + ".xml";
		epd.publishers.add(ref);
		Extensions.write(epd);


		Tests.withStore(store -> {
			store.put(epd.process);
			var copy = Extensions.read(
				store.get(Process.class, id),
				EpdProfiles.getDefault());

			assertEquals(1, copy.publishers.size());
			var copyRef = copy.publishers.get(0);
			assertEquals(ref.uuid, copyRef.uuid);
			assertEquals(ref.name.get(0).value, copyRef.name.get(0).value);
		});
	}
}
