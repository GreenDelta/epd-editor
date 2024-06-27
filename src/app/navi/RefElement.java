package app.navi;

import java.util.Objects;

import org.eclipse.swt.graphics.Image;
import org.openlca.ilcd.commons.Ref;

import app.App;
import app.rcp.Icon;
import app.store.RefExt;
import epd.util.Strings;

public class RefElement extends NavigationElement<Ref> {

	private final NavigationElement<?> parent;

	public RefElement(NavigationElement<?> parent, Ref ref) {
		this.parent = parent;
		this.content = ref;
	}

	@Override
	public NavigationElement<?> getParent() {
		return parent;
	}

	@Override
	public int compareTo(NavigationElement<?> other) {
		if (!(other instanceof RefElement))
			return 1;
		return Strings.compare(this.getLabel(), other.getLabel());
	}

	@Override
	public String getLabel() {
		var ref = getContent();
		var name = App.s(ref.getName());
		var db = RefExt.getDatabase(ref);
		if (db.isPresent()) {
			name += " - " + db.get();
		}
		var refYear = RefExt.getReferenceYear(ref);
		if (refYear.isPresent()) {
			name += " - " + refYear.get();
		}
		return name;
	}

	@Override
	public Image getImage() {
		return Icon.img(getContent().getType());
	}

	@Override
	public void update() {
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof RefElement other))
			return false;
		return Objects.equals(this.content, other.content);
	}
}
