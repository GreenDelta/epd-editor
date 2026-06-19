package app.editors.epd.results;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.openlca.commons.Strings;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.epd.EpdIndicatorResult;
import org.openlca.ilcd.epd.EpdProfile;
import org.openlca.ilcd.epd.EpdProfileIndicator;
import org.openlca.ilcd.epd.EpdProfileModule;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.epd.EpdModuleEntry;
import org.openlca.ilcd.processes.epd.EpdScenario;
import org.openlca.ilcd.processes.epd.EpdValue;
import org.openlca.ilcd.util.Epds;
import org.slf4j.LoggerFactory;

import app.App;

public class ExcelImport implements Runnable {

	private final Process epd;
	private final EpdProfile profile;
	private final File file;

	private ExcelImport(Process epd, EpdProfile profile, File file) {
		this.epd = epd;
		this.profile = profile;
		this.file = file;
	}

	public static ExcelImport of(Process epd, EpdProfile profile, File file) {
		return new ExcelImport(epd, profile, file);
	}

	@Override
	public void run() {
		try (var wb = WorkbookFactory.create(file)) {
			var sheet = findSheetOrDefault(wb, "results", true);
			if (sheet == null)
				return;

			var slots = syncModuleEntries(sheet);
			syncScenarios(wb, slots);

			var results = new ArrayList<EpdIndicatorResult>();
			for (int i = 1; ; i++) {
				var row = sheet.getRow(i);
				if (row == null)
					break;
				var indicator = findIndicatorOf(row);
				if (indicator == null)
					continue;
				var r = indicator.createResult();
				for (var slot : slots) {
					var value = slot.read(row).orElse(null);
					if (value == null)
						continue;
					r.values().add(value);
				}
				if (!r.values().isEmpty()) {
					results.add(r);
				}
			}
			EpdIndicatorResult.writeClean(epd, results);
		} catch (Exception e) {
			LoggerFactory.getLogger(getClass()).error("import failed", e);
		}
	}

	private void syncScenarios(Workbook wb, List<ValSlot> slots) {

		Function<String, String> keyFn =
			s -> s != null ? s.strip().toLowerCase(Locale.US) : null;

		// collect existing scenarios
		var scenarios = new HashMap<String, EpdScenario>();
		for (var scen : Epds.getScenarios(epd)) {
			var key = keyFn.apply(scen.getName());
			if (key != null) {
				scenarios.put(key, scen);
			}
		}

		// read scenarios from Excel sheet
		var sheet = findSheetOrDefault(wb, "scenarios", false);
		if (sheet != null) {
			for (int i = 1; ; i++) {
				var row = sheet.getRow(i);
				if (row == null)
					break;
				var name = str(row.getCell(0));
				if (Strings.isBlank(name))
					continue;
				var key = keyFn.apply(name);
				if (scenarios.containsKey(key))
					continue;

				var scen = new EpdScenario()
					.withName(name)
					.withGroup(str(row.getCell(1)))
					.withDefaultScenario(bool(row.getCell(3)));
				var desc = str(row.getCell(2));
				if (Strings.isNotBlank(desc)) {
					scen.withDescription().add(LangString.of(desc, App.lang()));
				}
				Epds.withScenarios(epd).add(scen);
				scenarios.put(key, scen);
			}
		}

		// add scenarios from module entries
		for (var s : slots) {
			var name = s.entry.getScenario();
			if (Strings.isBlank(name))
				continue;
			var key = keyFn.apply(name);
			if (scenarios.containsKey(key))
				continue;
			var scenario = new EpdScenario().withName(name);
			Epds.withScenarios(epd).add(scenario);
			scenarios.put(key, scenario);
		}
	}

	private EpdProfileIndicator findIndicatorOf(Row row) {
		if (row == null)
			return null;
		var id = str(row.getCell(0));
		var code = str(row.getCell(1));
		var name = str(row.getCell(2));

		for (var i : profile.getIndicators()) {

			if (Strings.isNotBlank(id)) {
				if (id.equals(i.getUUID()))
					return i;
				continue;
			}

			if (Strings.isNotBlank(code)) {
				if (code.equals(i.getCode()))
					return i;
				continue;
			}

			if (Strings.isNotBlank(name) && i.getRef() != null) {
				for (var refName : i.getRef().getName()) {
					if (name.equals(refName.getValue()))
						return i;
				}
			}
		}

		return null;
	}

