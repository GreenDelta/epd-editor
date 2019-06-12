package epd.model.content;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import org.openlca.ilcd.io.XmlBinder;
import org.openlca.ilcd.processes.Process;

import epd.io.conversion.Extensions;
import epd.model.EpdDataSet;
import epd.model.EpdProfile;

public class Main {
	public static void main(String[] args) throws Exception {
		String path = "C:\\Users\\ms\\Projects\\_current\\epd_editor\\sample_EPD.xml";
		XmlBinder binder = new XmlBinder();
		Process p = binder.fromFile(Process.class, new File(path));
		EpdDataSet ds = Extensions.read(p, new EpdProfile());

		ContentDeclaration decl = ds.contentDeclaration;
		Queue<ContentElement> queue = new ArrayDeque<>();
		queue.addAll(decl.content);

		while (!queue.isEmpty()) {
			ContentElement elem = queue.poll();
			// System.out.println(
			// elem.getClass() + ": " + elem.name + ":: " + elem.massPerc);
			queue.addAll(childs(elem));
		}

		StringWriter w = new StringWriter();
		ds = ds.clone();
		Extensions.write(ds);
		binder.toWriter(ds.process, w);
		System.out.println(w.toString().split("</epd:safetyMargins>")[1]
				.split("<quantitativeReference>")[0]);
	}

	private static List<? extends ContentElement> childs(ContentElement elem) {
		if (elem instanceof Component)
			return ((Component) elem).content;
		if (elem instanceof Material)
			return ((Material) elem).substances;
		return Collections.emptyList();
	}
}
