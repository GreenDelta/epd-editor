package epd.index;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.openlca.ilcd.commons.Ref;

public abstract class Node {

	public final Set<Ref> refs = new HashSet<>();
	public final List<CategoryNode> categories = new ArrayList<>();

	void remove(Ref ref) {
		if (ref == null)
			return;
		refs.remove(ref);
		for (CategoryNode cat : categories)
			cat.remove(ref);
	}

	Ref find(Ref ref) {
		if (ref == null)
			return null;
		for (Ref r : refs) {
			if (Objects.equals(r.uuid, ref.uuid))
				return r;
		}
		for (CategoryNode n : categories) {
			Ref r = n.find(ref);
			if (r != null)
				return r;
		}
		return null;
	}

	public boolean contains(Ref ref) {
		if (ref == null)
			return false;
		if (refs.contains(ref))
			return true;
		for (CategoryNode child : categories) {
			boolean b = child.contains(ref);
			if (b)
				return true;
		}
		return false;
	}
}
