package app.store;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.okworx.ilcd.validation.SchemaValidator;
import com.okworx.ilcd.validation.events.EventsList;
import com.okworx.ilcd.validation.events.IValidationEvent;
import com.okworx.ilcd.validation.util.IUpdateEventListener;

import app.App;

public class Validation implements IRunnableWithProgress, IUpdateEventListener {

	@Override
	public void run(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		// TODO: register EPD profile
		SchemaValidator sv = new SchemaValidator();
		sv.setObjectsToValidate(App.store.getRootFolder());
		sv.setReportSuccesses(true);
		sv.setUpdateEventListener(this);
		sv.validate();
		EventsList events = sv.getEventsList();
		for (IValidationEvent e : events.getEvents()) {
			System.out.println(e.getType());
		}
	}

	@Override
	public void updateProgress(double percentage) {
		// TODO Auto-generated method stub
	}

	@Override
	public void updateStatus(String status) {
		// TODO Auto-generated method stub
	}

}
