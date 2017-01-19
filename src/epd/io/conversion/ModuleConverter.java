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
import epd.model.Module;
import epd.model.ModuleEntry;
import epd.util.Strings;

class ModuleConverter {

	static List<ModuleEntry> readModules(Other other) {
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
		return Objects.equals(nsUri, ProcessExtensions.NAMESPACE_OLCA)
				&& Objects.equals(element.getLocalName(), "modules");
	}

	private static List<ModuleEntry> fromElement(Element element) {
		List<ModuleEntry> modules = new ArrayList<>();
		NodeList moduleList = element.getElementsByTagNameNS(
				ProcessExtensions.NAMESPACE_OLCA, "module");
		for (int i = 0; i < moduleList.getLength(); i++) {
			Node node = moduleList.item(i);
			NamedNodeMap attributes = node.getAttributes();
			ModuleEntry module = new ModuleEntry();
			modules.add(module);
			module.description = node.getTextContent();
			for (int m = 0; m < attributes.getLength(); m++) {
				String attribute = attributes.item(m).getLocalName();
				String value = attributes.item(m).getNodeValue();
				setAttributeValue(module, attribute, value);
			}
		}
		return modules;
	}

	private static void setAttributeValue(ModuleEntry module, String attribute,
			String value) {
		switch (attribute) {
		case "name":
			module.module = Module.fromLabel(value);
			break;
		case "productsystem-id":
			module.productSystemId = value;
			break;
		case "scenario":
			module.scenario = value;
			break;
		}
	}

	static void writeModules(EpdDataSet dataSet, Other other, Document doc) {
		if (other == null || doc == null || !shouldWriteEntries(dataSet))
			return;
		Element root = doc.createElementNS(ProcessExtensions.NAMESPACE_OLCA,
				"olca:modules");
		for (ModuleEntry module : dataSet.moduleEntries) {
			Element element = toElement(module, doc);
			if (element != null)
				root.appendChild(element);
		}
		other.any.add(root);
	}

	private static boolean shouldWriteEntries(EpdDataSet dataSet) {
		if (dataSet == null)
			return false;
		for (ModuleEntry entry : dataSet.moduleEntries) {
			if (Strings.notEmpty(entry.productSystemId)
					|| Strings.notEmpty(entry.description))
				return true;
		}
		return false;
	}

	private static Element toElement(ModuleEntry module, Document document) {
		if (document == null)
			return null;
		try {
			String nsUri = ProcessExtensions.NAMESPACE_OLCA;
			Element element = document.createElementNS(nsUri, "olca:module");
			if (module.module != null)
				element.setAttribute("olca:name", module.module.getLabel());
			if (module.productSystemId != null)
				element.setAttribute("olca:productsystem-id",
						module.productSystemId);
			if (module.scenario != null)
				element.setAttribute("olca:scenario", module.scenario);
			if (module.description != null)
				element.setTextContent(module.description);
			return element;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(ModuleConverter.class);
			log.error("failed to convert module to DOM element", e);
			return null;
		}
	}

}
