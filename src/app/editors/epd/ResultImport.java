package app.editors.epd;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.openlca.ilcd.processes.epd.EpdScenario;
import org.openlca.ilcd.util.Epds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.store.EpdProfiles;
import epd.model.Amount;
import epd.model.EpdDataSet;
import epd.model.EpdProfile;
import epd.model.Indicator;
import epd.model.IndicatorResult;
import epd.model.Module;
import epd.model.ModuleEntry;
import epd.util.Strings;

/**
 * Imports module results from an Excel file.
 */
class ResultImport implements Runnable {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final EpdDataSet dataSet;
	private final File excelFile;

	public ResultImport(EpdDataSet dataSet, File excelFile) {
		this.dataSet = dataSet;
		this.excelFile = excelFile;
	}

	@Override
	public void run() {
		log.trace("import results for {} from {}", dataSet, excelFile);
		try (FileInputStream fis = new FileInputStream(excelFile)) {
			Workbook workbook = WorkbookFactory.create(fis);
			Sheet sheet = workbook.getSheetAt(0);
			List<IndicatorResult> results = readRows(sheet);
			dataSet.results.clear();
			dataSet.results.addAll(results);
		} catch (Exception e) {
			log.error("failed to import results from file " + excelFile, e);
		}
	}

	private List<IndicatorResult> readRows(Sheet sheet) {
		if (sheet == null)
			return Collections.emptyList();
		EpdProfile profile = EpdProfiles.get(dataSet.profile);
		if (profile == null)
			profile = EpdProfiles.getDefault();
		List<IndicatorResult> results = new ArrayList<>();
		int rowNumber = 1;
		while (true) {
			Row row = sheet.getRow(rowNumber);
			rowNumber++;
			if (row == null)
				break;
			Amount amount = getAmount(row, profile);
			Indicator indicator = getIndicator(row, profile);
			if (amount == null || indicator == null)
				break;
			syncModule(amount);
			syncScenario(amount);
			addResult(results, indicator, amount);
		}
		return results;
	}

	private Indicator getIndicator(Row row, EpdProfile profile) {
		String name = getString(row.getCell(2));
		if (name == null)
			return null;
		name = name.trim();
		for (Indicator indicator : profile.indicators) {
			if (Objects.equals(indicator.name, name))
				return indicator;
		}
		return null;
	}

	private Amount getAmount(Row row, EpdProfile profile) {
		String moduleName = getString(row.getCell(0));
		Module module = profile.module(moduleName);
		if (module == null)
			return null;
		Amount amount = new Amount();
		amount.module = module;
		amount.scenario = getString(row.getCell(1));
		amount.value = getDouble(row.getCell(3));
		return amount;
	}

	private void syncModule(Amount amount) {
		if (amount == null || amount.module == null)
			return;
		Module module = amount.module;
		String scenario = amount.scenario;
		for (ModuleEntry e : dataSet.moduleEntries) {
			if (Objects.equals(e.module, module) && eq(e.scenario, scenario))
				return;
		}
		ModuleEntry e = new ModuleEntry();
		e.module = module;
		if (!Strings.nullOrEmpty(scenario))
			e.scenario = scenario;
		dataSet.moduleEntries.add(e);
	}

	private void syncScenario(Amount amount) {
		if (amount == null || Strings.nullOrEmpty(amount.scenario))
			return;
		for (var s : Epds.getScenarios(dataSet.process)) {
			if (eq(s.getName(), amount.scenario))
				return;
		}
		var scenario = new EpdScenario()
			.withName(amount.scenario.trim());
		Epds.withScenarios(dataSet.process).add(scenario);
	}

	private void addResult(List<IndicatorResult> results, Indicator indicator,
			Amount amount) {
		IndicatorResult result = null;
		for (IndicatorResult r : results) {
			if (Objects.equals(r.indicator, indicator)) {
				result = r;
				break;
			}
		}
		if (result == null) {
			result = new IndicatorResult();
			result.indicator = indicator;
			results.add(result);
		}
		result.amounts.add(amount);
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
