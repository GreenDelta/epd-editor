package app.editors.epd.results;

import java.util.List;

import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.epd.EpdProfileIndicator;
import org.openlca.ilcd.processes.epd.EpdResultExtension;
import org.openlca.ilcd.processes.epd.EpdValue;

import app.App;

interface IndicatorResult {

	Double[] modValues();

	Ref indicator();

	EpdProfileIndicator profileIndicator();

	default String getIndicatorName() {
		var pi = profileIndicator();
		return pi != null && pi.getRef() != null
				? App.s(pi.getRef())
				: App.s(indicator());
	}

	default String getIndicatorCode() {
		var pi = profileIndicator();
		return pi != null
				? pi.getCode()
				: null;
	}

	default String getIndicatorUnit() {
		var pi = profileIndicator();
		return pi != null && pi.getUnit() != null
				? App.s(pi.getUnit())
				: App.s(ext().getUnitGroup());
	}

	default Double getModValueAt(int i) {
		var vals = modValues();
		return i >= 0 && i < vals.length
				? vals[i]
				: null;
	}

	default List<EpdValue> getValues() {
		return ext().withValues();
	}

	default void setValue(Mod mod, int i, Double value) {
		modValues()[i] = value;

		var epdVals = getValues();
		EpdValue epdVal = null;
		for (var v : epdVals) {
			if (mod.matches(v)) {
				epdVal = v;
				break;
			}
		}

		if (epdVal != null) {
			epdVal.withAmount(value);
			return;
		}
		if (value == null)
			return;
		epdVal = new EpdValue()
				.withModule(mod.module())
				.withScenario(mod.scenario())
				.withAmount(value);
		epdVals.add(epdVal);
	}

	EpdResultExtension ext();

}
