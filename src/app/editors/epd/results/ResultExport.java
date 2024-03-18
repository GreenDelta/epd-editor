package app.editors.epd.results;

import app.App;
import app.M;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.util.EpdIndicatorResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Exports the module results of an EPD data set to an Excel file.
 */
class ResultExport implements Runnable {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Process epd;
	private final File excelFile;
	private boolean success;

	public ResultExport(Process epd, File excelFile) {
		this.epd = epd;
		this.excelFile = excelFile;
	}

	public boolean isDoneWithSuccess() {
		return success;
	}

	@Override
	public void run() {
		success = false;
		log.trace("export results of {} to {}", epd, excelFile);
		try (var out = new FileOutputStream(excelFile)) {
			var workbook = new XSSFWorkbook();
			var sheet = workbook.createSheet("results");
			createHeaders(workbook, sheet);
			createRows(sheet);
			for (int col = 0; col < 5; col++) {
				sheet.autoSizeColumn(col);
			}
			workbook.write(out);
			success = true;
		} catch (Exception e) {
			log.error("failed to export module results to " + excelFile, e);
		}
	}

	private void createHeaders(Workbook workbook, Sheet sheet) {
		CellStyle style = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setBold(true);
		style.setFont(font);
		Row row = sheet.createRow(0);
		String[] columns = new String[]{M.UUID, M.Module, M.Scenario,
			M.Indicator, M.Value, M.Unit};
		for (int col = 0; col < columns.length; col++) {
			Cell cell = row.createCell(col);
			cell.setCellValue(columns[col]);
			cell.setCellStyle(style);
			sheet.autoSizeColumn(col);
		}
	}

	private void createRows(Sheet sheet) {
		int rowNumber = 1;
		for (var r : EpdIndicatorResult.allOf(epd)) {
			if (r.indicator() == null)
				continue;
			for (var v : r.values()) {
				if (v.getModule() == null)
					continue;
				Row row = sheet.createRow(rowNumber++);
				row.createCell(0).setCellValue(r.indicator().getUUID());
				row.createCell(1).setCellValue(v.getModule());
				row.createCell(2).setCellValue(v.getScenario());
				row.createCell(3).setCellValue(App.s(r.indicator().getName()));
				row.createCell(4).setCellValue(v.getAmount());
				var unit = r.unitGroup() != null
					? App.s(r.unitGroup().getName())
					: "";
				row.createCell(5).setCellValue(unit);
			}
		}
	}
}
