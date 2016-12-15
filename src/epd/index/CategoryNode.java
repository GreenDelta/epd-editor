package epd.index;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.commons.Ref;

public class CategoryNode {

	public Category category;
	public final List<CategoryNode> childs = new ArrayList<>();
	public final Set<Ref> refs = new HashSet<>();

	void remove(Ref ref) {
		if (ref == null)
			return;
		refs.remove(ref);
		for (CategoryNode child : childs)
			child.remove(ref);
	}

	Ref find(Ref ref) {
		if (ref == null)
			return null;
		for (Ref r : refs) {
			if (Objects.equals(r.uuid, ref.uuid))
				return r;
		}
		for (CategoryNode n : childs) {
			Ref r = n.find(ref);
			if (r != null)
				return r;
		}
		return null;
	}

	boolean contains(Ref ref) {
		if (ref == null)
			return false;
		if (refs.contains(ref))
			return true;
		for (CategoryNode child : childs) {
			boolean b = child.contains(ref);
			if (b)
				return true;
		}
		return false;
	}
}
