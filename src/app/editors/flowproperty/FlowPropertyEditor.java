package app.editors.flowproperty;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.openlca.ilcd.commons.AdminInfo;
import org.openlca.ilcd.commons.DataEntry;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.flowproperties.DataSetInfo;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flowproperties.FlowPropertyInfo;
import org.openlca.ilcd.flowproperties.QuantitativeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.editors.DataSetEditor;
import app.editors.Editors;
import app.editors.RefEditorInput;
import epd.model.Version;
import epd.model.Xml;
import epd.util.Strings;

public class FlowPropertyEditor extends DataSetEditor {

	private static final String ID = "flowproperty.editor";

	FlowProperty property;

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
			property = App.store.get(FlowProperty.class, in.ref.uuid);
			initStructs();
		} catch (Exception e) {
			throw new PartInitException(
					"Failed to open editor: no correct input", e);
		}
	}

	private void initStructs() {
		if (property == null)
			property = new FlowProperty();
		if (property.adminInfo == null)
			property.adminInfo = new AdminInfo();
		if (property.adminInfo.dataEntry == null)
			property.adminInfo.dataEntry = new DataEntry();
		if (property.adminInfo.publication == null)
			property.adminInfo.publication = new Publication();
		if (property.flowPropertyInfo == null)
			property.flowPropertyInfo = new FlowPropertyInfo();
		if (property.flowPropertyInfo.dataSetInfo == null)
			property.flowPropertyInfo.dataSetInfo = new DataSetInfo();
		if (property.flowPropertyInfo.quantitativeReference == null)
			property.flowPropertyInfo.quantitativeReference = new QuantitativeReference();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			updateVersion();
			App.store.put(property, property.getUUID());
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
		AdminInfo info = property.adminInfo;
		Version v = Version.fromString(info.publication.version);
		v.incUpdate();
		info.publication.version = v.toString();
		info.dataEntry.timeStamp = Xml.now();
	}

	@Override
	protected void addPages() {
		try {
			addPage(new FlowPropertyPage(this));
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to add page", e);
		}
	}
}
