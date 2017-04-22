package app.editors.settings;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.AppSettings;
import app.editors.BaseEditor;
import app.editors.Editors;
import app.editors.SimpleEditorInput;
import app.util.Controls;
import app.util.UI;

public class SettingsPage extends BaseEditor {

	private AppSettings settings = App.settings().clone();

	public static void open() {
		SimpleEditorInput input = new SimpleEditorInput("app.Settings");
		Editors.open(input, "app.Settings");
	}

	@Override
	protected void addPages() {
		try {
			addPage(new Page());
		} catch (PartInitException e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to add page", e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		App.settings().setValues(settings);
		App.settings().save();
		dirty = false;
		editorDirtyStateChanged();
	}

	private class Page extends FormPage {

		private FormToolkit toolkit;

		public Page() {
			super(SettingsPage.this, "SettingsPage", "#Settings");
		}

		@Override
		protected void createFormContent(IManagedForm mform) {
			toolkit = mform.getToolkit();
			ScrolledForm form = UI.formHeader(mform, "#Settings");
			Composite body = UI.formBody(form, mform.getToolkit());
			Composite comp = UI.formSection(body, toolkit, "#Data sets");
			LangCombo langCombo = new LangCombo(settings.lang);
			langCombo.render(comp, toolkit);
			langCombo.onChange(lang -> {
				settings.lang = lang;
				SettingsPage.this.setDirty();
			});
			xmlCheck(comp);
			dependencyCheck(comp);
			form.reflow(true);
		}

		private void dependencyCheck(Composite comp) {
			Button depCheck = UI.formCheckBox(comp, toolkit,
					"#Show dependencies in editors");
			depCheck.setSelection(settings.showDataSetDependencies);
			Controls.onSelect(depCheck, e -> {
				settings.showDataSetDependencies = depCheck
						.getSelection();
				SettingsPage.this.setDirty();
			});
		}

		private void xmlCheck(Composite comp) {
			Button xmlCheck = UI.formCheckBox(comp, toolkit,
					"#Show XML pages in editors");
			xmlCheck.setSelection(settings.showDataSetXML);
			Controls.onSelect(xmlCheck, e -> {
				settings.showDataSetXML = xmlCheck.getSelection();
				SettingsPage.this.setDirty();
			});
		}
	}
}
