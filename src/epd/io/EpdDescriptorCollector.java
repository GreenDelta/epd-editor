package epd.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import epd.model.EpdDescriptor;

class EpdDescriptorCollector extends DefaultHandler {

	private List<EpdDescriptor> descriptors = new ArrayList<>();

	private boolean isEpdDataSet;
	private boolean inName;
	private boolean inUUID;
	private boolean inVersion;

	private String lang;
	private String currentName;
	private String currentId;
	private String currentVersion;

	public List<EpdDescriptor> getDescriptors() {
		return descriptors;
	}

	@Override
	public void startDocument() throws SAXException {
		isEpdDataSet = false;
		inName = false;
		inUUID = false;
		inVersion = false;
		currentName = null;
		currentId = null;
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (matchElement("processInformation", localName, qName))
			isEpdDataSet = true; // TODO: we need to check the data set type
		if (matchElement("UUID", localName, qName)) {
			inUUID = true;
		} else if (matchElement("dataSetVersion", localName, qName)) {
			inVersion = true;
		} else if (matchElement("baseName", localName, qName)) {
			lang = attributes.getValue("xml:lang");
			inName = true;
		}
	}

	private boolean matchElement(String element, String localName,
			String qName) {
		if (Objects.equals(element, localName)
				|| Objects.equals(element, qName))
			return true;
		if (qName == null || qName.isEmpty())
			return false;
		String[] parts = qName.split(":");
		if (parts.length == 2)
			return Objects.equals(element, parts[1]);
		else
			return false;
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (inUUID)
			currentId = new String(ch, start, length);
		if (inVersion)
			currentVersion = new String(ch, start, length);
		if (inName) {
			if (currentName == null || Objects.equals(EpdStore.lang, lang))
				currentName = new String(ch, start, length);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		inName = false;
		inUUID = false;
		inVersion = false;
	}

	@Override
	public void endDocument() throws SAXException {
		if (!isEpdDataSet)
			return;
		EpdDescriptor d = new EpdDescriptor();
		d.name = currentName;
		d.refId = currentId;
		d.version = currentVersion;
		descriptors.add(d);
	}

}
