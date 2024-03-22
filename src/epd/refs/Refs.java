package epd.refs;


import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.io.DataStore;

public final class Refs {

	private Refs() {
	}

	public static List<Ref> allEditableOf(IDataSet ds) {
		return DataSetRefs.allEditableOf(ds);
	}

	/**
	 * Collects transitively all editable dataset dependencies starting from
	 * the given dataset reference. The returned set will also contain that
	 * reference if it is a valid reference.
	 */
	public static Set<Ref> allEditableDependenciesOf(DataStore store, Ref root) {
		if (store == null || root == null || !root.isValid())
			return Collections.emptySet();

		var handled = new HashSet<Ref>();
		handled.add(root);
		var queue = new ArrayDeque<Ref>();
		queue.add(root);
		var dependencies = new HashSet<Ref>();

		while (!queue.isEmpty()) {
			var ref = queue.poll();
			var ds = store.get(ref.getDataSetClass(), ref.getUUID());
			if (ds == null)
				continue;
			dependencies.add(Ref.of(ds));
			for (var nextDep : allEditableOf(ds)) {
				if (!nextDep.isValid() || handled.contains(nextDep))
					continue;
				queue.add(nextDep);
				handled.add(nextDep);
			}
		}
		return dependencies;
	}

}
