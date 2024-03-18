package epd.model;

import java.util.Objects;

import org.openlca.ilcd.commons.Copyable;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.QuantitativeReferenceType;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.ProcessName;
import org.openlca.ilcd.util.Processes;

import epd.model.qmeta.QMetaData;

public class EpdDataSet implements Copyable<EpdDataSet> {

	public final Process process;
	public QMetaData qMetaData;

	public EpdDataSet(Process process) {
		this.process = Objects.requireNonNull(process);
	}

	public EpdDataSet() {
		this(new Process());
	}

	public EpdDescriptor toDescriptor(String lang) {
		var d = new EpdDescriptor();
		if (process == null)
			return d;
		d.refId = Processes.getUUID(process);
		ProcessName name = Processes.getProcessName(process);
		if (name != null)
			d.name = LangString.getFirst(name.getBaseName(), lang, "en");
		return d;
	}

	@Override
	public EpdDataSet copy() {
		var clone = new EpdDataSet(process.copy());
		clone.qMetaData = qMetaData != null
			? qMetaData.clone()
			: null;
		return clone;
	}

	/**
	 * Returns the product exchange of the EPD data set. If the EPD does not
	 * have such a reference exchange it will be directly created when this
	 * method is called.
	 */
	public Exchange productExchange() {
		var qRef = process.withProcessInfo()
			.withQuantitativeReference()
			.withType(QuantitativeReferenceType.REFERENCE_FLOWS);
		if (qRef.getReferenceFlows().isEmpty()) {
			qRef.withReferenceFlows().add(1);
		}
		int id = qRef.getReferenceFlows().get(0);
		for (var exchange : process.getExchanges()) {
			if (id == exchange.getId())
				return exchange;
		}
		var e = new Exchange()
			.withId(id)
			.withMeanAmount(1d)
			.withResultingAmount(1d);
		process.withExchanges().add(e);
		return e;
	}
}
