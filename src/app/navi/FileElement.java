package app.navi;

import java.io.File;
import java.util.Objects;

import org.eclipse.swt.graphics.Image;

import app.rcp.Icon;
import epd.util.Strings;

public class FileElement extends NavigationElement<File> {

	private final FolderElement parent;

	public FileElement(FolderElement parent, File file) {
		this.content = file;
		this.parent = parent;
	}

	@Override
	public NavigationElement<?> getParent() {
		return parent;
	}

	@Override
	public int compareTo(NavigationElement<?> e) {
		if (!(e instanceof FileElement other))
			return 1;
		return Strings.compare(this.getLabel(), other.getLabel());
	}

	@Override
	public String getLabel() {
		var file = getContent();
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
		return Objects.equals(this.content, other.content);
	}

}
