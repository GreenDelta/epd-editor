package app.editors.epd.qmeta;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
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

		Composite comp1 = UI.formSection(body, tk, QLabel.Q1);
		UI.gridLayout(comp1, 1);
		multiChoice(comp1, tk, "1.1", "1.2", "1.3", "1.4");
		comment(comp1, tk, qdata.getQuestion("1"));

		Composite comp2 = UI.formSection(body, tk, QLabel.Q2);
		UI.gridLayout(comp2, 1);
		multiChoice(comp2, tk, "2.1", "2.2", "2.3", "2.4");
		comment(comp2, tk, qdata.getQuestion("2"));

		Composite comp3 = UI.formSection(body, tk, QLabel.Q3);
		UI.gridLayout(comp3, 1);
		multiChoice(comp3, tk, "3.1", "3.2", "3.3", "3.4");
		comment(comp3, tk, qdata.getQuestion("3"));

	}

	private void multiChoice(Composite root, FormToolkit tk,
			String... ids) {
		Composite comp = tk.createComposite(root);
		UI.gridLayout(comp, 2, 10, 0);

		QQuestion[] questions = new QQuestion[ids.length];
		Button[] buttons = new Button[ids.length];
		Runnable onChange = () -> {
			for (int i = 0; i < buttons.length; i++) {
				if (buttons[i].getSelection()) {
					questions[i].answer.yesNo = QBoolean.Yes;
				} else {
					questions[i].answer.yesNo = QBoolean.No;
				}
			}
			editor.setDirty();
		};

		for (int i = 0; i < ids.length; i++) {
			QQuestion question = qdata.getQuestion(ids[i]);
			if (question.answer == null) {
				question.answer = new QAnswer();
			}
			if (question.answer.yesNo == null) {
				question.answer.yesNo = QBoolean.None;
			}
			Button button = tk.createButton(comp, "", SWT.RADIO);
			tk.createLabel(comp, QLabel.get(ids[i]));
			button.setSelection(question.answer.yesNo == QBoolean.Yes);
			Controls.onSelect(button, _e -> onChange.run());
			questions[i] = question;
			buttons[i] = button;
		}
	}

	private void comment(Composite comp, FormToolkit tk, QQuestion q1) {
		Composite c = tk.createComposite(comp);
		UI.gridData(c, true, false);
		UI.gridLayout(c, 2, 10, 0);
		Text text = UI.formText(c, tk, "Comment:");
		text.addModifyListener(e -> {
			q1.comment = text.getText();
		});
	}

}
