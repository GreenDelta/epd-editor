package app.editors.unitgroup;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.openlca.ilcd.commons.AdminInfo;
import org.openlca.ilcd.commons.DataEntry;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.units.DataSetInfo;
import org.openlca.ilcd.units.QuantitativeReference;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.units.UnitGroupInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.editors.BaseEditor;
import app.editors.DependencyPage;
import app.editors.Editors;
import app.editors.RefEditorInput;
import epd.model.Version;
import epd.model.Xml;
import epd.util.Strings;

public class UnitGroupEditor extends BaseEditor {

	private static final String ID = "unitgroup.editor";

	UnitGroup unitGroup;

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
			unitGroup = App.store.get(UnitGroup.class, in.ref.uuid);
			initStructs();
		} catch (Exception e) {
			throw new PartInitException(
					"Failed to open editor: no correct input", e);
		}
	}

	private void initStructs() {
		if (unitGroup == null)
			unitGroup = new UnitGroup();
		if (unitGroup.adminInfo == null)
			unitGroup.adminInfo = new AdminInfo();
		if (unitGroup.adminInfo.dataEntry == null)
			unitGroup.adminInfo.dataEntry = new DataEntry();
		if (unitGroup.adminInfo.publication == null)
			unitGroup.adminInfo.publication = new Publication();
		if (unitGroup.unitGroupInfo == null)
			unitGroup.unitGroupInfo = new UnitGroupInfo();
		if (unitGroup.unitGroupInfo.dataSetInfo == null)
			unitGroup.unitGroupInfo.dataSetInfo = new DataSetInfo();
		if (unitGroup.unitGroupInfo.quantitativeReference == null)
			unitGroup.unitGroupInfo.quantitativeReference = new QuantitativeReference();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			updateVersion();
			App.store.put(unitGroup, unitGroup.getUUID());
			// TODO: navigation refresh
			for (Runnable handler : saveHandlers) {
				handler.run();
			}
			dirty = false;
			editorDirtyStateChanged();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to save contact data set");
		}
	}

	private void updateVersion() {
		AdminInfo info = unitGroup.adminInfo;
		Version v = Version.fromString(info.publication.version);
		v.incUpdate();
		info.publication.version = v.toString();
		info.dataEntry.timeStamp = Xml.now();
	}

	@Override
	protected void addPages() {
		try {
			addPage(new UnitGroupPage(this));
			addPage(new DependencyPage(this, unitGroup));
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to add page", e);
		}
	}
}
