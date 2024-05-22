package app.editors.settings;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.AppSettings;
import app.M;
import app.editors.BaseEditor;
import app.editors.Editors;
import app.editors.SimpleEditorInput;
import app.navi.Navigator;
import app.rcp.IniFile;
import app.util.UI;
import epd.util.Strings;

public class SettingsPage extends BaseEditor {

	AppSettings settings = App.settings().copy();
	IniFile ini = IniFile.read();
	private final IniFile originalIni = ini.copy();

	public static void open() {
		SimpleEditorInput input = new SimpleEditorInput("app.Settings");
		Editors.open(input, "app.Settings");
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		setPartName(M.Settings);
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
		boolean langChange = !Strings.nullOrEqual(
				App.settings().lang, settings.lang);
		App.settings().setValues(settings);
		App.settings().save(App.getWorkspace());
		dirty = false;
		if (!ini.equals(originalIni))
			ini.write();
		editorDirtyStateChanged();
		if (langChange) {
			Navigator.refreshViewer();
		}
	}

	private class Page extends FormPage {

		public Page() {
			super(SettingsPage.this, "SettingsPage", M.Settings);
		}

		@Override
		protected void createFormContent(IManagedForm mform) {
			FormToolkit tk = mform.getToolkit();
			ScrolledForm form = UI.formHeader(mform, M.Settings);
			Composite body = UI.formBody(form, mform.getToolkit());
			new DataSetSection(SettingsPage.this).render(body, tk);
			new ValidationSection(SettingsPage.this).render(body, tk);
			new AppSection(SettingsPage.this).render(body, tk);
			form.reflow(true);
		}
	}
}
