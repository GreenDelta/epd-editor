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
import org.openlca.ilcd.util.Processes;

import app.M;
import app.Tooltips;
import app.editors.RefTable;
import app.rcp.Labels;
import app.util.TextBuilder;
import app.util.UI;
import app.util.Viewers;
import epd.model.EpdDataSet;
import epd.model.SubType;

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
			Processes.method(process).methodSources)
			.withEditor(editor)
			.withTitle(M.LCAMethodDetails)
			.withTooltip(Tooltips.EPD_LCAMethodDetails)
			.render(body, toolkit);

		RefTable.create(DataSetType.SOURCE,
			Processes.representativeness(process).dataHandlingSources)
			.withEditor(editor)
			.withTitle("#Documentation of data quality management")
			.render(body, toolkit);

		RefTable.create(DataSetType.SOURCE,
			Processes.representativeness(process).sources)
			.withEditor(editor)
			.withTitle(M.DataSources)
			.withTooltip(Tooltips.EPD_DataSources)
			.render(body, toolkit);

		createComplianceSection(body);

		RefTable.create(DataSetType.SOURCE, epd.originalEPDs)
			.withEditor(editor)
			.withTitle("Original EPDs")
			.render(body, toolkit);

		new ReviewSection(editor, this)
			.render(body, toolkit, form);
		form.reflow(true);
	}

	private void createModelingSection(Composite parent) {
		Composite comp = UI.formSection(parent, toolkit,
			M.ModellingAndValidation, Tooltips.EPD_ModellingAndValidation);
		UI.formLabel(comp, toolkit, M.Subtype, Tooltips.EPD_Subtype);
		createSubTypeViewer(comp);
		TextBuilder tb = new TextBuilder(editor, this, toolkit);
		tb.multiText(comp, M.UseAdvice, Tooltips.EPD_UseAdvice,
			Processes.representativeness(process).useAdvice);
	}

	private void createSubTypeViewer(Composite parent) {
		var combo = new ComboViewer(parent, SWT.READ_ONLY);
		UI.gridData(combo.getControl(), true, false);
		combo.setContentProvider(ArrayContentProvider.getInstance());
		combo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof SubType) {
					var subType = (SubType) element;
					return Labels.get(subType);
				}
				return super.getText(element);
			}
		});

		combo.setInput(SubType.values());
		if (epd.subType != null) {
			combo.setSelection(new StructuredSelection(epd.subType));
		}
		combo.addSelectionChangedListener(e -> {
			editor.dataSet.subType = Viewers.getFirst(e.getSelection());
			editor.setDirty();
		});
	}

	private void createComplianceSection(Composite body) {
		List<Ref> systems = new ArrayList<>();
		Processes.getComplianceDeclarations(process).forEach(s -> {
			if (s.system != null)
				systems.add(s.system);
		});
		RefTable table = RefTable.create(DataSetType.SOURCE, systems)
			.withTitle(M.ComplianceDeclarations)
			.withTooltip(Tooltips.EPD_ComplianceDeclarations);
		table.render(body, toolkit);
		table.onAdd(system -> {
			ComplianceDeclaration decl = Processes.getComplianceDeclaration(
				process, system);
			if (decl != null)
				return;
			Processes.complianceDeclaration(process).system = system;
			editor.setDirty();
		});
		table.onRemove(system -> {
			var decl = Processes.getComplianceDeclaration(process, system);
			if (decl == null)
				return;
			Processes.getComplianceDeclarations(process).remove(decl);
			if (process.modelling.complianceDeclarations.entries.isEmpty()) {
				process.modelling.complianceDeclarations = null;
			}
			editor.setDirty();
		});
	}
}
