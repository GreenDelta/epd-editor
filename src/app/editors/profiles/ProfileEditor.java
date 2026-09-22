package app.editors.profiles;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.openlca.ilcd.epd.EpdProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.M;
import app.editors.BaseEditor;
import app.editors.Editors;
import app.editors.SimpleEditorInput;
import app.store.Profiles;
import app.util.MsgBox;

public class ProfileEditor extends BaseEditor {

	private static final String ID = "epd.profile.editor";

	private final Logger log = LoggerFactory.getLogger(getClass());

	private EpdProfile profile;
	private boolean readOnly;

	public static void open(EpdProfile profile) {
		if (profile == null || profile.getId() == null)
			return;
		var input = new SimpleEditorInput(profile.getName(), profile.getId());
		Editors.open(input, ID);
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		Editors.setTabTitle(input, this);
		if (input instanceof SimpleEditorInput in) {
			profile = Profiles.get(in.id);
		}
		readOnly = Profiles.isBuiltIn(profile);
	}

	/// Indicates whether the edited profile is a built-in profile. Built-in
	/// profiles are read-only; they can only be duplicated.
	boolean isReadOnly() {
		return readOnly;
	}

	@Override
	protected void addPages() {
		try {
			if (profile != null)
				addPage(new Page(this, profile));
		} catch (Exception e) {
			log.error("failed to add editor page", e);
		}
	}

	@Override
	public void setDirty() {
		if (!readOnly) {
			super.setDirty();
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		if (readOnly || profile == null)
			return;
		if (!Profiles.save(profile)) {
			MsgBox.error(M.FailedToSaveProfile);
			return;
		}
		dirty = false;
		editorDirtyStateChanged();
		setPartName(profile.getName() != null ? profile.getName() : "");
	}
}
