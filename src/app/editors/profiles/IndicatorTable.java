package app.editors.profiles;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

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

class IndicatorTable {

	private final ProfileEditor editor;
	private final EpdProfile profile;
	private TableViewer table;

	IndicatorTable(ProfileEditor editor, EpdProfile profile) {
		this.editor = editor;
		this.profile = profile;
	}

	void render(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, M.EnvironmentalIndicators);
		Composite comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		table = Tables.createViewer(comp, M.Indicator,
				M.DataSetReference, M.UnitReference);
		Tables.bindColumnWidths(table, 0.4, 0.3, 0.3);
		table.setLabelProvider(new Label());
		table.setInput(profile.indicators);
		Sync sync = new Sync();
		Actions.bind(section, sync);
		Actions.bind(table, sync);
	}

	private class Sync extends Action {

		public Sync() {
			setText("#Sync");
			setImageDescriptor(Icon.RELOAD.des());
		}

		@Override
		public void run() {
			List<RefStatus> errors = new ArrayList<>();
			App.run("#Sync", () -> {
				List<RefStatus> stats = EpdProfiles.sync(profile);
				stats.stream()
						.filter(stat -> stat.value == RefStatus.ERROR)
						.forEach(stat -> errors.add(stat));
			}, () -> {
				table.setInput(profile.indicators);
				editor.setDirty();
				if (!errors.isEmpty()) {
					StatusView.open("#Sync Errors for "
							+ profile.name, errors);
				}
			});
		}
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
				String type = indicator.type == Type.LCI
						? M.Flow
						: M.LCIAMethod;
				return type + ": " + indicator.uuid;
			case 2:
				return indicator.unit + ": " + indicator.unitGroupUUID;
			default:
				return null;
			}
		}

	}

}
