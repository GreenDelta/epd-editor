package app.editors.settings;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.editors.BaseEditor;
import app.editors.Editors;
import app.editors.SimpleEditorInput;
import app.util.UI;

public class SettingsPage extends BaseEditor {

	private String dataSetLang = App.lang();

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
		App.settings().lang = dataSetLang;
		App.settings().save();
		dirty = false;
		editorDirtyStateChanged();
	}

	private class Page extends FormPage {

		public Page() {
			super(SettingsPage.this, "SettingsPage", "#Settings");
		}

		@Override
		protected void createFormContent(IManagedForm mform) {
			FormToolkit tk = mform.getToolkit();
			ScrolledForm form = UI.formHeader(mform, "#Settings");
			Composite body = UI.formBody(form, mform.getToolkit());
			Composite comp = UI.formSection(body, tk, "#Data sets");

			LangCombo langCombo = new LangCombo(dataSetLang);
			langCombo.render(comp, tk);
			langCombo.onChange(lang -> {
				dataSetLang = lang;
				SettingsPage.this.setDirty();
			});

			form.reflow(true);
		}

	}

}
