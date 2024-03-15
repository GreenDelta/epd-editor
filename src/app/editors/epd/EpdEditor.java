package app.editors.epd;

import app.App;
import app.AppSettings;
import app.M;
import app.editors.BaseEditor;
import app.editors.Editors;
import app.editors.RefCheck;
import app.editors.RefEditorInput;
import app.editors.epd.contents.ContentDeclarationPage;
import app.editors.epd.qmeta.QMetaDataPage;
import app.editors.epd.results.ResultPage;
import app.store.Data;
import app.util.UI;
import epd.model.EpdDataSet;
import epd.model.Version;
import epd.model.Xml;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.ProcessName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class EpdEditor extends BaseEditor {

	private static final String ID = "epd.editor";

	private final Logger log = LoggerFactory.getLogger(getClass());

	public EpdDataSet dataSet;

	public static void open(Ref ref) {
		if (ref == null)
			return;
		RefEditorInput input = new RefEditorInput(ref);
		Editors.open(input, ID);
	}

	@Override
	public void init(IEditorSite s, IEditorInput input)
			throws PartInitException {
		super.init(s, input);
		Editors.setTabTitle(input, this);
		try {
			RefEditorInput in = (RefEditorInput) input;
			dataSet = Data.getEPD(in.ref);
			RefCheck.on(dataSet.process);
		} catch (Exception e) {
			throw new PartInitException(
					"Failed to open editor: no correct input", e);
		}
	}

	@Override
	protected void addPages() {
		try {
			addPage(new InfoPage(this));
			addPage(new ModelingPage(this));
			addPage(new AdminPage(this));
			addPage(new ResultPage(this));

			// pages that are configurable via the settings
			AppSettings settings = App.settings();
			if (settings.showContentDeclarations) {
				addPage(new ContentDeclarationPage(this));
			}
			if (settings.showQMetadata) {
				addPage(new QMetaDataPage(this));
			}
			Editors.addInfoPages(this, dataSet.process);
		} catch (Exception e) {
			log.error("failed to add editor page", e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			Data.updateVersion(dataSet);
			EpdDataSet ds = dataSet.copy();
			Data.save(ds);
			for (Runnable handler : saveHandlers) {
				handler.run();
			}
			dirty = false;
			editorDirtyStateChanged();
			Editors.setTabTitle(dataSet.process, this);
		} catch (Exception e) {
			log.error("failed to save EPD data set", e);
		}
	}

	@Override
	public void doSaveAs() {
		InputDialog d = new InputDialog(UI.shell(), M.SaveAs,
				M.SaveAs_Message + ": ",
				M.EPD + " " + M.Name, null);
		if (d.open() != Window.OK)
			return;
		String name = d.getValue();
		try {
			EpdDataSet clone = dataSet.copy();
			Process p = clone.process;
			ProcessName cName = p.withProcessInfo()
				.withDataSetInfo()
				.withProcessName();
			LangString.set(cName.withBaseName(), name, App.lang());
			p.withProcessInfo().withDataSetInfo().withUUID(UUID.randomUUID().toString());
			p.withAdminInfo().withPublication().withVersion(Version.asString(0));
			p.withAdminInfo().withDataEntry().withTimeStamp(Xml.now());
			Data.save(clone);
			EpdEditor.open(Ref.of(p));
		} catch (Exception e) {
			log.error("failed to save EPD as new data set", e);
		}
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

}
