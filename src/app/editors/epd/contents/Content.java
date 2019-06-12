package app.editors.epd.contents;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

import epd.model.content.Component;
import epd.model.content.ContentDeclaration;
import epd.model.content.ContentElement;
import epd.model.content.Material;

final class Content {

	private Content() {
	}

	static boolean contains(ContentDeclaration decl, ContentElement elem) {
		if (decl == null || elem == null)
			return false;
		boolean found = false;
		Queue<ContentElement> queue = new ArrayDeque<>();
		queue.addAll(decl.content);
		while (!queue.isEmpty()) {
			ContentElement e = queue.poll();
			if (Objects.equals(e, elem)) {
				found = true;
				break;
			}
			queue.addAll(childs(elem));
		}
		return found;
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
