package app.editors.epd.contents;

import epd.model.content.Component;
import epd.model.content.ContentElement;
import epd.model.content.Material;
import epd.model.content.Substance;

enum ContentType {

	COMPONENT,

	MATERIAL,

	SUBSTANCE;

	ContentElement newInstance() {
		return switch (this) {
			case COMPONENT -> new Component();
			case MATERIAL -> new Material();
			case SUBSTANCE -> new Substance();
		};
	}

}
