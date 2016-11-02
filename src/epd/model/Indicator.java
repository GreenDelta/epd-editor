package epd.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Environmental indicators defined in the DIN 15804 (see section 7.2). Each
 * indicator contains the field values that are necessary for serializing the
 * indicator results into the extended ILCD format. Inventory indicators are
 * serialized as exchanges and impact assessment indicators are serialized as
 * LCIA results in an ILCD data set.
 */
public enum Indicator {

	ABIOTIC_RESOURCE_DEPLETION_ELEMENTS(
										false,
										IndicatorGroup.RESOURCE_USE,
										"kg Sb eq."),

	ABIOTIC_RESOURCE_DEPLETION_FOSSIL_FUELS(
											false,
											IndicatorGroup.RESOURCE_USE,
											"MJ"),

	ACIDIFICATION(
					false,
					IndicatorGroup.ENVIRONMENTAL,
					"kg SO2 eq."),

	COMPONENTS_REUSE(
						true,
						IndicatorGroup.OUTPUT_FLOWS,
						"kg"),

	EUTROPHICATION(
					false,
					IndicatorGroup.ENVIRONMENTAL,
					"kg (PO4)3- eq."),

	EXPORTED_ENERGY_ELECTRICAL(
								true,
								IndicatorGroup.OUTPUT_FLOWS,
								"MJ"),

	EXPORTED_ENERGY_THERMAL(
							true,
							IndicatorGroup.OUTPUT_FLOWS,
							"MJ"),

	GLOBAL_WARMING(
					false,
					IndicatorGroup.ENVIRONMENTAL,
					"kg CO2 eq."),

	HAZARDOUS_WASTE(
					true,
					IndicatorGroup.WASTE_DISPOSAL,
					"kg"),

	MATERIALS_ENERGY_RECOVERY(
								true,
								IndicatorGroup.OUTPUT_FLOWS,
								"kg"),

	MATERIALS_RECYCLING(
						true,
						IndicatorGroup.OUTPUT_FLOWS,
						"kg"),

	NET_FRESH_WATER(
					true,
					IndicatorGroup.RESOURCE_USE,
					"m3"),

	NON_HAZARDOUS_WASTE(
						true,
						IndicatorGroup.WASTE_DISPOSAL,
						"kg"),

	NON_RENEWABLE_PRIMARY_ENERGY_NON_RAW_MATERIALS(
													true,
													IndicatorGroup.RESOURCE_USE,
													"MJ"),

	NON_RENEWABLE_PRIMARY_ENERGY_RAW_MATERIALS(
												true,
												IndicatorGroup.RESOURCE_USE,
												"MJ"),

	NON_RENEWABLE_SECONDARY_FUELS(
									true,
									IndicatorGroup.RESOURCE_USE,
									"MJ"),

	OZONE_DEPLETION(
					false,
					IndicatorGroup.ENVIRONMENTAL,
					"kg CFC 11 eq."),

	PHOTOCHEMICAL_OZONE_CREATION(
									false,
									IndicatorGroup.ENVIRONMENTAL,
									"kg C2H2 eq."),

	RADIOACTIVE_WASTE_DISPOSED(
								true,
								IndicatorGroup.WASTE_DISPOSAL,
								"kg"),

	RENEWABLE_PRIMARY_ENERGY_NON_RAW_MATERIALS(
												true,
												IndicatorGroup.RESOURCE_USE,
												"MJ"),

	RENEWABLE_PRIMARY_ENERGY_RAW_MATERIALS(
											true,
											IndicatorGroup.RESOURCE_USE,
											"MJ"),

	RENEWABLE_SECONDARY_FUELS(
								true,
								IndicatorGroup.RESOURCE_USE,
								"MJ"),

	SECONDARY_MATERIAL(
						true,
						IndicatorGroup.RESOURCE_USE,
						"kg"),

	TOTAL_NON_RENEWABLE_PRIMARY_ENERGY(
										true,
										IndicatorGroup.RESOURCE_USE,
										"MJ"),

	TOTAL_RENEWABLE_PRIMARY_ENERGY(
									true,
									IndicatorGroup.RESOURCE_USE,
									"MJ");

	private final boolean inventoryIndicator;
	private final IndicatorGroup group;
	private final String unit;

	private Indicator(boolean lci, IndicatorGroup category, String unit) {
		this.inventoryIndicator = lci;
		this.group = category;
		this.unit = unit;
	}

	/**
	 * Returns true when the indicator is an inventory indicator and should be
	 * serialized as exchange. Otherwise it is an impact assessment indicator
	 * and should be serialized as LCIA result.
	 */
	public boolean isInventoryIndicator() {
		return inventoryIndicator;
	}

	public IndicatorGroup getGroup() {
		return group;
	}

	/**
	 * Returns the indicator unit as defined in DIN 15084.
	 */
	public String getUnit() {
		return unit;
	}

	public static List<Indicator> getIndicators(IndicatorGroup group) {
		List<Indicator> list = new ArrayList<>();
		for (Indicator indicator : values()) {
			if (indicator.group == group)
				list.add(indicator);
		}
		return list;
	}

}
