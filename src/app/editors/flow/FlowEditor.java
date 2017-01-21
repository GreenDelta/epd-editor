package app.editors.flow;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.util.Flows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.editors.BaseEditor;
import app.editors.DependencyPage;
import app.editors.Editors;
import app.editors.RefEditorInput;
import app.editors.XmlPage;
import epd.io.conversion.FlowExtensions;
import epd.model.EpdProduct;
import epd.model.Version;
import epd.model.Xml;
import epd.util.Strings;

public class FlowEditor extends BaseEditor {

	private static final String ID = "flow.editor";

	public EpdProduct product;

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
			Flow flow = App.store.get(Flow.class, in.ref.uuid);
			product = FlowExtensions.read(flow);
		} catch (Exception e) {
			throw new PartInitException("Failed to open flow editor", e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			updateVersion();
			FlowExtensions.write(product);
			Flow flow = product.flow;
			App.store.put(flow);
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
		Publication pub = Flows.publication(product.flow);
		Version v = Version.fromString(pub.version);
		v.incUpdate();
		pub.version = v.toString();
		Flows.dataEntry(product.flow).timeStamp = Xml.now();
	}

	@Override
	protected void addPages() {
		try {
			addPage(new FlowPage(this));
			addPage(new DependencyPage(this, product.flow));
			addPage(new XmlPage(this, product.flow));
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to add page", e);
		}
	}
}
