package app.editors.settings;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.Ref;

import app.App;
import app.AppSettings;
import app.M;
import app.store.EpdProfiles;
import app.store.RefTrees;
import app.util.Controls;
import app.util.UI;
import epd.model.EpdProfile;
import epd.util.Strings;

class DataSetSection {

	private final SettingsPage page;

	DataSetSection(SettingsPage page) {
		this.page = page;
	}

	private AppSettings settings() {
		return page.settings;
	}

	void render(Composite body, FormToolkit tk) {
		Composite comp = UI.formSection(body, tk, M.DataSets);
		LangCombo langCombo = new LangCombo(settings().lang);
		langCombo.render(comp, tk);
		langCombo.onChange(lang -> {
			settings().lang = lang;
			page.setDirty();
		});
		profileCombo(comp, tk);
		xmlCheck(comp, tk);
		dependencyCheck(comp, tk);
		syncCheck(comp, tk);
		productUpdateCheck(comp, tk);
	}

	private void dependencyCheck(Composite comp, FormToolkit tk) {
		Button depCheck = UI.formCheckBox(comp, tk,
				M.ShowDependenciesInEditors);
		depCheck.setSelection(settings().showDataSetDependencies);
		Controls.onSelect(depCheck, e -> {
			settings().showDataSetDependencies = depCheck
					.getSelection();
			page.setDirty();
		});
	}

	private void xmlCheck(Composite comp, FormToolkit tk) {
		Button xmlCheck = UI.formCheckBox(comp, tk,
				M.ShowXMLInEditors);
		xmlCheck.setSelection(settings().showDataSetXML);
		Controls.onSelect(xmlCheck, e -> {
			settings().showDataSetXML = xmlCheck.getSelection();
			page.setDirty();
		});
	}

	private void syncCheck(Composite comp, FormToolkit tk) {
		Button check = UI.formCheckBox(comp, tk,
				M.SynchronizeReferenceDataOnStartup);
		check.setSelection(settings().syncRefDataOnStartup);
		Controls.onSelect(check, e -> {
			settings().syncRefDataOnStartup = check.getSelection();
			page.setDirty();
		});
	}

	private void productUpdateCheck(Composite comp, FormToolkit tk) {
		Button check = UI.formCheckBox(comp, tk,
				M.CheckEPDsOnProductUpdates);
		check.setSelection(settings().checkEPDsOnProductUpdates);
		Controls.onSelect(check, e -> {
			boolean b = check.getSelection();
			if (!b) {
				check.setSelection(b);
				settings().checkEPDsOnProductUpdates = b;
				page.setDirty();
				return;
			}
			AtomicBoolean allIndexed = new AtomicBoolean(false);
			// index the data sets
			App.run(monitor -> {
				List<Ref> refs = App.index.getRefs()
						.stream()
						.filter(ref -> ref.type == DataSetType.PROCESS)
						.collect(Collectors.toList());
				monitor.beginTask(M.IndexProductRelations, refs.size());
				for (int i = 0; i < refs.size(); i++) {
					if (monitor.isCanceled())
						break;
					Ref ref = refs.get(i);
					RefTrees.get(ref);
					monitor.worked(1);
					if (i == (refs.size() - 1)) {
						allIndexed.set(true);
					}
				}
			}, () -> {
				settings().checkEPDsOnProductUpdates = allIndexed.get();
				check.setSelection(allIndexed.get());
				page.setDirty();
			});
		});
	}

	private void profileCombo(Composite comp, FormToolkit tk) {
		Combo combo = UI.formCombo(comp, tk, M.DefaultEPDProfile);
		UI.gridData(combo, false, false).widthHint = 300;
		List<EpdProfile> profiles = EpdProfiles.getAll();
		profiles.sort((p1, p2) -> Strings.compare(p1.name, p2.name));
		String[] items = new String[profiles.size()];
		int selected = -1;
		for (int i = 0; i < items.length; i++) {
			EpdProfile p = profiles.get(i);
			if (EpdProfiles.isDefault(p)) {
				selected = i;
			}
			items[i] = p.name;
		}
		combo.setItems(items);
		if (selected >= 0) {
			combo.select(selected);
		}
		Controls.onSelect(combo, e -> {
			int idx = combo.getSelectionIndex();
			if (idx < 0)
				return;
			settings().profile = profiles.get(idx).id;
			page.setDirty();
		});
	}
}
