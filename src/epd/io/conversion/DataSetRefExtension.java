package epd.io.conversion;

import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.Other;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import epd.model.Ref;

/**
 * Creates a data set reference element in an extension point (see the generic
 * product or vendor information in the extended product data sets).
 */
class DataSetRefExtension {

	private String type;
	private String path;
	private String tagName;

	public static Ref readFlow(String tagName, Other extension) {
		DataSetRefExtension ext = new DataSetRefExtension(tagName,
				DataSetType.FLOW);
		return ext.read(DataSetType.FLOW, extension);
	}

	public static Ref readActor(String tagName, Other extension) {
		DataSetRefExtension ext = new DataSetRefExtension(tagName,
				DataSetType.CONTACT);
		return ext.read(DataSetType.CONTACT, extension);
	}

	public static Ref readSource(String tagName, Other extension) {
		DataSetRefExtension ext = new DataSetRefExtension(tagName,
				DataSetType.SOURCE);
		return ext.read(DataSetType.SOURCE, extension);
	}

	public static void write(Ref descriptor, String tagName, Other extension) {
		if (tagName == null || extension == null)
			return;
		Element old = Util.getElement(extension, tagName);
		if (old != null)
			extension.any.remove(old);
		if (descriptor == null)
			return;
		DataSetRefExtension ext = new DataSetRefExtension(tagName,
				descriptor.type);
		ext.write(descriptor, extension);
	}

	private DataSetRefExtension(String tagName, DataSetType modelType) {
		this.tagName = tagName;
		initTypeAndPath(modelType);
	}

	private void initTypeAndPath(DataSetType modelType) {
		if (modelType == null) {
			type = "other external file";
			path = "unknown";
		}
		switch (modelType) {
		case CONTACT:
			type = "contact data set";
			path = "contacts";
			break;
		case SOURCE:
			type = "source data set";
			path = "sources";
			break;
		case FLOW:
			type = "flow data set";
			path = "flows";
			break;
		case LCIA_METHOD:
			type = "LCIA method data set";
			path = "lciamethods";
			break;
		case PROCESS:
			type = "process data set";
			path = "processes";
			break;
		case FLOW_PROPERTY:
			type = "flow property data set";
			path = "flowproperties";
			break;
		case UNIT_GROUP:
			type = "unit group data set";
			path = "unitgroups";
			break;
		default:
			type = "other external file";
			path = "unknown";
			break;
		}
	}

	private Ref read(DataSetType type, Other other) {
		Element element = Util.getElement(other, tagName);
		if (element == null)
			return null;
		try {
			Ref ref = new Ref();
			ref.type = type;
			ref.uuid = element.getAttribute("refObjectId");
			Element descriptionElement = Util.getChild(element,
					"shortDescription");
			if (descriptionElement != null)
				ref.name = descriptionElement.getTextContent();
			return ref;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to create descriptor instance for " + type, e);
			return null;
		}
	}

	private void write(Ref d, Other other) {
		if (other == null)
			return;
		Element element = Util.getElement(other, tagName);
		if (element != null)
			other.any.remove(element);
		element = createElement(d);
		if (element == null)
			return;
		other.any.add(element);
	}

	private Element createElement(Ref d) {
		Document doc = Util.createDocument();
		if (doc == null || d == null)
			return null;
		Element e = doc.createElementNS(Converter.NAMESPACE, "epd:" + tagName);
		e.setAttribute("type", type);
		e.setAttribute("refObjectId", d.uuid);
		e.setAttribute("uri", "../" + path + "/" + d.uuid);
		Element descriptionElement = doc.createElementNS(
				"http://lca.jrc.it/ILCD/Common", "common:shortDescription");
		e.appendChild(descriptionElement);
		descriptionElement.setTextContent(d.name);
		return e;
	}
}
