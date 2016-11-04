package app.wizards;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.openlca.ilcd.commons.Ref;

import app.util.UI;

public class EpdWizard extends Wizard {

	protected EpdWizardPage page;

	public static void open() {
		WizardDialog wizardDialog = new WizardDialog(UI.shell(),
				new EpdWizard());
		wizardDialog.open();
	}

	@Override
	public void addPages() {
		page = new EpdWizardPage();
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		String name = page.getEpdName();
		Ref flow = page.getFlow();
		new EpdCreationJob(name, flow).schedule();
		return true;
	}
}
