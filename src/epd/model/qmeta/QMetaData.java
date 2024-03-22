package epd.model.qmeta;

import epd.io.Dom;
import epd.util.Strings;
import org.openlca.ilcd.Vocab;
import org.openlca.ilcd.commons.Copyable;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.util.Epds;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class QMetaData implements Copyable<QMetaData> {

	public List<QQuestion> questions = new ArrayList<>();

	/**
	 * Get the question with the given ID from this meta data. It is created if
	 * it does not exist yet.
	 */
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
	 * Returns the selected question/answer for the given group. This method is
	 * useful for `one-in-list` questions that can have exactly one answer. It
	 * returns the (first) question of the given group that is stored in this Q
	 * metadata with an answer.
	 */
	public QQuestion getSelected(QGroup group) {
		if (group == null)
			return null;
		Set<String> ids = group.questions.stream()
			.map(q -> q.id)
			.collect(Collectors.toSet());
		for (QQuestion q : questions) {
			if (!ids.contains(q.id))
				continue;
			if (q.answer != null) {
				q.group = group.name;
				return q;
			}
		}
		return null;
	}

	/**
	 * Remove all questions from the given group from this meta data.
	 */
	public void removeAll(QGroup group) {
		if (group == null)
			return;
		Set<String> ids = group.questions.stream()
			.map(q -> q.id)
			.collect(Collectors.toSet());
		List<QQuestion> removals = this.questions.stream()
			.filter(q -> ids.contains(q.id))
			.toList();
		questions.removeAll(removals);
	}

	/**
	 * Read the Q-Meta data extension from the given ILCD process data set. The
	 * extension can be located under the `modellingAndValidation/other` or
	 * `administrativeInformation/other` elements.
	 */
	public static QMetaData read(Process epd) {
		if (epd == null)
			return null;
		var mod = Epds.getModelling(epd);
		if (mod != null) {
			var qmeta = read(mod.getOther());
			if (qmeta != null)
				return qmeta;
		}
		var adm = Epds.getAdminInfo(epd);
		if (adm != null) {
			return read(adm.getOther());
		}
		return null;
	}

	private static QMetaData read(Other other) {
		if (other == null)
			return null;

		// find the root element
		Element root = Dom.getChild(other, "ILCD-SBE", Vocab.SBE_ILCD);
		if (root == null)
			return null;
		root = Dom.getChild(root, "Q-Metadata", Vocab.SBE_ILCD);
		if (root == null)
			return null;

		// add the questions
		var qdata = new QMetaData();
		Dom.eachChild(root, e -> {
			QQuestion q = QQuestion.read(e);
			if (q != null) {
				qdata.questions.add(q);
			}
		});
		return qdata;
	}

	public static void write(Process epd, QMetaData data) {
		if (epd == null)
			return;
		var mod = Epds.getModelling(epd);
		if (mod != null) {
			mod.withOther(null);
		}
		var adm = Epds.getAdminInfo(epd);
		if (adm != null) {
			adm.withOther(null);
		}
		if (data == null || data.questions.isEmpty())
			return;
		data.write(epd.withModelling().withOther(), Dom.createDocument());
	}

	private void write(Other other, Document doc) {
		if (other == null || doc == null)
			return;
		Element root = Dom.getChild(other, "ILCD-SBE", Vocab.SBE_ILCD);
		if (root == null) {
			root = doc.createElementNS(Vocab.SBE_ILCD, "norreq:ILCD-SBE");
			other.withAny().add(root);
		}

		// remove the old content
		Element qmeta = Dom.getChild(root, "Q-Metadata", Vocab.SBE_ILCD);
		if (qmeta != null) {
			root.removeChild(qmeta);
		}
		if (questions.isEmpty())
			return;

		qmeta = Dom.addChild(root, "norreq:Q-Metadata", Vocab.SBE_ILCD);
		for (QQuestion q : questions) {
			if (q != null) {
				q.write(qmeta);
			}
		}
	}

	@Override
	public QMetaData copy() {
		var copy = new QMetaData();
		for (QQuestion q : questions) {
			if (q != null) {
				copy.questions.add(q.copy());
			}
		}
		return copy;
	}

}
