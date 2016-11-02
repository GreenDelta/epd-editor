package epd.io.conversion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.openlca.ilcd.commons.Other;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import epd.model.EpdDataSet;
import epd.model.Scenario;

class ScenarioConverter {

	static List<Scenario> readScenarios(Other other) {
		if (other == null)
			return Collections.emptyList();
		for (Object any : other.any) {
			if (!(any instanceof Element))
				continue;
			Element element = (Element) any;
			if (!isValid(element))
				continue;
			return fromElement(element);
		}
		return Collections.emptyList();
	}

	private static boolean isValid(Element element) {
		if (element == null)
			return false;
		String nsUri = element.getNamespaceURI();
		if (!Objects.equals(nsUri, Converter.NAMESPACE))
			return false;
		if (!Objects.equals(element.getLocalName(), "scenarios"))
			return false;
		else
			return true;
	}

	private static List<Scenario> fromElement(Element element) {
		List<Scenario> scenarios = new ArrayList<>();
		NodeList list = element.getElementsByTagNameNS(Converter.NAMESPACE,
				"scenario");
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			NamedNodeMap attributes = node.getAttributes();
			Scenario scenario = new Scenario();
			for (int m = 0; m < attributes.getLength(); m++) {
				String attName = attributes.item(m).getLocalName();
				String attVal = attributes.item(m).getNodeValue();
				setField(scenario, attName, attVal);
			}
			scenario.description = node.getTextContent();
			scenarios.add(scenario);
		}
		return scenarios;
	}

	private static void setField(Scenario scenario, String attName,
			String attVal) {
		switch (attName) {
		case "default":
			try {
				scenario.defaultScenario = Boolean.parseBoolean(attVal);
			} catch (Exception e) {
			}
			break;
		case "group":
			scenario.group = attVal;
			break;
		case "name":
			scenario.name = attVal;
			break;
		}
	}

	static void writeScenarios(EpdDataSet dataSet, Other other, Document doc) {
		if (Util.hasNull(dataSet, other, doc)
				|| dataSet.scenarios.isEmpty())
			return;
		Util.clear(other, "scenarios");
		Element root = doc.createElementNS(Converter.NAMESPACE, "epd:scenarios");
		for (Scenario scenario : dataSet.scenarios) {
			Element element = toElement(scenario, doc);
			if (element != null)
				root.appendChild(element);
		}
		other.any.add(root);
	}

	private static Element toElement(Scenario scenario, Document doc) {
		try {
			String nsUri = Converter.NAMESPACE;
			Element element = doc.createElementNS(nsUri, "epd:scenario");
			if (scenario.name != null)
				element.setAttribute("epd:name", scenario.name);
			if (scenario.defaultScenario)
				element.setAttribute("epd:default", "true");
			if (scenario.group != null)
				element.setAttribute("epd:group", scenario.group);
			if (scenario.description != null) {
				Element description = doc.createElementNS(nsUri, "description");
				description.setTextContent(scenario.description);
				element.appendChild(description);
			}
			return element;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(ScenarioConverter.class);
			log.error("failed to convert safety margins to DOM element", e);
			return null;
		}
	}

}
