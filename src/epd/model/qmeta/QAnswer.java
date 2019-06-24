package epd.model.qmeta;

import org.w3c.dom.Element;

import epd.io.conversion.Dom;
import epd.io.conversion.Vocab;

public class QAnswer {

	public Boolean yesNo;

	void write(Element parent) {
		if (parent == null)
			return;
		Element elem = Dom.addChild(parent, "epd2:QuestionAnswer",
				Vocab.NS_EPDv2);
		if (yesNo != null) {
			Element ynElem = Dom.addChild(elem, "epd2:YesNo", Vocab.NS_EPDv2);
			ynElem.setTextContent(yesNo ? "true" : "false");
		}
	}

	static QAnswer read(Element parent) {
		if (parent == null)
			return null;
		Element elem = Dom.getChild(parent, "QuestionAnswer", Vocab.NS_EPDv2);
		if (elem == null)
			return null;
		QAnswer answer = new QAnswer();
		Element ynElem = Dom.getChild(elem, "YesNo", Vocab.NS_EPDv2);
		if (ynElem != null) {
			String ynStr = ynElem.getTextContent();
			answer.yesNo = "true".equalsIgnoreCase(ynStr) || "1".equals(ynStr);
		}
		return answer;
	}

	@Override
	public QAnswer clone() {
		QAnswer clone = new QAnswer();
		clone.yesNo = yesNo;
		return clone;
	}
}
