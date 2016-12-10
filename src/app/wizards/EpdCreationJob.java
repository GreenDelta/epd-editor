package app.wizards;

import java.util.UUID;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.UIJob;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.processes.ComplianceDeclaration;
import org.openlca.ilcd.processes.DataEntry;
import org.openlca.ilcd.processes.Modelling;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.util.Processes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.Store;
import app.editors.epd.EpdEditor;
import app.navi.Navigator;
import epd.model.EpdDataSet;
import epd.model.Xml;

public class EpdCreationJob extends UIJob {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final String epdName;
	private final Ref productRef;

	public EpdCreationJob(String epdName, Ref productRef) {
		super("Create EPD " + epdName);
		this.epdName = epdName;
		this.productRef = productRef;
	}

	@Override
	public IStatus runInUIThread(IProgressMonitor monitor) {
		log.trace("Create EPD {}", epdName);
		try {
			monitor.beginTask("Create EPD", IProgressMonitor.UNKNOWN);
			if (epdName == null || productRef == null) {
				log.error("EPD name or product is null");
				return Status.CANCEL_STATUS;
			}
			Ref ref = createEpd();
			EpdEditor.open(ref);
			Navigator.refresh();
			monitor.done();
			return Status.OK_STATUS;
		} catch (Exception e) {
			log.error("Failed to create EPD data set", e);
			return Status.CANCEL_STATUS;
		}
	}

	private Ref createEpd() throws Exception {
		String refId = UUID.randomUUID().toString();
		EpdDataSet ds = new EpdDataSet();
		Process p = new Process();
		p.version = "1.1";
		ds.process = p;
		Processes.dataSetInfo(p).uuid = refId;
		LangString.set(Processes.processName(p).name,
				epdName, App.lang);
		Processes.dataEntry(p).timeStamp = Xml.now();
		Processes.publication(p).version = "00.00";
		setDataFormats(p);
		ds.productExchange().flow = productRef;
		writeCompliance(p);
		Store.saveEPD(ds);
		App.index.add(p);
		App.dumpIndex();
		return Ref.of(p);
	}

	private void setDataFormats(Process p) {
		DataEntry entry = Processes.dataEntry(p);
		Ref ref = new Ref();
		entry.formats.add(ref);
		ref.uuid = "a97a0155-0234-4b87-b4ce-a45da52f2a40";
		ref.uri = "../sources/ILCD_Format_" +
				"a97a0155-0234-4b87-b4ce-a45da52f2a40.xml";
		ref.type = DataSetType.SOURCE;
		LangString.set(ref.name, "ILCD format 1.1", "en");

		ref = new Ref();
		entry.formats.add(ref);
		ref.uuid = "cba73800-7874-11e3-981f-0800200c9a66";
		ref.uri = "../sources/cba73800-7874-11e3-981f-0800200c9a66.xml";
		ref.type = DataSetType.SOURCE;
		LangString.set(ref.name,
				"EPD Data Format Extensions", "en");
	}

	private void writeCompliance(Process p) {
		ComplianceDeclaration decl = new ComplianceDeclaration();
		Ref ref = new Ref();
		ref.type = DataSetType.SOURCE;
		ref.uri = "../sources/b00f9ec0-7874-11e3-981f-0800200c9a66";
		ref.uuid = "b00f9ec0-7874-11e3-981f-0800200c9a66";
		LangString.set(ref.name, "DIN EN 15804", App.lang);
		decl.system = ref;
		Modelling m = Processes.modelling(p);
		m.complianceDeclatations = new ComplianceDeclaration[] { decl };
	}
}
