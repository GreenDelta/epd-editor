package epd.model.content;

import java.util.ArrayList;
import java.util.List;

public class Component extends ContentElement {

	/**
	 * A component can contain materials and substances.
	 */
	public final List<ContentElement> content = new ArrayList<>();

}
