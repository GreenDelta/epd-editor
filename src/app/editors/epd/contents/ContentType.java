package app.editors.epd.contents;

import org.openlca.ilcd.processes.epd.EpdContentComponent;
import org.openlca.ilcd.processes.epd.EpdContentElement;
import org.openlca.ilcd.processes.epd.EpdContentMaterial;
import org.openlca.ilcd.processes.epd.EpdContentSubstance;

enum ContentType {

	COMPONENT,

	MATERIAL,

	SUBSTANCE;

	EpdContentElement<?> newInstance() {
		return switch (this) {
			case COMPONENT -> new EpdContentComponent();
			case MATERIAL -> new EpdContentMaterial();
			case SUBSTANCE -> new EpdContentSubstance();
		};
	}

}
