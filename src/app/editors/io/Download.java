package app.editors.io;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.io.SodaClient;

class Download implements IRunnableWithProgress {

	private final SodaClient client;
	boolean withDependencies = false;
	boolean overwriteExisting = false;

	private final Queue<Ref> queue = new ArrayDeque<>();
	private final Set<Ref> handled = new HashSet<>();

	Download(SodaClient client, List<Ref> refs) {
		this.client = client;
		queue.addAll(refs);
	}

	@Override
	public void run(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		monitor.beginTask("#Download data sets", IProgressMonitor.UNKNOWN);

	}

}
