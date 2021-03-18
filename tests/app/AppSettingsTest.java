package app;

import org.junit.Assert;
import org.junit.Test;

public class AppSettingsTest {

	@Test
	public void testClone() throws Exception {
		var settings = AppSettings.load(Workspace.openDefault());
		settings.checkEPDsOnProductUpdates = true;
		settings.showDataSetXML = false;
		settings.showDataSetDependencies = true;
		settings.showContentDeclarations = false;
		settings.validationProfile = "some_prf";
		settings.lang = "en";
		var clone = settings.clone();
		Assert.assertNotSame(settings, clone);
		for (var field : AppSettings.class.getDeclaredFields()) {
			var value = field.get(settings);
			var clonedValue = field.get(clone);
			Assert.assertEquals(value, clonedValue);
		}
	}
}
