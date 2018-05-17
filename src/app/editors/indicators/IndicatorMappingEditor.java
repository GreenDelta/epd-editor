package app.editors.indicators;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.editors.BaseEditor;
import app.editors.Editors;
import app.editors.SimpleEditorInput;
import app.store.EpdProfiles;
import epd.model.IndicatorGroup;
import epd.model.IndicatorMapping;

public class IndicatorMappingEditor extends BaseEditor {

	private static final String ID = "indicator.mapping.editor";

	private Logger log = LoggerFactory.getLogger(getClass());

	private List<IndicatorMapping> mappings;

	public static void open() {
		Editors.open(new SimpleEditorInput("Indicator Mapping"), ID);
	}

	@Override
	protected void addPages() {
		try {
			mappings = EpdProfiles.get();
			addPage(new Page(this));
		} catch (Exception e) {
			log.error("failed to add editor page", e);
		}
	}

	List<IndicatorMapping> getGroup(IndicatorGroup group) {
		List<IndicatorMapping> filtered = new ArrayList<>();
		for (IndicatorMapping m : mappings) {
			if (m.indicator == null)
				continue;
			if (m.indicator.getGroup() == group)
				filtered.add(m);
		}
		return filtered;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

}
