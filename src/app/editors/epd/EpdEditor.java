package app.editors.epd;

import java.util.UUID;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.processes.DataEntry;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.ProcessName;
import org.openlca.ilcd.processes.Publication;
import org.openlca.ilcd.util.Processes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.M;
import app.editors.BaseEditor;
import app.editors.Editors;
import app.editors.RefEditorInput;
import app.store.Data;
import app.util.UI;
import epd.model.EpdDataSet;
import epd.model.Version;
import epd.model.Xml;

public class EpdEditor extends BaseEditor {

	private static final String ID = "epd.editor";

	private Logger log = LoggerFactory.getLogger(getClass());

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
			addPage(new ModulePage(this));
			Editors.addInfoPages(this, dataSet.process);
		} catch (Exception e) {
			log.error("failed to add editor page", e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			updateVersion();
			Data.save(dataSet);
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

	private void updateVersion() {
		Publication pub = Processes.publication(dataSet.process);
		Version v = Version.fromString(pub.version);
		v.incUpdate();
		pub.version = v.toString();
		DataEntry entry = Processes.dataEntry(dataSet.process);
		entry.timeStamp = Xml.now();
	}

	@Override
	public void doSaveAs() {
		InputDialog d = new InputDialog(UI.shell(), M.SaveAs,
				"#Save EPD as a new data set with the following name:",
				M.EPD + " " + M.Name, null);
		if (d.open() != Window.OK)
			return;
		String name = d.getValue();
		try {
			EpdDataSet clone = dataSet.clone();
			Process p = clone.process;
			ProcessName cName = Processes.processName(p);
			LangString.set(cName.name, name, App.lang());
			Processes.dataSetInfo(p).uuid = UUID.randomUUID().toString();
			Processes.publication(p).version = Version.asString(0);
			Processes.dataEntry(p).timeStamp = Xml.now();
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
