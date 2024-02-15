package app.editors.epd;

import app.M;
import app.Tooltips;
import app.editors.RefLink;
import app.editors.RefTable;
import app.rcp.Icon;
import app.util.Actions;
import app.util.TextBuilder;
import app.util.UI;
import app.util.Viewers;
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
import org.openlca.ilcd.processes.Review;
import org.openlca.ilcd.processes.Validation;
import org.openlca.ilcd.util.Processes;

class ReviewSection {

	private final Validation validation;
	private final EpdEditor editor;
	private final FormPage page;

	private Composite parent;
	private FormToolkit toolkit;
	private ScrolledForm form;

	public ReviewSection(EpdEditor editor, FormPage page) {
		this.validation = Processes.forceValidation(editor.dataSet.process);
		this.editor = editor;
		this.page = page;
	}

	public void render(Composite body, FormToolkit toolkit, ScrolledForm form) {
		this.toolkit = toolkit;
		this.form = form;
		Section section = UI.section(body, toolkit, M.Reviews);
		section.setToolTipText(Tooltips.EPD_Review);
		parent = UI.sectionClient(section, toolkit);
		UI.gridLayout(parent, 1);
		for (Review review : validation.reviews) {
			new Sec(review);
		}
		Action addAction = Actions.create(M.AddReview,
			Icon.ADD.des(), this::addReview);
		Actions.bind(section, addAction);
		form.reflow(true);
	}

	private void addReview() {
		Review review = new Review();
		validation.reviews.add(review);
		new Sec(review);
		form.reflow(true);
		editor.setDirty();
	}

	private class Sec {

		private final Review review;
		private Section section;

		Sec(Review model) {
			this.review = model;
			createUi();
		}

		private void createUi() {
			int idx = validation.reviews.indexOf(review) + 1;
			section = UI.section(parent, toolkit, M.Review + " " + idx);
			section.setToolTipText(Tooltips.EPD_Review);
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
			UI.formLabel(comp, toolkit,
				M.CompleteReviewReport, Tooltips.EPD_ReviewReport);
			RefLink t = new RefLink(comp, toolkit, DataSetType.SOURCE);
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
				.withTooltip(Tooltips.EPD_Reviewer)
				.render(comp, toolkit);
		}

		private void detailsText(Composite comp) {
			TextBuilder tb = new TextBuilder(editor, page, toolkit);
			tb.multiText(comp, M.ReviewDetails,
				Tooltips.EPD_ReviewDetails, review.details);
		}

		private void typeCombo(Composite comp) {
			UI.formLabel(comp, toolkit, M.ReviewType, Tooltips.EPD_ReviewType);
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
				review.type = Viewers.getFirst(e.getSelection());
				editor.setDirty();
			});
		}

		private void delete() {
			validation.reviews.remove(review);
			section.dispose();
			form.reflow(true);
			editor.setDirty();
		}
	}

	private static class TypeLabel extends LabelProvider {

		@Override
		public String getText(Object obj) {
			if (!(obj instanceof ReviewType type))
				return null;
			return switch (type) {
				case ACCREDITED_THIRD_PARTY_REVIEW -> M.AccreditedThirdPartyReview;
				case DEPENDENT_INTERNAL_REVIEW -> M.DependentInternalReview;
				case INDEPENDENT_EXTERNAL_REVIEW -> M.IndependentExternalReview;
				case INDEPENDENT_INTERNAL_REVIEW -> M.IndependentInternalReview;
				case INDEPENDENT_REVIEW_PANEL -> M.IndependentReviewPanel;
				case NOT_REVIEWED -> M.NotReviewed;
			};
		}
	}
}
