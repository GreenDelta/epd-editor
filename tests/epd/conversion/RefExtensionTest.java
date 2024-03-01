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
		Processes.withUUID(epd.process, id);
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
		Processes.withUUID(epd.process, id);
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
			assertTrue(r.getName().get(0).value.startsWith("test ref"));
			assertEquals(type, r.getType());
		}
	}

	private ArrayList<Ref> makeRefs(DataSetType type) {
		var refs = new ArrayList<Ref>();
		for (int i = 0; i < 10; i++) {
			var uuid = UUID.randomUUID().toString();
			var ref = new Ref()
				.withUUID(uuid)
				.withType(type)
				.withVersion("01.00.000")
				.withUri("../" + uuid + ".xml");
			ref.withName().add(LangString.of("test ref " + i, "en"));
			refs.add(ref);
		}
		return refs;
	}
}
