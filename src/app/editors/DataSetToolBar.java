package app.editors;

import java.util.function.BiConsumer;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.M;
import app.editors.contact.ContactEditor;
import app.editors.epd.EpdEditor;
import app.editors.flow.FlowEditor;
import app.editors.flowproperty.FlowPropertyEditor;
import app.editors.io.UploadDialog;
import app.editors.methods.MethodEditor;
import app.editors.source.SourceEditor;
import app.editors.unitgroup.UnitGroupEditor;
import app.rcp.Icon;
import app.store.validation.ValidationDialog;
import app.util.Actions;
import app.util.MsgBox;

public class DataSetToolBar extends EditorActionBarContributor {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public void contributeToToolBar(IToolBarManager manager) {
		manager.add(Actions.create(M.Reload,
				Icon.RELOAD.des(), this::reload));
		manager.add(Actions.create(M.UploadDataSet,
				Icon.UPLOAD.des(), this::tryUpload));
		manager.add(Actions.create(M.ValidateDataSet,
				Icon.OK.des(), this::tryValidate));
	}

	private void reload() {
		with((editor, dataSet) -> {
			if (editor.isDirty()) {
				boolean b = MsgBox.ask(
						M.UnsavedChanges, M.ReloadUnsaved_Message);
				if (!b)
					return;
			}
			Ref ref = Ref.of(dataSet);
			Editors.close(ref);
			Editors.open(ref);
		});
	}

	private void tryUpload() {
		with((editor, dataSet) -> {
			if (editor.isDirty()) {
				MsgBox.error(M.UnsavedChanges, M.UnsavedChanges_Message);
				return;
			}
			UploadDialog.open(Ref.of(dataSet));
		});
	}

	private void tryValidate() {
		with((editor, dataSet) -> {
			if (editor.isDirty()) {
				MsgBox.error(M.UnsavedChanges, M.UnsavedChanges_Message);
				return;
			}
			ValidationDialog.open(Ref.of(dataSet));
		});
	}

	private void with(BiConsumer<IEditorPart, IDataSet> fn) {
		var editor = Editors.getActive();
		if (editor == null) {
			log.error("could not get the active editor");
			return;
		}
		var dataSet = getDataSet(editor);
		if (dataSet == null) {
			log.error("could not get data set from {}", editor);
			return;
		}
		fn.accept(editor, dataSet);
	}

	private IDataSet getDataSet(IEditorPart editor) {
		if (editor instanceof ContactEditor)
			return ((ContactEditor) editor).contact;
		if (editor instanceof EpdEditor)
			return ((EpdEditor) editor).dataSet.process;
		if (editor instanceof FlowEditor)
			return ((FlowEditor) editor).product.flow;
		if (editor instanceof FlowPropertyEditor)
			return ((FlowPropertyEditor) editor).property;
		if (editor instanceof SourceEditor)
			return ((SourceEditor) editor).source;
		if (editor instanceof UnitGroupEditor)
			return ((UnitGroupEditor) editor).unitGroup;
		if (editor instanceof MethodEditor)
			return ((MethodEditor) editor).method;
		return null;
	}

}
