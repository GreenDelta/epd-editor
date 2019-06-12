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
		switch (this) {
		case COMPONENT:
			return new Component();
		case MATERIAL:
			return new Material();
		case SUBSTANCE:
			return new Substance();
		default:
			throw new IllegalStateException(
					"unknown content type");
		}
	}

}
