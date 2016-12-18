package app.editors.contact;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.openlca.ilcd.commons.AdminInfo;
import org.openlca.ilcd.commons.DataEntry;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.contacts.ContactInfo;
import org.openlca.ilcd.contacts.DataSetInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.editors.BaseEditor;
import app.editors.DependencyPage;
import app.editors.Editors;
import app.editors.RefEditorInput;
import epd.model.Version;
import epd.model.Xml;
import epd.util.Strings;

public class ContactEditor extends BaseEditor {

	private static final String ID = "contact.editor";

	Contact contact;

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
		setPartName(Strings.cut(input.getName(), 75));
		try {
			RefEditorInput in = (RefEditorInput) input;
			contact = App.store.get(Contact.class, in.ref.uuid);
			initStructs();
		} catch (Exception e) {
			throw new PartInitException(
					"Failed to open editor: no correct input", e);
		}
	}

	private void initStructs() {
		if (contact == null)
			contact = new Contact();
		if (contact.adminInfo == null)
			contact.adminInfo = new AdminInfo();
		if (contact.adminInfo.dataEntry == null)
			contact.adminInfo.dataEntry = new DataEntry();
		if (contact.adminInfo.publication == null)
			contact.adminInfo.publication = new Publication();
		if (contact.contactInfo == null)
			contact.contactInfo = new ContactInfo();
		if (contact.contactInfo.dataSetInfo == null)
			contact.contactInfo.dataSetInfo = new DataSetInfo();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			updateVersion();
			App.store.put(contact, contact.getUUID());
			// TODO: navigation refresh
			for (Runnable handler : saveHandlers) {
				handler.run();
			}
			dirty = false;
			editorDirtyStateChanged();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to save contact data set");
		}
	}

	private void updateVersion() {
		AdminInfo info = contact.adminInfo;
		Version v = Version.fromString(info.publication.version);
		v.incUpdate();
		info.publication.version = v.toString();
		info.dataEntry.timeStamp = Xml.now();
	}

	@Override
	protected void addPages() {
		try {
			addPage(new ContactPage(this));
			addPage(new DependencyPage(this, contact));
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to add page", e);
		}
	}
}
