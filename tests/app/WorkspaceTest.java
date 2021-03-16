package app;

import static org.junit.Assert.*;

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

}
