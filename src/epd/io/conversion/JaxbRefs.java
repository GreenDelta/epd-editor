package epd.io.conversion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.Ref;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import epd.util.Strings;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Contains some utility methods for reading and writing Ref instances from and
 * to DOM elements.
 */
final class JaxbRefs {

	private JaxbRefs() {
	}

	static void copyFields(Ref from, Ref to) {
		if (from == null || to == null || from == to)
			return;
		to.withType(from.getType())
			.withUUID(from.getUUID())
			.withUri(from.getUri())
			.withVersion(from.getVersion());
		to.withName().clear();
		to.withName().addAll(from.getName());
	}

	static <T extends Ref> void write(Class<T> type, List<T> refs, Other ext) {
		if (refs.isEmpty() || ext == null)
			return;
		ext.withAny().clear();
		try {
			var context = JAXBContext.newInstance(type);
			var marshaller = context.createMarshaller();
			for (var ref : refs) {
				var doc = Dom.createDocument();
				if (doc == null)
					continue;
				marshaller.marshal(ref, doc);
				var elem = doc.getDocumentElement();
				ext.withAny().add(elem);
			}
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(JaxbRefs.class);
			log.error("failed to marshal Ref elements of " + type, e);
		}
	}

	static <T extends Ref> List<Ref> read(Class<T> type, Other ext) {
		if (ext == null || type == null || ext.getAny().isEmpty())
			return Collections.emptyList();
		var rootDef = type.getAnnotation(XmlRootElement.class);
		if (rootDef == null)
			return Collections.emptyList();

		try {
			var list = new ArrayList<Ref>();
			Unmarshaller unmarshaller = null;
			for (var obj : ext.getAny()) {
				if (!(obj instanceof Element elem))
					continue;
				if (!Strings.nullOrEqual(elem.getLocalName(), rootDef.name()))
					continue;
				if (unmarshaller == null) {
					var context = JAXBContext.newInstance(type);
					unmarshaller = context.createUnmarshaller();
				}
				var instance = unmarshaller.unmarshal(elem);
				if (type.isInstance(instance)) {
					list.add(unwrap(type.cast(instance)));
				}
			}
			return list;
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(Dom.class);
			log.error("failed to unmarshal Ref type of " + type, e);
			return Collections.emptyList();
		}
	}

	private static Ref unwrap(Ref ref) {
		if (ref == null)
			return null;
		if (ref.getClass().equals(Ref.class))
			return ref;
		var raw = new Ref()
			.withType(ref.getType())
			.withUri(ref.getUri())
			.withUUID(ref.getUUID())
			.withVersion(ref.getVersion());
		raw.withName().addAll(ref.getName());
		return raw;
	}

}
