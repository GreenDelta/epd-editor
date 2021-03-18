package app;

import java.io.File;

import static org.junit.Assert.*;

import app.store.EpdProfiles;
import app.store.validation.ValidationProfiles;
import epd.index.Index;
import org.junit.Test;

public class WorkspaceTest {

	@Test
	public void testOpenDefault() {
		var ws = Workspace.openDefault();
		assertNotNull(ws.index);
		assertNotNull(ws.store);
		var next = ws.updateIndex(ws.index);
		assertNotSame(next.store, ws.store);
	}

	@Test
	public void testValidationProfile() {
		var ws = Workspace.openDefault();
		ws.syncWith(new File("build/default_data"));
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
			.syncWith(new File("build/default_data"));
		var ws = Workspace.openDefault();
		var profile = EpdProfiles.getDefault();
		var indicators = 0;
		for (var indicator : profile.indicators) {
			var ref = ws.index.find(indicator.getRef("en"));
			assertNotNull("could not find indicator " + indicator.name, ref);
			indicators++;
		}
		assertTrue(indicators > 0);
	}

}
