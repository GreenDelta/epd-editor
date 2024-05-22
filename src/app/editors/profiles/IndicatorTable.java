package app.editors.profiles;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.epd.EpdProfile;
import org.openlca.ilcd.epd.EpdProfileIndicator;

import app.App;
import app.M;
import app.editors.Editors;
import app.rcp.Icon;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;

class IndicatorTable {

	private final EpdProfile profile;
	private TableViewer table;

	IndicatorTable(EpdProfile profile) {
		this.profile = profile;
	}

	void render(Composite body, FormToolkit tk) {
		var section = UI.section(body, tk, M.EnvironmentalIndicators);
		var comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		table = Tables.createViewer(comp, M.Indicator,
			M.DataSetReference, M.UnitReference);
		Tables.bindColumnWidths(table, 0.4, 0.3, 0.3);
		table.setLabelProvider(new Label());
		table.setInput(profile.getIndicators());

		Tables.onDoubleClick(table, $ -> {
			EpdProfileIndicator indicator = Viewers.getFirstSelected(table);
			if (indicator == null)
				return;
			var ref = indicator.getRef();
			var indexRef = App.index().find(ref);
			if (indexRef != null) {
				Editors.open(indexRef);
			}
		});
	}

	private static class Label extends LabelProvider
		implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			if (!(obj instanceof EpdProfileIndicator indicator))
				return null;
			if (col == 2)
				return Icon.UNIT.img();
			if (col == 1) {
				return indicator.isInventoryIndicator()
					? Icon.img(DataSetType.FLOW)
					: Icon.img(DataSetType.IMPACT_METHOD);
			}
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof EpdProfileIndicator indicator))
				return null;
			return switch (col) {
				case 0 -> App.s(indicator.getRef());
				case 1 -> {
					String type = indicator.isInventoryIndicator()
						? M.Flow
						: M.LCIAMethod;
					yield type + ": " + indicator.getUUID();
				}
				case 2 -> App.s(indicator.getUnit());
				default -> null;
			};
		}
	}
}
