package epd.io.conversion;

import epd.model.EpdProduct;
import epd.model.MaterialPropertyValue;
import org.openlca.ilcd.Vocab;
import org.openlca.ilcd.commons.Extension;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.util.Flows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import java.util.List;

public class FlowExtensions {

	public static EpdProduct read(Flow flow) {
		EpdProduct product = new EpdProduct();
		if (flow == null)
			return product;
		product.flow = flow;
		readInfoExtension(product);
		readMethodExtension(product);
		return product;
	}

	private static void readInfoExtension(EpdProduct p) {
		var extension = getInfoExtension(p.flow, false);
		if (extension == null)
			return;
		MatML matML = new MatML(extension);
		List<MaterialPropertyValue> values = matML.readValues();
		p.properties.addAll(values);
		p.genericFlow = DataSetRefExtension.readFlow("isA", extension);
	}

	private static void readMethodExtension(EpdProduct p) {
		Other extension = getMethodExtension(p.flow, false);
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
			writeInfoExtension(p);
			writeMethodExtension(p);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(FlowExtensions.class);
			log.error("failed to write flow properties for " + p, e);
		}
	}

	private static void writeInfoExtension(EpdProduct p) {
		if (p.genericFlow == null && p.properties.isEmpty()) {
			var info = Flows.getDataSetInfo(p.flow);
			// clear the extension point
			if (info != null) {
				info.withEpdExtension(null);
			}
			return;
		}

		var ext = getInfoExtension(p.flow, true);
		DataSetRefExtension.write(p.genericFlow, "isA", ext);
		MatML matML = new MatML(ext);
		if (p.properties.isEmpty()) {
			matML.clear();
		} else {
			matML.createStructure(LangString.getFirst(Flows.getBaseName(p.flow)));
			for (MaterialPropertyValue value : p.properties) {
				matML.append(value);
			}
		}
	}

	private static void writeMethodExtension(EpdProduct p) {
		Other extension = getMethodExtension(p.flow, true);
		writeVendorSpecificTag(p, extension);
		DataSetRefExtension.write(
			p.vendor, "referenceToVendor", extension);
		DataSetRefExtension.write(
			p.documentation, "referenceToSource", extension);
	}

	private static void writeVendorSpecificTag(EpdProduct p, Other ext) {
		String tag = "vendorSpecificProduct";
		Element e = Dom.getElement(ext, tag);
		if (e == null) {
			e = Dom.createElement(Vocab.EPD_2013, tag);
			ext.withAny().add(e);
		}
		e.setTextContent(Boolean.toString(p.vendorSpecific));
	}

	private static Extension getInfoExtension(Flow flow, boolean create) {
		if (create)
			return flow.withFlowInfo()
				.withDataSetInfo()
				.withEpdExtension();
		var info = Flows.getDataSetInfo(flow);
		return info != null
			? info.getEpdExtension()
			: null;
	}

	private static Other getMethodExtension(Flow flow, boolean create) {
		if (create)
			return flow.withModelling()
				.withInventoryMethod()
				.withOther();
		var method = Flows.getInventoryMethod(flow);
		return method != null
			? method.getOther()
			: null;
	}
}
