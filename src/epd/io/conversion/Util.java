package epd.io.conversion;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.openlca.ilcd.commons.Other;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utility methods for data set conversions.
 */
class Util {

	private Util() {
	}

	static boolean hasNull(Object... vals) {
		for (Object val : vals) {
			if (val == null)
				return true;
		}
		return false;
	}

	static Document createDocument() {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			return db.newDocument();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Util.class);
			log.error("failed to init DOM doc", e);
			return null;
		}
	}

	static Element createElement(String tagName) {
		Document doc = createDocument();
		if (doc == null)
			return null;
		return doc.createElementNS(
				Vocab.NS_EPD, "epd:" + tagName);
	}

	static Double getDoubleContent(NodeList nodeList) {
		String text = getTextContent(nodeList);
		if (text == null)
			return null;
		try {
			return Double.parseDouble(text);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Util.class);
			log.error("content of {} is not numeric", nodeList);
			return null;
		}
	}

	static String getTextContent(NodeList nodeList) {
		Node node = getFirstNode(nodeList);
		if (node == null)
			return null;
		return node.getTextContent();
	}

	static Node getFirstNode(NodeList nodeList) {
		if (nodeList == null || nodeList.getLength() == 0)
			return null;
		return nodeList.item(0);
	}

	static Element getElement(Other extension, String tagName) {
		if (extension == null || tagName == null)
			return null;
		for (Object any : extension.any) {
			if (!(any instanceof Element))
				continue;
			Element e = (Element) any;
			if (Objects.equals(tagName, e.getLocalName()))
				return e;
		}
		return null;
	}

	/** Removes all elements with the given tag-name from the extensions. */
	static void clear(Other extension, String tagName) {
		if (extension == null || tagName == null)
			return;
		List<Element> matches = new ArrayList<>();
		for (Object any : extension.any) {
			if (!(any instanceof Element))
				continue;
			Element e = (Element) any;
			if (Objects.equals(tagName, e.getLocalName()))
				matches.add(e);
		}
		extension.any.removeAll(matches);
	}

	/** Returns true if the given extension element is null or empty. */
	static boolean isEmpty(Other ext) {
		if (ext == null || ext.any == null)
			return true;
		if (ext.any.isEmpty())
			return true;
		for (Object o : ext.any) {
			if (o != null)
				return false;
		}
		return true;
	}

	static Element getChild(Element root, String... path) {
		if (root == null || path.length == 0)
			return null;
		Element element = root;
		for (String tagName : path) {
			if (element == null)
				return null;
			NodeList list = element.getChildNodes();
			if (list == null || list.getLength() == 0)
				return null;
			element = null;
			for (int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);
				if (!(node instanceof Element))
					continue;
				Element child = (Element) node;
				if (Objects.equals(child.getLocalName(), tagName)) {
					element = child;
					break;
				}
			}
		}
		return element;
	}

}
