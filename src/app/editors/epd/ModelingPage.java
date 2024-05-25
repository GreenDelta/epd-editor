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
import app.util.LangText;
import app.util.UI;
import app.util.Viewers;

class ModelingPage extends FormPage {

	private FormToolkit tk;

	private final EpdEditor editor;
	private final Process epd;

	public ModelingPage(EpdEditor editor) {
		super(editor, "EpdInfoPage", M.ModellingAndValidation);
		this.editor = editor;
		this.epd = editor.epd;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		tk = mform.getToolkit();
		var form = UI.formHeader(mform, M.ModellingAndValidation);
		var body = UI.formBody(form, mform.getToolkit());

		createModelingSection(body);

		RefTable.create(DataSetType.SOURCE,
						Processes.withInventoryMethod(epd).withSources())
				.withEditor(editor)
				.withTitle(M.LCAMethodDetails)
				.withTooltip(Tooltips.EPD_LCAMethodDetails)
				.render(body, tk);

		RefTable.create(DataSetType.SOURCE,
						Processes.withRepresentativeness(epd).withDataHandlingSources())
				.withEditor(editor)
				.withTitle(M.DocumentationDataQualityManagement)
				.withTooltip(Tooltips.EPD_DocumentationDataQualityManagement)
				.render(body, tk);

		RefTable.create(DataSetType.SOURCE,
						Processes.withRepresentativeness(epd).withSources())
				.withEditor(editor)
				.withTitle(M.DataSources)
				.withTooltip(Tooltips.EPD_DataSources)
				.render(body, tk);

		createComplianceSection(body);

		RefTable.create(DataSetType.SOURCE, Epds.withOriginalEpds(epd))
				.withEditor(editor)
				.withTitle(M.ReferenceOriginalEPD)
				.withTooltip(Tooltips.EPD_ReferenceOriginal)
				.render(body, tk);

		new ReviewSection(editor)
				.render(body, tk, form);
		form.reflow(true);
	}

	private void createModelingSection(Composite parent) {
		var comp = UI.formSection(parent, tk,
				M.ModellingAndValidation, Tooltips.EPD_ModellingAndValidation);
		UI.formLabel(comp, tk, M.Subtype, Tooltips.EPD_Subtype);
		createSubTypeViewer(comp);

		var rep = Epds.withRepresentativeness(epd);
		LangText.builder(editor, tk)
				.nextMulti(M.UseAdvice, Tooltips.EPD_UseAdvice)
				.val(rep.getUseAdvice())
				.edit(rep::withUseAdvice)
				.draw(comp);
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
		var current = Epds.getSubType(epd);
		if (current != null) {
			combo.setSelection(new StructuredSelection(current));
		}
		combo.addSelectionChangedListener(e -> {
			EpdSubType next = Viewers.getFirst(e.getSelection());
			Epds.withSubType(epd, next);
			editor.setDirty();
		});
	}

	private void createComplianceSection(Composite body) {
		List<Ref> systems = new ArrayList<>();
		epd.withModelling().withComplianceDeclarations().forEach(s -> {
			if (s.withSystem() != null)
				systems.add(s.withSystem());
		});
		RefTable table = RefTable.create(DataSetType.SOURCE, systems)
				.withTitle(M.ComplianceDeclarations)
				.withTooltip(Tooltips.EPD_ComplianceDeclarations);
		table.render(body, tk);

		table.onAdd(system -> {
			var dec = Processes.getComplianceDeclaration(epd, system);
			if (dec != null)
				return;
			dec = new ComplianceDeclaration();
			dec.withSystem(system);
			Processes.withComplianceDeclarations(epd).add(dec);
			editor.setDirty();
		});

		table.onRemove(system -> {
			var dec = Processes.getComplianceDeclaration(epd, system);
			if (dec == null)
				return;
			var dcs = Processes.getComplianceDeclarations(epd);
			if (dcs.isEmpty())
				return;
			dcs.remove(dec);
			editor.setDirty();
		});
	}
}
