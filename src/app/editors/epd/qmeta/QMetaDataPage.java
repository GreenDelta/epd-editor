package app.editors.epd.qmeta;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import app.App;
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
		super(editor, "QMetaDataPage", "Q Metadata");
		this.editor = editor;
		this.qdata = editor.qMetaData();
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		tk = mform.getToolkit();
		ScrolledForm form = UI.formHeader(mform, "Q Metadata");
		body = UI.formBody(form, mform.getToolkit());

		// Note that we have two kinds of `QQuestion` objects here:
		// those that come from the configuration and define the
		// available questions and data for the UI and those that
		// contain answers and are saved in the `QMetaData` object.
		List<QGroup> config = readConfig();
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

	private List<QGroup> readConfig() {
		File dir = new File(App.workspaceFolder(), "q-metadata");
		File file = new File(dir, "questions.json");
		if (file.exists()) {
			List<QGroup> config = QGroup.fromFile(file);
			if (!config.isEmpty())
				return config;
		}
		return QGroup.fromJson(
				this.getClass().getResourceAsStream("qmeta_questions.json"));
	}

	private void oneInList(QGroup group) {
		var root = UI.formSection(body, tk, group.name);
		UI.gridLayout(root, 1);
		var comp = tk.createComposite(root);
		UI.gridLayout(comp, 2, 10, 0);

		var config = group.questions.toArray(new QQuestion[0]);
		var selected = new AtomicReference<>(qdata.getSelected(group));

		var buttons = new Button[config.length];
		for (int i = 0; i < config.length; i++) {

			var button = tk.createButton(comp, "", SWT.RADIO);
			button.setLayoutData(
					new GridData(SWT.LEFT, SWT.TOP, false, false));
			var label = tk.createLabel(
					comp, Strings.wrap(config[i].text, 120));
			Controls.onClick(label, _e -> {
				if (button.getSelection())
					return;
				for (Button other : buttons) {
					other.setSelection(false);
				}
				button.setSelection(true);
				button.notifyListeners(SWT.Selection, new Event());
				});
			
			if (Objects.equals(selected.get(), config[i])) {
				var selectedQ = selected.get();
				if (selectedQ.answer == null) {
					selectedQ.answer = new QAnswer();
				}
				// update the text of the selected answer with
				// the text of the configuration
				selectedQ.answer.listText = config[i].text;
				button.setSelection(true);
			}
			buttons[i] = button;
		}

		var comment = commentText(root, tk);
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
				var q = qdata.getQuestion(config[i].id);
				q.answer = new QAnswer();
				q.answer.listText = config[i].text;
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
		QQuestion[] config = group.questions.toArray(new QQuestion[0]);
		for (var q : config) {
			var question = qdata.getQuestion(q.id);
			question.group = group.name;
			if (question.answer == null) {
				question.answer = new QAnswer();
			}
			if (question.answer.yesNo == null) {
				question.answer.yesNo = false;
			}

			// create the check button
			Button button = tk.createButton(comp, "", SWT.CHECK);
			button.setLayoutData(new GridData(
					SWT.LEFT, SWT.TOP, false, false));
			button.setSelection(question.answer.yesNo != null
					&& question.answer.yesNo);
			Controls.onSelect(button, _e -> {
				question.answer.yesNo = button.getSelection();
				editor.setDirty();
			});

			// create the label; also react on clicks on the label
			Label label = tk.createLabel(comp,
					Strings.wrap(q.text, 120));
			Controls.onClick(label, _e -> {
				button.setSelection(!button.getSelection());
				button.notifyListeners(SWT.Selection, new Event());
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
