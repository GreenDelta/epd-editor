package app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import app.store.EpdProfiles;
import app.store.validation.ValidationProfiles;

public class WorkspaceTest {

	@Test
	public void testOpenDefault() {
		var ws = Workspace.openDefault();
		assertNotNull(ws.index());
		assertNotNull(ws.store);
	}

	@Test
	public void testVersion() {
		var ws = Workspace.openDefault();
		ws.setVersion("0.0.1");
		ws = Workspace.openDefault();
		assertEquals("0.0.1", ws.version());
	}

	@Test
	public void testValidationProfile() {
		var ws = Workspace.openDefault();
		ws.syncFilesFrom(new File("build/default_data"));
		var settings = AppSettings.load(ws);
		if (settings.validationProfile != null) {
			var profile = ValidationProfiles.getActive();
			assertNotNull(profile);
		}
	}

	@Test
	public void testEPDProfile() {
		// make sure that at least the default index is loaded
		Workspace.openDefault()
				.syncFilesFrom(new File("build/default_data"));
		var ws = Workspace.openDefault();
		var profile = EpdProfiles.getDefault();
		var indicators = 0;
		var index = ws.index();
		for (var indicator : profile.indicators) {
			var ref = index.find(indicator.getRef("en"));
			assertNotNull("could not find indicator " + indicator.name, ref);
			indicators++;
		}
		assertTrue(indicators > 0);
	}

}
