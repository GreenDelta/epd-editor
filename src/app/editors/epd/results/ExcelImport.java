package app.editors.epd.results;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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

import epd.util.Strings;

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
			var sheet = wb.getSheetAt(0);
			var slots = syncModuleEntries(sheet);
			syncScenarios(slots);
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
			LoggerFactory.getLogger(getClass())
					.error("import failed", e);
		}
	}

	private void syncScenarios(List<ValSlot> slots) {
		var scenarios = new HashSet<String>();
		for (var scen : Epds.getScenarios(epd)) {
			if (Strings.notEmpty(scen.getName())) {
				scenarios.add(scen.getName().strip());
			}
		}
		for (var s : slots) {
			var scenario = s.entry.getScenario();
			if (Strings.notEmpty(scenario)
					&& !scenarios.contains(scenario)) {
				Epds.withScenarios(epd)
						.add(new EpdScenario().withName(scenario));
			}
		}
	}

	private EpdProfileIndicator findIndicatorOf(Row row) {
		if (row == null)
			return null;
		var id = str(row.getCell(0));
		var code = str(row.getCell(1));
		var name = str(row.getCell(2));

		for (var i : profile.getIndicators()) {

			if (Strings.notEmpty(id)) {
				if (id.equals(i.getUUID()))
					return i;
				continue;
			}

			if (Strings.notEmpty(code)) {
				if (code.equals(i.getCode()))
					return i;
				continue;
			}

			if (Strings.notEmpty(name) && i.getRef() != null) {
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
			if (Strings.nullOrEmpty(s))
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

	private EpdModuleEntry parseModuleKey(
			String label, Set<String> knownMods
	) {
		if (Strings.nullOrEmpty(label))
			return null;
		var parts = label.split("/");
		var mod = parts[0].strip();
		if (!knownMods.contains(mod))
			return null;
		var e = new EpdModuleEntry().withModule(mod);
		if (parts.length == 1)
			return e;
		var sce = parts[1].strip();
		return Strings.notEmpty(sce)
				? e.withScenario(sce)
				: e;
	}

	private String str(Cell cell) {
		return cell != null && cell.getCellType() == CellType.STRING
				? cell.getStringCellValue()
				: null;
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
