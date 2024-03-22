package epd.refs;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Optional;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.openlca.ilcd.Vocab;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;
import org.slf4j.LoggerFactory;

import epd.EditorVocab;

public class RefFetch {

	private final Ref ref;
	private final XMLStreamReader reader;

	private StringBuilder textBuff;
	private String lang;

	private RefFetch(XMLStreamReader reader) {
		ref = new Ref();
		this.reader = reader;

	}

	public static Optional<Ref> get(File file) {
		if (file == null)
			return Optional.empty();
		try (var stream = new FileInputStream(file)) {
			return get(stream);
		} catch (Exception e) {
			LoggerFactory.getLogger(RefFetch.class)
				.error("failed to fetch reference from " + file, e);
			return Optional.empty();
		}
	}

	public static Optional<Ref> get(InputStream stream) {
		if (stream == null)
			return Optional.empty();
		try {
			var reader = XMLInputFactory.newFactory()
				.createXMLStreamReader(stream);
			var fetch = new RefFetch(reader);
			fetch.parse();
			return fetch.ref.isValid()
				? Optional.of(fetch.ref)
				: Optional.empty();
		} catch (Exception e) {
			LoggerFactory.getLogger(RefFetch.class)
				.error("failed to fetch reference", e);
			return Optional.empty();
		}
	}

	private void parse() throws Exception {
		boolean root = true;
		boolean stop = false;
		while (reader.hasNext()) {
			reader.next();
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT:
					if (root) {
						root = false;
						ref.withType(getType());
					} else {
						start();
					}
					break;
				case XMLStreamConstants.CHARACTERS:
					text();
					break;
				case XMLStreamConstants.END_ELEMENT:
					stop = end();
					break;
			}
			if (stop)
				break;
		}
	}

	DataSetType getType() {
		String element = reader.getLocalName();
		if (element == null)
			return null;
		return switch (element) {
			case "LCIAMethodDataSet" -> DataSetType.IMPACT_METHOD;
			case "processDataSet" -> DataSetType.PROCESS;
			case "contactDataSet" -> DataSetType.CONTACT;
			case "sourceDataSet" -> DataSetType.SOURCE;
			case "flowDataSet" -> DataSetType.FLOW;
			case "flowPropertyDataSet" -> DataSetType.FLOW_PROPERTY;
			case "unitGroupDataSet" -> DataSetType.UNIT_GROUP;
			case "lifeCycleModelDataSet" -> DataSetType.MODEL;
			default -> null;
		};
	}

	private void start() {
		var name = reader.getName();
		if (name == null)
			return;
		switch (name.getLocalPart()) {
			case "UUID":
			case "dataSetVersion":
			case "permanentDataSetURI":
			case "referenceYear":
				textBuff = new StringBuilder();
				return;
		}
		if (matchName(reader.getName())) {
			lang = reader.getAttributeValue(Vocab.XML, "lang");
			textBuff = new StringBuilder();
		}
	}

	private void text() {
		if (textBuff == null)
			return;
		int pos = reader.getTextStart();
		int len = reader.getTextLength();
		textBuff.append(reader.getTextCharacters(), pos, len);
	}

	/**
	 * Handles an element-ends-event and returns true if we can stop parsing
	 * the document.
	 */
	private boolean end() {
		if (textBuff == null)
			return false;
		String text = textBuff.toString().trim();
		textBuff = null;
		String element = reader.getLocalName();
		if (element == null)
			return false;
		switch (element) {
			case "UUID":
				ref.withUUID(text);
				return false;
			case "dataSetVersion":
				ref.withVersion(text);
				return false;
			case "referenceYear":
				ref.withOtherAttributes()
					.put(EditorVocab.referenceYear(), text);
				return false;
			case "permanentDataSetURI":
				ref.withUri(text);
				return true;
		}
		if (matchName(reader.getName())) {
			ref.withName().add(LangString.of(text, lang));
		}
		return false;
	}

	private boolean matchName(QName name) {
		var type = ref.getType();
		return switch (name.getLocalPart()) {
			case "name" -> isCommon(name) && (
				type == DataSetType.CONTACT
					|| type == DataSetType.FLOW_PROPERTY
					|| type == DataSetType.UNIT_GROUP
					|| type == DataSetType.IMPACT_METHOD);
			case "baseName" -> type == DataSetType.FLOW
				|| type == DataSetType.PROCESS;
			case "shortName" -> type == DataSetType.SOURCE;
			default -> false;
		};
	}

	private boolean isCommon(QName name) {
		return name != null
			&& Vocab.COMMON.equalsIgnoreCase(name.getNamespaceURI());
	}
}
