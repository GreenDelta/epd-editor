package app.editors.epd.qmeta;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

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
import app.rcp.Texts;
import app.util.Controls;
import app.util.UI;
import epd.model.qmeta.QAnswer;
import epd.model.qmeta.QGroup;
import epd.model.qmeta.QMetaData;
import epd.model.qmeta.QQuestion;
import epd.model.qmeta.QQuestionType;
import epd.util.Strings;

public class QMetaDataPage extends FormPage {

	private final EpdEditor editor;
	private final QMetaData qdata;

	private FormToolkit tk;
	private Composite body;

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
		tk = mform.getToolkit();
		ScrolledForm form = UI.formHeader(mform, "Q metadata");
		body = UI.formBody(form, mform.getToolkit());

		// Note that we have two kinds of `QQuestion` objects here:
		// those that come from the configuration and define the
		// available questions and data for the UI and those that
		// contain answers and are saved in the `QMetaData` object.
		List<QGroup> config = QGroup.fromJson(
				this.getClass().getResourceAsStream("qmeta_questions.json"));
		for (QGroup group : config) {
			if (group == null || group.name == null)
				continue;
			QQuestionType type = group.getType();
			if (type == null)
				continue;
			switch (type) {
			case ONE_IN_LIST:
				oneInList(group);
				break;
			case BOOLEAN:
				multiChecks(group);
			default:
				break;
			}
		}
		form.reflow(true);
	}

	private void oneInList(QGroup group) {
		Composite root = UI.formSection(body, tk, group.name);
		UI.gridLayout(root, 1);
		Composite comp = tk.createComposite(root);
		UI.gridLayout(comp, 2, 10, 0);

		QQuestion[] config = group.questions.toArray(
				new QQuestion[group.questions.size()]);
		AtomicReference<QQuestion> selected = new AtomicReference<>(
				qdata.getSelected(group));

		Button[] buttons = new Button[config.length];
		for (int i = 0; i < config.length; i++) {
			Button button = tk.createButton(comp, "", SWT.RADIO);
			button.setLayoutData(
					new GridData(SWT.LEFT, SWT.TOP, false, false));
			tk.createLabel(
					comp, Strings.wrap(config[i].text, 120));
			button.setSelection(
					Objects.equals(selected.get(), config[i]));
			buttons[i] = button;
		}

		Text comment = commentText(root, tk);
		if (selected.get() != null) {
			Texts.set(comment, selected.get().comment);
		}
		comment.addModifyListener(e -> {
			if (selected.get() != null) {
				selected.get().comment = comment.getText();
				editor.setDirty();
			}
		});

		Runnable onChange = () -> {
			qdata.removeAll(group);
			for (int i = 0; i < buttons.length; i++) {
				if (!buttons[i].getSelection())
					continue;
				QQuestion q = qdata.getQuestion(config[i].id);
				q.answer = new QAnswer();
				q.answer.yesNo = true;
				q.comment = comment.getText();
				selected.set(q);
			}
			editor.setDirty();
		};
		for (Button b : buttons) {
			Controls.onSelect(b, _e -> onChange.run());
		}

	}

	private void multiChecks(QGroup group) {
		Composite comp = UI.formSection(body, tk, group.name);
		QQuestion[] config = group.questions.toArray(
				new QQuestion[group.questions.size()]);

		for (int i = 0; i < config.length; i++) {
			QQuestion question = qdata.getQuestion(config[i].id);
			if (question.answer == null) {
				question.answer = new QAnswer();
			}
			if (question.answer.yesNo == null) {
				question.answer.yesNo = false;
			}

			Button button = tk.createButton(comp, "", SWT.CHECK);
			button.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			tk.createLabel(comp, Strings.wrap(config[i].text, 120));
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

			Text comment = commentText(comp, tk);
			Texts.set(comment, question.comment);
			comment.addModifyListener(e -> {
				question.comment = comment.getText();
				editor.setDirty();
			});
		}

	}

	private Text commentText(Composite comp, FormToolkit tk) {
		Composite c = tk.createComposite(comp);
		UI.gridLayout(c, 2, 10, 0);
		Text text = UI.formMultiText(c, tk, "Comment:");
		GridData gd = UI.gridData(text, true, false);
		gd.widthHint = 500;
		gd.heightHint = 50;
		return text;
	}

}
