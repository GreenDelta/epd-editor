package epd.io.conversion;

import java.util.List;

import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.flows.AdminInfo;
import org.openlca.ilcd.flows.DataSetInfo;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.FlowInfo;
import org.openlca.ilcd.flows.LCIMethod;
import org.openlca.ilcd.flows.Modelling;
import org.openlca.ilcd.io.FileStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import epd.model.EpdProduct;
import epd.model.MaterialPropertyValue;

/**
 * We use the original flow data set from the server or the openLCA export and
 * just write or read the values that the user can enter and modify to/from this
 * flow via this decorator.
 */
public class FlowDecorator {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final EpdProduct product;
	private final FileStore store;

	public FlowDecorator(EpdProduct product, FileStore store) {
		this.product = product;
		this.store = store;
	}

	public void read() {
		if (product == null || store == null)
			return;
		log.trace("read flow properties for {}", product);
		Flow flow = product.flow;
		if (flow == null)
			return;
		readInfoExtension(flow);
		readMethodExtension(flow);
		String v = readVersion(flow);
		product.version = v;
	}

	private void readInfoExtension(Flow flow) {
		Other extension = getInfoExtension(flow, false);
		if (extension == null)
			return;
		MatML matML = new MatML(extension);
		List<MaterialPropertyValue> values = matML.readValues();
		product.properties.addAll(values);
		product.genericFlow = DataSetRefExtension.readFlow("isA",
				extension);
	}

	private void readMethodExtension(Flow flow) {
		Other extension = getMethodExtension(flow, false);
		if (extension == null)
			return;
		Element e = Util.getElement(extension, "vendorSpecificProduct");
		if (e != null) {
			try {
				product.vendorSpecific = Boolean
						.parseBoolean(e.getTextContent());
			} catch (Exception e2) {
				log.error("vendorSpecificProduct contains not a boolean", e);
			}
		}
		product.vendor = DataSetRefExtension.readActor(
				"referenceToVendor", extension);
		product.documentation = DataSetRefExtension.readSource(
				"referenceToSource", extension);
	}

	private String readVersion(Flow flow) {
		if (flow == null)
			return null;
		AdminInfo info = flow.adminInfo;
		if (info == null)
			return null;
		Publication pub = info.publication;
		return pub == null ? null : pub.version;
	}

	public void write() {
		if (product == null || store == null)
			return;
		log.trace("write flow properties for {}", product);
		try {
			Flow flow = product.flow;
			if (flow == null)
				return;
			writeInfoExtension(flow);
			writeMethodExtension(flow);
			writeVersion(flow);
			store.put(flow, flow.getUUID());
		} catch (Exception e) {
			log.error("failed to write flow properties for " + product, e);
		}
	}

	private void writeInfoExtension(Flow flow) {
		Other extension = getInfoExtension(flow, true);
		DataSetRefExtension.write(product.genericFlow, "isA", extension);
		MatML matML = new MatML(extension);
		if (product.properties.isEmpty())
			matML.clear();
		else {
			matML.createStructure(LangString.getFirst(product.flow.getName()));
			for (MaterialPropertyValue value : product.properties)
				matML.append(value);
		}
	}

	private void writeMethodExtension(Flow flow) {
		Other extension = getMethodExtension(flow, true);
		writeVendorSpecificTag(extension);
		DataSetRefExtension.write(product.vendor, "referenceToVendor",
				extension);
		DataSetRefExtension.write(product.documentation,
				"referenceToSource", extension);
	}

	private void writeVendorSpecificTag(Other extension) {
		String tagName = "vendorSpecificProduct";
		Element e = Util.getElement(extension, tagName);
		if (e == null) {
			e = Util.createElement(extension, tagName);
			extension.any.add(e);
		}
		e.setTextContent(Boolean.toString(product.vendorSpecific));
	}

	private Other getInfoExtension(Flow flow, boolean create) {
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

	private Other getMethodExtension(Flow flow, boolean create) {
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

	private void writeVersion(Flow flow) {
		if (flow == null)
			return;
		AdminInfo info = flow.adminInfo;
		if (info == null) {
			info = new AdminInfo();
			flow.adminInfo = info;
		}
		Publication pub = info.publication;
		if (pub == null) {
			pub = new Publication();
			info.publication = pub;
		}
		pub.version = product.version;
	}

}
