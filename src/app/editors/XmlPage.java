package app.editors;

import java.io.StringWriter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
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
		editor.onSaved(this::fillText);
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		FormToolkit tk = mform.getToolkit();
		ScrolledForm form = UI.formHeader(mform, "#XML");
		Composite body = UI.formBody(form, tk);
		text = new StyledText(body, SWT.NONE);
		tk.adapt(text);
		UI.gridData(text, true, true);
		fillText();
		form.reflow(true);
	}

	private void fillText() {
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
	}

	private void styleText(String xml) {
		try {
			XmlTokenizer tokenizer = new XmlTokenizer();
			for (Token token : tokenizer.parse(xml)) {
				StyleRange range = new StyleRange();
				range.start = token.start;
				range.length = token.end - token.start;
				range.foreground = color(token.type);
				text.setStyleRange(range);
			}
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to convert data set to XML", e);
		}
	}

	private Color color(TokenType type) {
		if (type == null)
			return Colors.darkGray(); // TODO: red ..
		switch (type) {
		case INSTRUCTION:
			return Colors.get(217, 217, 217);
		case ATTRIBUTE:
			return Colors.get(186, 36, 91);
		case ATTRIBUTE_VALUE:
			return Colors.get(241, 128, 22);
		case MARKUP:
			return Colors.get(0, 128, 128);
		case MARKUP_VALUE:
			return Colors.black();
		default:
			return Colors.black();
		}
	}

}
