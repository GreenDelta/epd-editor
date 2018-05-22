package app.editors.profiles;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import app.util.UI;
import epd.model.EpdProfile;

class Page extends FormPage {

	private final EpdProfile profile;

	Page(ProfileEditor editor, EpdProfile profile) {
		super(editor, "Page", profile.name);
		this.profile = profile;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		FormToolkit tk = mform.getToolkit();
		ScrolledForm form = UI.formHeader(mform, profile.name);
		Composite body = UI.formBody(form, mform.getToolkit());
		IndicatorTable.of(profile).render(body, tk);
		ModuleTable.of(profile).render(body, tk);
		form.reflow(true);
	}

}
