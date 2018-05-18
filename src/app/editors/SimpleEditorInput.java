package app.editors;

import java.util.Objects;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class SimpleEditorInput implements IEditorInput {

	private final String name;
	public final String id;

	public SimpleEditorInput(String name) {
		this(name, null);
	}

	public SimpleEditorInput(String name, String id) {
		this.name = name;
		this.id = id;
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
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof SimpleEditorInput))
			return false;
		SimpleEditorInput other = (SimpleEditorInput) obj;
		if (id != null || other.id != null)
			Objects.equals(this.id, other.id);
		return Objects.equals(this.getName(), other.getName());
	}

	@Override
	public int hashCode() {
		if (name == null)
			return super.hashCode();
		if (id == null)
			return Objects.hash(name);
		return Objects.hash(id);
	}

}
