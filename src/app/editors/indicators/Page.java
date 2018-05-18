package app.editors.indicators;

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
import app.util.Tables;
import app.util.UI;
import epd.model.EpdProfile;
import epd.model.Indicator;
import epd.model.Indicator.Type;

class Page extends FormPage {

	private final EpdProfile profile;

	Page(ProfileEditor editor, EpdProfile profile) {
		super(editor, "Page", profile.name);
		this.profile = profile;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		FormToolkit tk = mform.getToolkit();
		ScrolledForm form = UI.formHeader(mform, profile.name);
		Composite body = UI.formBody(form, mform.getToolkit());
		indicatorTable(body, tk);
		form.reflow(true);
	}

	private void indicatorTable(Composite body, FormToolkit tk) {
		Composite comp = UI.formSection(body, tk, M.EnvironmentalIndicators);
		UI.gridLayout(comp, 1);
		TableViewer table = Tables.createViewer(comp, M.Indicator,
				M.DataSetReference, M.UnitReference);
		Tables.bindColumnWidths(table, 0.4, 0.3, 0.3);
		table.setLabelProvider(new Label());
		table.setInput(profile.indicators);
	}

	private class Label extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof Indicator))
				return null;
			Indicator indicator = (Indicator) obj;
			switch (col) {
			case 0:
				return indicator.name;
			case 1:
				String pref = indicator.type == Type.LCI ? M.Flow
						: M.LCIAMethod;
				return pref + ": " + indicator.uuid;
			case 2:
				return indicator.unit + ": " + indicator.unitGroupUUID;
			default:
				return null;
			}
		}

	}
}
