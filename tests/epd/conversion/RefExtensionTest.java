package epd.conversion;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

import app.store.EpdProfiles;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import epd.io.conversion.Dom;
import epd.io.conversion.Extensions;
import epd.io.conversion.Vocab;
import epd.model.EpdDataSet;
import epd.util.Strings;
import org.junit.Test;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.util.Processes;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public class RefExtensionTest {

	@Test
	public void testPublisherRefs() throws Exception {
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

		var r = Processes.representativeness(epd.process);
		r.other = new Other();

		var doc = Dom.createDocument();
		var context = JAXBContext.newInstance(PublisherRef.class);
		var marshaller = context.createMarshaller();
		marshaller.marshal(PublisherRef.of(ref), doc);
		marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapper() {
			@Override
			public String getPreferredPrefix(String namespace, String suggestion, boolean required) {
				var vocab = Vocab.prefixOf(namespace);
				return vocab.orElse(suggestion);
			}
		});
		r.other.any.add(doc.getDocumentElement());

		Extensions.write(epd);


		Tests.withStore(store -> {
			store.put(epd.process);
			var copy = Extensions.read(
				store.get(Process.class, id),
				EpdProfiles.getDefault());

			var copyR = Processes.getRepresentativeness(copy.process);
			copy.publishers.addAll(jaxbRefsOf(PublisherRef.class, copyR.other));

			assertEquals(1, copy.publishers.size());
			var copyRef = copy.publishers.get(0);
			assertEquals(ref.uuid, copyRef.uuid);
			assertEquals(ref.name.get(0).value, copyRef.name.get(0).value);

		});

	}

	@XmlRootElement(
		name = "referenceToPublisher",
		namespace = Vocab.NS_EPDv2)
	static class PublisherRef extends Ref {

		static PublisherRef of(Ref other) {
			var pubRef = new PublisherRef();
			pubRef.type = DataSetType.CONTACT;
			pubRef.uuid = other.uuid;
			pubRef.name.addAll(other.name);
			pubRef.uri = other.uri;
			pubRef.version = other.version;
			return pubRef;
		}
	}


	static <T extends Ref> List<T> jaxbRefsOf(Class<T> type, Other ext) {
		if (ext == null || type == null || ext.any.isEmpty())
			return Collections.emptyList();
		var rootDef = type.getAnnotation(XmlRootElement.class);
		if (rootDef == null)
			return Collections.emptyList();

		try {
			var list = new ArrayList<T>();
			Unmarshaller unmarshaller = null;
			for (var obj : ext.any) {
				if (!(obj instanceof Element))
					continue;
				var elem = (Element) obj;
				if (!Strings.nullOrEqual(elem.getLocalName(), rootDef.name()))
					continue;
				if (unmarshaller == null) {
					var context = JAXBContext.newInstance(type);
					unmarshaller = context.createUnmarshaller();
				}
				var instance = unmarshaller.unmarshal(elem);
				if (type.isInstance(instance)) {
					list.add(type.cast(instance));
				}
			}
			return list;
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(Dom.class);
			log.error("failed to unmarshal Ref type of " + type, e);
			return Collections.emptyList();
		}
	}


}
