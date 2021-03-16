package app.editors.flow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.processes.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.M;
import app.editors.RefTableLabel;
import app.rcp.Icon;
import app.store.Data;
import app.store.RefTrees;
import app.util.MsgBox;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;
import epd.index.RefSync;
import epd.util.Strings;

class FlowUpdateCheck {

	static void with(Flow flow) {
		if (flow == null)
			return;
		if (!App.settings().checkEPDsOnProductUpdates)
			return;
		List<Ref> usages = new ArrayList<>();
		App.run("Check flow usage ...",
				() -> findUsages(flow, usages),
				() -> {
					if (usages.isEmpty())
						return;
					Dialog.show(flow, usages);
				});
	}

	private static void findUsages(Flow flow, List<Ref> usages) {
		String flowID = flow.getUUID();
		if (flowID == null)
			return;
		App.index().getRefs()
				.stream()
				.filter(ref -> ref.type == DataSetType.PROCESS)
				.forEach(ref -> {
					try {
						AtomicBoolean b = new AtomicBoolean(false);
						RefTrees.get(ref).eachRef(pRef -> {
							if (b.get())
								return;
							if (pRef.type == DataSetType.FLOW
									&& Objects.equals(flowID, pRef.uuid)) {
								usages.add(ref);
								b.set(true);
							}
						});
					} catch (Exception e) {
						Logger log = LoggerFactory
								.getLogger(FlowUpdateCheck.class);
						log.error("Failed to load process/EPD " + ref, e);
					}
				});
	}

	private static class Dialog extends Wizard {

		private final Flow flow;
		private final List<Ref> usages;
		private final HashMap<String, Boolean> selected;

		static void show(Flow flow, List<Ref> usages) {
			Dialog d = new Dialog(flow, usages);
			d.setWindowTitle(M.UpdateReferences);
			WizardDialog dialog = new WizardDialog(UI.shell(), d);
			dialog.setPageSize(150, 300);
			dialog.open();
		}

		private Dialog(Flow flow, List<Ref> usages) {
			this.flow = flow;
			usages.sort((r1, r2) -> Strings.compare(
				App.s(r1.name), App.s(r2.name)));
			selected = new HashMap<>();
			for (Ref ref : usages) {
				selected.put(ref.uuid, Boolean.TRUE);
			}
			this.usages = usages;
			setNeedsProgressMonitor(true);
		}

		@Override
		public boolean performFinish() {
			List<Ref> updates = new ArrayList<>();
			for (Ref ref : usages) {
				if (Boolean.TRUE.equals(selected.get(ref.uuid))) {
					updates.add(ref);
				}
			}
			if (updates.isEmpty())
				return true;
			try {
				getContainer().run(true, true, monitor -> {
					monitor.beginTask("Update data set", updates.size());
					for (Ref ref : updates) {
						monitor.subTask(Strings.cut(App.s(ref.name), 25));
						try {
							Process p = App.store().get(Process.class, ref.uuid);
							RefSync.updateRefs(p, App.index());
							Data.updateVersion(p);
							Data.save(p);
						} catch (Exception innerE) {
							throw new RuntimeException(innerE);
						}
						monitor.worked(1);
					}
				});
			} catch (Exception e) {
				MsgBox.error("#Update failed", e.getMessage());
				return false;
			}
			return true;
		}

		@Override
		public void addPages() {
			Page page = new Page();
			addPage(page);
		}

		private class Page extends WizardPage {

			private TableViewer table;

			private Page() {
				super("DialogPage", M.UpdateReferences, null);
				setDescription("The saved flow data set '"
						+ Strings.cut(App.s(flow.getName()), 25)
						+ "' is used in the following EPDs. Should these EPDs"
						+ " be updated as well?");
				setPageComplete(true);
			}

			@Override
			public void createControl(Composite root) {
				Composite parent = new Composite(root, SWT.NONE);
				setControl(parent);
				parent.setLayout(new FillLayout());
				table = Tables.createViewer(parent,
						M.DataSet,
						M.UUID,
						M.DataSetVersion);
				table.setLabelProvider(new Label());
				Tables.bindColumnWidths(table, 0.6, 0.2, 0.2);
				table.setInput(usages);
				table.addSelectionChangedListener(e -> {
					Ref ref = Viewers.getFirstSelected(table);
					if (ref == null)
						return;
					Boolean b = selected.get(ref.uuid);
					if (b == null || !b) {
						selected.put(ref.uuid, Boolean.TRUE);
					} else {
						selected.put(ref.uuid, Boolean.FALSE);
					}
					table.refresh();
				});
			}
		}

		private class Label extends RefTableLabel {
			@Override
			public Image getColumnImage(Object obj, int col) {
				if (!(obj instanceof Ref))
					return null;
				if (col != 0)
					return null;
				Ref ref = (Ref) obj;
				if (Boolean.TRUE.equals(selected.get(ref.uuid)))
					return Icon.CHECK_TRUE.img();
				else
					return Icon.CHECK_FALSE.img();
			}
		}
	}
}
