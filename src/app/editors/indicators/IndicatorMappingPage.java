package com.greendelta.olca.plugins.oekobaudat.rcp.ui.mappings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.openlca.app.M;
import org.openlca.app.db.Database;
import org.openlca.app.util.UI;
import org.openlca.app.viewers.ISelectionChangedListener;
import org.openlca.app.viewers.combo.ImpactMethodViewer;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greendelta.olca.plugins.oekobaudat.rcp.Messages;

import epd.io.MappingConfig;
import epd.model.Indicator;
import epd.model.IndicatorGroup;
import epd.model.IndicatorMapping;

class IndicatorMappingPage extends FormPage {

	private Logger log = LoggerFactory.getLogger(getClass());

	private IndicatorMappingEditor editor;
	private MappingConfig config;
	private FormToolkit toolkit;

	private List<IndicatorViewer> viewers = new ArrayList<>();
	private ImpactMethodViewer methodViewer;

	public IndicatorMappingPage(IndicatorMappingEditor editor,
			MappingConfig config) {
		super(editor, "EpdInfoPage", Messages.Modules);
		this.editor = editor;
		this.config = config;
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		toolkit = managedForm.getToolkit();
		ScrolledForm form = UI.formHeader(managedForm,
				Messages.IndicatorMapping);
		Composite body = UI.formBody(form, managedForm.getToolkit());
		createMethodViewer(body);
		createMappingViewers(body);
		setInitialInput();
		// add the listener after the initial input is set to avoid an event
		methodViewer.addSelectionChangedListener(new MethodChange());
		form.reflow(true);
	}

	private void setInitialInput() {
		ImpactMethodDescriptor configMethod = getConfigMethod();
		if (configMethod == null)
			return;
		methodViewer.select(configMethod);
		List<ImpactCategoryDescriptor> categories = getCategories(configMethod);
		for (IndicatorViewer viewer : viewers)
			viewer.setCategories(categories);
	}

	private void createMethodViewer(Composite parent) {
		Section section = UI.section(parent, toolkit, M.ImpactAssessmentMethod);
		Composite composite = UI.sectionClient(section, toolkit);
		UI.formLabel(section, M.ImpactAssessmentMethod);
		methodViewer = new ImpactMethodViewer(composite);
		methodViewer.setInput(Database.get());
	}

	private ImpactMethodDescriptor getConfigMethod() {
		String refId = config.impactMethodRefId;
		if (refId == null)
			return null;
		try {
			ImpactMethodDao dao = new ImpactMethodDao(Database.get());
			List<ImpactMethodDescriptor> descriptors = dao.getDescriptors();
			for (ImpactMethodDescriptor descriptor : descriptors) {
				if (Objects.equals(refId, descriptor.getRefId()))
					return descriptor;
			}
			return null;
		} catch (Exception e) {
			log.error("failed to get impact method descriptor " + refId, e);
			return null;
		}
	}

	private void createMappingViewers(Composite parent) {
		IndicatorGroup[] groups = { IndicatorGroup.ENVIRONMENTAL,
				IndicatorGroup.RESOURCE_USE, IndicatorGroup.WASTE_DISPOSAL,
				IndicatorGroup.OUTPUT_FLOWS };
		for (IndicatorGroup group : groups) {
			String title = getTitle(group);
			Section section = UI.section(parent, toolkit, title);
			Composite composite = UI.sectionClient(section, toolkit);
			UI.gridLayout(composite, 1);
			List<IndicatorMapping> mappings = getMappings(group);
			IndicatorViewer viewer = new IndicatorViewer(composite, editor,
					mappings);
			viewers.add(viewer);
		}
	}

	private String getTitle(IndicatorGroup group) {
		switch (group) {
		case ENVIRONMENTAL:
			return Messages.EnvironmentalParameters;
		case OUTPUT_FLOWS:
			return Messages.OutputParameters;
		case RESOURCE_USE:
			return Messages.ResourceParameters;
		case WASTE_DISPOSAL:
			return Messages.WasteParameters;
		default:
			return null;
		}
	}

	private List<IndicatorMapping> getMappings(IndicatorGroup group) {
		List<IndicatorMapping> mappings = new ArrayList<>();
		for (Indicator indicator : Indicator.getIndicators(group)) {
			IndicatorMapping mapping = config.getIndicatorMapping(indicator);
			if (mapping == null) {
				// normally this should never happen if the default mapping
				// config is correct
				mapping = new IndicatorMapping();
				mapping.indicator = indicator;
				config.indicatorMappings.add(mapping);
			}
			mappings.add(mapping);
		}
		return mappings;
	}

	private List<ImpactCategoryDescriptor> getCategories(
			ImpactMethodDescriptor descriptor) {
		try {
			ImpactMethodDao dao = new ImpactMethodDao(Database.get());
			return dao.getCategoryDescriptors(descriptor.getId());
		} catch (Exception e) {
			log.error("failed to get LCIA categories for method " + descriptor,
					e);
			return Collections.emptyList();
		}
	}

	private class MethodChange implements
			ISelectionChangedListener<ImpactMethodDescriptor> {

		@Override
		public void selectionChanged(ImpactMethodDescriptor method) {
			if (method == null) {
				config.impactMethodName = null;
				config.impactMethodRefId = null;
			} else {
				config.impactMethodName = method.getName();
				config.impactMethodRefId = method.getRefId();
			}
			for (IndicatorMapping mapping : config.indicatorMappings) {
				mapping.indicatorLabel = null;
				mapping.indicatorRefId = null;
			}
			List<ImpactCategoryDescriptor> categories = getCategories(method);
			for (IndicatorViewer viewer : viewers) {
				viewer.setCategories(categories);
				viewer.refresh();
			}
			editor.setDirty(true);
		}
	}

}