package app.editors.profiles;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.commons.Strings;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.epd.EpdProfile;

import app.App;
import app.M;
import app.StatusView;
import app.editors.refs.RefSelectionDialog;
import app.navi.NaviSync;
import app.rcp.Texts;
import app.store.Profiles;
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
		Composite comp = UI.formSection(body, tk, M.GeneralInformation);
		UI.gridLayout(comp, 3);

		// built-in profiles cannot be modified
		if (readOnly) {
			var notice = UI.formLabel(comp, tk, M.BuiltInProfileNotice);
			UI.applyItalicFont(notice);
			if (notice.getLayoutData() instanceof GridData gd) {
				gd.horizontalSpan = 3;
			}
			UI.filler(comp);
			var duplicate = tk.createButton(comp, M.DuplicateProfile, SWT.NONE);
			Controls.onSelect(duplicate, _ -> duplicate());
			UI.filler(comp);
		}

		// name
		Text nameText = UI.formText(comp, tk, M.Name);
		Texts.set(nameText, profile.getName()).setEditable(!readOnly);
		if (readOnly) {
			UI.filler(comp);
		} else {
			nameText.addModifyListener(_ -> {
				profile.withName(nameText.getText());
				editor.setDirty();
			});
			UI.filler(comp);
		}

		// description
		Text descrText = UI.formMultiText(comp, tk, M.Description);
		Texts.set(descrText, profile.getDescription()).setEditable(!readOnly);
		if (readOnly) {
			UI.filler(comp);
		} else {
			descrText.addModifyListener(_ -> {
				profile.withDescription(descrText.getText());
				editor.setDirty();
			});
			UI.filler(comp);
		}

		// reference data URL
		Text urlText = UI.formText(comp, tk, M.ReferenceDataURL);
		Texts.set(urlText, profile.getDataUrl()).setEditable(!readOnly);
		if (readOnly) {
			UI.filler(comp);
		} else {
			urlText.addModifyListener(_ -> {
				profile.withDataUrl(urlText.getText());
				editor.setDirty();
			});
			UI.filler(comp);
		}

		complianceSystem(tk, comp);

		// download reference data sets
		UI.filler(comp);
		var button = tk.createButton(comp, M.DownloadDataSets, SWT.NONE);
		Controls.onSelect(button, _ -> syncRefData(urlText.getText()));
		UI.filler(comp);
	}

	private void complianceSystem(FormToolkit tk, Composite comp) {
		UI.formLabel(comp, tk, M.ComplianceSystem);
		var ref = profile.getComplianceSystem();
		var value = tk.createLabel(comp, ref != null ? App.s(ref) : M.None);
		UI.stretchX(value);
		if (readOnly) {
			UI.filler(comp);
			return;
		}
		var buttons = UI.formComposite(comp, tk);
		var select = tk.createButton(buttons, M.SelectDataSet, SWT.NONE);
		Controls.onSelect(select, _ -> {
			var selected = RefSelectionDialog.select(DataSetType.SOURCE);
			if (selected == null)
				return;
			profile.withComplianceSystem(selected);
			value.setText(App.s(selected));
			editor.setDirty();
		});
		var clear = tk.createButton(buttons, M.Remove, SWT.NONE);
		Controls.onSelect(clear, _ -> {
			profile.withComplianceSystem(null);
			value.setText(M.None);
			editor.setDirty();
		});
	}

	private void duplicate() {
		var copy = Profiles.copyOf(profile);
		if (copy == null) {
			MsgBox.error(M.FailedToSaveProfile);
			return;
		}
		ProfileEditor.open(copy);
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
