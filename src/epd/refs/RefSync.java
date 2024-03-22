package epd.refs;

import java.util.concurrent.atomic.AtomicBoolean;

import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.util.DataSets;

import epd.index.Index;
import epd.model.Version;

/**
 * Provides methods for synchronizing the data set references in a data set with
 * the data set references in an index.
 */
public final class RefSync {

	public static boolean hasOutdatedRefs(IDataSet ds, Index index) {
		if (ds == null || index == null)
			return false;
		var b = new AtomicBoolean(false);
		Refs.allEditableOf(ds).forEach(ref -> {
			if (b.get()) {
				return;
			}
			Ref indexRef = index.find(ref);
			if (isNewer(indexRef, ref)) {
				b.set(true);
			}
		});
		return b.get();
	}

	public static void updateRefs(IDataSet ds, Index index) {
		if (ds == null || index == null)
			return;
		Refs.allEditableOf(ds).forEach(ref -> {
			Ref indexRef = index.find(ref);
			if (!isNewer(indexRef, ref))
				return;
			ref.withUri(indexRef.getUri())
				.withVersion(indexRef.getVersion())
				.withName().clear();
			ref.withName().addAll(indexRef.getName());
		});
	}

	/**
	 * In case the given dataset contains one or more references to itself,
	 * the version of that reference is updated with version of the dataset.
	 */
	public static void updateSelfRefVersion(IDataSet ds) {
		var uuid = DataSets.getUUID(ds);
		if (uuid == null)
			return;
		for (var ref : Refs.allEditableOf(ds)) {
			if (uuid.equals(ref.getUUID())) {
				ref.withVersion(DataSets.getVersion(ds));
			}
		}
	}

	private static boolean isNewer(Ref indexRef, Ref than) {
		if (indexRef == null || than == null)
			return false;
		var indexVersion = Version.fromString(indexRef.getVersion());
		var otherVersion = Version.fromString(than.getVersion());
		return indexVersion.compareTo(otherVersion) > 0;
	}

}
