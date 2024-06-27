package app.navi.actions;

import org.eclipse.jface.action.Action;
import org.openlca.ilcd.commons.Ref;

import app.M;
import app.editors.Editors;
import app.navi.RefElement;
import app.rcp.Icon;
import app.store.Data;
import app.util.MsgBox;

public class RefDeleteAction extends Action {

	private final RefElement e;

	public RefDeleteAction(RefElement e) {
		this.e = e;
		setText(M.Delete);
		setImageDescriptor(Icon.DELETE.des());
	}

	@Override
	public void run() {
		if (e == null || e.ref() == null)
			return;
		boolean b = MsgBox.ask(M.DeleteDataSet, M.DeleteDataSetQuestion);
		if (!b)
			return;
		Ref ref = e.ref();
		Editors.close(ref);
		Data.delete(ref);
	}

}
