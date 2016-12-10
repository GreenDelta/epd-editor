package epd.model;

import java.util.ArrayList;
import java.util.List;

import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.ProcessName;
import org.openlca.ilcd.processes.QuantitativeReference;
import org.openlca.ilcd.util.Processes;

public class EpdDataSet {

	public Process process;

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
		if (process == null)
			return d;
		d.refId = process.getUUID();
		ProcessName name = Processes.getProcessName(process);
		if (name != null)
			d.name = LangString.getFirst(name.name, lang, "en");
		return d;
	}

	@Override
	public EpdDataSet clone() {
		EpdDataSet clone = new EpdDataSet();
		clone.subType = subType;
		if (process != null)
			clone.process = process.clone();
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

	public Exchange getProductExchange() {
		QuantitativeReference qRef = Processes
				.getQuantitativeReference(process);
		if (qRef == null || qRef.referenceFlows.isEmpty())
			return null;
		Integer id = qRef.referenceFlows.get(0);
		if (id == null)
			return null;
		for (Exchange exchange : process.exchanges) {
			if (id == exchange.id)
				return exchange;
		}
		return null;
	}
}
