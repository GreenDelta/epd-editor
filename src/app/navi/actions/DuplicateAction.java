package app.navi.actions;

import java.util.UUID;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.util.Contacts;

import app.App;
import app.M;
import app.navi.RefElement;
import app.rcp.Icon;
import app.util.UI;

public class DuplicateAction extends Action {

	private final RefElement e;

	public DuplicateAction(RefElement e) {
		this.e = e;
		setText("#Duplicate");
		setImageDescriptor(Icon.SAVE_AS.des());
	}

	@Override
	public void run() {
		if (e == null || e.ref == null || !e.ref.isValid())
			return;
		InputDialog d = new InputDialog(UI.shell(), M.SaveAs,
				"#Save as new data set with the following name:",
				"#Copy of " + App.s(e.ref.name), null);
		if (d.open() != Window.OK)
			return;
		String name = d.getValue();
		try {
			switch (e.ref.type) {
			case CONTACT:
				contact(name, e.ref);
				break;

			default:
				break;
			}

		} catch (Exception e) {

		}

		System.out.println("#TODO: save data set " + e.ref + " as " + name);
	}

	private void contact(String name, Ref ref) throws Exception {
		Contact contact = App.store.get(Contact.class, ref.uuid);
		var info = Contacts.dataSetInfo(contact);
		info.uuid = UUID.randomUUID().toString();
		LangString.set(info.name, name, App.lang());

	}

}
