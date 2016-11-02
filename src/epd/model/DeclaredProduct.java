package epd.model;

import java.util.ArrayList;

public class DeclaredProduct {

	public double amount = 1;
	public Ref flow;
	public Ref genericFlow;
	public boolean vendorSpecific;
	public Ref vendor;
	public Ref documentation;
	public String version;
	public final ArrayList<MaterialPropertyValue> properties = new ArrayList<>();

	@Override
	public DeclaredProduct clone() {
		DeclaredProduct clone = new DeclaredProduct();
		clone.amount = amount;
		clone.flow = flow;
		clone.genericFlow = genericFlow;
		clone.vendorSpecific = vendorSpecific;
		clone.vendor = vendor;
		clone.documentation = documentation;
		clone.version = version;
		for (MaterialPropertyValue v : properties) {
			clone.properties.add(v);
		}
		return clone;
	}
}
