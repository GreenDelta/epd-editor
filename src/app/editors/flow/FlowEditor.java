package app.editors.flow;

import epd.model.MaterialPropertyValue;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.flows.Flow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.editors.BaseEditor;
import app.editors.Editors;
import app.editors.RefCheck;
import app.editors.RefEditorInput;
import app.store.Data;
import epd.io.conversion.FlowExtensions;
import epd.model.EpdProduct;

import java.util.ArrayList;
import java.util.List;

public class FlowEditor extends BaseEditor {

	private static final String ID = "flow.editor";

	public EpdProduct product;
	List<MaterialPropertyValue> materialProperties;

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
			var in = (RefEditorInput) input;
			var flow = App.store().get(Flow.class, in.ref.getUUID());
			RefCheck.on(flow);
			product = FlowExtensions.read(flow);
			materialProperties = new ArrayList<>(
				MaterialPropertyValue.readFrom(flow));
		} catch (Exception e) {
			throw new PartInitException("Failed to open flow editor", e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			Data.updateVersion(product.flow);
			MaterialPropertyValue.write(materialProperties, product.flow);
			Data.save(product);
			saveHandlers.forEach(Runnable::run);
			dirty = false;
			editorDirtyStateChanged();
			Editors.setTabTitle(product.flow, this);
			FlowUpdateCheck.with(product.flow);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to save flow data set", e);
		}
	}

	@Override
	protected void addPages() {
		try {
			addPage(new FlowPage(this));
			Editors.addInfoPages(this, product.flow);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to add page", e);
		}
	}
}
