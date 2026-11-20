package epd.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.openlca.commons.Copyable;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.epd.matml.Material;
import org.openlca.ilcd.flows.epd.matml.MaterialDoc;
import org.openlca.ilcd.flows.epd.matml.PropertyData;
import org.openlca.ilcd.flows.epd.matml.PropertyDetails;
import org.openlca.ilcd.flows.epd.matml.Unit;
import org.openlca.ilcd.util.Flows;
import org.slf4j.LoggerFactory;

public final class MaterialPropertyValue implements Copyable<MaterialPropertyValue> {

	/// A simplified check for valid `xsd:ID` values (which must be NCNames).
	/// It accepts all IDs that the material property editor allows.
	private static final Pattern VALID_ID = Pattern.compile(
		"[\\p{L}_][\\p{L}\\p{Nd}._-]*");

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
		var material = new Material();
		doc.withMaterials().add(material);
		var docData = material.withBulkDetails()
				.withName(LangString.getDefault(Flows.getBaseName(flow)))
			.withProperties();

		// add property values; property IDs are assigned per document so that
		// different properties never share an ID (see DocIds)
		var ids = DocIds.of(vals);
		for (var v : vals) {
			if (v.property == null)
				continue;
			var docVal = new PropertyData()
				.withProperty(ids.idOf(v.property));
			docVal.withValue()
				.withFormat("float")
				.withValue(Double.toString(v.value));
			docData.add(docVal);
		}

		// add property metadata
		var docProps = doc.withProperties();
		for (var def : ids.defs) {
			var prop = def.prop;
			var docProp = new PropertyDetails()
				.withId(def.docId)
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

	/// A property definition written to the document with its document ID.
	private record Def(MaterialProperty prop, String docId) {
	}

	/**
	 * Assigns the document IDs of the material properties. Material property IDs
	 * are only unique within a MatML document. Properties from different sources
	 * can thus have the same ID but a different meaning (e.g. user-defined
	 * properties of different installations), or the same meaning but a
	 * different ID. Properties that describe the same physical property (see
	 * {@link MaterialProperty#isSameAs}) share one definition and ID. A property
	 * keeps its own ID if it is valid and not taken yet; otherwise, a new ID is
	 * generated that does not collide with any ID of the given properties.
	 *
	 * @param byProp maps the property objects (by identity) to their definition
	 * @param defs   the distinct definitions in the order of the values
	 */
	private record DocIds(Map<MaterialProperty, Def> byProp, List<Def> defs) {

		static DocIds of(List<MaterialPropertyValue> vals) {
			var reserved = new HashSet<String>();
			for (var v : vals) {
				if (v.property != null && isValidId(v.property.id)) {
					reserved.add(v.property.id);
				}
			}

			var byProp = new IdentityHashMap<MaterialProperty, Def>();
			var defs = new ArrayList<Def>();
			var used = new HashSet<String>();
			int next = 1;
			for (var v : vals) {
				var prop = v.property;
				if (prop == null || byProp.containsKey(prop))
					continue;
				var def = defs.stream()
					.filter(d -> d.prop.isSameAs(prop))
					.findFirst()
					.orElse(null);
				if (def == null) {
					var id = prop.id;
					if (!isValidId(id) || used.contains(id)) {
						do {
							id = "pr" + next++;
						} while (reserved.contains(id) || used.contains(id));
					}
					used.add(id);
					def = new Def(prop, id);
					defs.add(def);
				}
				byProp.put(prop, def);
			}
			return new DocIds(byProp, defs);
		}

		String idOf(MaterialProperty prop) {
			return byProp.get(prop).docId;
		}

		private static boolean isValidId(String id) {
			return id != null && VALID_ID.matcher(id).matches();
		}
	}

}
