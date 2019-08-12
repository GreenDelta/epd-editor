package epd.model.qmeta;

import java.util.HashMap;
import java.util.Map;

import org.openlca.ilcd.processes.DataSetInfo;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.util.Processes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import epd.io.conversion.Dom;
import epd.io.conversion.Vocab;
import epd.util.Strings;

/**
 * A temporary upgrade procedure to convert old Q-Metadata versions to the
 * current schema. We should remove this when all data sets are upgraded.
 */
class QUpgrade {

	/**
	 * Try to run an upgrade and return true if an upgrade was done.
	 */
	static QMetaData on(Process p) {
		if (p == null)
			return null;
		DataSetInfo info = Processes.getDataSetInfo(p);
		if (info == null || info.other == null)
			return null;
		Element root = Dom.getChild(info.other, "Q-Metadata", Vocab.NS_EPDv2);
		if (root == null)
			return null;

		Logger log = LoggerFactory.getLogger(QUpgrade.class);
		log.info("Upgrade Q-Metadata information in data set");

		Map<String, String> comments = collectComments(root);
		QMetaData mappedQD = new QMetaData();
		Dom.eachChild(root, elem -> {
			if (!Dom.matches(elem, "Question", Vocab.NS_EPDv2))
				return;

			String qID = id(elem);
			if (qID == null || !qID.contains("."))
				return; // a question group
			boolean multiChoice = qID.startsWith("5.");

			if (multiChoice) {
				QQuestion qq = new QQuestion();
				qq.id = mapID(qID);
				qq.comment = comments.get(qID);
				qq.type = QQuestionType.BOOLEAN;
				qq.answer = new QAnswer();
				qq.answer.yesNo = answer(elem);
				mappedQD.questions.add(qq);
			} else {
				if (!answer(elem))
					return;
				QQuestion qq = new QQuestion();
				qq.id = mapID(qID);
				qq.type = QQuestionType.ONE_IN_LIST;
				qq.comment = comments.get(qID.split("\\.")[0]);
				qq.answer = new QAnswer();
				// the text will be overwritten when opened in the editor
				qq.answer.listText = qq.id;
				mappedQD.questions.add(qq);
			}
		});

		if (mappedQD.questions.isEmpty())
			return null;
		Dom.clear(info.other, "Q-Metadata");
		return mappedQD;
	}

	private static Map<String, String> collectComments(Element root) {
		HashMap<String, String> comments = new HashMap<>();
		Dom.eachChild(root, elem -> {
			if (!Dom.matches(elem, "Question", Vocab.NS_EPDv2))
				return;
			Element ce = Dom.getChild(elem, "Comment", Vocab.NS_EPDv2);
			if (ce == null)
				return;
			String text = ce.getTextContent();
			if (Strings.nullOrEmpty(text))
				return;
			String id = id(elem);
			if (Strings.nullOrEmpty(id))
				return;
			comments.put(id, text);
		});
		return comments;
	}

	private static String id(Element elem) {
		if (elem == null)
			return null;
		Element idElem = Dom.getChild(elem, "QuestionID", Vocab.NS_EPDv2);
		if (idElem == null)
			return null;
		return idElem.getTextContent();
	}

	private static boolean answer(Element elem) {
		if (elem == null)
			return false;
		Element aElem = Dom.getChild(elem, "QuestionAnswer", Vocab.NS_EPDv2);
		if (aElem == null)
			return false;
		aElem = Dom.getChild(aElem, "YesNo", Vocab.NS_EPDv2);
		if (aElem == null)
			return false;
		try {
			return Boolean.parseBoolean(aElem.getTextContent());
		} catch (Exception e) {
			return false;
		}
	}

	private static String mapID(String oldID) {
		if (oldID == null)
			return null;
		String[] parts = oldID.split("\\.");
		if (parts.length != 2)
			return null;
		return "QM0" + parts[0] + "0" + parts[1];
	}

}
