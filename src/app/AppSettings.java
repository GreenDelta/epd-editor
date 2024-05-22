package app;

import java.io.File;

import org.openlca.ilcd.commons.Copyable;
import org.openlca.ilcd.epd.EpdProfiles;

import app.store.Json;

public final class AppSettings implements Copyable<AppSettings> {

	/** The ID of the EPD profile that should be used in the application. */
	public String profile = EpdProfiles.EN_15804_A2_EF30.name();

	/**
	 * If true, synchronize the reference data (defined as data stock URLs in
	 * the EPD profiles) when the application starts.
	 */
	public boolean syncRefDataOnStartup = false;

	public String lang = "en";
	public boolean showDataSetXML = false;
	public boolean showDataSetDependencies = false;

	/**
	 * If true, it searches for EPDs where a product is used and asks if these
	 * EPDs should be also updated when the respective product is updated.
	 */
	public boolean checkEPDsOnProductUpdates = true;

	/**
	 * The name of the validation profile that is used when validating the data
	 * sets. It is the name of the respective file that is stored in the
	 * 'validation_profiles' folder of the workspace directory.
	 */
	public String validationProfile;

	/**
	 * If set to {@code true}, the IDs of categories are never shown in the
	 * navigation and category selection. Otherwise, they are displayed in
	 * front of the category name if they are reasonable short.
	 */
	public boolean hideCategoryIds = false;

	/**
	 * Indicates whether the editor tab for content declarations should be shown
	 * in the EPD editor or not.
	 */
	public boolean showContentDeclarations = false;

	/**
	 * Indicates whether the editor tab for Q metadata should be shown in the
	 * EPD editor or not.
	 */
	public boolean showQMetadata = false;

	public void save(Workspace workspace) {
		Json.write(this, new File(workspace.folder, "settings.json"));
	}

	static AppSettings load(Workspace workspace) {
		File f = new File(workspace.folder, "settings.json");
		if (!f.exists())
			return new AppSettings();
		AppSettings as = Json.read(f, AppSettings.class);
		return as != null ? as : new AppSettings();
	}

	@Override
	public AppSettings copy() {
		var copy = new AppSettings();
		copy.setValues(this);
		return copy;
	}

	public void setValues(AppSettings from) {
		if (from == null)
			return;
		profile = from.profile;
		lang = from.lang;
		showDataSetXML = from.showDataSetXML;
		showDataSetDependencies = from.showDataSetDependencies;
		showContentDeclarations = from.showContentDeclarations;
		showQMetadata = from.showQMetadata;
		validationProfile = from.validationProfile;
		hideCategoryIds = from.hideCategoryIds;
		syncRefDataOnStartup = from.syncRefDataOnStartup;
		checkEPDsOnProductUpdates = from.checkEPDsOnProductUpdates;
	}
}
