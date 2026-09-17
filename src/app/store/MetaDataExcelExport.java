package app.store;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.io.Xml;
import org.openlca.ilcd.methods.ImpactMethod;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.util.DataSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.M;
import app.util.MsgBox;

/// Writes the meta-data of the data sets in the local data store into an Excel
/// file: one sheet for each data set type that contains data sets with the
/// columns UUID, name, version and last update. Other content of the workspace
/// (like locations, categories, EPD profiles or connections) is not written.
public class MetaDataExcelExport implements IRunnableWithProgress {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final File file;

	private boolean success;

	private MetaDataExcelExport(File file) {
		this.file = file;
	}

	/// Creates an export of the meta-data of the data sets in the local data
	/// store into the given Excel file.
	public static MetaDataExcelExport of(File file) {
		return new MetaDataExcelExport(file);
	}

	/// Indicates whether the meta-data were written into the file.
	public boolean isDoneWithSuccess() {
		return success;
	}

	@Override
	public void run(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		if (file == null)
			return;
		var defs = sheetDefs();
		monitor.beginTask(M.ExportMetaData, defs.size());
		try (var wb = new XSSFWorkbook()) {
			var bold = boldStyle(wb);
			for (var def : defs) {
				if (monitor.isCanceled())
					return;
				monitor.subTask(def.name());
				var dataSets = readAll(def.type());
				if (!dataSets.isEmpty()) {
					writeSheet(wb, def, dataSets, bold);
				}
				monitor.worked(1);
			}
			if (wb.getNumberOfSheets() == 0) {
				// an Excel file without sheets is not valid; the user is
				// informed about this, and it is not an export error
				MsgBox.warn(M.DataFolderIsEmpty, M.DataFolderIsEmpty_Message);
				success = true;
				return;
			}
			try (var out = new FileOutputStream(file)) {
				wb.write(out);
				success = true;
			}
		} catch (Exception e) {
			log.error("failed to export meta data to {}", file, e);
		} finally {
			monitor.done();
		}
	}

	/// A data set type and the name of the sheet into which the data sets of
	/// this type are written.
	private record SheetDef(Class<? extends IDataSet> type, String name) {
	}

	/// The data set types in the same order as in the navigation. Only data
	/// sets are written; locations, categories, EPD profiles, connections and
	/// other content of the workspace are not included.
	private static List<SheetDef> sheetDefs() {
		return List.of(
			new SheetDef(Process.class, M.EPDs),
			new SheetDef(Contact.class, M.Contacts),
			new SheetDef(Source.class, M.Sources),
			new SheetDef(Flow.class, M.Flows),
			new SheetDef(FlowProperty.class, M.FlowProperties),
			new SheetDef(UnitGroup.class, M.UnitGroups),
			new SheetDef(ImpactMethod.class, M.LCIAMethods));
	}

	/// Reads the data sets of the given type from the local data store.
	private List<IDataSet> readAll(Class<? extends IDataSet> type) {
		var dataSets = new ArrayList<IDataSet>();
		var folder = App.store().getFolder(type);
		if (folder == null || !folder.isDirectory())
			return dataSets;
		var files = folder.listFiles();
		if (files == null)
			return dataSets;
		for (var file : files) {
			if (!file.isFile() || !file.getName().endsWith(".xml"))
				continue;
			try {
				var ds = Xml.read(type, file);
				if (ds != null) {
					dataSets.add(ds);
				}
			} catch (Exception e) {
				log.error("failed to read data set {}", file, e);
			}
		}
		return dataSets;
	}

	private void writeSheet(
			Workbook wb, SheetDef def, List<IDataSet> dataSets,
			CellStyle bold) {
		var sheet = wb.createSheet(def.name());
		sheet.setColumnWidth(0, 40 * 256);
		sheet.setColumnWidth(1, 60 * 256);
		sheet.setColumnWidth(2, 16 * 256);
		sheet.setColumnWidth(3, 24 * 256);

		var header = sheet.createRow(0);
		headerCell(bold, header, 0, M.UUID);
		headerCell(bold, header, 1, M.Name);
		headerCell(bold, header, 2, M.Version);
		headerCell(bold, header, 3, M.LastUpdate);

		int row = 1;
		for (var ds : dataSets) {
			var r = sheet.createRow(row++);
			cell(r, 0, DataSets.getUUID(ds));
			cell(r, 1, App.s(DataSets.getBaseName(ds)));
			cell(r, 2, DataSets.getVersion(ds));
			cell(r, 3, epd.model.Xml.toString(DataSets.getTimeStamp(ds)));
		}
	}

	private CellStyle boldStyle(Workbook wb) {
		var style = wb.createCellStyle();
		var font = wb.createFont();
		font.setBold(true);
		style.setFont(font);
		return style;
	}

	private void headerCell(CellStyle bold, Row row, int col, String value) {
		var cell = row.createCell(col);
		cell.setCellValue(value == null ? "" : value);
		cell.setCellStyle(bold);
	}

	private void cell(Row row, int col, String value) {
		row.createCell(col).setCellValue(value == null ? "" : value);
	}
}
