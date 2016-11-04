package app.editors.epd;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.openlca.app.M;
import org.openlca.app.util.Editors;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.processes.AdminInfo;
import org.openlca.ilcd.processes.DataSetInfo;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greendelta.olca.plugins.oekobaudat.rcp.ui.editor.product.DeclaredProductPage;
import com.greendelta.olca.plugins.oekobaudat.rcp.ui.start.StartPageView;

import app.util.UI;
import epd.io.EpdStore;
import epd.model.DeclaredProduct;
import epd.model.EpdDataSet;
import epd.model.EpdDescriptor;
import epd.model.Xml;
import epd.util.Strings;

public class EpdEditor extends FormEditor {

	private static final String ID = "epd.editor";

	private Logger log = LoggerFactory.getLogger(getClass());

	private EpdDataSet dataSet;
	private boolean dirty;
	private boolean productChanged;

	private List<Runnable> saveHandlers = new ArrayList<>();

	public static void open(EpdDescriptor d) {
		if (!Plugin.getEpdStore().contains(d)) {
			Error.showBox(Messages.EPD_DOWNLOAD_FAILED);
			return;
		}
		Editors.open(new EditorInput(d), ID);
	}

	@Override
	public void init(IEditorSite s, IEditorInput input)
			throws PartInitException {
		super.init(s, input);
		setPartName(Strings.cut(input.getName(), 75));
		try {
			EditorInput in = (EditorInput) input;
			dataSet = Plugin.getEpdStore().open(in.descriptor);
			dataSet.structs();
		} catch (Exception e) {
			throw new PartInitException(
					"Failed to open editor: no correct input", e);
		}
	}

	public EpdDataSet getDataSet() {
		return dataSet;
	}

	@Override
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
			addPage(new ModulePage(this));
			addPage(new DeclaredProductPage(this));
		} catch (Exception e) {
			log.error("failed to add editor page", e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			updateVersion();
			Plugin.getEpdStore().save(dataSet);
			for (Runnable handler : saveHandlers) {
				handler.run();
			}
			setDirty(false);
			productChanged = false;
			StartPageView.refresh();
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
				Messages.EPD + " " + M.Name, null);
		if (d.open() != Window.OK)
			return;
		String name = d.getValue();
		try {
			EpdDataSet clone = dataSet.clone();
			clone.structs();
			DataSetInfo dsInfo = clone.processInfo.dataSetInfo;
			LangString.set(dsInfo.name.name, name, EpdStore.lang);
			dsInfo.uuid = UUID.randomUUID().toString();
			AdminInfo info = clone.adminInfo;
			info.publication.version = Version.asString(0);
			info.dataEntry.timeStamp = Xml.now();
			Plugin.getEpdStore().save(clone);
			EpdEditor.open(clone.toDescriptor(EpdStore.lang));
			StartPageView.refresh();
		} catch (Exception e) {
			log.error("failed to save EPD as new data set", e);
		}
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	private static class EditorInput implements IEditorInput {

		private EpdDescriptor descriptor;

		public EditorInput(EpdDescriptor descriptor) {
			this.descriptor = descriptor;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public Object getAdapter(Class adapter) {
			return null;
		}

		@Override
		public boolean exists() {
			return true;
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return null;
		}

		@Override
		public String getName() {
			if (descriptor == null || descriptor.name == null)
				return "New EPD";
			else
				return descriptor.name;
		}

		@Override
		public IPersistableElement getPersistable() {
			return null;
		}

		@Override
		public String getToolTipText() {
			return getName();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (obj == this)
				return true;
			if (!(obj instanceof EditorInput))
				return false;
			EditorInput other = (EditorInput) obj;
			return Objects.equals(this.descriptor, other.descriptor);
		}
	}
}
