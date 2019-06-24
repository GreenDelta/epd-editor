package epd.model.qmeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openlca.ilcd.commons.Other;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import epd.io.conversion.Dom;
import epd.io.conversion.Vocab;
import epd.util.Strings;

public class QMetaData {

	public List<QQuestion> questions = new ArrayList<>();

	public QQuestion getQuestion(String id) {
		QQuestion q = questions.stream()
				.filter(e -> Strings.nullOrEqual(e.id, id))
				.findFirst().orElse(null);
		if (q != null)
			return q;
		q = new QQuestion();
		q.id = id;
		questions.add(q);
		return q;
	}

	public static QMetaData read(Other other) {
		if (other == null)
			return null;

		// find the root element
		Element root = null;
		for (Object any : other.any) {
			if (!(any instanceof Element))
				continue;
			Element e = (Element) any;
			if (Objects.equals(Vocab.NS_EPDv2, e.getNamespaceURI())
					&& Objects.equals("Q-Metadata", e.getLocalName())) {
				root = e;
				break;
			}
		}
		if (root == null)
			return null;

		QMetaData qdata = new QMetaData();
		Dom.eachChild(root, e -> {
			// TODO: read questions
		});
		return qdata;
	}

	public void write(Other other, Document doc) {
		if (other == null || doc == null)
			return;
		Dom.clear(other, "Q-Metadata");
		if (questions.isEmpty())
			return;
		Element root = doc.createElementNS(
				Vocab.NS_EPDv2, "epd2:contentDeclaration");
		other.any.add(root);
		for (QQuestion q : questions) {
			// TODO: write questions
		}
	}

}
