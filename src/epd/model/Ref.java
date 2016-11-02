package epd.model;

import java.util.Objects;

import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;

/**
 * Describes a data set reference.
 */
public class Ref {

	public String uuid;
	public String name;
	public String version;
	public String description;
	public DataSetType type;
	public String uri;

	public static Ref of(DataSetReference r) {
		return of(r, "en");
	}

	public static Ref of(DataSetReference r, String... langs) {
		Ref ref = new Ref();
		if (r == null)
			return ref;
		ref.name = LangString.getFirst(r.description, langs);
		ref.type = r.type;
		ref.uri = r.uri;
		ref.uuid = r.uuid;
		ref.version = r.version;
		return ref;
	}

	public boolean isValid() {
		return uuid != null && type != null;
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, uuid);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof Ref))
			return false;
		Ref other = (Ref) obj;
		return Objects.equals(this.type, other.type)
				&& Objects.equals(this.uuid, other.uuid);
	}

	@Override
	public String toString() {
		return "Ref [type=" + type + ", uuid=" + uuid + "]";
	}

	public DataSetReference toDataSetReference(String lang) {
		DataSetReference ref = new DataSetReference();
		ref.uuid = uuid;
		LangString.set(ref.description, name, lang);
		ref.version = version;
		ref.type = type;
		ref.uri = uri;
		return ref;
	}

	public Class<?> getDataSetType() {
		if (type == null)
			return null;
		switch (type) {
		case CONTACT:
			return Contact.class;
		case SOURCE:
			return Source.class;
		case UNIT_GROUP:
			return UnitGroup.class;
		case FLOW_PROPERTY:
			return FlowProperty.class;
		case FLOW:
			return Flow.class;
		case PROCESS:
			return org.openlca.ilcd.processes.Process.class;
		case LCIA_METHOD:
			return LCIAMethod.class;
		default:
			return null;
		}
	}
}
