package app.store;

import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.methods.ImpactMethod;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.navi.NaviSync;
import epd.io.conversion.FlowExtensions;
import epd.model.EpdProduct;
import epd.model.Version;
import epd.model.Xml;

public final class Data {

	private Data() {
	}

	public static void save(EpdProduct product) {
		if (product == null)
			return;
		try {
			FlowExtensions.write(product);
			save(product.flow);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Data.class);
			log.error("failed to save flow " + product, e);
		}
	}

	public static void save(IDataSet ds) {
		if (ds == null)
			return;
		if (ds instanceof Process p) {
			p.withEpdVersion("1.2");
		}
		try {
			Ref ref = Ref.of(ds);
			var workspace = App.getWorkspace();
			workspace.store.put(ds);

			var index = workspace.index();
			index.remove(ref);
			index.add(ds);
			workspace.saveIndex();

			RefTrees.cache(ds);
			new NaviSync(workspace.index()).run();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Data.class);
			log.error("Failed to update data set: " + ds, e);
		}
	}

	public static void delete(Ref ref) {
		if (ref == null)
			return;
		try {
			var workspace = App.getWorkspace();
			workspace.store.delete(ref.getDataSetClass(), ref.getUUID());
			workspace.index().remove(ref);
			workspace.saveIndex();
			new NaviSync(workspace.index()).run();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Data.class);
			log.error("failed to delete data set " + ref, e);
		}
	}

	public static IDataSet load(Ref ref) {
		if (ref == null || !ref.isValid())
			return null;
		try {
			var store = App.getWorkspace().store;
			return store.get(ref.getDataSetClass(), ref.getUUID());
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Data.class);
			log.error("failed to load data set " + ref, e);
			return null;
		}
	}

	public static void updateVersion(IDataSet ds) {
		if (ds instanceof Process x) {
			var pub = x.withAdminInfo().withPublication();
			var v = Version.fromString(pub.getVersion())
				.incUpdate()
				.toString();
			pub.withVersion(v);
			x.withAdminInfo()
				.withDataEntry()
				.withTimeStamp(Xml.now());
		}

		if (ds instanceof Flow x) {
			var pub = x.withAdminInfo().withPublication();
			var v = Version.fromString(pub.getVersion())
				.incUpdate()
				.toString();
			pub.withVersion(v);
			x.withAdminInfo()
				.withDataEntry()
				.withTimeStamp(Xml.now());
		}

		if (ds instanceof FlowProperty x) {
			var pub = x.withAdminInfo().withPublication();
			var v = Version.fromString(pub.getVersion())
				.incUpdate()
				.toString();
			pub.withVersion(v);
			x.withAdminInfo()
				.withDataEntry()
				.withTimeStamp(Xml.now());
		}

		if (ds instanceof UnitGroup x) {
			var pub = x.withAdminInfo().withPublication();
			var v = Version.fromString(pub.getVersion())
				.incUpdate()
				.toString();
			pub.withVersion(v);
			x.withAdminInfo()
				.withDataEntry()
				.withTimeStamp(Xml.now());
		}

		if (ds instanceof Contact x) {
			var pub = x.withAdminInfo().withPublication();
			var v = Version.fromString(pub.getVersion())
				.incUpdate()
				.toString();
			pub.withVersion(v);
			x.withAdminInfo()
				.withDataEntry()
				.withTimeStamp(Xml.now());
		}

		if (ds instanceof Source x) {
			var pub = x.withAdminInfo().withPublication();
			var v = Version.fromString(pub.getVersion())
				.incUpdate()
				.toString();
			pub.withVersion(v);
			x.withAdminInfo()
				.withDataEntry()
				.withTimeStamp(Xml.now());
		}

		if (ds instanceof ImpactMethod x) {
			var pub = x.withAdminInfo().withPublication();
			var v = Version.fromString(pub.getVersion())
				.incUpdate()
				.toString();
			pub.withVersion(v);
			x.withAdminInfo()
				.withDataEntry()
				.withTimeStamp(Xml.now());
		}
	}

}
