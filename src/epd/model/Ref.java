package epd.model;

import java.util.Objects;

import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.ProcessName;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	public static Ref of(Object dataSet, String lang) {
		if (dataSet == null)
			return new Ref();
		if (dataSet instanceof Process)
			return of((Process) dataSet, lang);
		Logger log = LoggerFactory.getLogger(Ref.class);
		log.error("Could not create Ref from {}", dataSet);
		return new Ref();
	}

	public static Ref of(Process p, String lang) {
		Ref ref = new Ref();
		ref.type = DataSetType.PROCESS;
		if (p == null)
			return ref;
		if (p.adminInfo != null && p.adminInfo.publication != null) {
			ref.uri = p.adminInfo.publication.uri;
			ref.version = p.adminInfo.publication.version;
		}
		if (p.processInfo != null && p.processInfo.dataSetInfo != null) {
			ref.uuid = p.processInfo.dataSetInfo.uuid;
			ref.description = LangString.getVal(
					p.processInfo.dataSetInfo.comment, lang);
			ProcessName name = p.processInfo.dataSetInfo.name;
			if (name != null)
				ref.name = LangString.getVal(name.name, lang);
		}
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
