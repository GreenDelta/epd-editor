package app.navi;

import app.rcp.Icon;
import epd.profiles.EpdProfile;
import epd.util.Strings;
import org.eclipse.swt.graphics.Image;

public class ProfileElement extends NavigationElement<EpdProfile> {

	private final ProfileFolder parent;

	public ProfileElement(ProfileFolder parent, EpdProfile profile) {
		this.parent = parent;
		this.content = profile;
	}

	@Override
	public NavigationElement getParent() {
		return parent;
	}

	@Override
	public int compareTo(NavigationElement elem) {
		if (!(elem instanceof ProfileElement other))
			return 1;
		if (this.content == null || other.content == null)
			return 0;
		return Strings.compare(this.content.name, other.content.name);
	}

	@Override
	public String getLabel() {
		if (this.content == null)
			return "?";
		return this.content.name;
	}

	@Override
	public Image getImage() {
		return Icon.SETTINGS.img();
	}

	@Override
	public void update() {
	}
}
