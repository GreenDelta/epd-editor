package app.editors.epd;

import app.App;
import app.M;
import epd.model.EpdDataSet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

	private final EpdDataSet dataSet;
	private final File excelFile;
	private boolean success;

	public ResultExport(EpdDataSet dataSet, File excelFile) {
		this.dataSet = dataSet;
		this.excelFile = excelFile;
	}

	public boolean isDoneWithSuccess() {
		return success;
	}

	@Override
	public void run() {
		success = false;
		log.trace("export results of {} to {}", dataSet, excelFile);
		try (FileOutputStream out = new FileOutputStream(excelFile)) {
			Workbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet("results");
			createHeaders(workbook, sheet);
			createRows(sheet);
			for (int col = 0; col < 5; col++)
				sheet.autoSizeColumn(col);
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
		String[] columns = new String[]{M.Module, M.Scenario,
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
		for (var r : EpdIndicatorResult.allOf(dataSet.process)) {
			if (r.indicator() == null)
				continue;
			for (var v : r.values()) {
				if (v.getModule() == null)
					continue;
				Row row = sheet.createRow(rowNumber++);
				row.createCell(0).setCellValue(v.getModule());
				row.createCell(1).setCellValue(v.getScenario());
				row.createCell(2).setCellValue(App.s(r.indicator().getName()));
				row.createCell(3).setCellValue(v.getAmount());
				var unit = r.unitGroup() != null
					? App.s(r.unitGroup().getName())
					: "";
				row.createCell(4).setCellValue(unit);
			}
		}
	}
}
