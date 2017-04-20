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

}
