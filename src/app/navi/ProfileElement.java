package app.navi;

import org.eclipse.swt.graphics.Image;
import org.openlca.ilcd.epd.EpdProfile;

import app.rcp.Icon;
import epd.util.Strings;

public class ProfileElement extends NavigationElement {

	private final ProfileFolder parent;
	private final EpdProfile profile;

	public ProfileElement(ProfileFolder parent, EpdProfile profile) {
		this.parent = parent;
		this.profile = profile;
	}

	public EpdProfile profile() {
		return profile;
	}

	@Override
	public NavigationElement getParent() {
		return parent;
	}

	@Override
	public int compareTo(NavigationElement elem) {
		if (!(elem instanceof ProfileElement other))
			return 1;
		if (this.profile == null || other.profile == null)
			return 0;
		return Strings.compare(this.profile.getName(), other.profile.getName());
	}

	@Override
	public String getLabel() {
		return this.profile != null
				? profile.getName()
				: "?";
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
		var p1 = this.profile;
		var p2 = other.profile;
		if (p1 == null && p2 == null)
			return true;
		if (p1 == null || p2 == null)
			return false;
		return Strings.nullOrEqual(p1.getName(), p2.getName());
	}
}
