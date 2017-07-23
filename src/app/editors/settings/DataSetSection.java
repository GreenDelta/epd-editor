package app.editors.settings;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import app.AppSettings;
import app.M;
import app.util.Controls;
import app.util.UI;

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
		xmlCheck(comp, tk);
		dependencyCheck(comp, tk);
	}

	private void dependencyCheck(Composite comp, FormToolkit tk) {
		Button depCheck = UI.formCheckBox(comp, tk,
				"#Show dependencies in editors");
		depCheck.setSelection(settings().showDataSetDependencies);
		Controls.onSelect(depCheck, e -> {
			settings().showDataSetDependencies = depCheck
					.getSelection();
			page.setDirty();
		});
	}

	private void xmlCheck(Composite comp, FormToolkit tk) {
		Button xmlCheck = UI.formCheckBox(comp, tk,
				"#Show XML pages in editors");
		xmlCheck.setSelection(settings().showDataSetXML);
		Controls.onSelect(xmlCheck, e -> {
			settings().showDataSetXML = xmlCheck.getSelection();
			page.setDirty();
		});
	}
}
