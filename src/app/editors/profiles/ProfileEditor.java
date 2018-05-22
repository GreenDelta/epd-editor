package app.editors.profiles;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.editors.BaseEditor;
import app.editors.Editors;
import app.editors.SimpleEditorInput;
import app.store.EpdProfiles;
import epd.model.EpdProfile;

public class ProfileEditor extends BaseEditor {

	private Logger log = LoggerFactory.getLogger(getClass());
	private EpdProfile profile;

	public static void open(EpdProfile profile) {
		if (profile == null || profile.id == null)
			return;
		Editors.open(
				new SimpleEditorInput(profile.name, profile.id),
				"epd.profile.editor");
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		Editors.setTabTitle(input, this);
		SimpleEditorInput in = (SimpleEditorInput) input;
		profile = EpdProfiles.get(in.id);
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
	public void doSave(IProgressMonitor monitor) {
	}

}
