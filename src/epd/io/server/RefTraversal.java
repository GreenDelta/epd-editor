package epd.io.server;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.openlca.ilcd.commons.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Traverses an object graph and collects all references to other data sets.
 */
class RefTraversal {

	private Logger log = LoggerFactory.getLogger(getClass());

	public Set<Ref> traverse(Object graph) {
		if (graph == null)
			return Collections.emptySet();
		Queue<Object> traversals = new ArrayDeque<>();
		traversals.add(graph);
		Map<Object, Boolean> handled = new IdentityHashMap<>();
		Set<Ref> refs = new HashSet<>();
		while (!traversals.isEmpty()) {
			Object next = traversals.poll();
			if (next == null)
				continue;
			if (next instanceof Ref) {
				Ref ref = (Ref) next;
				if (ref.isValid())
					refs.add(ref);
				continue;
			}
			handled.put(next, Boolean.TRUE);
			List<Object> nextLayer = getTraversals(next);
			for (Object traversal : nextLayer) {
				if (handled.get(traversal) == null
						&& !traversals.contains(traversal)) {
					traversals.add(traversal);
				}
			}
		}
		return refs;
	}

	private List<Object> getTraversals(Object obj) {
		if (obj == null)
			return Collections.emptyList();
		try {
			List<Object> traversals = new ArrayList<>();
			for (Field field : obj.getClass().getDeclaredFields()) {
				if (!follow(field.getType()))
					continue;
				field.setAccessible(true);
				Object val = field.get(obj);
				if (val instanceof Collection)
					addCollectionEntries(traversals, val);
				else if (val != null)
					traversals.add(val);
			}
			return traversals;
		} catch (Exception e) {
			log.error("failed to find traversals for " + obj, e);
			return Collections.emptyList();
		}
	}

	private void addCollectionEntries(List<Object> traversals, Object val) {
		Collection<?> collection = (Collection<?>) val;
		for (Object entry : collection) {
			if (entry != null && follow(entry.getClass()))
				traversals.add(entry);
		}
	}

	private boolean follow(Class<?> clazz) {
		if (clazz == null)
			return false;
		if (!Object.class.isAssignableFrom(clazz))
			return false;
		if (Number.class.isAssignableFrom(clazz))
			return false;
		if (String.class.isAssignableFrom(clazz))
			return false;
		if (Character.class.isAssignableFrom(clazz))
			return false;
		else
			return true;
	}

}
