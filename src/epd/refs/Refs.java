package epd.refs;


import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.processes.Process;

public final class Refs {

	private Refs() {
	}

	public static List<Ref> allEditableOf(IDataSet ds) {
		return DataSetRefs.allEditableOf(ds);
	}

	public static List<Ref> allUploadableOf(IDataSet ds) {
		if (!(ds instanceof Process epd))
			return allEditableOf(ds);
		return EpdRefs.of(epd)
			.declaredProduct()
			.externalDocumentation()
			.flowDiagramsAndPictures()
			.methodDetails()
			.dataQualitySources()
			.dataSources()
			.complianceSystems()
			.originalEpds()
			.reviewersAndReviewReports()
			.commissioners()
			.dataDocumentor()
			.dataGenerators()
			.dataFormats()
			.registrationAuthorities()
			.owner()
			.publishers()
			.asList();
	}

	public static List<Ref> allDownloadableOf(IDataSet ds) {
		return allUploadableOf(ds);
	}

	/**
	 * Collects transitively all editable dataset dependencies starting from
	 * the given dataset reference using the given collector method. The
	 * returned set will also contain that "root" reference if it is a valid
	 * reference.
	 */
	public static Set<Ref> allDependenciesOf(
		DataStore store, Ref root, Function<IDataSet, List<Ref>> collector
	) {
		if (store == null || root == null || !root.isValid())
			return Collections.emptySet();

		var handled = new HashSet<Ref>();
		handled.add(root);
		var queue = new ArrayDeque<Ref>();
		queue.add(root);
		var dependencies = new HashSet<Ref>();

		while (!queue.isEmpty()) {
			var ref = queue.poll();
			var ds = store.get(ref.getDataSetClass(), ref.getUUID());
			if (ds == null)
				continue;
			dependencies.add(Ref.of(ds));
			for (var nextDep : collector.apply(ds)) {
				if (!nextDep.isValid() || handled.contains(nextDep))
					continue;
				queue.add(nextDep);
				handled.add(nextDep);
			}
		}
		return dependencies;
	}

	static <T> void addAll(List<Ref> refs, T obj, Function<T, List<Ref>> fn) {
		if (obj == null)
			return;
		var list = fn.apply(obj);
		if (list == null || list.isEmpty())
			return;
		refs.addAll(list);
	}

	static <T> void add(List<Ref> refs, T obj, Function<T, Ref> fn) {
		if (obj == null)
			return;
		var ref = fn.apply(obj);
		if (ref != null) {
			refs.add(ref);
		}
	}

}
