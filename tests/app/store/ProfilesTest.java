package app.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.ilcd.epd.EpdProfile;
import org.openlca.ilcd.epd.EpdProfiles;

public class ProfilesTest {

	private File dir;

	@Before
	public void setup() throws Exception {
		dir = Files.createTempDirectory("epd-profiles").toFile();
		Profiles.useFolder(dir);
	}

	@After
	public void cleanup() {
		Profiles.useFolder(null);
	}

	@Test
	public void testBuiltInDetection() {
		assertTrue(Profiles.isBuiltIn(EpdProfiles.EN_15804.get()));
		assertTrue(Profiles.isBuiltIn(EpdProfiles.EN_15804_A2_EF30.get()));
		assertTrue(Profiles.isBuiltIn("EN_15804_A2_EF31"));
		assertFalse(Profiles.isBuiltIn("some-random-id"));
		assertFalse(Profiles.isBuiltIn((EpdProfile) null));
		assertFalse(Profiles.isBuiltIn((String) null));
	}

	@Test
	public void testRefusesBuiltInProfiles() {
		var builtIn = EpdProfiles.EN_15804_A2_EF30.get();
		assertFalse(Profiles.save(builtIn));
		assertFalse(Profiles.delete(builtIn));
	}

	@Test
	public void testCreateSaveReadAndDelete() {
		var profile = Profiles.create();
		assertNotNull(profile);
		var id = profile.getId();
		assertNotNull(id);
		assertFalse(Profiles.isBuiltIn(id));

		// it is registered and stored as a file
		assertSame(profile, Profiles.get(id));
		var file = new File(dir, id + ".xml");
		assertTrue(file.exists());

		// it is read again from that file
		Profiles.reload();
		var read = Profiles.get(id);
		assertNotNull(read);
		assertEquals(id, read.getId());
		assertEquals(profile.getName(), read.getName());

		// and it can be deleted again
		assertTrue(Profiles.delete(read));
		assertFalse(file.exists());
		assertNull(Profiles.get(id));
	}

	@Test
	public void testCopyOfBuiltIn() {
		var source = EpdProfiles.EN_15804_A2_EF30.get();
		var copy = Profiles.copyOf(source);
		assertNotNull(copy);
		assertNotSame(source, copy);
		assertNotEquals(source.getId(), copy.getId());
		assertFalse(Profiles.isBuiltIn(copy.getId()));
		assertTrue(copy.getName().contains(source.getName()));
		assertTrue(Profiles.userProfiles().contains(copy));
	}

	@Test
	public void testGetAll() {
		var all = Profiles.getAll();
		assertTrue(all.contains(EpdProfiles.EN_15804.get()));
		assertTrue(all.contains(EpdProfiles.EN_15804_A2_EF30.get()));
		assertTrue(all.contains(EpdProfiles.EN_15804_A2_EF31.get()));

		var user = Profiles.create();
		assertTrue(Profiles.getAll().contains(user));
	}

	/// A profile file without indicator and module elements must still be
	/// usable: the lists are normalized to empty lists so that the result
	/// mapping of an EPD can iterate them without a null check.
	@Test
	public void testLoadNormalizesLists() {
		var profile = new EpdProfile()
			.withId(UUID.randomUUID().toString())
			.withName("minimal");
		EpdProfiles.write(profile, new File(dir, "minimal.xml"));

		Profiles.reload();
		var read = Profiles.get(profile.getId());
		assertNotNull(read);
		assertNotNull(read.getIndicators());
		assertNotNull(read.getModules());
	}

	/// A profile file that uses the ID of a built-in profile must not shadow
	/// that built-in profile.
	@Test
	public void testLoadSkipsBuiltInId() {
		var shadow = EpdProfiles.EN_15804_A2_EF30.get().copy();
		shadow.withName("shadow");
		shadow.withDescription("must not be loaded");
		EpdProfiles.write(shadow, new File(dir, "shadow.xml"));

		Profiles.reload();
		assertTrue(Profiles.userProfiles().isEmpty());

		var builtIn = Profiles.get("EN_15804_A2_EF30");
		assertNotNull(builtIn);
		assertNotEquals("shadow", builtIn.getName());
		assertNotEquals("must not be loaded", builtIn.getDescription());
	}
}
