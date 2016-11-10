package app.editors.epd;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.processes.Modelling;

import app.App;
import app.M;
import app.editors.RefTable;
import app.rcp.Labels;
import app.util.TextBuilder;
import app.util.UI;
import app.util.Viewers;
import epd.model.EpdDataSet;
import epd.model.SubType;

class ModelingPage extends FormPage {

	private FormToolkit toolkit;

	private final String lang;
	private EpdEditor editor;
	private EpdDataSet dataSet;
	private Modelling modelling;

	public ModelingPage(EpdEditor editor) {
		super(editor, "EpdInfoPage", M.ModellingAndValidation);
		this.editor = editor;
		dataSet = editor.getDataSet();
		modelling = dataSet.modelling;
		lang = App.lang;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		toolkit = mform.getToolkit();
		ScrolledForm form = UI.formHeader(mform,
				M.ModellingAndValidation);
		Composite body = UI.formBody(form, mform.getToolkit());
		createModelingSection(body);
		RefTable.create(DataSetType.SOURCE, modelling.method.methodSources)
				.withEditor(editor)
				.withTitle(M.LCAMethodDetails)
				.render(body, toolkit);
		RefTable.create(DataSetType.SOURCE,
				modelling.representativeness.sources)
				.withEditor(editor)
				.withTitle(M.DataSources)
				.render(body, toolkit);
		new ReviewSection(modelling, editor, this)
				.render(body, toolkit, form);
		form.reflow(true);
	}

	private void createModelingSection(Composite parent) {
		Composite comp = UI.formSection(parent, toolkit,
				M.ModellingAndValidation);
		UI.formLabel(comp, toolkit, M.Subtype);
		createSubTypeViewer(comp);
		TextBuilder tb = new TextBuilder(editor, this, toolkit);
		tb.multiText(comp, M.UseAdvice,
				modelling.representativeness.useAdvice);
	}

	private ComboViewer createSubTypeViewer(Composite parent) {
		ComboViewer viewer = new ComboViewer(parent, SWT.READ_ONLY);
		UI.gridData(viewer.getControl(), true, false);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof SubType) {
					SubType subType = (SubType) element;
					return Labels.get(subType);
				}
				return super.getText(element);
			}
		});
		viewer.setInput(SubType.values());
		selectSubType(viewer);
		viewer.addSelectionChangedListener((event) -> {
			SubType type = Viewers.getFirst(event.getSelection());
			dataSet.subType = type;
			editor.setDirty();
		});
		return viewer;
	}

	private void selectSubType(ComboViewer combo) {
		if (dataSet.subType == null)
			return;
		StructuredSelection s = new StructuredSelection(
				dataSet.subType);
		combo.setSelection(s);
	}
}
