package app.editors.flowproperty;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.openlca.ilcd.commons.DataEntry;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.flowproperties.AdminInfo;
import org.openlca.ilcd.flowproperties.DataSetInfo;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flowproperties.FlowPropertyInfo;
import org.openlca.ilcd.flowproperties.QuantitativeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.editors.BaseEditor;
import app.editors.Editors;
import app.editors.RefCheck;
import app.editors.RefEditorInput;
import app.store.Data;

public class FlowPropertyEditor extends BaseEditor {

	private static final String ID = "flowproperty.editor";

	public FlowProperty property;

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
			property = App.store.get(FlowProperty.class, in.ref.uuid);
			RefCheck.on(property);
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
			Data.updateVersion(property);
			Data.save(property);
			for (Runnable handler : saveHandlers) {
				handler.run();
			}
			dirty = false;
			editorDirtyStateChanged();
			Editors.setTabTitle(property, this);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to save contact data set", e);
		}
	}

	@Override
	protected void addPages() {
		try {
			addPage(new FlowPropertyPage(this));
			Editors.addInfoPages(this, property);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to add page", e);
		}
	}
}
