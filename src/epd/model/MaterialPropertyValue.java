package epd.model;

import org.openlca.ilcd.commons.Copyable;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.epd.matml.Material;
import org.openlca.ilcd.flows.epd.matml.MaterialDoc;
import org.openlca.ilcd.flows.epd.matml.PropertyData;
import org.openlca.ilcd.flows.epd.matml.PropertyDetails;
import org.openlca.ilcd.flows.epd.matml.Unit;
import org.openlca.ilcd.util.Flows;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public final class MaterialPropertyValue implements Copyable<MaterialPropertyValue> {

	public MaterialProperty property;
	public double value;

	@Override
	public MaterialPropertyValue copy() {
		var copy = new MaterialPropertyValue();
		copy.property = property;
		copy.value = value;
		return copy;
	}

	public static List<MaterialPropertyValue> readFrom(Flow flow) {
		if (flow == null)
			return Collections.emptyList();
		var info = Flows.getDataSetInfo(flow);
		var doc = info != null && info.getEpdExtension() != null
			? info.getEpdExtension().getMaterialDoc()
			: null;
		if (doc == null)
			return Collections.emptyList();

		var props = new HashMap<String, MaterialProperty>();
		for (var p : doc.getProperties()) {
			var prop = new MaterialProperty();
			prop.id = p.getId();
			prop.name = p.getName();
			if (p.getUnits() != null) {
				prop.unit = p.getUnits().getName();
				prop.unitDescription = p.getUnits().getDescription();
			}
			props.put(prop.id, prop);
		}

		var vals = new ArrayList<MaterialPropertyValue>();
		for (var mat : doc.getMaterials()) {
			var details = mat.getBulkDetails();
			if (details == null)
				continue;
			for (var p : details.getProperties()) {
				var prop = props.get(p.getProperty());
				if (prop == null || p.getValue() == null)
					continue;
				var val = new MaterialPropertyValue();
				val.property = prop;
				try {
					val.value = Double.parseDouble(p.getValue().getValue());
					vals.add(val);
				} catch (Exception e) {
					LoggerFactory.getLogger(MaterialPropertyValue.class)
						.warn("cannot parse non-numeric material properties", e);
				}
			}
		}
		return vals;
	}

	public static void write(List<MaterialPropertyValue> vals, Flow flow) {
		if (flow == null)
			return;
		if (vals == null || vals.isEmpty()) {
			var info = Flows.getDataSetInfo(flow);
			if (info != null && info.getEpdExtension() != null) {
				info.getEpdExtension().withMaterialDoc(null);
			}
			return;
		}

		var doc = new MaterialDoc();
		var props = new HashMap<String, MaterialProperty>();

		var material = new Material();
		doc.withMaterials().add(material);
		var docData = material.withBulkDetails()
			.withName(LangString.getFirst(Flows.getBaseName(flow)))
			.withProperties();

		// add property values
		for (var v : vals) {
			if (v.property == null)
				continue;
			props.put(v.property.id, v.property);
			var docVal = new PropertyData()
				.withProperty(v.property.id);
			docVal.withValue()
				.withFormat("float")
				.withValue(Double.toString(v.value));
			docData.add(docVal);
		}

		// add property metadata
		var docProps = doc.withProperties();
		for (var prop : props.values()) {
			var docProp = new PropertyDetails()
				.withId(prop.id)
				.withName(prop.name);
			docProp.withUnits()
				.withName(prop.unit)
				.withDescription(prop.unitDescription)
				.withUnits()
				.add(new Unit().withName(prop.unit));
			docProps.add(docProp);
		}

		Flows.withDataSetInfo(flow)
			.withEpdExtension()
			.withMaterialDoc(doc);
	}

}
