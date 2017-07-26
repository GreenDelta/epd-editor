package app.editors.indicators;

import java.util.List;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import app.M;
import app.rcp.Labels;
import app.util.Tables;
import app.util.UI;
import epd.model.IndicatorGroup;
import epd.model.IndicatorMapping;

class Page extends FormPage {

	private IndicatorMappingEditor editor;

	Page(IndicatorMappingEditor editor) {
		super(editor, "Page", M.IndicatorMappings);
		this.editor = editor;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		FormToolkit tk = mform.getToolkit();
		ScrolledForm form = UI.formHeader(mform, M.IndicatorMappings);
		Composite body = UI.formBody(form, mform.getToolkit());
		for (IndicatorGroup group : IndicatorGroup.values()) {
			createSection(group, body, tk);
		}
		form.reflow(true);
	}

	private void createSection(IndicatorGroup group, Composite parent,
			FormToolkit tk) {
		Composite comp = UI.formSection(parent, tk, Labels.get(group));
		UI.gridLayout(comp, 1);
		TableViewer table = Tables.createViewer(comp, M.Indicator,
				M.DataSetReference, M.UnitReference);
		Tables.bindColumnWidths(table, 0.4, 0.3, 0.3);
		table.setLabelProvider(new Label());
		List<IndicatorMapping> list = editor.getGroup(group);
		table.setInput(list);
	}

	private class Label extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof IndicatorMapping))
				return null;
			IndicatorMapping im = (IndicatorMapping) obj;
			if (im.indicator == null)
				return null;
			switch (col) {
			case 0:
				return Labels.get(im.indicator);
			case 1:
				String pref = im.indicator.isInventoryIndicator() ? M.Flow
						: M.LCIAMethod;
				return pref + ": " + im.indicatorRefId;
			case 2:
				return im.unitLabel + ": " + im.unitRefId;
			default:
				return null;
			}
		}

	}
}
