package app.editors.source;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.ilcd.sources.FileRef;

public class FileRefsTest {

	private static final String UUID = "c50ec8b7-691d-458b-8379-a8d787e4e4b4";

	@Test
	public void testWithUuid() {
		Assert.assertEquals("flow_chart_" + UUID + ".png",
			FileRefs.withUuid("flow_chart.png", UUID));
		Assert.assertEquals("archive.tar_" + UUID + ".gz",
			FileRefs.withUuid("archive.tar.gz", UUID));
		Assert.assertEquals("README_" + UUID,
			FileRefs.withUuid("README", UUID));
		Assert.assertEquals(".gitignore_" + UUID,
			FileRefs.withUuid(".gitignore", UUID));
	}

	@Test
	public void testHasSourceUuid() {
		Assert.assertTrue(FileRefs.hasSourceUuid(
			new FileRef().withUri("flow_chart_" + UUID + ".png"), UUID));
		Assert.assertFalse(FileRefs.hasSourceUuid(
			new FileRef().withUri("flow_chart.png"), UUID));
		Assert.assertFalse(FileRefs.hasSourceUuid(
			new FileRef().withUri("flow_chart_" + UUID + ".png"), null));
		Assert.assertFalse(FileRefs.hasSourceUuid(
			new FileRef().withUri(null), UUID));
		Assert.assertFalse(FileRefs.hasSourceUuid(null, UUID));
	}

	@Test
	public void testMissingUuid() {
		Assert.assertEquals("flow_chart.png",
			FileRefs.withUuid("flow_chart.png", null));
		Assert.assertEquals("flow_chart.png",
			FileRefs.withUuid("flow_chart.png", ""));
		Assert.assertEquals("flow_chart.png",
			FileRefs.withUuid("flow_chart.png", "   "));
		Assert.assertNull(FileRefs.withUuid(null, UUID));
		Assert.assertEquals("", FileRefs.withUuid("", UUID));
	}
}
