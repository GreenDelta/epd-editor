package app.rcp;

import java.io.File;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.progress.IProgressService;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.io.ZipStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.M;
import app.SearchPage;
import app.StatusView;
import app.editors.TranslationView;
import app.editors.indicators.IndicatorMappingEditor;
import app.editors.matprops.MaterialPropertyEditor;
import app.editors.settings.SettingsPage;
import app.navi.Navigator;
import app.navi.TypeElement;
import app.navi.actions.NewDataSetAction;
import app.store.CleanUp;
import app.store.IndexBuilder;
import app.store.ZipExport;
import app.store.ZipImport;
import app.store.validation.Validation;
import app.util.Actions;
import app.util.FileChooser;
import app.util.MsgBox;

public class ActionBar extends ActionBarAdvisor {

	private IWorkbenchAction save;
	private IWorkbenchAction saveAs;
	private IWorkbenchAction saveAll;

	public ActionBar(IActionBarConfigurer conf) {
		super(conf);
	}

	@Override
	protected void makeActions(IWorkbenchWindow window) {
		save = ActionFactory.SAVE.create(window);
		saveAs = ActionFactory.SAVE_AS.create(window);
		saveAll = ActionFactory.SAVE_ALL.create(window);
	}

	@Override
	protected void fillMenuBar(IMenuManager menuBar) {
		super.fillMenuBar(menuBar);
		MenuManager fileMenu = new MenuManager(M.File,
				IWorkbenchActionConstants.M_FILE);
		addNewMenu(fileMenu);
		fileMenu.add(Actions.create(M.ValidateDataSets,
				Icon.OK.des(), this::validateStore));
		fileMenu.add(Actions.create(M.ImportDataPackage,
				Icon.IMPORT.des(), this::importZip));
		fileMenu.add(Actions.create(M.ExportDataPackage,
				Icon.EXPORT.des(), this::exportZip));
		menuBar.add(fileMenu);
		menuBar.add(editMenu());
	}

	private MenuManager editMenu() {
		MenuManager m = new MenuManager(M.Edit,
				IWorkbenchActionConstants.M_EDIT);
		m.add(Actions.create(M.Settings,
				Icon.SETTINGS.des(), SettingsPage::open));
		m.add(Actions.create(M.TranslationView,
				Icon.MESSAGE.des(), TranslationView::open));
		m.add(Actions.create(M.MaterialProperties,
				Icon.QUANTITY.des(), MaterialPropertyEditor::open));
		m.add(Actions.create(M.IndicatorMappings,
				Icon.QUANTITY.des(), IndicatorMappingEditor::open));
		m.add(new Separator());
		m.add(Actions.create(M.ReloadNavigation,
				Icon.RELOAD.des(), () -> App.run(new IndexBuilder())));
		m.add(Actions.create(M.DeleteAllDataSets,
				Icon.DELETE.des(), this::cleanUp));
		return m;
	}

	private void addNewMenu(MenuManager fileMenu) {
		MenuManager mm = new MenuManager(M.New, Icon.NEW_DATA_SET.des(), M.New);
		fileMenu.add(mm);
		DataSetType[] types = { DataSetType.PROCESS, DataSetType.CONTACT,
				DataSetType.SOURCE, DataSetType.FLOW, DataSetType.FLOW_PROPERTY,
				DataSetType.UNIT_GROUP, DataSetType.LCIA_METHOD };
		for (DataSetType type : types) {
			TypeElement navElem = Navigator.getTypeRoot(type);
			Action a = new NewDataSetAction(navElem);
			mm.add(a);
		}
	}

	@Override
	protected void fillCoolBar(ICoolBarManager coolBar) {
		super.fillCoolBar(coolBar);
		IToolBarManager toolbar = new ToolBarManager(SWT.FLAT | SWT.LEFT);
		coolBar.add(new ToolBarContributionItem(toolbar, "main"));
		toolbar.add(Actions.create(M.Search, Icon.SEARCH.des(),
				SearchPage::open));
		toolbar.add(save);
		toolbar.add(saveAs);
		toolbar.add(saveAll);

	}

	private void cleanUp() {
		boolean b = MsgBox.ask("#Really delete all data sets?",
				"#Do you really want to delete all data sets?");
		if (!b)
			return;
		App.run(new CleanUp());
	}

	private void importZip() {
		File zipFile = FileChooser.open("*.zip");
		if (zipFile == null)
			return;
		boolean b = MsgBox.ask("#Import data sets?", "#Should we import all "
				+ "data sets from the selected file?");
		if (!b)
			return;
		IProgressService progress = PlatformUI.getWorkbench()
				.getProgressService();
		try {
			ZipStore zip = new ZipStore(zipFile);
			progress.run(true, true, new ZipImport(zip));
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to import data sets", e);
			MsgBox.error("#Data export failed: " + e.getMessage());
		}
	}

	private void exportZip() {
		File zipFile = FileChooser.save("ILCD_package", "*.zip");
		if (zipFile == null)
			return;
		IProgressService progress = PlatformUI.getWorkbench()
				.getProgressService();
		try {
			progress.run(true, true, new ZipExport(zipFile));
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to export data sets", e);
			MsgBox.error("#Data export failed: " + e.getMessage());
		}
	}

	private void validateStore() {
		if (App.settings().validationProfile == null) {
			MsgBox.error(M.NoValidationProfile, M.NoValidationProfile_Error);
			return;
		}
		IProgressService progress = PlatformUI.getWorkbench()
				.getProgressService();
		try {
			Validation v = new Validation();
			progress.run(true, true, v);
			StatusView.open(M.Validation, v.getStatus());
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to validate data sets", e);
			MsgBox.error("#Data validation failed: " + e.getMessage());
		}
	}
}
