package epd.model;

import org.junit.Assert;
import org.junit.Test;

public class VersionTest {

	@Test
	public void testIsNewer() {
		Assert.assertTrue(Version.isNewer("02.00.000", "01.01.000"));
		Assert.assertTrue(Version.isNewer("01.02.000", "01.01.000"));
		Assert.assertTrue(Version.isNewer("01.01.001", "01.01.000"));
		Assert.assertTrue(Version.isNewer("01.10.000", "01.09.000"));
		Assert.assertFalse(Version.isNewer("01.01.000", "01.01.000"));
		Assert.assertFalse(Version.isNewer("01.01.000", "01.02.000"));
		Assert.assertFalse(Version.isNewer("01.01.000", "02.00.000"));
	}

	@Test
	public void testMissingVersions() {
		Assert.assertTrue(Version.isNewer("1.0", null));
		Assert.assertFalse(Version.isNewer(null, "1.0"));
		Assert.assertFalse(Version.isNewer(null, null));
		Assert.assertFalse(Version.isNewer("", ""));
	}
}
