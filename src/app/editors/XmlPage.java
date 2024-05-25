package app.editors;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.io.Xml;
import org.openlca.ilcd.util.DataSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.util.Colors;
import app.util.UI;

public class XmlPage extends FormPage {

	private final IDataSet dataSet;
	private StyledText text;
	private ScrolledForm form;

	public XmlPage(BaseEditor editor, IDataSet dataSet) {
		super(editor, "XmlPage", "XML");
		this.dataSet = dataSet;
		editor.onSaved(this::fillText);
	}

	@Override
	protected void createFormContent(IManagedForm mForm) {
		var tk = mForm.getToolkit();
		form = UI.formHeader(mForm, "XML");
		var body = UI.formBody(form, tk);
		text = new StyledText(body, SWT.NONE);
		tk.adapt(text);
		UI.gridData(text, true, true);
		fillText();
		form.reflow(true);
	}

	private void fillText() {
		if (dataSet == null || text == null)
			return;
		try {
			var xml = readFile().orElse(Xml.toString(dataSet));
			text.setText(xml);
			styleText(xml);
			form.reflow(true);
		} catch (Exception e) {
			LoggerFactory.getLogger(getClass())
					.error("failed to convert data set to XML", e);
		}
	}

	private Optional<String> readFile() {
		try {
			var file = App.store().getFile(
					dataSet.getClass(), DataSets.getUUID(dataSet));
			if (file == null)
				return Optional.empty();
			var xml = Files.readString(file.toPath(), StandardCharsets.UTF_8);
			return Optional.of(xml);
		} catch (Exception e) {
			LoggerFactory.getLogger(getClass())
					.error("failed to read file of {}", dataSet, e);
			return Optional.empty();
		}
	}

	private void styleText(String xml) {
		try {
			var tokenizer = new XmlTokenizer();
			for (Token token : tokenizer.parse(xml)) {
				var range = new StyleRange();
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
			return Colors.darkGray();
		return switch (type) {
			case INSTRUCTION -> Colors.get(217, 217, 217);
			case ATTRIBUTE -> Colors.get(186, 36, 91);
			case ATTRIBUTE_VALUE -> Colors.get(241, 128, 22);
			case MARKUP -> Colors.get(0, 128, 128);
			default -> Colors.black();
		};
	}
}
