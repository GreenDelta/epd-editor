package epd.io.conversion;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlRootElement;

import epd.model.EpdDataSet;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.util.Processes;
import org.slf4j.LoggerFactory;

@XmlRootElement(
	name = "referenceToPublisher",
	namespace = Vocab.NS_EPDv2)
final class PublisherRef extends Ref {

	static PublisherRef of(Ref other) {
		var pubRef = new PublisherRef();
		pubRef.type = DataSetType.CONTACT;
		pubRef.uuid = other.uuid;
		pubRef.name.addAll(other.name);
		pubRef.uri = other.uri;
		pubRef.version = other.version;
		return pubRef;
	}

	private static Ref unwrap(Ref ref) {
		if (ref == null)
			return null;
		if (ref.getClass().equals(Ref.class))
			return ref;
		var raw = new Ref();
		raw.name.addAll(ref.name);
		raw.type = ref.type;
		raw.uri = ref.uri;
		raw.uuid = ref.uuid;
		raw.version = ref.version;
		return raw;
	}

	/**
	 * Write the publisher references to the underlying
	 * process data set of the given EPD.
	 */
	static void write(EpdDataSet epd) {
		if (epd == null)
			return;

		// remove possible DOM elements
		if (epd.publishers.isEmpty()) {
			var r = Processes.getRepresentativeness(epd.process);
			if (r == null || r.other == null)
				return;
			// currently nothing else is written to this extension
			// point; so we can just drop it
			r.other.any.clear();
			r.other = null;
			return;
		}

		var r = Processes.representativeness(epd.process);
		if (r.other == null) {
			r.other = new Other();
		}
		r.other.any.clear();
		try {
			var context = JAXBContext.newInstance(PublisherRef.class);
			var marshaller = context.createMarshaller();
			for (var ref : epd.publishers) {
				var doc = Dom.createDocument();
				if (doc == null)
					continue;
				marshaller.marshal(PublisherRef.of(ref), doc);
				var elem = doc.getDocumentElement();
				r.other.any.add(elem);
			}
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(PublisherRef.class);
			log.error("failed to marshal publisher elements", e);
		}
	}

	/**
	 * Write the publisher references from the underlying
	 * process data set to the given EPD.
	 */
	static void read(EpdDataSet epd) {
		if (epd == null)
			return;
		var r = Processes.getRepresentativeness(epd.process);
		if (r == null || r.other == null)
			return;
		var refs = Dom.jaxbRefsOf(PublisherRef.class, r.other);
		if (refs.isEmpty())
			return;
		refs.stream()
			.map(PublisherRef::unwrap)
			.forEach(epd.publishers::add);
	}

}
