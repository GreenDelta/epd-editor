package app.editors.refs;

import org.eclipse.jface.viewers.ViewerComparator;
import org.openlca.ilcd.commons.Ref;

public class RefComparator extends ViewerComparator {

	private static final RefComparator instance = new RefComparator();

	public static RefComparator get() {
		return instance;
	}

	@Override
	public int category(Object obj) {
		if (!(obj instanceof Ref ref))
			return -1;
		if (ref.getType() == null)
			return -1;
		return switch (ref.getType()) {
			case MODEL -> 0;
			case PROCESS -> 1;
			case FLOW -> 2;
			case IMPACT_METHOD -> 3;
			case SOURCE -> 4;
			case CONTACT -> 5;
			case FLOW_PROPERTY -> 6;
			case UNIT_GROUP -> 7;
			case EXTERNAL_FILE -> 8;
		};
	}
}
