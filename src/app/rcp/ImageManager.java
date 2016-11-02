package app.rcp;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

class ImageManager {

	private static ImageRegistry reg;

	public static void dispose() {
		if (reg != null) {
			reg.dispose();
			reg = null;
		}
	}

	public static Image getImage(Icon type) {
		if (reg == null)
			reg = new ImageRegistry();
		String name = type.getFileName();
		Image image = reg.get(name);
		if (image == null || image.isDisposed()) {
			ImageDescriptor d = Activator.imageDescriptorFromPlugin(
					"epd-editor", "icons/" + name);
			image = d.createImage();
			reg.put(name, image);
		}
		return image;
	}

	public static ImageDescriptor getImageDescriptor(Icon type) {
		if (reg == null)
			reg = new ImageRegistry();
		String name = type.getFileName();
		ImageDescriptor d = reg.getDescriptor(name);
		if (d != null)
			return d;
		d = Activator.imageDescriptorFromPlugin("epd-editor", "icons/" + name);
		reg.put(name, d);
		return d;
	}
}
