package app.editors.connection;

import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.PartInitException;
import org.openlca.ilcd.io.SodaConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.editors.BaseEditor;
import app.editors.Editors;
import app.navi.Navigator;
import app.rcp.Icon;
import app.store.Connections;
import epd.util.Strings;

public class ConnectionEditor extends BaseEditor {

	SodaConnection con;

	public static void open(SodaConnection con) {
		if (con == null)
			return;
		Input input = new Input(con);
		Editors.open(input, "connection.editor");
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		setPartName(Strings.cut(input.getName(), 75));
		try {
			Input i = (Input) input;
			con = i.con;
		} catch (Exception e) {
			throw new PartInitException("Failed to open editor", e);
		}
	}

	@Override
	protected void addPages() {
		try {
			addPage(new ConnectionPage(this));
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to add page", e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		Connections.save(con);
		Navigator.refresh(); // TODO
		dirty = false;
		editorDirtyStateChanged();
		setPartName(Strings.cut(con.toString(), 75));
	}

	private static class Input implements IEditorInput {

		final SodaConnection con;

		Input(SodaConnection con) {
			this.con = con;
		}

		@Override
		public <T> T getAdapter(Class<T> adapter) {
			return null;
		}

		@Override
		public boolean exists() {
			return true;
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return Icon.CONNECTION.des();
		}

		@Override
		public String getName() {
			return con.toString();
		}

		@Override
		public IPersistableElement getPersistable() {
			return null;
		}

		@Override
		public String getToolTipText() {
			return con.toString();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (obj == this)
				return true;
			if (!(obj instanceof Input))
				return false;
			Input other = (Input) obj;
			return Objects.equals(this.con, other.con);
		}
	}

}
