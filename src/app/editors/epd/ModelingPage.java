package app.editors.epd;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.processes.ComplianceDeclaration;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.epd.EpdSubType;
import org.openlca.ilcd.util.Epds;
import org.openlca.ilcd.util.Processes;

import app.M;
import app.Tooltips;
import app.editors.RefTable;
import app.rcp.Labels;
import app.util.TextBuilder;
import app.util.UI;
import app.util.Viewers;
import epd.model.EpdDataSet;

class ModelingPage extends FormPage {

	private FormToolkit toolkit;

	private final EpdEditor editor;
	private final EpdDataSet epd;
	private final Process process;

	public ModelingPage(EpdEditor editor) {
		super(editor, "EpdInfoPage", M.ModellingAndValidation);
		this.editor = editor;
		this.epd = editor.dataSet;
		process = epd.process;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		toolkit = mform.getToolkit();
		var form = UI.formHeader(mform, M.ModellingAndValidation);
		var body = UI.formBody(form, mform.getToolkit());

		createModelingSection(body);

		RefTable.create(DataSetType.SOURCE,
				process.withModelling().withInventoryMethod().withSources())
			.withEditor(editor)
			.withTitle(M.LCAMethodDetails)
			.withTooltip(Tooltips.EPD_LCAMethodDetails)
			.render(body, toolkit);

		RefTable.create(DataSetType.SOURCE,
				process.withModelling().withRepresentativeness()
					.withDataHandlingSources())
			.withEditor(editor)
			.withTitle(M.DocumentationDataQualityManagement)
			.withTooltip(Tooltips.EPD_DocumentationDataQualityManagement)
			.render(body, toolkit);

		RefTable.create(DataSetType.SOURCE,
				process.withModelling().withRepresentativeness().withSources())
			.withEditor(editor)
			.withTitle(M.DataSources)
			.withTooltip(Tooltips.EPD_DataSources)
			.render(body, toolkit);

		createComplianceSection(body);

		RefTable.create(DataSetType.SOURCE, epd.originalEPDs)
			.withEditor(editor)
			.withTitle(M.ReferenceOriginalEPD)
			.withTooltip(Tooltips.EPD_ReferenceOriginal)
			.render(body, toolkit);

		new ReviewSection(editor, this)
			.render(body, toolkit, form);
		form.reflow(true);
	}

	private void createModelingSection(Composite parent) {
		var comp = UI.formSection(parent, toolkit,
			M.ModellingAndValidation, Tooltips.EPD_ModellingAndValidation);
		UI.formLabel(comp, toolkit, M.Subtype, Tooltips.EPD_Subtype);
		createSubTypeViewer(comp);
		var tb = new TextBuilder(editor, this, toolkit);
		tb.multiText(comp, M.UseAdvice, Tooltips.EPD_UseAdvice,
			Processes.withRepresentativeness(process).withUseAdvice());
	}

	private void createSubTypeViewer(Composite parent) {
		var combo = new ComboViewer(parent, SWT.READ_ONLY);
		UI.gridData(combo.getControl(), true, false);
		combo.setContentProvider(ArrayContentProvider.getInstance());
		combo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof EpdSubType subType) {
					return Labels.get(subType);
				}
				return super.getText(element);
			}
		});

		combo.setInput(EpdSubType.values());
		var current = Epds.getSubType(epd.process);
		if (current != null) {
			combo.setSelection(new StructuredSelection(current));
		}
		combo.addSelectionChangedListener(e -> {
			EpdSubType next = Viewers.getFirst(e.getSelection());
			Epds.withSubType(epd.process, next);
			editor.setDirty();
		});
	}

	private void createComplianceSection(Composite body) {
		List<Ref> systems = new ArrayList<>();
		process.withModelling().withComplianceDeclarations().forEach(s -> {
			if (s.withSystem() != null)
				systems.add(s.withSystem());
		});
		RefTable table = RefTable.create(DataSetType.SOURCE, systems)
			.withTitle(M.ComplianceDeclarations)
			.withTooltip(Tooltips.EPD_ComplianceDeclarations);
		table.render(body, toolkit);

		table.onAdd(system -> {
			var dec = Processes.getComplianceDeclaration(process, system);
			if (dec != null)
				return;
			dec = new ComplianceDeclaration();
			dec.withSystem(system);
			Processes.withComplianceDeclarations(process).add(dec);
			editor.setDirty();
		});

		table.onRemove(system -> {
			var dec = Processes.getComplianceDeclaration(process, system);
			if (dec == null)
				return;
			var dcs = Processes.getComplianceDeclarations(process);
			if (dcs.isEmpty())
				return;
			dcs.remove(dec);
			editor.setDirty();
		});
	}
}
