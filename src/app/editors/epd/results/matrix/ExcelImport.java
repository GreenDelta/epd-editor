package app.editors.epd.results.matrix;

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
import org.openlca.ilcd.epd.EpdProfile;
import org.openlca.ilcd.epd.EpdProfileModule;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.epd.EpdModuleEntry;
import org.openlca.ilcd.processes.epd.EpdValue;

import app.editors.epd.results.EpdModuleEntries;
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

	@Override
	public void run() {
		try (var wb = WorkbookFactory.create(file)) {
			var sheet = wb.getSheetAt(0);


		} catch (Exception e) {

		}
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
