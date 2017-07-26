package app.editors.settings;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.M;
import app.util.UI;

class AppSection {

	private final SettingsPage page;

	AppSection(SettingsPage page) {
		this.page = page;
	}

	void render(Composite body, FormToolkit tk) {
		Composite comp = UI.formSection(body, tk, M.ApplicationSettings);
		LangCombo langCombo = new LangCombo(page.ini.lang);
		langCombo.render(comp, tk);
		langCombo.onChange(lang -> {
			page.ini.lang = lang;
			page.setDirty();
		});
		Text memText = UI.formText(comp, tk, "Memory [MB]");
		memText.setText(Integer.toString(page.ini.maxMemory));
		UI.gridData(memText, false, false).widthHint = 315;
		memText.addModifyListener(e -> {
			String s = memText.getText().trim();
			try {
				page.ini.maxMemory = Integer.parseInt(s);
				page.setDirty();
			} catch (Exception ex) {
				Logger log = LoggerFactory.getLogger(getClass());
				log.warn("ini-settings: {} is not a number", s);
			}
		});
	}

}
