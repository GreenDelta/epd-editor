package epd.io.conversion;

import java.util.List;

import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.flows.DataSetInfo;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.FlowInfo;
import org.openlca.ilcd.flows.LCIMethod;
import org.openlca.ilcd.flows.Modelling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import epd.model.EpdProduct;
import epd.model.MaterialPropertyValue;

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
		Other extension = getInfoExtension(p.flow, false);
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
		Element e = Util.getElement(extension, "vendorSpecificProduct");
		if (e != null) {
			try {
				p.vendorSpecific = Boolean
						.parseBoolean(e.getTextContent());
			} catch (Exception e2) {
				Logger log = LoggerFactory.getLogger(FlowExtensions.class);
				log.error("vendorSpecificProduct contains not a boolean", e);
			}
		}
		p.vendor = DataSetRefExtension.readActor(
				"referenceToVendor", extension);
		p.documentation = DataSetRefExtension.readSource(
				"referenceToSource", extension);
	}

	public static void write(EpdProduct p) {
		if (p == null || p.flow == null)
			return;
		try {
			Flow flow = p.flow;
			if (flow == null)
				return;
			writeInfoExtension(p);
			writeMethodExtension(p);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(FlowExtensions.class);
			log.error("failed to write flow properties for " + p, e);
		}
	}

	private static void writeInfoExtension(EpdProduct p) {
		Other extension = getInfoExtension(p.flow, true);
		DataSetRefExtension.write(p.genericFlow, "isA", extension);
		MatML matML = new MatML(extension);
		if (p.properties.isEmpty())
			matML.clear();
		else {
			matML.createStructure(LangString.getFirst(p.flow.getName()));
			for (MaterialPropertyValue value : p.properties)
				matML.append(value);
		}
	}

	private static void writeMethodExtension(EpdProduct p) {
		Other extension = getMethodExtension(p.flow, true);
		writeVendorSpecificTag(p, extension);
		DataSetRefExtension.write(p.vendor, "referenceToVendor",
				extension);
		DataSetRefExtension.write(p.documentation,
				"referenceToSource", extension);
	}

	private static void writeVendorSpecificTag(EpdProduct p, Other extension) {
		String tagName = "vendorSpecificProduct";
		Element e = Util.getElement(extension, tagName);
		if (e == null) {
			e = Util.createElement(extension, tagName);
			extension.any.add(e);
		}
		e.setTextContent(Boolean.toString(p.vendorSpecific));
	}

	private static Other getInfoExtension(Flow flow, boolean create) {
		FlowInfo flowInfo = flow.flowInfo;
		if (flowInfo == null) {
			if (!create)
				return null;
			flowInfo = new FlowInfo();
			flow.flowInfo = flowInfo;
		}
		DataSetInfo dataInfo = flowInfo.dataSetInfo;
		if (dataInfo == null) {
			if (!create)
				return null;
			dataInfo = new DataSetInfo();
			flowInfo.dataSetInfo = dataInfo;
		}
		Other other = dataInfo.other;
		if (other == null && create) {
			other = new Other();
			dataInfo.other = other;
		}
		return other;
	}

	private static Other getMethodExtension(Flow flow, boolean create) {
		Modelling mav = flow.modelling;
		if (mav == null) {
			if (!create)
				return null;
			mav = new Modelling();
			flow.modelling = mav;
		}
		LCIMethod method = mav.lciMethod;
		if (method == null) {
			if (!create)
				return null;
			method = new LCIMethod();
			mav.lciMethod = method;
		}
		Other other = method.other;
		if (other == null && create) {
			other = new Other();
			method.other = other;
		}
		return other;
	}
}
