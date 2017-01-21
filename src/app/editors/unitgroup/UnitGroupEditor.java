package app.editors.unitgroup;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.openlca.ilcd.commons.DataEntry;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.util.UnitGroups;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.editors.BaseEditor;
import app.editors.DependencyPage;
import app.editors.Editors;
import app.editors.RefEditorInput;
import app.editors.XmlPage;
import epd.model.Version;
import epd.model.Xml;
import epd.util.Strings;

public class UnitGroupEditor extends BaseEditor {

	private static final String ID = "unitgroup.editor";

	public UnitGroup unitGroup;

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
		} catch (Exception e) {
			throw new PartInitException(
					"Failed to open editor: no correct input", e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			updateVersion();
			App.store.put(unitGroup);
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
		Publication pub = UnitGroups.publication(unitGroup);
		DataEntry entry = UnitGroups.dataEntry(unitGroup);
		Version v = Version.fromString(pub.version);
		v.incUpdate();
		pub.version = v.toString();
		entry.timeStamp = Xml.now();
	}

	@Override
	protected void addPages() {
		try {
			addPage(new InfoPage(this));
			addPage(new DependencyPage(this, unitGroup));
			addPage(new XmlPage(this, unitGroup));
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to add page", e);
		}
	}
}
