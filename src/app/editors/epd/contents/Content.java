package app.editors.epd.contents;

import java.util.Collections;
import java.util.List;

import epd.model.content.Component;
import epd.model.content.ContentDeclaration;
import epd.model.content.ContentElement;
import epd.model.content.Material;

final class Content {

	private Content() {
	}

	static List<? extends ContentElement> childs(ContentElement elem) {
		if (elem instanceof Component)
			return ((Component) elem).content;
		if (elem instanceof Material)
			return ((Material) elem).substances;
		return Collections.emptyList();
	}

	static void remove(ContentDeclaration decl, ContentElement elem) {
		if (decl == null || elem == null)
			return;
		remove(decl.content, elem);
	}

	private static void remove(List<? extends ContentElement> list,
			ContentElement elem) {
		if (list == null || list.isEmpty() || elem == null)
			return;
		list.remove(elem);
		for (ContentElement other : list) {
			remove(childs(other), elem);
		}
	}
}
