package app.editors.epd.results;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openlca.ilcd.epd.EpdProfile;
import org.openlca.ilcd.processes.Process;
import org.slf4j.LoggerFactory;

import app.M;
import epd.util.Strings;

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

			var modEntries = EpdModuleEntries.withAllOf(epd);
			var mods = Mod.allOf(profile, modEntries);
			var results = IndicatorResults.of(epd, profile, mods);

			var sheet = wb.createSheet("results");
			createHeaders(wb, sheet, mods);
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
			wb.write(out);
			success.set(true);
		} catch (Exception e) {
			LoggerFactory.getLogger(getClass())
					.error("failed to export results to Excel", e);
			success.set(false);
		}

	}

	private void cell(Row row, int col, String val) {
		if (Strings.nullOrEmpty(val))
			return;
		row.createCell(col).setCellValue(val);
	}

	private void cell(Row row, int col, Double val) {
		if (val == null)
			return;
		row.createCell(col).setCellValue(val);
	}

	private void createHeaders(Workbook wb, Sheet sheet, Mod[] mods) {
		var style = wb.createCellStyle();
		var font = wb.createFont();
		font.setBold(true);
		style.setFont(font);
		var row = sheet.createRow(0);

		BiConsumer<Integer, String> nextCell = (col, label) -> {
			var cell = row.createCell(col);
			cell.setCellValue(label);
			cell.setCellStyle(style);
		};

		var hds = new String[]{M.UUID, M.Code, M.Indicator, M.Unit};
		for (int col = 0; col < hds.length; col++) {
			nextCell.accept(col, hds[col]);
		}
		for (int col = 0; col < mods.length; col++) {
			nextCell.accept(col + hds.length, mods[col].key());
		}
	}
}
