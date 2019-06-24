package app.editors.epd.qmeta;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import app.editors.epd.EpdEditor;
import app.util.Controls;
import app.util.UI;
import epd.model.qmeta.QAnswer;
import epd.model.qmeta.QBoolean;
import epd.model.qmeta.QMetaData;
import epd.model.qmeta.QQuestion;

public class QMetaDataPage extends FormPage {

	private final EpdEditor editor;
	private final QMetaData qdata;

	public QMetaDataPage(EpdEditor editor) {
		super(editor, "QMetaDataPage", "Q metadata");
		this.editor = editor;
		// TODO select Q data from EPD data set
		this.qdata = new QMetaData();
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		FormToolkit tk = mform.getToolkit();
		ScrolledForm form = UI.formHeader(mform, "Q metadata");
		Composite body = UI.formBody(form, mform.getToolkit());

		QQuestion q1 = qdata.getQuestion("Criteria 1: Product comparability");
		Composite comp = UI.formSection(body, tk, q1.id);
		UI.gridLayout(comp, 1);
		multiChoice(comp, tk,
				"1.1 Valid for a single product",
				"1.2 Valid for several products with variation below 10%",
				"1.3 Valid for products with variation above 10%",
				"1.4 Valid for a product or several products where variation is not defined");
		Composite c = tk.createComposite(comp);
		UI.gridData(c, true, false);
		UI.innerGrid(c, 2);
		UI.formText(c, tk, "Comment:");

	}

	private void multiChoice(Composite root, FormToolkit tk,
			String... choices) {
		Composite comp = tk.createComposite(root);
		UI.innerGrid(comp, 2);

		QQuestion[] questions = new QQuestion[choices.length];
		Button[] buttons = new Button[choices.length];
		Runnable onChange = () -> {
			for (int i = 0; i < buttons.length; i++) {
				if (buttons[i].getSelection()) {
					questions[i].answer.yesNo = QBoolean.Yes;
				} else {
					questions[i].answer.yesNo = QBoolean.No;
				}
			}
		};

		for (int i = 0; i < choices.length; i++) {
			String choice = choices[i];
			QQuestion question = qdata.getQuestion(choice);
			if (question.answer == null) {
				question.answer = new QAnswer();
			}
			if (question.answer.yesNo == null) {
				question.answer.yesNo = QBoolean.None;
			}
			Button button = tk.createButton(comp, "", SWT.RADIO);
			tk.createLabel(comp, choice);
			button.setSelection(question.answer.yesNo == QBoolean.Yes);
			Controls.onSelect(button, _e -> onChange.run());
			questions[i] = question;
			buttons[i] = button;
		}
	}

}
