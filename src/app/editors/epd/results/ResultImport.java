package app.editors.epd.results;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.epd.EpdResult;
import org.openlca.ilcd.processes.epd.EpdScenario;
import org.openlca.ilcd.util.EpdIndicatorResult;
import org.openlca.ilcd.util.Epds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import epd.profiles.EpdProfile;
import epd.profiles.EpdProfiles;
import epd.util.Strings;

/**
 * Imports module results from an Excel file.
 */
class ResultImport implements Runnable {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Process epd;
	private final EpdProfile profile;
	private final File excelFile;

	public ResultImport(Process epd, File excelFile) {
		this.epd = epd;
		this.profile = EpdProfiles.get(epd);
		this.excelFile = excelFile;
	}

	@Override
	public void run() {
		log.trace("import results for {} from {}", epd, excelFile);
		try (var fis = new FileInputStream(excelFile)) {
			var workbook = WorkbookFactory.create(fis);
			var sheet = workbook.getSheetAt(0);
			var results = readRows(sheet);
			EpdIndicatorResult.writeClean(epd, results);
		} catch (Exception e) {
			log.error("failed to import results from file " + excelFile, e);
		}
	}

	private List<EpdIndicatorResult> readRows(Sheet sheet) {
		if (sheet == null)
			return Collections.emptyList();
		var results = new ArrayList<EpdIndicatorResult>();
		int rowNumber = 1;
		while (true) {
			var row = sheet.getRow(rowNumber);
			rowNumber++;
			if (row == null)
				break;
			var result = resultOf(row, results);
			if (result == null)
				continue;
			var value = valueOf(row);
			if (value == null)
				continue;
			result.values().add(value);
			syncModule(value);
			syncScenario(value);
		}
		return results;
	}

	private EpdIndicatorResult resultOf(Row row, List<EpdIndicatorResult> results) {
		// TODO identify indicators by UUID
		var name = getString(row.getCell(2));
		if (Strings.nullOrEmpty(name))
			return null;

		// search the indicator in created results
		for (var r : results) {
			if (name.equals(App.s(r.indicator().getName())))
				return r;
		}

		// search the indicator in the profile
		for (var i : profile.getIndicators()) {
			if (!name.equals(App.s(i.getRef())))
				continue;
			var r = i.createResult();
			results.add(r);
			return r;
		}

		log.warn("unknown indicator: {}", name);
		return null;
	}

	private EpdResult valueOf(Row row) {
		var module = getString(row.getCell(0));
		if (Strings.nullOrEmpty(module))
			return null;
		return new EpdResult()
			.withModule(module)
			.withScenario(getString(row.getCell(1)))
			.withAmount(getDouble(row.getCell(3)));
	}

	private void syncModule(EpdResult value) {
		if (Strings.nullOrEmpty(value.getModule()))
			return;
		// TODO: sync module entries
	}

	private void syncScenario(EpdResult value) {
		if (Strings.nullOrEmpty(value.getScenario()))
			return;
		for (var s : Epds.getScenarios(epd)) {
			if (eq(s.getName(), value.getScenario()))
				return;
		}
		var scenario = new EpdScenario()
			.withName(value.getScenario().trim());
		Epds.withScenarios(epd).add(scenario);
	}

	private String getString(Cell cell) {
		if (cell == null)
			return null;
		return cell.getCellType() != CellType.STRING
			? null
			: cell.getStringCellValue();
	}

	private Double getDouble(Cell cell) {
		if (cell == null
			|| cell.getCellType() == CellType.STRING
			|| cell.getCellType() == CellType.BLANK)
			return null;
		try {
			return cell.getNumericCellValue();
		} catch (Exception e) {
			return null;
		}
	}

	private boolean eq(String s1, String s2) {
		if (Strings.nullOrEmpty(s1) && Strings.nullOrEmpty(s2))
			return true;
		return Strings.nullOrEqual(s1, s2);
	}
}
