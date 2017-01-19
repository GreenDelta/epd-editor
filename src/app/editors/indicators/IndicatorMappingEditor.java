package app.editors.indicators;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.editors.BaseEditor;
import app.editors.Editors;
import app.editors.SimpleEditorInput;
import epd.io.Configs;
import epd.io.EpdStore;
import epd.io.MappingConfig;
import epd.model.EpdDataSet;

public class IndicatorMappingEditor extends BaseEditor {

	private static final String ID = "epd.indicator_mapping";

	private Logger log = LoggerFactory.getLogger(getClass());

	private IndicatorMappingPage mappingPage;
	private EpdDataSet dataSet;
	private boolean dirty;
	private MappingConfig config;

	public static void open() {
		Editors.open(new SimpleEditorInput("Indicator Mapping"), ID);
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		try {
			EpdStore store = Plugin.getEpdStore();
			config = Configs.getMappingConfig(store);
		} catch (Exception e) {
			log.error("failed to get mapping config", e);
			config = new MappingConfig();
		}
	}

	public EpdDataSet getDataSet() {
		return dataSet;
	}

	@Override
	protected void addPages() {
		try {
			mappingPage = new IndicatorMappingPage(this, config);
			addPage(mappingPage);
		} catch (Exception e) {
			log.error("failed to add editor page", e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			EpdStore store = Plugin.getEpdStore();
			Configs.save(config, store);
			setDirty(false);
		} catch (Exception e) {
			log.error("failed to save mapping config", e);
		}
	}

	@Override
	public void doSaveAs() {
		setDirty(false);
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

}
