package epd.model.qmeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.processes.AdminInfo;
import org.openlca.ilcd.processes.Modelling;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.util.Processes;
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

	/**
	 * Read the Q-Meta data extension from the given ILCD process data set. The
	 * extension can be located under the `modellingAndValidation/other` or
	 * `administrativeInformation/other` elements.
	 */
	public static QMetaData read(Process p) {
		if (p == null)
			return null;
		Modelling mod = Processes.getModelling(p);
		if (mod != null) {
			QMetaData qmeta = read(mod.other);
			if (qmeta != null)
				return qmeta;
		}
		AdminInfo adm = Processes.getAdminInfo(p);
		if (adm == null)
			return null;
		return read(adm.other);
	}

	private static QMetaData read(Other other) {
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

		// add the questions
		QMetaData qdata = new QMetaData();
		Dom.eachChild(root, e -> {
			QQuestion q = QQuestion.read(e);
			if (q != null) {
				qdata.questions.add(q);
			}
		});
		return qdata;
	}

	/**
	 * Write this Q-Meta data object to the given extension.
	 */
	public void write(Other other, Document doc) {
		if (other == null || doc == null)
			return;
		Dom.clear(other, "Q-Metadata");
		if (questions.isEmpty())
			return;
		Element root = doc.createElementNS(
				Vocab.NS_EPDv2, "epd2:Q-Metadata");
		other.any.add(root);
		for (QQuestion q : questions) {
			if (q != null) {
				q.write(root);
			}
		}
	}

	@Override
	public QMetaData clone() {
		QMetaData clone = new QMetaData();
		for (QQuestion q : questions) {
			if (q != null) {
				clone.questions.add(q.clone());
			}
		}
		return clone;
	}

}
