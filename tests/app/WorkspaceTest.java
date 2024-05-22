package app;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

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
}
