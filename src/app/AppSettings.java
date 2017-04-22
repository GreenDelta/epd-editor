package app;

import java.io.File;

import app.store.Json;

public class AppSettings {

	public String lang = "en";
	public boolean showDataSetXML = false;
	public boolean showDataSetDependencies = false;

	public void save() {
		Json.write(this, new File(App.workspace, "settings.json"));
	}

	static AppSettings load() {
		File f = new File(App.workspace, "settings.json");
		if (!f.exists())
			return new AppSettings();
		AppSettings as = Json.read(f, AppSettings.class);
		return as != null ? as : new AppSettings();
	}

	@Override
	public AppSettings clone() {
		AppSettings clone = new AppSettings();
		clone.lang = lang;
		clone.showDataSetXML = showDataSetXML;
		clone.showDataSetDependencies = showDataSetDependencies;
		return clone;
	}

	public void setValues(AppSettings from) {
		if (from == null)
			return;
		lang = from.lang;
		showDataSetXML = from.showDataSetXML;
		showDataSetDependencies = from.showDataSetDependencies;
	}
}
