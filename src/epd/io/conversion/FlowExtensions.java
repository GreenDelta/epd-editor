package epd.io.conversion;

import epd.model.EpdProduct;
import org.openlca.ilcd.Vocab;
import org.openlca.ilcd.commons.Extension;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.util.Flows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public class FlowExtensions {

	public static EpdProduct read(Flow flow) {
		EpdProduct product = new EpdProduct();
		if (flow == null)
			return product;
		product.flow = flow;
		readMethodExtension(product);
		return product;
	}

	private static void readMethodExtension(EpdProduct p) {
		var extension = getMethodExtension(p.flow, false);
		if (extension == null)
			return;
		Element elem = Dom.getElement(extension, "vendorSpecificProduct");
		if (elem != null) {
			try {
				p.vendorSpecific = Boolean
					.parseBoolean(elem.getTextContent());
			} catch (Exception e) {
				var log = LoggerFactory.getLogger(FlowExtensions.class);
				log.error("vendorSpecificProduct contains not a boolean", e);
			}
		}
		p.vendor = DataSetRefExtension.readActor(
			"referenceToVendor", extension);
		p.documentation = DataSetRefExtension.readSource(
			"referenceToSource", extension);
	}

	/**
	 * Writes the EPD extensions of the given product to the underlying ILCD
	 * flow data set.
	 */
	public static void write(EpdProduct p) {
		if (p == null || p.flow == null)
			return;
		try {
			writeMethodExtension(p);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(FlowExtensions.class);
			log.error("failed to write flow properties for " + p, e);
		}
	}

	private static void writeMethodExtension(EpdProduct p) {
		var extension = getMethodExtension(p.flow, true);
		writeVendorSpecificTag(p, extension);
		DataSetRefExtension.write(
			p.vendor, "referenceToVendor", extension);
		DataSetRefExtension.write(
			p.documentation, "referenceToSource", extension);
	}

	private static void writeVendorSpecificTag(EpdProduct p, Extension ext) {
		String tag = "vendorSpecificProduct";
		Element e = Dom.getElement(ext, tag);
		if (e == null) {
			e = Dom.createElement(Vocab.EPD_2013, tag);
			ext.withAny().add(e);
		}
		e.setTextContent(Boolean.toString(p.vendorSpecific));
	}

	private static Extension getMethodExtension(Flow flow, boolean create) {
		if (create)
			return flow.withModelling()
				.withInventoryMethod()
				.withEpdExtension();
		var method = Flows.getInventoryMethod(flow);
		return method != null
			? method.getEpdExtension()
			: null;
	}
}
