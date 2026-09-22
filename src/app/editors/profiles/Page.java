package app.editors.profiles;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.commons.Strings;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.epd.EpdProfile;

import app.App;
import app.M;
import app.StatusView;
import app.editors.refs.RefLink;
import app.navi.NaviSync;
import app.rcp.Texts;
import app.store.RefDataSync;
import app.util.Controls;
import app.util.MsgBox;
import app.util.UI;

class Page extends FormPage {

	private final ProfileEditor editor;
	private final EpdProfile profile;
	private final boolean readOnly;

	Page(ProfileEditor editor, EpdProfile profile) {
		super(editor, "Page", profile.getName());
		this.editor = editor;
		this.profile = profile;
		this.readOnly = editor.isReadOnly();
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		var tk = mform.getToolkit();
		var form = UI.formHeader(mform, profile.getName());
		var body = UI.formBody(form, mform.getToolkit());
		infoSection(tk, body);
		new IndicatorTable(editor, profile).render(body, tk);
		ModuleTable.of(editor, profile).render(body, tk);
		form.reflow(true);
	}

	private void infoSection(FormToolkit tk, Composite body) {
		var comp = UI.formSection(body, tk, M.GeneralInformation);

		// built-in profiles cannot be modified
		if (readOnly) {
			var notice = UI.formLabel(comp, tk, M.BuiltInProfileNotice);
			UI.applyItalicFont(notice);
			UI.stretchNone(notice).horizontalSpan = 2;
		}

		// name
		var nameText = UI.formText(comp, tk, M.Name);
		Texts.set(nameText, profile.getName()).setEditable(!readOnly);
		if (!readOnly) {
			nameText.addModifyListener(_ -> {
				profile.withName(nameText.getText());
				editor.setDirty();
			});
		}

		// description
		var descrText = UI.formMultiText(comp, tk, M.Description);
		Texts.set(descrText, profile.getDescription()).setEditable(!readOnly);
		if (!readOnly) {
			descrText.addModifyListener(_ -> {
				profile.withDescription(descrText.getText());
				editor.setDirty();
			});
		}

		UI.formLabel(comp, tk, M.ComplianceSystem);
		var link = new RefLink(comp, tk, DataSetType.SOURCE);
		link.setRef(profile.getComplianceSystem());
		if (!readOnly) {
			link.onChange(ref -> {
				profile.withComplianceSystem(ref);
				editor.setDirty();
			});
		}

		// reference data URL
		var urlText = UI.formText(comp, tk, M.ReferenceDataURL);
		Texts.set(urlText, profile.getDataUrl()).setEditable(!readOnly);
		if (!readOnly) {
			urlText.addModifyListener(_ -> {
				profile.withDataUrl(urlText.getText());
				editor.setDirty();
			});
		}
		UI.filler(comp);
		var button = tk.createButton(comp, M.DownloadDataSets, SWT.NONE);
		Controls.onSelect(button, _ -> syncRefData(urlText.getText()));
	}

	private void syncRefData(String url) {
		if (Strings.isBlank(url)) {
			MsgBox.error("No URL given.");
			return;
		}
		RefDataSync sync = new RefDataSync(url);
		App.run("Synchronize reference data ...", sync, () -> {
			if (!sync.errors.isEmpty()) {
				MsgBox.error(sync.errors.getFirst());
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
