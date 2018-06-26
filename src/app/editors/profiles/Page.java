package app.editors.profiles;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import app.M;
import app.rcp.Texts;
import app.util.Controls;
import app.util.UI;
import epd.model.EpdProfile;

class Page extends FormPage {

	private final ProfileEditor editor;
	private final EpdProfile profile;

	Page(ProfileEditor editor, EpdProfile profile) {
		super(editor, "Page", profile.name);
		this.editor = editor;
		this.profile = profile;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		FormToolkit tk = mform.getToolkit();
		ScrolledForm form = UI.formHeader(mform, profile.name);
		Composite body = UI.formBody(form, mform.getToolkit());
		infoSection(tk, body);
		IndicatorTable.of(profile).render(body, tk);
		ModuleTable.of(profile).render(body, tk);
		form.reflow(true);
	}

	private void infoSection(FormToolkit tk, Composite body) {
		Composite comp = UI.formSection(body, tk, M.GeneralInformation);
		UI.gridLayout(comp, 3);
		// name
		Text nameText = UI.formText(comp, tk, M.Name);
		Texts.set(nameText, profile.name).setEditable(false);
		UI.filler(comp);
		// description
		Text descrText = UI.formMultiText(comp, tk, M.Description);
		Texts.set(descrText, profile.description).setEditable(false);
		UI.filler(comp);
		// reference data URL
		Text urlText = UI.formText(comp, tk, "#Reference data URL");
		Texts.set(urlText, profile.referenceDataUrl);
		urlText.addModifyListener(e -> {
			profile.referenceDataUrl = urlText.getText();
			editor.setDirty();
		});

		Button button = tk.createButton(comp, M.DownloadDataSets, SWT.NONE);
		Controls.onSelect(button, e -> {
			// TODO: download reference data
		});
	}

}
