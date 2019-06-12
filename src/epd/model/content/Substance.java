package epd.model.content;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import epd.io.conversion.Vocab;
import epd.util.Fn;
import epd.util.Strings;

public class Substance extends ContentElement {

	/** The optional data dictionary GUID (whatever this is). */
	public String guid;

	/** CAS Number of the material or substance. */
	public String casNumber;

	/** EC Number of the material or substance. */
	public String ecNumber;

	/** The percentage of renewable resources contained. */
	public Double renewable;

	/** The percentage of recycled materials contained. */
	public Double recycled;

	/** The percentage of recyclable materials contained. */
	public Double recyclable;

	public Boolean packaging;

	@Override
	Substance read(Element e) {
		if (e == null)
			return this;
		super.read(e);
		guid = e.getAttributeNS(Vocab.NS_EPDv2, "ddGUID");
		casNumber = e.getAttributeNS(Vocab.NS_EPDv2, "CASNumber");
		ecNumber = e.getAttributeNS(Vocab.NS_EPDv2, "ECNumber");

		Function<String, Double> dfn = attr -> {
			try {
				String s = e.getAttributeNS(Vocab.NS_EPDv2, attr);
				if (Strings.nullOrEmpty(s))
					return null;
				return Double.parseDouble(s);
			} catch (Exception ex) {
				Logger log = LoggerFactory.getLogger(getClass());
				log.error("failed to read attr " + attr, ex);
				return null;
			}
		};
		renewable = dfn.apply("renewable");
		recycled = dfn.apply("recycled");
		recyclable = dfn.apply("recyclable");

		packaging = null;
		Fn.with(e.getAttributeNS(Vocab.NS_EPDv2, "packaging"), s -> {
			if (!Strings.nullOrEmpty(s)) {
				s = s.trim().toLowerCase();
				packaging = "true".equals(s) || "1".equals(s);
			}
		});
		return this;
	}

	@Override
	void copyTo(ContentElement other) {
		super.copyTo(other);
		if (other instanceof Substance) {
			Substance s = (Substance) other;
			s.guid = guid;
			s.casNumber = casNumber;
			s.ecNumber = ecNumber;
			s.renewable = renewable;
			s.recycled = recycled;
			s.recyclable = recyclable;
			s.packaging = packaging;
		}
	}

	@Override
	public Substance clone() {
		Substance clone = new Substance();
		copyTo(clone);
		return clone;
	}

}
