package app.navi;

import org.eclipse.swt.graphics.Image;
import org.openlca.ilcd.epd.EpdProfile;

import app.rcp.Icon;
import epd.util.Strings;

public class ProfileElement extends NavigationElement<EpdProfile> {

	private final ProfileFolder parent;

	public ProfileElement(ProfileFolder parent, EpdProfile profile) {
		this.parent = parent;
		this.content = profile;
	}

	@Override
	public NavigationElement<?> getParent() {
		return parent;
	}

	@Override
	public int compareTo(NavigationElement<?> elem) {
		if (!(elem instanceof ProfileElement other))
			return 1;
		if (this.content == null || other.content == null)
			return 0;
		return Strings.compare(this.content.getName(), other.content.getName());
	}

	@Override
	public String getLabel() {
		if (this.content == null)
			return "?";
		return this.content.getName();
	}

	@Override
	public Image getImage() {
		return Icon.SETTINGS.img();
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
		if (!(obj instanceof ProfileElement other))
			return false;
		var p1 = this.content;
		var p2 = other.content;
		if (p1 == null && p2 == null)
			return true;
		if (p1 == null || p2 == null)
			return false;
		return Strings.nullOrEqual(p1.getName(), p2.getName());
	}
}
