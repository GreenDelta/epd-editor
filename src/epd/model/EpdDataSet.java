package epd.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.QuantitativeReferenceType;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.ProcessName;
import org.openlca.ilcd.processes.QuantitativeReference;
import org.openlca.ilcd.util.Processes;

import epd.model.content.ContentDeclaration;
import epd.model.qmeta.QMetaData;

public class EpdDataSet {

	public final Process process;
	public String profile;
	public SubType subType;
	public LocalDate publicationDate;

	public SafetyMargins safetyMargins;
	public ContentDeclaration contentDeclaration;
	public QMetaData qMetaData;

	public final List<IndicatorResult> results = new ArrayList<>();
	public final List<ModuleEntry> moduleEntries = new ArrayList<>();
	public final List<Scenario> scenarios = new ArrayList<>();

	public EpdDataSet(Process process) {
		this.process = Objects.requireNonNull(process);
	}

	public EpdDataSet() {
		this(new Process());
	}

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
		EpdDataSet clone = new EpdDataSet(process.clone());
		clone.profile = profile;
		clone.subType = subType;
		if (safetyMargins != null)
			clone.safetyMargins = safetyMargins.clone();
		for (IndicatorResult r : results)
			clone.results.add(r.clone());
		for (ModuleEntry e : moduleEntries)
			clone.moduleEntries.add(e.clone());
		for (Scenario s : scenarios)
			clone.scenarios.add(s.clone());
		if (contentDeclaration != null) {
			clone.contentDeclaration = contentDeclaration.clone();
		}
		if (qMetaData != null) {
			clone.qMetaData = qMetaData.clone();
		}
		return clone;
	}

	/**
	 * Returns the product exchange of the EPD data set. If the EPD does not
	 * have such a reference exchange it will be directly created when this
	 * method is called.
	 */
	public Exchange productExchange() {
		QuantitativeReference qRef = Processes
				.quantitativeReference(process);
		qRef.type = QuantitativeReferenceType.REFERENCE_FLOWS;
		if (qRef.referenceFlows.isEmpty())
			qRef.referenceFlows.add(1);
		int id = qRef.referenceFlows.get(0);
		for (Exchange exchange : process.exchanges) {
			if (id == exchange.id)
				return exchange;
		}
		Exchange e = Processes.exchange(process);
		e.meanAmount = 1d;
		e.resultingAmount = 1d;
		e.id = id;
		return e;
	}
}
