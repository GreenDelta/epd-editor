package app.editors;

import java.util.Objects;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;

import app.App;
import app.rcp.Icon;

public class RefEditorInput implements IEditorInput {

	public final Ref ref;

	public RefEditorInput(Ref ref) {
		this.ref = ref;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return ref == null ? null : Icon.des(ref.type);
	}

	@Override
	public String getName() {
		if (ref == null)
			return "??";
		String name = LangString.getFirst(ref.name, App.lang());
		return name == null ? "??" : name;
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
		if (!(obj instanceof RefEditorInput))
			return false;
		RefEditorInput other = (RefEditorInput) obj;
		return Objects.equals(this.ref, other.ref);
	}
}
