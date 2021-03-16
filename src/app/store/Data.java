package app.store;

import java.util.function.Consumer;

import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.util.Contacts;
import org.openlca.ilcd.util.FlowProperties;
import org.openlca.ilcd.util.Flows;
import org.openlca.ilcd.util.Methods;
import org.openlca.ilcd.util.Processes;
import org.openlca.ilcd.util.Sources;
import org.openlca.ilcd.util.UnitGroups;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.navi.Sync;
import epd.io.conversion.Extensions;
import epd.io.conversion.FlowExtensions;
import epd.model.EpdDataSet;
import epd.model.EpdProduct;
import epd.model.EpdProfile;
import epd.model.Version;
import epd.model.Xml;

public final class Data {

	private Data() {
	}

	public static EpdDataSet getEPD(Ref ref) {
		try {
			Process process = App.store.get(Process.class, ref.uuid);
			EpdProfile profile = EpdProfiles.get(process);
			EpdDataSet dataSet = Extensions.read(process, profile);
			return dataSet;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Data.class);
			log.error("failed to open EPD data set " + ref, e);
			return null;
		}
	}

	public static void save(EpdDataSet epd) {
		if (epd == null)
			return;
		try {
			XmlCleanUp.on(epd);
			Extensions.write(epd);
			save(epd.process);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Data.class);
			log.error("failed to save EPD data set " + epd, e);
		}
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
		try {
			Ref ref = Ref.of(ds);
			var workspace = App.getWorkspace();
			workspace.store.put(ds);
			workspace.index.remove(ref);
			workspace.index.add(ds);
			workspace.saveIndex();
			RefTrees.cache(ds);
			new Sync(workspace.index).run();
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
			workspace.store.delete(ref.getDataSetClass(), ref.uuid);
			workspace.index.remove(ref);
			workspace.saveIndex();
			new Sync(workspace.index).run();
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
			return store.get(ref.getDataSetClass(), ref.uuid);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Data.class);
			log.error("failed to load data set " + ref, e);
			return null;
		}
	}

	public static void updateVersion(EpdDataSet ds) {
		if (ds == null || ds.process == null)
			return;
		updateVersion(ds.process);
	}

	public static void updateVersion(IDataSet ds) {
		if (ds instanceof Process) {
			Process p = (Process) ds;
			with(Processes.publication(p), pub -> {
				pub.version = Version.fromString(pub.version)
						.incUpdate().toString();
			});
			Processes.dataEntry(p).timeStamp = Xml.now();
		}

		if (ds instanceof Flow) {
			Flow f = (Flow) ds;
			with(Flows.publication(f), pub -> {
				pub.version = Version.fromString(pub.version)
						.incUpdate().toString();
			});
			Flows.dataEntry(f).timeStamp = Xml.now();
		}

		if (ds instanceof FlowProperty) {
			FlowProperty fp = (FlowProperty) ds;
			with(FlowProperties.publication(fp), pub -> {
				pub.version = Version.fromString(pub.version)
						.incUpdate().toString();
			});
			FlowProperties.dataEntry(fp).timeStamp = Xml.now();
		}

		if (ds instanceof UnitGroup) {
			UnitGroup ug = (UnitGroup) ds;
			with(UnitGroups.publication(ug), pub -> {
				pub.version = Version.fromString(pub.version)
						.incUpdate().toString();
			});
			UnitGroups.dataEntry(ug).timeStamp = Xml.now();
		}

		if (ds instanceof Contact) {
			Contact c = (Contact) ds;
			with(Contacts.publication(c), pub -> {
				pub.version = Version.fromString(pub.version)
						.incUpdate().toString();
			});
			Contacts.dataEntry(c).timeStamp = Xml.now();
		}

		if (ds instanceof Source) {
			Source s = (Source) ds;
			with(Sources.publication(s), pub -> {
				pub.version = Version.fromString(pub.version)
						.incUpdate().toString();
			});
			Sources.dataEntry(s).timeStamp = Xml.now();
		}

		if (ds instanceof LCIAMethod) {
			LCIAMethod l = (LCIAMethod) ds;
			with(Methods.publication(l), pub -> {
				pub.version = Version.fromString(pub.version)
						.incUpdate().toString();
			});
			Methods.dataEntry(l).timeStamp = Xml.now();
		}
	}

	/** Just a trick to avoid type declarations. */
	private static <T> void with(T obj, Consumer<T> fn) {
		if (obj != null) {
			fn.accept(obj);
		}
	}
}
