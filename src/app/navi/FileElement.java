package app.navi;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import app.rcp.Icon;
import epd.util.Strings;

public class FileElement implements NavigationElement {

	public final File file;
	private final NavigationElement parent;

	public FileElement(NavigationElement parent, File file) {
		this.file = file;
		this.parent = parent;
	}

	@Override
	public List<NavigationElement> getChilds() {
		return Collections.emptyList();
	}

	@Override
	public NavigationElement getParent() {
		return parent;
	}

	@Override
	public int compareTo(NavigationElement e) {
		if (!(e instanceof FileElement))
			return 1;
		FileElement other = (FileElement) e;
		return Strings.compare(this.getLabel(), other.getLabel());
	}

	@Override
	public String getLabel() {
		return file == null ? "?" : file.getName();
	}

	@Override
	public Image getImage() {
		return Icon.DOCUMENT.img();
	}

	@Override
	public void update() {
	}

}
