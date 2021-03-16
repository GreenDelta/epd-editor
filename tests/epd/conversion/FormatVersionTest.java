package epd.conversion;

import javax.xml.namespace.QName;

import epd.io.conversion.Extensions;
import epd.io.conversion.Vocab;
import epd.model.EpdDataSet;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FormatVersionTest {

	@Test
	public void testFormatVersion() {
		var epd = new EpdDataSet();
		Extensions.write(epd);
		var qName = "{" + Vocab.NS_EPDv2 + "}epd-version";
		var version = epd.process.otherAttributes.get(QName.valueOf(qName));
		assertEquals("1.2", version);
	}

}
