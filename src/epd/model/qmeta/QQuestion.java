package epd.model.qmeta;

import org.w3c.dom.Element;

import epd.io.conversion.Dom;
import epd.io.conversion.Vocab;
import epd.util.Strings;

public class QQuestion {

	public String id;
	public QAnswer answer;
	public String comment;
	public QQuestionType type;
	public String text;

	void write(Element parent) {
		if (parent == null)
			return;
		Element elem = Dom.addChild(parent, "epd2:Question", Vocab.NS_EPDv2);
		if (id != null) {
			Element idElem = Dom.addChild(elem, "epd2:QuestionID",
					Vocab.NS_EPDv2);
			idElem.setTextContent(id);
		}
		if (answer != null) {
			answer.write(elem);
		}
		if (comment != null) {
			Element commentElem = Dom.addChild(elem, "epd2:Comment",
					Vocab.NS_EPDv2);
			commentElem.setTextContent(comment);
		}
	}

	static QQuestion read(Element elem) {
		if (elem == null)
			return null;
		if (!"Question".equals(elem.getLocalName()))
			return null;
		if (!Vocab.NS_EPDv2.equals(elem.getNamespaceURI()))
			return null;
		QQuestion q = new QQuestion();
		Element idElem = Dom.getChild(elem, "QuestionID", Vocab.NS_EPDv2);
		if (idElem != null) {
			q.id = idElem.getTextContent();
		}
		q.answer = QAnswer.read(elem);
		Element commentElem = Dom.getChild(elem, "Comment", Vocab.NS_EPDv2);
		if (commentElem != null) {
			q.comment = commentElem.getTextContent();
		}
		return q;
	}

	@Override
	public QQuestion clone() {
		QQuestion clone = new QQuestion();
		clone.id = id;
		clone.comment = comment;
		if (answer != null) {
			clone.answer = answer.clone();
		}
		return clone;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof QQuestion))
			return false;
		QQuestion other = (QQuestion) obj;
		return Strings.nullOrEqual(this.id, other.id);
	}
}
