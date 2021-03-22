package app.editors.profiles;

import java.util.ArrayList;
import java.util.List;

import app.editors.Editors;
import app.util.Viewers;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import app.App;
import app.M;
import app.StatusView;
import app.rcp.Icon;
import app.store.EpdProfiles;
import app.util.Actions;
import app.util.Tables;
import app.util.UI;
import epd.model.EpdProfile;
import epd.model.Indicator;
import epd.model.Indicator.Type;
import epd.model.RefStatus;
import org.openlca.ilcd.commons.DataSetType;

class IndicatorTable {

	private final ProfileEditor editor;
	private final EpdProfile profile;
	private TableViewer table;

	IndicatorTable(ProfileEditor editor, EpdProfile profile) {
		this.editor = editor;
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
		table.setInput(profile.indicators);

		Tables.onDoubleClick(table, $ -> {
			Indicator indicator = Viewers.getFirstSelected(table);
			if (indicator == null)
				return;
			var ref = indicator.getRef(App.lang());
			var indexRef = App.index().find(ref);
			if (indexRef != null) {
				Editors.open(indexRef);
			}
		});

		Sync sync = new Sync();
		Actions.bind(section, sync);
		Actions.bind(table, sync);
	}

	private class Sync extends Action {

		public Sync() {
			setText(M.Synchronize);
			setImageDescriptor(Icon.RELOAD.des());
		}

		@Override
		public void run() {
			List<RefStatus> errors = new ArrayList<>();
			App.run(M.Synchronize, () -> {
				List<RefStatus> stats = EpdProfiles.sync(profile);
				stats.stream()
					.filter(stat -> stat.value == RefStatus.ERROR)
					.forEach(errors::add);
			}, () -> {
				table.setInput(profile.indicators);
				editor.setDirty();
				if (!errors.isEmpty()) {
					StatusView.open(M.Error + ": "
													+ profile.name, errors);
				}
			});
		}
	}

	private static class Label extends LabelProvider
		implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			if (!(obj instanceof Indicator))
				return null;
			if (col == 2)
				return Icon.UNIT.img();
			if (col == 1) {
				var indicator = (Indicator) obj;
				return indicator.type == Type.LCI
					? Icon.img(DataSetType.FLOW)
					: Icon.img(DataSetType.LCIA_METHOD);
			}
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof Indicator))
				return null;
			var indicator = (Indicator) obj;
			return switch (col) {
				case 0 -> indicator.name;
				case 1 -> {
					String type = indicator.type == Type.LCI
						? M.Flow
						: M.LCIAMethod;
					yield type + ": " + indicator.uuid;
				}
				case 2 -> indicator.unit;
				default -> null;
			};
		}

	}

}
