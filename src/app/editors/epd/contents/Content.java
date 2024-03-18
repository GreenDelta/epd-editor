package app.editors.epd.contents;

import app.App;
import epd.util.Strings;
import org.openlca.ilcd.processes.epd.EpdContentComponent;
import org.openlca.ilcd.processes.epd.EpdContentDeclaration;
import org.openlca.ilcd.processes.epd.EpdContentElement;
import org.openlca.ilcd.processes.epd.EpdContentMaterial;
import org.openlca.ilcd.processes.epd.EpdContentSubstance;
import org.openlca.ilcd.processes.epd.EpdInnerContentElement;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

final class Content {

	private Content() {
	}

	static List<? extends EpdContentElement<?>> childs(
		EpdContentElement<?> elem
	) {
		if (elem instanceof EpdContentComponent comp)
			return comp.getElements();
		if (elem instanceof EpdContentMaterial mat)
			return mat.getSubstances();
		return Collections.emptyList();
	}

	static void remove(EpdContentDeclaration decl, EpdContentElement<?> elem) {
		if (decl == null || elem == null)
			return;
		remove(decl.getElements(), elem);
	}

	private static void remove(
		List<? extends EpdContentElement<?>> list, EpdContentElement<?> elem
	) {
		if (list == null
			|| list.isEmpty()
			|| elem == null
			|| list.remove(elem))
			return;
		for (var other : list) {
			remove(childs(other), elem);
		}
	}

	/**
	 * Returns true when the given element can have a parent in the given
	 * content declaration.
	 */
	static boolean canHaveParent(
		EpdContentElement<?> elem, EpdContentDeclaration decl
	) {
		if (elem == null || decl == null)
			return false;
		if (elem instanceof EpdContentComponent)
			return false;
		for (var candidate : decl.getElements()) {
			if (isPossibleParent(elem, candidate))
				return true;
		}
		return false;
	}

	/**
	 * Returns true when `candidate` can be a parent of `elem`.
	 */
	static boolean isPossibleParent(
		EpdContentElement<?> elem, EpdContentElement<?> candidate) {
		if (elem == null || candidate == null)
			return false;
		if (isPackaging(elem) != isPackaging(candidate))
			return false;
		if (elem instanceof EpdContentMaterial)
			return candidate instanceof EpdContentComponent;
		if (elem instanceof EpdContentSubstance)
			return candidate instanceof EpdContentComponent
				|| candidate instanceof EpdContentMaterial;
		return false;
	}

	static boolean addChild(EpdContentElement<?> parent, EpdContentElement<?> elem) {
		if (!(elem instanceof EpdInnerContentElement<?> inner))
			return false;
		if (!isPossibleParent(elem, parent))
			return false;
		if (parent instanceof EpdContentComponent comp) {
			comp.withElements().add(inner);
			return true;
		}
		if (parent instanceof EpdContentMaterial mat) {
			if (!(elem instanceof EpdContentSubstance subst))
				return false;
			mat.withSubstances().add(subst);
			return true;
		}
		return false;
	}

	static EpdContentElement<?> getParent(
		EpdContentElement<?> elem, EpdContentDeclaration decl
	) {
		if (elem == null || decl == null)
			return null;
		if (elem instanceof EpdContentComponent)
			return null;
		Queue<EpdContentElement<?>> queue = new ArrayDeque<>(decl.getElements());
		while (!queue.isEmpty()) {
			var p = queue.poll();
			if (p == elem)
				continue;
			var childs = childs(p);
			if (childs.contains(elem))
				return p;
			queue.addAll(childs);
		}
		return null;
	}

	static boolean isPackaging(EpdContentElement<?> elem) {
		return elem instanceof EpdInnerContentElement<?> inner
			&& inner.getPackaging() != null
			&& inner.getPackaging();
	}

	/**
	 * Sort the given list of elements by name recursively (so also the child
	 * elements etc.).
	 */
	static void sort(List<? extends EpdContentElement<?>> elems) {
		if (elems == null || elems.isEmpty())
			return;
		elems.sort((e1, e2) -> Strings.compare(App.s(e1.getName()), App.s(e2.getName())));
		for (var e : elems) {
			sort(childs(e));
		}
	}
}
