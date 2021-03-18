package app;

import java.io.File;

import static org.junit.Assert.*;

import app.store.validation.ValidationProfiles;
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
	public void testDefaultProfile() {
		var ws = Workspace.openDefault();
		ws.syncWith(new File("build/default_data"));
		var settings = AppSettings.load(ws);
		if (settings.validationProfile != null) {
			var profile = ValidationProfiles.getActive();
			assertNotNull(profile);
		}
	}

}
