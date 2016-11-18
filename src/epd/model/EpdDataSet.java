package epd.model;

import java.util.ArrayList;
import java.util.List;

import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Time;
import org.openlca.ilcd.processes.AdminInfo;
import org.openlca.ilcd.processes.DataEntry;
import org.openlca.ilcd.processes.DataGenerator;
import org.openlca.ilcd.processes.DataSetInfo;
import org.openlca.ilcd.processes.Geography;
import org.openlca.ilcd.processes.Location;
import org.openlca.ilcd.processes.Method;
import org.openlca.ilcd.processes.Modelling;
import org.openlca.ilcd.processes.ProcessInfo;
import org.openlca.ilcd.processes.ProcessName;
import org.openlca.ilcd.processes.Publication;
import org.openlca.ilcd.processes.QuantitativeReference;
import org.openlca.ilcd.processes.Representativeness;
import org.openlca.ilcd.processes.Technology;
import org.openlca.ilcd.processes.Validation;

public class EpdDataSet {

	public ProcessInfo processInfo;
	public Modelling modelling;
	public AdminInfo adminInfo;

	public SubType subType;
	public SafetyMargins safetyMargins;
	public final List<IndicatorResult> results = new ArrayList<>();
	public final List<ModuleEntry> moduleEntries = new ArrayList<>();
	public final List<Scenario> scenarios = new ArrayList<>();

	public IndicatorResult getResult(Indicator indicator) {
		for (IndicatorResult result : results)
			if (result.indicator == indicator)
				return result;
		return null;
	}

	public EpdDescriptor toDescriptor(String lang) {
		EpdDescriptor d = new EpdDescriptor();
		if (processInfo == null || processInfo.dataSetInfo == null)
			return d;
		DataSetInfo info = processInfo.dataSetInfo;
		d.refId = processInfo.dataSetInfo.uuid;
		if (info.name != null)
			d.name = LangString.getFirst(info.name.name, lang, "en");
		return d;
	}

	/** Creates the internal data structures if they do not exist yet. */
	public void structs() {
		if (processInfo == null)
			processInfo = new ProcessInfo();
		if (processInfo.dataSetInfo == null)
			processInfo.dataSetInfo = new DataSetInfo();
		if (processInfo.dataSetInfo.name == null)
			processInfo.dataSetInfo.name = new ProcessName();
		if (processInfo.geography == null)
			processInfo.geography = new Geography();
		if (processInfo.geography.location == null)
			processInfo.geography.location = new Location();
		if (processInfo.quantitativeReference == null)
			processInfo.quantitativeReference = new QuantitativeReference();
		if (processInfo.technology == null)
			processInfo.technology = new Technology();
		if (processInfo.time == null)
			processInfo.time = new Time();
		if (modelling == null)
			modelling = new Modelling();
		if (modelling.method == null)
			modelling.method = new Method();
		if (modelling.representativeness == null)
			modelling.representativeness = new Representativeness();
		if (modelling.validation == null)
			modelling.validation = new Validation();
		if (adminInfo == null)
			adminInfo = new AdminInfo();
		if (adminInfo.publication == null)
			adminInfo.publication = new Publication();
		if (adminInfo.dataEntry == null)
			adminInfo.dataEntry = new DataEntry();
		if (adminInfo.dataGenerator == null)
			adminInfo.dataGenerator = new DataGenerator();
	}

	@Override
	public EpdDataSet clone() {
		EpdDataSet clone = new EpdDataSet();
		clone.subType = subType;
		if (processInfo != null)
			clone.processInfo = processInfo.clone();
		if (modelling != null)
			clone.modelling = modelling.clone();
		if (adminInfo != null)
			clone.adminInfo = adminInfo.clone();
		if (safetyMargins != null)
			clone.safetyMargins = safetyMargins.clone();
		for (IndicatorResult r : results)
			clone.results.add(r.clone());
		for (ModuleEntry e : moduleEntries)
			clone.moduleEntries.add(e.clone());
		for (Scenario s : scenarios)
			clone.scenarios.add(s.clone());
		return clone;
	}
}
