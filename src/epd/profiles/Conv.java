package epd.profiles;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;

import com.google.gson.JsonObject;

import app.store.Json;
import jakarta.xml.bind.JAXB;

public class Conv {

	public static void main(String[] args) {

		var pid = "EN_15804_A2";
		var is = Conv.class.getResourceAsStream(pid + ".json");
		var obj = Json.read(is, JsonObject.class);
		Objects.requireNonNull(obj);

		var profile = new EpdProfile();
		profile.withId(str(obj, "id"))
			.withName(str(obj, "name"))
			.withDataUrl(str(obj, "referenceDataUrl"));

		for (var oi : array(obj, "modules")) {
			var mod = new Module()
				.withName(str(oi, "name"))
				.withIndex(idx(oi));
			profile.withModules().add(mod);
		}

		for (var oi : array(obj, "indicators")) {
			var id = str(oi, "uuid");
			var type = Objects.equals(str(oi, "type"), "LCI")
				? DataSetType.FLOW
				: DataSetType.IMPACT_METHOD;
			var url = type == DataSetType.FLOW
				? "../flows/" + id + ".xml"
				: "../lciamethods/" + id + ".xml";

			var name = str(oi, "name");
			var ref = new Ref().withUUID(id)
				.withVersion("01.00.000")
				.withType(type)
				.withUri(url);
			ref.withName().add(LangString.of(name, "en"));

			var unit = str(oi, "unit");
			var unitGroupId = str(oi, "unitGroupUUID");
			var unitGroupRef = new Ref().withUUID(unitGroupId)
				.withVersion("01.00.000")
				.withType(DataSetType.UNIT_GROUP)
				.withUri("../unitgroups/" + unitGroupId + ".xml");
			unitGroupRef.withName().add(LangString.of(unit, "en"));

			var indicator = new Indicator()
				.withGroup(str(oi, "group"))
				.withCode(str(oi, "code"))
				.withRef(ref)
				.withUnit(unitGroupRef);

			if (type == DataSetType.FLOW) {
				var b = oi.get("isInput");
				if (b != null && b.isJsonPrimitive() && b.getAsBoolean()) {
					indicator.withInputIndicator(true);
				}
			}

			profile.withIndicators().add(indicator);
		}


		JAXB.marshal(profile, new File(pid + ".xml"));
	}

	private static String str(JsonObject obj, String prop) {
		var e = obj.get(prop);
		if (e == null || !e.isJsonPrimitive())
			return null;
		var prim = e.getAsJsonPrimitive();
		return prim.isString() ? prim.getAsString() : null;
	}

	private static int idx(JsonObject obj) {
		var e = obj.get("index");
		if (e == null || !e.isJsonPrimitive())
			return -1;
		var prim = e.getAsJsonPrimitive();
		return prim.isNumber() ? prim.getAsInt() : -1;
	}

	private static List<JsonObject> array(JsonObject obj, String prop) {
		var e = obj.get(prop);
		if (e == null || !e.isJsonArray())
			return Collections.emptyList();
		var array = e.getAsJsonArray();
		var objs = new ArrayList<JsonObject>(array.size());
		for (var ei : array) {
			if (!ei.isJsonObject())
				continue;
			objs.add(ei.getAsJsonObject());
		}
		return objs;
	}

}
