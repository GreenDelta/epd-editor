package app.editors.indicators;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.editors.BaseEditor;
import app.editors.Editors;
import app.editors.SimpleEditorInput;

public class IndicatorMappingEditor extends BaseEditor {

	private static final String ID = "indicator.mapping.editor";

	private Logger log = LoggerFactory.getLogger(getClass());

	public static void open() {
		Editors.open(new SimpleEditorInput("Indicator Mapping"), ID);
	}

	@Override
	protected void addPages() {
		try {
			addPage(new Page(this));
		} catch (Exception e) {
			log.error("failed to add editor page", e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

}