	private List<ValSlot> syncModuleEntries(Sheet sheet) {
		var row = sheet.getRow(0);
		if (row == null)
			return Collections.emptyList();

		var knownMods = profile.getModules()
			.stream()
			.map(EpdProfileModule::getName)
			.collect(Collectors.toSet());
		var entries = EpdModuleEntries.withAllOf(epd);
		var oldEntries = new HashMap<String, EpdModuleEntry>();
		for (var e : entries) {
			oldEntries.put(Mod.key(e), e);
		}
		entries.clear();

		var slots = new ArrayList<ValSlot>();
		var handled = new HashSet<String>();
		int pos = 4;
		Cell cell;
		while ((cell = row.getCell(pos)) != null) {
			var s = str(cell);
			if (Strings.isBlank(s))
				break;
			pos++;

			var entry = parseModuleKey(s, knownMods);
			if (entry == null)
				continue;
			var key = Mod.key(entry);
			if (handled.contains(key))
				continue;
			handled.add(key);
			var oldEntry = oldEntries.get(key);
			if (oldEntry != null) {
				entries.add(oldEntry);
				slots.add(new ValSlot(oldEntry, pos - 1));
			} else {
				entries.add(entry);
				slots.add(new ValSlot(entry, pos - 1));
			}
		}
		return slots;
	}

	private EpdModuleEntry parseModuleKey(String label, Set<String> mods) {
		if (Strings.isBlank(label))
			return null;
		int splitIdx = label.indexOf('/');
		if (splitIdx <= 0) {
			var mod = label.strip();
			return mods.contains(mod)
				? new EpdModuleEntry().withModule(mod)
				: null;
		}

		var mod = label.substring(0, splitIdx).strip();
		if (!mods.contains(mod))
			return null;
		var scenario = label.substring(splitIdx + 1).strip();
		return Strings.isNotBlank(scenario)
			? new EpdModuleEntry().withModule(mod).withScenario(scenario)
			: new EpdModuleEntry().withModule(mod);
	}

	private String str(Cell cell) {
		return cell != null && cell.getCellType() == CellType.STRING
			? cell.getStringCellValue()
			: null;
	}

	private boolean bool(Cell cell) {
		if (cell == null) return false;
		if (cell.getCellType() == CellType.BOOLEAN)
			return cell.getBooleanCellValue();
		if (cell.getCellType() == CellType.NUMERIC)
			return cell.getNumericCellValue() != 0;
		if (cell.getCellType() != CellType.STRING)
			return false;
		var s = cell.getStringCellValue();
		if (Strings.isBlank(s))
			return false;
		s = s.strip().toLowerCase(Locale.ROOT);
		return s.startsWith("y")
			|| s.startsWith("j")
			|| s.startsWith("t")
			|| s.startsWith("w")
			|| !s.equals("0");
	}

	private Sheet findSheetOrDefault(
		Workbook wb, String label, boolean selectDefault
	) {
		var count = wb != null ? wb.getNumberOfSheets() : 0;
		if (count == 0)
			return null;
		for (int i = 0; i < count; i++) {
			var sheet = wb.getSheetAt(i);
			var name = sheet.getSheetName();
			if (Strings.isBlank(name))
				continue;
			if (name.strip().equalsIgnoreCase(label))
				return sheet;
		}
		return selectDefault ? wb.getSheetAt(0) : null;
	}

	private record ValSlot(EpdModuleEntry entry, int col) {

		Optional<EpdValue> read(Row row) {
			if (row == null)
				return Optional.empty();
			var cell = row.getCell(col);
			if (cell == null)
				return Optional.empty();
			if (cell.getCellType() != CellType.NUMERIC
				&& cell.getCellType() != CellType.FORMULA)
				return Optional.empty();
			try {
				double num = cell.getNumericCellValue();
				var val = new EpdValue()
					.withModule(entry.getModule())
					.withScenario(entry.getScenario())
					.withAmount(num);
				return Optional.of(val);
			} catch (Exception e) {
				return Optional.empty();
			}
		}
	}
}
