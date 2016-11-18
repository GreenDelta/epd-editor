package epd.model;

import java.util.ArrayList;

import org.openlca.ilcd.commons.Ref;

public class EpdProduct {

	public Ref flow;
	public Ref genericFlow;
	public boolean vendorSpecific;
	public Ref vendor;
	public Ref documentation;
	public String version;
	public final ArrayList<MaterialPropertyValue> properties = new ArrayList<>();

	@Override
	public EpdProduct clone() {
		EpdProduct clone = new EpdProduct();
		if (flow != null)
			clone.flow = flow.clone();
		if (genericFlow != null)
			clone.genericFlow = genericFlow.clone();
		clone.vendorSpecific = vendorSpecific;
		if (vendor != null)
			clone.vendor = vendor.clone();
		if (documentation != null)
			clone.documentation = documentation.clone();
		clone.version = version;
		for (MaterialPropertyValue v : properties) {
			if (v != null)
				clone.properties.add(v.clone());
		}
		return clone;
	}
}
