package epd.index;

import java.util.concurrent.atomic.AtomicBoolean;

import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.util.RefTree;

import epd.model.Version;

/**
 * Provides methods for synchronizing the data set references in a data set with
 * the data set references in an index.
 */
public final class RefSync {

	public static boolean hasOutdatedRefs(IDataSet ds, Index index) {
		if (ds == null || index == null)
			return false;
		AtomicBoolean b = new AtomicBoolean(false);
		RefTree.create(ds).eachRef(ref -> {
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
		RefTree.create(ds).eachRef(ref -> {
			Ref indexRef = index.find(ref);
			if (isNewer(indexRef, ref)) {
				ref.name.clear();
				ref.name.addAll(indexRef.name);
				ref.uri = indexRef.uri;
				ref.version = indexRef.version;
			}
		});
	}

	public static void updateSelfRefVersion(IDataSet ds) {
		if (ds == null)
			return;
		var selfRef = RefTree.create(ds).getRefs().stream()
				.filter(ref -> ref.uuid.equals(ds.getUUID()))
				.findFirst()
				.orElse(null);
		if (selfRef == null)
			return;
		selfRef.version = ds.getVersion();
	}
	
	private static boolean isNewer(Ref indexRef, Ref than) {
		if (indexRef == null || than == null)
			return false;
		Version indexV = Version.fromString(indexRef.version);
		Version thanV = Version.fromString(than.version);
		return indexV.compareTo(thanV) > 0;
	}

}
