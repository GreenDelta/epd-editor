package epd.io.conversion;

import java.util.Objects;

import org.openlca.ilcd.commons.Other;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import epd.model.EpdDataSet;
import epd.model.SafetyMargins;

class SafetyMarginsConverter {

	static SafetyMargins read(Other other) {
		if (other == null)
			return null;
		for (Object any : other.any) {
			if (!(any instanceof Element))
				continue;
			Element element = (Element) any;
			if (!isValid(element))
				continue;
			return fromElement(element);
		}
		return null;
	}

	private static boolean isValid(Element element) {
		if (element == null)
			return false;
		String nsUri = element.getNamespaceURI();
		return Objects.equals(nsUri, ProcessExtensions.NAMESPACE)
				&& Objects.equals(element.getLocalName(), "safetyMargins");
	}

	private static SafetyMargins fromElement(Element element) {
		SafetyMargins margins = new SafetyMargins();
		NodeList nodeList = element.getElementsByTagNameNS(
				ProcessExtensions.NAMESPACE, "margins");
		Double val = Util.getDoubleContent(nodeList);
		margins.margins = val;
		nodeList = element.getElementsByTagNameNS(ProcessExtensions.NAMESPACE,
				"description");
		margins.description = Util.getTextContent(nodeList);
		return margins;
	}

	static void write(EpdDataSet ds, Other other, Document doc) {
		if (Util.hasNull(ds, other, doc))
			return;
		Util.clear(other, "safetyMargins");
		SafetyMargins m = ds.safetyMargins;
		if (m == null || (m.margins == null && m.description == null))
			return;
		Element element = toElement(m, doc);
		other.any.add(element);
	}

	private static Element toElement(SafetyMargins margins, Document doc) {
		try {
			String nsUri = ProcessExtensions.NAMESPACE;
			Element root = doc.createElementNS(nsUri, "epd:safetyMargins");
			if (margins.margins != null) {
				Element e = doc.createElementNS(nsUri, "epd:margins");
				root.appendChild(e);
				e.setTextContent(margins.margins.toString());
			}
			if (margins.description != null) {
				Element e = doc.createElementNS(nsUri, "description");
				e.setTextContent(margins.description);
				root.appendChild(e);
			}
			return root;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(SafetyMarginsConverter.class);
			log.error("failed to convert safety margins to DOM element", e);
			return null;
		}
	}

}
