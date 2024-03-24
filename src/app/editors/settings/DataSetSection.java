package app.editors.settings;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.AppSettings;
import app.M;
import app.store.RefTrees;
import app.util.Controls;
import app.util.FileChooser;
import app.util.MsgBox;
import app.util.UI;
import epd.model.qmeta.QGroup;
import epd.profiles.EpdProfile;
import epd.profiles.EpdProfiles;
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
		var comp = UI.formSection(body, tk, M.DataSets);
		var langCombo = new LangCombo(settings().lang);
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
		contentDeclarationsCheck(comp, tk);
		hideCategoryIdsCheck(comp, tk);
		qMetaCheck(comp, tk);
		qMetaDataFile(comp, tk);
	}

	private void dependencyCheck(Composite comp, FormToolkit tk) {
		var depCheck = UI.formCheckBox(comp, tk,
				M.ShowDependenciesInEditors);
		depCheck.setSelection(settings().showDataSetDependencies);
		Controls.onSelect(depCheck, e -> {
			settings().showDataSetDependencies = depCheck
					.getSelection();
			page.setDirty();
		});
	}

	private void xmlCheck(Composite comp, FormToolkit tk) {
		var xmlCheck = UI.formCheckBox(comp, tk,
				M.ShowXMLInEditors);
		xmlCheck.setSelection(settings().showDataSetXML);
		Controls.onSelect(xmlCheck, e -> {
			settings().showDataSetXML = xmlCheck.getSelection();
			page.setDirty();
		});
	}

	private void syncCheck(Composite comp, FormToolkit tk) {
		var check = UI.formCheckBox(comp, tk,
				M.SynchronizeReferenceDataOnStartup);
		check.setSelection(settings().syncRefDataOnStartup);
		Controls.onSelect(check, e -> {
			settings().syncRefDataOnStartup = check.getSelection();
			page.setDirty();
		});
	}

	private void contentDeclarationsCheck(Composite comp, FormToolkit tk) {
		var check = UI.formCheckBox(comp, tk,
				M.ShowContentDeclarationEditor);
		check.setSelection(settings().showContentDeclarations);
		Controls.onSelect(check, e -> {
			settings().showContentDeclarations = check.getSelection();
			page.setDirty();
		});
	}

	private void qMetaCheck(Composite comp, FormToolkit tk) {
		var check = UI.formCheckBox(comp, tk,
				M.ShowQMetadataEditor);
		check.setSelection(settings().showQMetadata);
		Controls.onSelect(check, e -> {
			settings().showQMetadata = check.getSelection();
			page.setDirty();
		});
	}

	private void hideCategoryIdsCheck(Composite comp, FormToolkit tk) {
		var check = UI.formCheckBox(comp, tk, "Hide category IDs");
		check.setSelection(settings().hideCategoryIds);
		Controls.onSelect(check, e -> {
			settings().hideCategoryIds = check.getSelection();
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
				List<Ref> refs = App.index().getRefs()
						.stream()
						.filter(ref -> ref.getType() == DataSetType.PROCESS)
						.toList();
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
		profiles.sort((p1, p2) -> Strings.compare(p1.getName(), p2.getName()));
		String[] items = new String[profiles.size()];
		int selected = -1;
		for (int i = 0; i < items.length; i++) {
			EpdProfile p = profiles.get(i);
			if (EpdProfiles.isDefault(p)) {
				selected = i;
			}
			items[i] = p.getName();
		}
		combo.setItems(items);
		if (selected >= 0) {
			combo.select(selected);
		}
		Controls.onSelect(combo, e -> {
			int idx = combo.getSelectionIndex();
			if (idx < 0)
				return;
			settings().profile = profiles.get(idx).getId();
			page.setDirty();
		});
	}

	private void qMetaDataFile(Composite comp, FormToolkit tk) {
		File dir = new File(App.workspaceFolder(), "q-metadata");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File file = new File(dir, "questions.json");

		UI.formLabel(comp, tk, "Q-Metadata questions");
		Composite inner = tk.createComposite(comp);
		UI.innerGrid(inner, 2);
		Text text = tk.createText(inner, "");
		text.setEditable(false);
		UI.gridData(text, false, false).widthHint = 310;
		if (file.exists()) {
			text.setText("~/.epd_editor/q-metatdata/questions.json");
		}

		Button btn = tk.createButton(inner, "Browse ...", SWT.NONE);
		Controls.onSelect(btn, _e -> {
			File impFile = FileChooser.open("*.json");
			if (impFile == null)
				return;
			List<QGroup> groups = QGroup.fromFile(impFile);
			if (groups.isEmpty()) {
				MsgBox.error("Invalid file",
						"Could not read Q-Metadata questions from file");
				return;
			}
			try {
				Files.copy(impFile.toPath(), file.toPath(),
						StandardCopyOption.REPLACE_EXISTING);
				text.setText("~/.epd_editor/q-metatdata/questions.json");
			} catch (Exception e) {
				MsgBox.error("Failed to copy file",
						"Could not copy file to workspace");
				Logger log = LoggerFactory.getLogger(getClass());
				log.error("Failed to copy file", e);
			}
		});

	}
}
