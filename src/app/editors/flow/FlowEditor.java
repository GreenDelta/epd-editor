package app.editors.flow;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.flows.AdminInfo;
import org.openlca.ilcd.flows.DataEntry;
import org.openlca.ilcd.flows.DataSetInfo;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.FlowInfo;
import org.openlca.ilcd.flows.QuantitativeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.editors.Editors;
import app.editors.IEditor;
import app.editors.RefEditorInput;
import epd.model.Version;
import epd.model.Xml;
import epd.util.Strings;

public class FlowEditor extends FormEditor implements IEditor {

	private static final String ID = "flow.editor";

	Flow flow;

	private boolean dirty;
	private List<Runnable> saveHandlers = new ArrayList<>();

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
		setPartName(Strings.cut(input.getName(), 75));
		try {
			RefEditorInput in = (RefEditorInput) input;
			flow = App.store.get(Flow.class, in.ref.uuid);
			initStructs();
		} catch (Exception e) {
			throw new PartInitException(
					"Failed to open editor: no correct input", e);
		}
	}

	private void initStructs() {
		if (flow == null)
			flow = new Flow();
		if (flow.adminInfo == null)
			flow.adminInfo = new AdminInfo();
		if (flow.adminInfo.dataEntry == null)
			flow.adminInfo.dataEntry = new DataEntry();
		if (flow.adminInfo.publication == null)
			flow.adminInfo.publication = new Publication();
		if (flow.flowInfo == null)
			flow.flowInfo = new FlowInfo();
		if (flow.flowInfo.dataSetInfo == null)
			flow.flowInfo.dataSetInfo = new DataSetInfo();
		if (flow.flowInfo.quantitativeReference == null)
			flow.flowInfo.quantitativeReference = new QuantitativeReference();
	}

	@Override
	public void setDirty() {
		if (!dirty) {
			dirty = true;
			editorDirtyStateChanged();
		}
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			updateVersion();
			App.store.put(flow, flow.getUUID());
			// TODO: navigation refresh
			for (Runnable handler : saveHandlers) {
				handler.run();
			}
			dirty = false;
			editorDirtyStateChanged();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to save flow data set", e);
		}
	}

	private void updateVersion() {
		AdminInfo info = flow.adminInfo;
		Version v = Version.fromString(info.publication.version);
		v.incUpdate();
		info.publication.version = v.toString();
		info.dataEntry.timeStamp = Xml.now();
	}

	public void onSaved(Runnable handler) {
		saveHandlers.add(handler);
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	protected void addPages() {
		try {
			addPage(new FlowPage(this));
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to add page", e);
		}
	}
}
