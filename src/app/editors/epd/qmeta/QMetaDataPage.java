package app.editors.epd.qmeta;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
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
import epd.model.qmeta.QMetaData;
import epd.model.qmeta.QQuestion;
import epd.util.Strings;

public class QMetaDataPage extends FormPage {

	private final EpdEditor editor;
	private final QMetaData qdata;

	public QMetaDataPage(EpdEditor editor) {
		super(editor, "QMetaDataPage", "Q metadata");
		this.editor = editor;
		if (editor.dataSet.qMetaData == null) {
			editor.dataSet.qMetaData = new QMetaData();
		}
		this.qdata = editor.dataSet.qMetaData;
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

		Composite comp4 = UI.formSection(body, tk, QLabel.Q4);
		UI.gridLayout(comp4, 1);
		multiChoice(comp4, tk, "4.1", "4.2", "4.3", "4.4");
		comment(comp4, tk, qdata.getQuestion("4"));

		Composite comp5 = UI.formSection(body, tk, QLabel.Q5);
		multiChecks(comp5, tk, "5.1", "5.2", "5.3", "5.4",
				"5.5", "5.6", "5.7", "5.8");

		Composite comp6 = UI.formSection(body, tk, QLabel.Q6);
		UI.gridLayout(comp6, 1);
		multiChoice(comp6, tk, "6.1", "6.2", "6.3", "6.4");
		comment(comp6, tk, qdata.getQuestion("6"));

		form.reflow(true);
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
					questions[i].answer.yesNo = true;
				} else {
					questions[i].answer.yesNo = false;
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
				question.answer.yesNo = false;
			}
			Button button = tk.createButton(comp, "", SWT.RADIO);
			button.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			tk.createLabel(comp, Strings.wrap(QLabel.get(ids[i]), 120));
			button.setSelection(question.answer.yesNo != null
					&& question.answer.yesNo);
			Controls.onSelect(button, _e -> onChange.run());
			questions[i] = question;
			buttons[i] = button;
		}
	}

	private void multiChecks(Composite comp, FormToolkit tk,
			String... ids) {

		for (int i = 0; i < ids.length; i++) {
			QQuestion question = qdata.getQuestion(ids[i]);
			if (question.answer == null) {
				question.answer = new QAnswer();
			}
			if (question.answer.yesNo == null) {
				question.answer.yesNo = false;
			}

			Button button = tk.createButton(comp, "", SWT.CHECK);
			button.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			tk.createLabel(comp, Strings.wrap(QLabel.get(ids[i]), 120));
			button.setSelection(question.answer.yesNo != null
					&& question.answer.yesNo);
			Controls.onSelect(button, _e -> {
				if (button.getSelection()) {
					question.answer.yesNo = true;
				} else {
					question.answer.yesNo = false;
				}
				editor.setDirty();
			});
			UI.filler(comp);
			comment(comp, tk, question);
		}

	}

	private void comment(Composite comp, FormToolkit tk, QQuestion q1) {
		Composite c = tk.createComposite(comp);
		UI.gridLayout(c, 2, 10, 0);
		Text text = UI.formMultiText(c, tk, "Comment:");
		text.addModifyListener(e -> {
			q1.comment = text.getText();
			editor.setDirty();
		});
		GridData gd = UI.gridData(text, true, false);
		gd.widthHint = 500;
		gd.heightHint = 50;
	}

}
