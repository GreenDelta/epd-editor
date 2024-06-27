package app.navi;

import java.io.File;
import java.util.Objects;

import org.eclipse.swt.graphics.Image;

import app.rcp.Icon;
import epd.util.Strings;

public class FileElement extends NavigationElement {

	private final FolderElement parent;
	private final File file;

	public FileElement(FolderElement parent, File file) {
		this.file = file;
		this.parent = parent;
	}

	public File file() {
		return file;
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

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof FileElement other))
			return false;
		return Objects.equals(this.file, other.file);
	}

}
