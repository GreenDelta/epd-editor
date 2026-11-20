package epd.model;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.epd.matml.MaterialDoc;
import org.openlca.ilcd.io.Xml;
import org.openlca.ilcd.util.Flows;

public class MaterialPropertyValueTest {

	@Test
	public void testSameAsByNameAndUnit() {
		var a = prop("pr1", "bulk density", "kg/m^3");
		var b = prop("bulkDensity", "  Bulk   Density ", "kg/m^3");
		assertTrue(a.isSameAs(b));
		assertTrue(b.isSameAs(a));
	}

	@Test
	public void testNotSameWithSameIdButOtherName() {
		var a = prop("pr8", "foam thickness", "m");
		var b = prop("pr8", "fibre length", "m");
		assertFalse(a.isSameAs(b));
	}

	@Test
	public void testUnitNotation() {
		var a = prop("pr1", "bulk density", "kg/m^3");
		assertTrue(a.isSameAs(prop("x", "bulk density", "kg/m3")));
		assertTrue(a.isSameAs(prop("x", "bulk density", "kg / m\u00B3")));
		assertTrue(prop("pr2", "grammage", "kg/m\u00B2")
			.isSameAs(prop("y", "grammage", "kg/m^2")));
	}

	@Test
	public void testOtherUnitIsNotSame() {
		var a = prop("pr1", "bulk density", "kg/m^3");
		// also not with the same ID
		assertFalse(a.isSameAs(prop("pr1", "bulk density", "g/cm^3")));
		// units are case-sensitive
		assertFalse(prop("p", "weight per piece", "Mg")
			.isSameAs(prop("p", "weight per piece", "mg")));
	}

	@Test
	public void testUnnamedIsOnlySameAsItself() {
		var a = prop("pr3", null, "kg/m^3");
		assertTrue(a.isSameAs(a));
		assertFalse(a.isSameAs(prop("pr3", "gross density", "kg/m^3")));
		assertFalse(a.isSameAs(prop("pr3", " ", "kg/m^3")));
		assertFalse(prop("pr3", "gross density", "kg/m^3").isSameAs(a));
	}

	@Test
	public void testKeepsValidIds() {
		var doc = write(
			val(prop("pr1", "bulk density", "kg/m^3"), 1),
			val(prop("pr2", "grammage", "kg/m^2"), 2));
		assertEquals(List.of("pr1", "pr2"), detailIds(doc));
		assertEquals(List.of("pr1", "pr2"), dataRefs(doc));
	}

	@Test
	public void testSameIdOtherMeaningGetsNewId() {
		var doc = write(
			val(prop("pr8", "foam thickness", "m"), 1),
			val(prop("pr1", "bulk density", "kg/m^3"), 2),
			val(prop("pr8", "fibre length", "m"), 3));
		var ids = detailIds(doc);
		assertEquals(3, ids.size());
		assertEquals(3, new HashSet<>(ids).size());
		assertEquals("pr8", ids.get(0));
		assertEquals("pr1", ids.get(1));
		// the generated ID must not collide with any existing one
		assertEquals("pr2", ids.get(2));
		assertEquals(ids, dataRefs(doc));
	}

	@Test
	public void testInvalidIdGetsNewId() {
		var doc = write(
			val(prop("1 2", "bulk density", "kg/m^3"), 1),
			val(prop(null, "grammage", "kg/m^2"), 2));
		assertEquals(List.of("pr1", "pr2"), detailIds(doc));
	}

	@Test
	public void testNonDecimalDigitIdGetsNewId() {
		// superscript digits are numbers but not valid NCName characters
		var doc = write(val(prop("pr²", "grammage", "kg/m^2"), 1));
		assertEquals(List.of("pr1"), detailIds(doc));
	}

	@Test
	public void testKeepsUnicodeIds() {
		var doc = write(val(prop("Gr\u00F6\u00DFe1", "size", "m"), 1));
		assertEquals(List.of("Gr\u00F6\u00DFe1"), detailIds(doc));
	}

	@Test
	public void testOtherUnitGetsOwnDefinition() {
		// same name and ID but another unit: the value must not be written
		// against the unit of the other property
		var doc = write(
			val(prop("pr1", "bulk density", "kg/m^3"), 1000),
			val(prop("pr1", "bulk density", "g/cm^3"), 1));
		assertEquals(List.of("pr1", "pr2"), detailIds(doc));
		assertEquals(List.of("pr1", "pr2"), dataRefs(doc));
		var units = doc.getProperties().stream()
			.map(p -> p.getUnits().getName())
			.toList();
		assertEquals(List.of("kg/m^3", "g/cm^3"), units);
	}

	@Test
	public void testDuplicatesShareOneDefinition() {
		// existing duplicates are not removed but they reference the same
		// definition
		var doc = write(
			val(prop("pr1", "bulk density", "kg/m^3"), 1),
			val(prop("bulkDensity", "bulk density", "kg/m^3"), 2));
		assertEquals(List.of("pr1"), detailIds(doc));
		assertEquals(List.of("pr1", "pr1"), dataRefs(doc));
	}

	@Test
	public void testRoundTrip() throws Exception {
		var flow = new Flow();
		Flows.withDataSetInfo(flow).withUUID("abc");
		MaterialPropertyValue.write(List.of(
			val(prop("pr8", "foam thickness", "m"), 1),
			val(prop("pr8", "fibre length", "m"), 3)), flow);

		var out = new ByteArrayOutputStream();
		Xml.write(flow, out);
		var copy = Xml.read(Flow.class,
			new ByteArrayInputStream(out.toByteArray()));

		var vals = MaterialPropertyValue.readFrom(copy);
		assertEquals(2, vals.size());
		assertEquals("foam thickness", vals.get(0).property.name);
		assertEquals(1, vals.get(0).value, 1e-10);
		assertEquals("fibre length", vals.get(1).property.name);
		assertEquals(3, vals.get(1).value, 1e-10);
	}

	private MaterialDoc write(MaterialPropertyValue... vals) {
		var flow = new Flow();
		MaterialPropertyValue.write(List.of(vals), flow);
		return Flows.getDataSetInfo(flow).getEpdExtension().getMaterialDoc();
	}

	private List<String> detailIds(MaterialDoc doc) {
		return doc.getProperties().stream()
			.map(p -> p.getId())
			.toList();
	}

	private List<String> dataRefs(MaterialDoc doc) {
		return doc.getMaterials().getFirst()
			.getBulkDetails()
			.getProperties()
			.stream()
			.map(p -> p.getProperty())
			.toList();
	}

	private MaterialProperty prop(String id, String name, String unit) {
		var p = new MaterialProperty();
		p.id = id;
		p.name = name;
		p.unit = unit;
		return p;
	}

	private MaterialPropertyValue val(MaterialProperty prop, double value) {
		var v = new MaterialPropertyValue();
		v.property = prop;
		v.value = value;
		return v;
	}
}
