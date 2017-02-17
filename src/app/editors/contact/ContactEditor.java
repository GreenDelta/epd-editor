package app.editors.contact;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.util.Contacts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.editors.BaseEditor;
import app.editors.DependencyPage;
import app.editors.Editors;
import app.editors.RefEditorInput;
import app.editors.XmlPage;
import app.store.Data;
import epd.model.Version;
import epd.model.Xml;

public class ContactEditor extends BaseEditor {

	private static final String ID = "contact.editor";

	public Contact contact;

	public static void open(Ref ref) {
		if (ref == null)
			return;
		RefEditorInput input = new RefEditorInput(ref);
		Editors.open(input, ID);
	}

	@Override
	public void init(IEditorSite s, IEditorInput input)
			throws PartInitException {
		super.init(s, input);
		Editors.setTabTitle(input, this);
		try {
			RefEditorInput in = (RefEditorInput) input;
			contact = App.store.get(Contact.class, in.ref.uuid);
		} catch (Exception e) {
			throw new PartInitException(
					"Failed to open editor: no correct input", e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			updateVersion();
			Data.update(contact);
			for (Runnable handler : saveHandlers) {
				handler.run();
			}
			dirty = false;
			editorDirtyStateChanged();
			Editors.setTabTitle(contact, this);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to save contact data set");
		}
	}

	private void updateVersion() {
		Publication pub = Contacts.publication(contact);
		Version v = Version.fromString(pub.version);
		v.incUpdate();
		pub.version = v.toString();
		Contacts.dataEntry(contact).timeStamp = Xml.now();
	}

	@Override
	protected void addPages() {
		try {
			addPage(new ContactPage(this));
			addPage(new DependencyPage(this, contact));
			addPage(new XmlPage(this, contact));
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to add page", e);
		}
	}
}
