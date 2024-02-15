package app.navi;

import app.rcp.Icon;
import epd.util.Strings;
import org.eclipse.swt.graphics.Image;

import java.io.File;

public class FileElement extends NavigationElement {

	public final File file;
	private final FolderElement parent;

	public FileElement(FolderElement parent, File file) {
		this.file = file;
		this.parent = parent;
	}

	@Override
	public NavigationElement getParent() {
		return parent;
	}

	@Override
	public int compareTo(NavigationElement e) {
		if (!(e instanceof FileElement other))
			return 1;
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

	public FolderType getType() {
		return parent == null ? null : parent.type;
	}

}
