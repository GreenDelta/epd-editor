package epd.model.content;

import java.util.ArrayList;
import java.util.List;

import org.openlca.ilcd.commons.Other;

/**
 * Content declaration according to EN 15804/ISO 219301. The content declaration
 * may contain component, material and/or substance elements, which may (but do
 * not have to) be nested.
 */
public class ContentDeclaration {

	/**
	 * A content declaration can contain components, materials, and substances.
	 * Components can in turn contain materials and substances; materials can
	 * contain substances.
	 */
	public final List<ContentElement> content = new ArrayList<>();

	public static ContentDeclaration fromXml(Other other) {
		if (other == null)
			return null;

		// find the root element

	}
}
