package app.navi;

import java.io.File;

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

}
