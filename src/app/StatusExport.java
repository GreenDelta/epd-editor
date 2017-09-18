package app;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.rcp.Labels;
import app.util.FileChooser;
import epd.model.RefStatus;

class StatusExport implements Runnable {

	static void run(List<RefStatus> stats, String title) {
		if (stats == null)
			return;
		Date d = new Date();
		LocalDate ld = d.toInstant().atZone(ZoneId.systemDefault())
				.toLocalDate();
		int year = ld.getYear();
		int month = ld.getMonthValue();
		int day = ld.getDayOfMonth();
		String name = title + " " + year + "_" + month + "_" + day + ".xlsx";
		File file = FileChooser.save(name, "*.xlsx");
		if (file == null)
			return;
		App.runInUI(M.Export, new StatusExport(stats, file));
	}

	private final List<RefStatus> stats;
	private final File file;
	private final Logger log = LoggerFactory.getLogger(getClass());

	private StatusExport(List<RefStatus> stats, File file) {
		this.stats = stats;
		this.file = file;
	}

	public void run() {
		log.trace("export status view to {}", file);
		try (FileOutputStream out = new FileOutputStream(file)) {
			Workbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet("Status");
			createHeaders(workbook, sheet);
			createRows(workbook, sheet);
			for (int col = 0; col < 4; col++)
				sheet.autoSizeColumn(col);
			workbook.write(out);
		} catch (Exception e) {
			log.error("failed to export status to " + file, e);
		}
	}

	private void createHeaders(Workbook workbook, Sheet sheet) {
		CellStyle style = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setBold(true);
		style.setFont(font);
		Row row = sheet.createRow(0);
		String[] columns = new String[] { M.DataSet, M.Name,
				M.UUID, M.Version, M.Status };
		for (int col = 0; col < columns.length; col++) {
			Cell cell = row.createCell(col);
			cell.setCellValue(columns[col]);
			cell.setCellStyle(style);
		}
	}

	private void createRows(Workbook workbook, Sheet sheet) {
		int row = 1;
		for (RefStatus stat : stats) {
			Ref ref = stat.ref;
			if (stat.ref == null)
				continue;
			Row r = sheet.createRow(row++);
			r.createCell(0).setCellValue(Labels.get(ref.type));
			String name = LangString.getFirst(ref.name, App.lang());
			r.createCell(1).setCellValue(name);
			r.createCell(2).setCellValue(ref.uuid);
			r.createCell(3).setCellValue(ref.version);
			r.createCell(4).setCellValue(getPrefix(stat) + ": " + stat.message);
		}
	}

	private String getPrefix(RefStatus stat) {
		if (stat == null)
			return "";
		switch (stat.value) {
		case RefStatus.CANCEL:
			return "CANCELED";
		case RefStatus.ERROR:
			return "ERROR";
		case RefStatus.INFO:
			return "INFO";
		case RefStatus.OK:
			return "OK";
		case RefStatus.WARNING:
			return "WARNING";
		default:
			return "?";
		}
	}
}
