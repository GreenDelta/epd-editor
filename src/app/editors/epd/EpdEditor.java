package app.editors.epd;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.processes.AdminInfo;
import org.openlca.ilcd.processes.DataSetInfo;
import org.openlca.ilcd.processes.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.M;
import app.Store;
import app.editors.Editors;
import app.editors.RefEditorInput;
import app.util.UI;
import epd.model.DeclaredProduct;
import epd.model.EpdDataSet;
import epd.model.Version;
import epd.model.Xml;
import epd.util.Strings;

public class EpdEditor extends FormEditor {

	private static final String ID = "epd.editor";

	private Logger log = LoggerFactory.getLogger(getClass());

	private EpdDataSet dataSet;
	private boolean dirty;
	private boolean productChanged;

	private List<Runnable> saveHandlers = new ArrayList<>();

	public static void open(Ref ref) {
		RefEditorInput input = new RefEditorInput(ref);
		Editors.open(input, ID);
	}

	@Override
	public void init(IEditorSite s, IEditorInput input)
			throws PartInitException {
		super.init(s, input);
		setPartName(Strings.cut(input.getName(), 75));
		try {
			RefEditorInput in = (RefEditorInput) input;
			dataSet = Store.openEPD(in.ref);
			dataSet.structs();
		} catch (Exception e) {
			throw new PartInitException(
					"Failed to open editor: no correct input", e);
		}
	}

	public EpdDataSet getDataSet() {
		return dataSet;
	}

	public void setDirty(boolean b) {
		if (dirty != b) {
			dirty = b;
			editorDirtyStateChanged();
		}
	}

	public void setProductChanged() {
		this.productChanged = true;
		setDirty(true);
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	protected void addPages() {
		try {
			addPage(new InfoPage(this));
			addPage(new ModelingPage(this));
			addPage(new AdminPage(this));
			// addPage(new ModulePage(this));
			// addPage(new DeclaredProductPage(this));
		} catch (Exception e) {
			log.error("failed to add editor page", e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			updateVersion();
			Store.saveEPD(dataSet);
			for (Runnable handler : saveHandlers) {
				handler.run();
			}
			setDirty(false);
			productChanged = false;
			// TODO: StartPageView.refresh(); -> update navigation
		} catch (Exception e) {
			log.error("failed to save EPD data set", e);
		}
	}

	private void updateVersion() {
		AdminInfo info = dataSet.adminInfo;
		Version v = Version.fromString(info.publication.version);
		v.incUpdate();
		info.publication.version = v.toString();
		info.dataEntry.timeStamp = Xml.now();
		if (!productChanged || dataSet.declaredProduct == null)
			return;
		DeclaredProduct product = dataSet.declaredProduct;
		v = Version.fromString(product.version);
		v.incUpdate();
		product.version = v.toString();
	}

	public void onSaved(Runnable handler) {
		saveHandlers.add(handler);
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
			clone.structs();
			DataSetInfo dsInfo = clone.processInfo.dataSetInfo;
			LangString.set(dsInfo.name.name, name, App.lang);
			dsInfo.uuid = UUID.randomUUID().toString();
			AdminInfo info = clone.adminInfo;
			info.publication.version = Version.asString(0);
			info.dataEntry.timeStamp = Xml.now();
			Process process = Store.saveEPD(clone);
			EpdEditor.open(Ref.of(process));
			// TODO StartPageView.refresh();
		} catch (Exception e) {
			log.error("failed to save EPD as new data set", e);
		}
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

}
