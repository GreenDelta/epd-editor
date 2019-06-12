package epd.io.conversion;

import javax.xml.namespace.QName;

public class Vocab {

	static final String NS_EPD = "http://www.iai.kit.edu/EPD/2013";
	static final String NS_OLCA = "http://openlca.org/epd_ilcd";
	static final String NS_XML = "http://www.w3.org/XML/1998/namespace";
	public static final String NS_EPDv2 = "http://www.indata.network/EPD/2019";

	private Vocab() {
	}

	public static final QName PROFILE_ATTR = new QName(
			"http://www.okworx.com/ILCD/Extensions/2018/Profile", "profile");

}
