package app.editors;

import java.util.Objects;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.openlca.ilcd.commons.Ref;

import app.App;
import app.rcp.Icon;
import epd.util.Strings;

public record RefEditorInput(Ref ref) implements IEditorInput {

	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return ref == null ? null : Icon.des(ref.getType());
	}

	@Override
	public String getName() {
		if (ref == null)
			return "??";
		var name = App.s(ref.getName());
		return Strings.nullOrEmpty(name) ? "??" : name;
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return getName();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof RefEditorInput other))
			return false;
		return Objects.equals(this.ref, other.ref);
	}
}
