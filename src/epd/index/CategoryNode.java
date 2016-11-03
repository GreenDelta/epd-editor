package epd.index;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openlca.ilcd.commons.Category;

import epd.model.Ref;

public class CategoryNode {

	public Category category;
	public final List<CategoryNode> childs = new ArrayList<>();
	public final Set<Ref> refs = new HashSet<>();
}
