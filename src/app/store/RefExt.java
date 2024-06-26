package app.store;

import java.util.Optional;

import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.util.Epds;
import org.openlca.ilcd.util.Strings;

public class RefExt {

	public static final String NAMESPACE = "http://greendelta.com/epd-editor";

	private RefExt() {
	}

	private static QName referenceYear() {
		return new QName(NAMESPACE, "referenceYear");
	}

	private static QName database() {
		return new QName(NAMESPACE, "database");
	}

	public static void add(Process epd, Ref ref) {
		if (epd == null || ref == null)
			return;

		var time = Epds.getTime(epd);
		if (time != null && time.getReferenceYear() != null) {
			var refYear = Integer.toString(time.getReferenceYear());
			ref.withOtherAttributes().put(referenceYear(), refYear);
		}

		var rep = Epds.getRepresentativeness(epd);
		if (rep != null) {
			for (var ds : rep.getSources()) {
				if (putDatabase(ref, ds.getUUID()))
					break;
			}
		}
	}

	public static boolean putReferenceYear(Ref ref, String year) {
		if (ref == null || Strings.nullOrEmpty(year))
			return false;
		ref.withOtherAttributes().put(referenceYear(), year);
		return true;
	}

	public static boolean putDatabase(Ref ref, String sourceId) {
		if (ref == null || Strings.nullOrEmpty(sourceId))
			return false;
		var db = switch (sourceId) {
			case "28d74cc0-db8b-4d7e-bc44-5f6d56ce0c4a" -> "GaBi";
			case "b497a91f-e14b-4b69-8f28-f50eb1576766" -> "ecoinvent";
			default -> null;
		};
		if (db == null)
			return false;
		ref.withOtherAttributes().put(database(), db);
		return true;
	}

	public static Optional<String> getReferenceYear(Ref ref) {
		return getAttribute(ref, referenceYear());
	}

	public static Optional<String> getDatabase(Ref ref) {
		return getAttribute(ref, database());
	}

	private static Optional<String> getAttribute(Ref ref, QName attr) {
		if (ref == null)
			return Optional.empty();
		var ats = ref.getOtherAttributes();
		if (ats == null || ats.isEmpty())
			return Optional.empty();
		var s = ats.get(attr);
		return Optional.ofNullable(s);
	}
}
