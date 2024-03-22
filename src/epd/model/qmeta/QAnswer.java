package epd.model.qmeta;

import epd.io.Dom;
import org.openlca.ilcd.Vocab;
import org.openlca.ilcd.commons.Copyable;
import org.w3c.dom.Element;

public class QAnswer implements Copyable<QAnswer> {

	public Boolean yesNo;
	public String listText;

	void write(Element parent) {
		if (parent == null)
			return;
		Element elem = Dom.addChild(parent,
				"norreq:QuestionAnswer", Vocab.SBE_ILCD);
		if (yesNo != null) {
			Dom.addChild(elem,
					"norreq:YesNo", Vocab.SBE_ILCD)
					.setTextContent(yesNo ? "true" : "false");
			return;
		}
		if (listText != null) {
			Dom.addChild(elem,
					"norreq:QuestionListText", Vocab.SBE_ILCD)
					.setTextContent(listText);
		}
	}

	static QAnswer read(Element parent) {
		if (parent == null)
			return null;
		Element elem = Dom.getChild(parent, "QuestionAnswer", Vocab.SBE_ILCD);
		if (elem == null)
			return null;
		QAnswer answer = new QAnswer();
		Element alem = Dom.getChild(elem, "YesNo", Vocab.SBE_ILCD);
		if (alem != null) {
			String ynStr = alem.getTextContent();
			answer.yesNo = "true".equalsIgnoreCase(ynStr) || "1".equals(ynStr);
			return answer;
		}
		alem = Dom.getChild(elem, "QuestionListText", Vocab.SBE_ILCD);
		if (alem != null) {
			answer.listText = alem.getTextContent();
		}
		return answer;
	}

	@Override
	public QAnswer copy() {
		var copy = new QAnswer();
		copy.yesNo = yesNo;
		copy.listText = listText;
		return copy;
	}
}
