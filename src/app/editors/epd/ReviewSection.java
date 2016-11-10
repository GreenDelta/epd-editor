package app.editors.epd;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.ReviewType;
import org.openlca.ilcd.processes.Modelling;
import org.openlca.ilcd.processes.Review;

import app.M;
import app.editors.RefTable;
import app.editors.RefText;
import app.rcp.Icon;
import app.util.Actions;
import app.util.TextBuilder;
import app.util.UI;
import app.util.Viewers;

class ReviewSection {

	private Modelling modelling;
	private EpdEditor editor;
	private FormPage page;

	private Composite parent;
	private FormToolkit toolkit;
	private ScrolledForm form;

	public ReviewSection(Modelling modelling, EpdEditor editor, FormPage page) {
		this.modelling = modelling;
		this.editor = editor;
		this.page = page;
	}

	public void render(Composite body, FormToolkit toolkit, ScrolledForm form) {
		this.toolkit = toolkit;
		this.form = form;
		Section section = UI.section(body, toolkit, M.Reviews);
		parent = UI.sectionClient(section, toolkit);
		UI.gridLayout(parent, 1);
		for (Review review : modelling.validation.reviews)
			new Sec(review);
		Action addAction = Actions.create(M.AddReview,
				Icon.ADD.des(), this::addReview);
		Actions.bind(section, addAction);
		form.reflow(true);
	}

	private void addReview() {
		Review review = new Review();
		modelling.validation.reviews.add(review);
		new Sec(review);
		form.reflow(true);
		editor.setDirty();
	}

	private class Sec {

		private Review review;
		private Section section;

		Sec(Review model) {
			this.review = model;
			createUi();
		}

		private void createUi() {
			int idx = modelling.validation.reviews.indexOf(review) + 1;
			section = UI.section(parent, toolkit, M.Review + " " + idx);
			Composite body = UI.sectionClient(section, toolkit);
			UI.gridLayout(body, 1);
			Composite comp = UI.formComposite(body, toolkit);
			UI.gridData(comp, true, false);
			typeCombo(comp);
			detailsText(comp);
			createReportText(comp);
			createActorTable(body);
			Action deleteAction = Actions.create(M.DeleteReview,
					Icon.DELETE.des(), this::delete);
			Actions.bind(section, deleteAction);
		}

		private void createReportText(Composite comp) {
			UI.formLabel(comp, M.CompleteReviewReport);
			RefText t = new RefText(comp, toolkit, DataSetType.SOURCE);
			t.setRef(review.report);
			t.onChange(ref -> {
				review.report = ref;
				editor.setDirty();
			});
		}

		private void createActorTable(Composite comp) {
			RefTable.create(DataSetType.CONTACT, review.reviewers)
					.withEditor(editor)
					.withTitle(M.Reviewer)
					.render(comp, toolkit);
		}

		private void detailsText(Composite comp) {
			TextBuilder tb = new TextBuilder(editor, page, toolkit);
			tb.multiText(comp, M.ReviewDetails, review.details);
		}

		private void typeCombo(Composite comp) {
			UI.formLabel(comp, M.ReviewType);
			ComboViewer c = new ComboViewer(comp);
			UI.gridData(c.getControl(), true, false);
			c.setContentProvider(ArrayContentProvider.getInstance());
			c.setLabelProvider(new TypeLabel());
			c.setInput(ReviewType.values());
			if (review.type != null) {
				ISelection s = new StructuredSelection(review.type);
				c.setSelection(s);
			}
			c.addSelectionChangedListener((e) -> {
				ReviewType type = Viewers.getFirst(e.getSelection());
				review.type = type;
				editor.setDirty();
			});
		}

		private void delete() {
			modelling.validation.reviews.remove(review);
			section.dispose();
			form.reflow(true);
			editor.setDirty();
		}
	}

	private class TypeLabel extends LabelProvider {

		@Override
		public String getText(Object obj) {
			if (!(obj instanceof ReviewType))
				return null;
			ReviewType type = (ReviewType) obj;
			switch (type) {
			case ACCREDITED_THIRD_PARTY_REVIEW:
				return M.AccreditedThirdPartyReview;
			case DEPENDENT_INTERNAL_REVIEW:
				return M.DependentInternalReview;
			case INDEPENDENT_EXTERNAL_REVIEW:
				return M.IndependentExternalReview;
			case INDEPENDENT_INTERNAL_REVIEW:
				return M.IndependentInternalReview;
			case INDEPENDENT_REVIEW_PANEL:
				return M.IndependentReviewPanel;
			case NOT_REVIEWED:
				return M.NotReviewed;
			default:
				return null;
			}
		}
	}
}
