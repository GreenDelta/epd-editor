package app.editors;

import java.io.StringWriter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.io.XmlBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.util.Colors;
import app.util.UI;

public class XmlPage extends FormPage {

	private IDataSet dataSet;
	private StyledText text;

	public XmlPage(BaseEditor editor, IDataSet dataSet) {
		super(editor, "XmlPage", "#XML");
		this.dataSet = dataSet;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		FormToolkit tk = mform.getToolkit();
		ScrolledForm form = UI.formHeader(mform, "#XML");
		Composite body = UI.formBody(form, tk);
		text = new StyledText(body, SWT.NONE);
		tk.adapt(text);
		UI.gridData(text, true, true);
		StringWriter writer = new StringWriter();
		try {
			new XmlBinder().toWriter(dataSet, writer);
			String xml = writer.toString();
			text.setText(xml);
			styleText(xml);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to convert data set to XML", e);
		}
		form.reflow(true);
	}

	private void styleText(String xml) {
	}

	private void styleOperator(int start, int length) {
		StyleRange styleRange = new StyleRange();
		styleRange.start = start;
		styleRange.length = length;
		styleRange.foreground = Colors.gray();
		text.setStyleRange(styleRange);
	}

}
