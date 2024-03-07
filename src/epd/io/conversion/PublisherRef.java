package epd.io.conversion;

import epd.model.EpdDataSet;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.util.Processes;

import java.util.stream.Collectors;

@XmlRootElement(name = "referenceToPublisher", namespace = Vocab.NS_EPDv2)
final class PublisherRef extends Ref {

	private static PublisherRef wrap(Ref other) {
		var ref = new PublisherRef();
		JaxbRefs.copyFields(other, ref);
		ref.withType(DataSetType.CONTACT);
		return ref;
	}

	/**
	 * Write the publisher references to the underlying process data set of the
	 * given EPD.
	 */
	static void write(EpdDataSet epd) {
		if (epd == null)
			return;

		// remove possible DOM elements
		if (epd.publishers.isEmpty()) {
			var pub = Processes.getPublication(epd.process);
			if (pub != null) {
				// currently nothing else is written to this extension
				// point; so we can just drop it
				pub.withEpdExtension(null);
			}
			return;
		}

		var other = epd.process.withAdminInfo()
			.withPublication()
			.withEpdExtension();
		other.withAny().clear();
		var pubRefs = epd.publishers.stream()
			.map(PublisherRef::wrap)
			.collect(Collectors.toList());
		JaxbRefs.write(PublisherRef.class, pubRefs, other);
	}

	/**
	 * Write the publisher references from the underlying process data set to
	 * the given EPD.
	 */
	static void read(EpdDataSet epd) {
		if (epd == null)
			return;
		var pub = Processes.getPublication(epd.process);
		if (pub == null || pub.getEpdExtension() == null)
			return;
		var refs = JaxbRefs.read(PublisherRef.class, pub.getEpdExtension());
		if (refs.isEmpty())
			return;
		epd.publishers.addAll(refs);
	}
}
