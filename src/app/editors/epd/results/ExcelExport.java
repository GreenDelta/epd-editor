package app.editors.epd.results;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openlca.commons.Strings;
import org.openlca.ilcd.epd.EpdProfile;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.util.Epds;
import org.slf4j.LoggerFactory;

import app.App;
import app.M;

public class ExcelExport implements Runnable {

	private final Process epd;
	private final EpdProfile profile;
	private final File file;
	private final AtomicBoolean success;

	private ExcelExport(Process epd, EpdProfile profile, File file) {
		this.epd = epd;
		this.profile = profile;
		this.file = file;
		this.success = new AtomicBoolean(false);
	}

	public static ExcelExport of(Process epd, EpdProfile profile, File file) {
		return new ExcelExport(epd, profile, file);
	}

	public boolean isDoneWithSuccess() {
		return success.get();
	}

	@Override
	public void run() {
		try (var out = new FileOutputStream(file);
				 var wb = new XSSFWorkbook()) {

			// create a bold style
			var bold = wb.createCellStyle();
			var font = wb.createFont();
			font.setBold(true);
			bold.setFont(font);

			// write the results and scenarios
			writeResultSheet(wb, bold);
			writeScenarioSheet(wb, bold);

			wb.write(out);
			success.set(true);
		} catch (Exception e) {
			LoggerFactory.getLogger(getClass())
					.error("failed to export results to Excel", e);
			success.set(false);
		}
	}

	private void writeResultSheet(Workbook wb, CellStyle bold) {
		var modEntries = EpdModuleEntries.withAllOf(epd);
		var mods = Mod.allOf(profile, modEntries);
		var results = IndicatorResults.of(epd, profile, mods);

		var sheet = wb.createSheet("Results");
		resultHeaders(bold, sheet, mods);
		int rowNum = 1;
		for (var r : results) {
			var row = sheet.createRow(rowNum++);
			cell(row, 0, r.indicator().getUUID());
			cell(row, 1, r.getIndicatorCode());
			cell(row, 2, r.getIndicatorName());
			cell(row, 3, r.getIndicatorUnit());
			for (int col = 0; col < mods.length; col++) {
				cell(row, col + 4, r.getModValueAt(col));
			}
		}

		sheet.setColumnWidth(2, 265 * 80);
	}

	private void resultHeaders(CellStyle bold, Sheet sheet, Mod[] mods) {
		var row = sheet.createRow(0);
		var hds = new String[]{M.UUID, M.Code, M.Indicator, M.Unit};
		for (int col = 0; col < hds.length; col++) {
			headerCell(bold, row, col, hds[col]);
		}
		for (int col = 0; col < mods.length; col++) {
			headerCell(bold, row, col + hds.length, mods[col].key());
		}
	}

	private void writeScenarioSheet(Workbook wb, CellStyle bold) {
		var scenarios = Epds.getScenarios(epd);
		if (scenarios == null || scenarios.isEmpty())
			return;

		var sheet = wb.createSheet("Scenarios");
		var header = sheet.createRow(0);
		headerCell(bold, header, 0, M.Scenario);
		headerCell(bold, header, 1, M.Group);
		headerCell(bold, header, 2, M.Description);
		headerCell(bold, header, 3, M.Default);

		for (int i = 0; i < scenarios.size(); i++) {
			var scenario = scenarios.get(i);
			if (Strings.isBlank(scenario.getName()))
				continue;
			var row = sheet.createRow(i + 1);
			cell(row, 0, scenario.getName());
			cell(row, 1, scenario.getGroup());
			cell(row, 2, App.s(scenario.getDescription()));
			row.createCell(3).setCellValue(scenario.isDefaultScenario());
		}

		sheet.autoSizeColumn(0);
	}

	private Cell cell(Row row, int col, String val) {
		if (Strings.isBlank(val))
			return null;
		var cell = row.createCell(col);
		cell.setCellValue(val);
		return cell;
	}

	private void headerCell(CellStyle style, Row row, int col, String val) {
		var cell = cell(row, col, val);
		if (cell != null) {
			cell.setCellStyle(style);
		}
	}

	private void cell(Row row, int col, Double val) {
		if (val == null)
			return;
		row.createCell(col).setCellValue(val);
	}
}
