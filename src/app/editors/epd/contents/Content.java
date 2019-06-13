package app.editors.epd.contents;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import epd.model.content.Component;
import epd.model.content.ContentDeclaration;
import epd.model.content.ContentElement;
import epd.model.content.Material;
import epd.model.content.Substance;

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

	/**
	 * Returns true when the given element can have a parent in the given
	 * content declaration.
	 */
	static boolean canHaveParent(ContentElement elem,
			ContentDeclaration decl) {
		if (elem == null || decl == null)
			return false;
		if (elem instanceof Component)
			return false;
		for (ContentElement candidate : decl.content) {
			if (isPossibleParent(elem, candidate))
				return true;
		}
		return false;
	}

	/**
	 * Returns true when `candidate` can be a parent of `elem`.
	 */
	static boolean isPossibleParent(ContentElement elem,
			ContentElement candidate) {
		if (elem == null || candidate == null)
			return false;
		if (elem == candidate)
			return false;
		if (elem instanceof Component)
			return false;

		if (elem instanceof Material) {
			Material mat = (Material) elem;
			if (mat.packaging != null && mat.packaging)
				return false;
			return candidate instanceof Component;
		}

		if (elem instanceof Substance) {

			// substance of packaging materials
			Substance subst = (Substance) elem;
			if (subst.packaging != null && subst.packaging) {
				if (!(candidate instanceof Material))
					return false;
				Material mat = (Material) candidate;
				return mat.packaging != null && mat.packaging;
			}

			// some other substances
			return candidate instanceof Component
					|| candidate instanceof Material;
		}
		return false;
	}

	static boolean addChild(ContentElement parent, ContentElement elem) {
		if (!isPossibleParent(elem, parent))
			return false;
		if (parent instanceof Component) {
			((Component) parent).content.add(elem);
			return true;
		}
		if (parent instanceof Material) {
			if (!(elem instanceof Substance))
				return false;
			((Material) parent).substances.add((Substance) elem);
			return true;
		}
		return false;
	}

	static ContentElement getParent(ContentElement elem,
			ContentDeclaration decl) {
		if (elem == null || decl == null)
			return null;
		if (elem instanceof Component)
			return null;
		Queue<ContentElement> queue = new ArrayDeque<>();
		queue.addAll(decl.content);
		while (!queue.isEmpty()) {
			ContentElement p = queue.poll();
			if (p == elem)
				continue;
			if (childs(p).contains(elem))
				return p;
			queue.addAll(childs(p));
		}
		return null;
	}
}
