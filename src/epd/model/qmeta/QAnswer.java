package epd.model.qmeta;

import org.w3c.dom.Element;

import epd.io.conversion.Dom;
import epd.io.conversion.Vocab;

public class QAnswer {

	public Boolean yesNo;

	void write(Element parent) {
		if (parent == null)
			return;
		Dom.addChild(parent, "YesNo", Vocab.NS_EPDv2);
	}

	@Override
	public QAnswer clone() {
		QAnswer clone = new QAnswer();
		clone.yesNo = yesNo;
		return clone;
	}
}
