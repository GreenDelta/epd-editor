package epd.conversion;

import java.util.ArrayList;
import java.util.List;
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
import static org.junit.Assert.assertTrue;

public class RefExtensionTest {

	@Test
	public void testPublisherRefs() {
		var epd = new EpdDataSet();
		var id = UUID.randomUUID().toString();
		Processes.dataSetInfo(epd.process).uuid = id;
		epd.publishers.addAll(makeRefs(DataSetType.CONTACT));
		Extensions.write(epd);
		Tests.withStore(store -> {
			store.put(epd.process);
			var copy = Extensions.read(
				store.get(Process.class, id),
				EpdProfiles.getDefault());
			checkRefs(copy.publishers, DataSetType.CONTACT);
		});
	}

	@Test
	public void testOriginalEPDRefs() {
		var epd = new EpdDataSet();
		var id = UUID.randomUUID().toString();
		Processes.dataSetInfo(epd.process).uuid = id;
		epd.originalEPDs.addAll(makeRefs(DataSetType.SOURCE));
		Extensions.write(epd);
		Tests.withStore(store -> {
			store.put(epd.process);
			var copy = Extensions.read(
				store.get(Process.class, id),
				EpdProfiles.getDefault());
			checkRefs(copy.originalEPDs, DataSetType.SOURCE);
		});
	}

	private void checkRefs(List<Ref> refs, DataSetType type) {
		assertEquals(10, refs.size());
		for (var r : refs) {
			assertTrue(r.name.get(0).value.startsWith("test ref"));
			assertEquals(type, r.type);
		}
	}

	private ArrayList<Ref> makeRefs(DataSetType type) {
		var refs = new ArrayList<Ref>();
		for (int i = 0; i < 10; i++) {
			var ref = new Ref();
			ref.name.add(LangString.of("test ref " + i, "en"));
			ref.uuid = UUID.randomUUID().toString();
			ref.type = type;
			ref.version = "01.00.000";
			ref.uri = "../" + ref.uuid + ".xml";
			refs.add(ref);
		}
		return refs;
	}
}
