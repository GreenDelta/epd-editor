package app.navi;

import org.eclipse.swt.graphics.Image;

import app.M;
import app.rcp.Icon;
import app.store.EpdProfiles;
import epd.model.EpdProfile;

public class ProfileFolder extends NavigationElement {

	private final NavigationElement parent;

	public ProfileFolder(NavigationElement parent) {
		this.parent = parent;
	}

	@Override
	public NavigationElement getParent() {
		return parent;
	}

	@Override
	public int compareTo(NavigationElement other) {
		return 0;
	}

	@Override
	public String getLabel() {
		return M.EPDProfiles;
	}

	@Override
	public Image getImage() {
		return Icon.FOLDER.img();
	}

	@Override
	public void update() {
		if (childs == null)
			return;
		childs.clear();
		for (EpdProfile p : EpdProfiles.getAll()) {
			childs.add(new ProfileElement(this, p));
		}
	}
}
