package app.editors.profiles;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

import app.App;
import app.M;
import app.StatusView;
import app.navi.NaviSync;
import app.rcp.Texts;
import app.store.RefDataSync;
import app.util.Controls;
import app.util.MsgBox;
import app.util.UI;
import epd.profiles.EpdProfile;
import epd.util.Strings;

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
		var tk = mform.getToolkit();
		var form = UI.formHeader(mform, profile.name);
		var body = UI.formBody(form, mform.getToolkit());
		infoSection(tk, body);
		new IndicatorTable(profile).render(body, tk);
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
		Text urlText = UI.formText(comp, tk, M.ReferenceDataURL);
		Texts.set(urlText, profile.referenceDataUrl);
		urlText.addModifyListener(e -> {
			profile.referenceDataUrl = urlText.getText();
			editor.setDirty();
		});

		var button = tk.createButton(comp, M.DownloadDataSets, SWT.NONE);
		Controls.onSelect(button, e -> syncRefData(urlText.getText()));
	}

	private void syncRefData(String url) {
		if (Strings.nullOrEmpty(url)) {
			MsgBox.error("No URL given.");
			return;
		}
		RefDataSync sync = new RefDataSync(url);
		App.run("Synchronize reference data ...", sync, () -> {
			if (!sync.errors.isEmpty()) {
				MsgBox.error(sync.errors.get(0));
			} else if (sync.stats.isEmpty()) {
				MsgBox.info(M.NoDataFoundOnServer);
			}
			// show statistics + update navi, even if there was an error
			if (!sync.stats.isEmpty()) {
				new NaviSync(App.index()).run();
				StatusView.open(M.DataSets + " @" + url, sync.stats);
			}
		});
	}
}
