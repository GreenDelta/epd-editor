package app.navi;

import org.eclipse.swt.graphics.Image;

import app.rcp.Icon;
import epd.model.EpdProfile;
import epd.util.Strings;

public class ProfileElement extends NavigationElement {

	private final ProfileFolder parent;
	private final EpdProfile profile;

	public ProfileElement(ProfileFolder parent, EpdProfile profile) {
		this.parent = parent;
		this.profile = profile;
	}

	@Override
	public NavigationElement getParent() {
		return parent;
	}

	@Override
	public int compareTo(NavigationElement elem) {
		if (!(elem instanceof ProfileElement))
			return 1;
		ProfileElement other = (ProfileElement) elem;
		if (this.profile == null || other.profile == null)
			return 0;
		return Strings.compare(this.profile.name, other.profile.name);
	}

	@Override
	public String getLabel() {
		if (this.profile == null)
			return "?";
		return this.profile.name;
	}

	@Override
	public Image getImage() {
		return Icon.SETTINGS.img();
	}

	@Override
	public void update() {
	}
}
