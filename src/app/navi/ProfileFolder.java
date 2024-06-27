package app.navi;

import org.eclipse.swt.graphics.Image;
import org.openlca.ilcd.epd.EpdProfiles;

import app.M;
import app.rcp.Icon;

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
		for (var p : EpdProfiles.getAll()) {
			childs.add(new ProfileElement(this, p));
		}
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ProfileFolder;
	}
}
