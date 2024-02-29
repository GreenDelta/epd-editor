package app.util;

import java.awt.Desktop;
import java.io.File;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.util.DataSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.M;
import app.Tooltips;
import app.rcp.Icon;

public class UI {

	private UI() {
	}

	public static Composite infoSection(IDataSet ds, Composite parent,
			FormToolkit tk) {
		Composite comp = UI.formSection(parent, tk,
				M.GeneralInformation, Tooltips.All_GeneralInformation);
		Text text = UI.formText(comp, tk, M.UUID, Tooltips.All_UUID);
		text.setEditable(false);
		var uuid = DataSets.getUUID(ds);
		if (uuid != null) {
			text.setText(uuid);
		}
		return comp;
	}

	public static void fileLink(IDataSet ds, Composite comp, FormToolkit tk) {
		if (ds == null || comp == null || tk == null)
			return;
		UI.formLabel(comp, tk, M.File, Tooltips.All_File);
		ImageHyperlink link = tk.createImageHyperlink(comp, SWT.NONE);
		link.setForeground(Colors.linkBlue());
		link.setImage(Icon.DOCUMENT.img());
		link.setToolTipText(Tooltips.All_File);
		Ref ref = Ref.of(ds);
		File f = App.store().getFile(ref);
		if (f == null || !f.exists())
			return;
		String path = f.getAbsolutePath();
		if (path.length() > 80)
			path = "..." + path.substring(path.length() - 75);
		link.setText(path);
		Controls.onClick(link, e -> UI.open(f));
	}

	public static void open(File file) {
		if (file == null || !file.exists())
			return;
		try {
			Desktop.getDesktop().open(file);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(UI.class);
			log.error("failed to open file natively: " + file, e);
		}
	}

	public static Shell shell() {
		// first, we try to get the shell from the active workbench window
		Shell shell = null;
		try {
			var wb = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();
			if (wb != null) {
				shell = wb.getShell();
			}
			if (shell != null)
				return shell;
		} catch (Exception ignored) {
		}

		// then, try to get it from the display
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		if (display != null) {
			shell = display.getActiveShell();
		}
		if (shell != null)
			return shell;
		return display != null
				? new Shell(display)
				: new Shell();
	}

	public static Font boldFont() {
		return JFaceResources.getFontRegistry().getBold(
				JFaceResources.DEFAULT_FONT);
	}

	public static Font italicFont() {
		return JFaceResources.getFontRegistry().getItalic(
				JFaceResources.DEFAULT_FONT);
	}

	public static Font defautlFont() {
		return JFaceResources.getFontRegistry().defaultFont();
	}

	/**
	 * Creates an italic font using the font data of the given control. The
	 * returned font must be disposed by the respective caller.
	 */
	public static Font italicFont(Control control) {
		if (control == null)
			return null;
		FontData fd = control.getFont().getFontData()[0];
		fd.setStyle(SWT.ITALIC);
		return new Font(control.getDisplay(), fd);
	}

	public static void applyItalicFont(Control control) {
		control.setFont(italicFont(control));
	}

	public static void center(Shell parent, Shell child) {
		Rectangle shellBounds = parent.getBounds();
		Point size = child.getSize();
		int diffX = (shellBounds.width - size.x) / 2;
		int diffY = (shellBounds.height - size.y) / 2;
		child.setLocation(shellBounds.x + diffX, shellBounds.y + diffY);
	}

	public static void adapt(FormToolkit toolkit, Composite composite) {
		toolkit.adapt(composite);
		toolkit.paintBordersFor(composite);
	}

	public static GridData gridData(Control control, boolean hFill,
			boolean vFill) {
		int hStyle = hFill ? SWT.FILL : SWT.LEFT;
		int vStyle = vFill ? SWT.FILL : SWT.CENTER;
		GridData data = new GridData(hStyle, vStyle, hFill, vFill);
		control.setLayoutData(data);
		return data;
	}

	public static GridData gridWidth(Control control, int width) {
		GridData data = gridData(control, false, false);
		data.widthHint = width;
		return data;
	}

	/** Creates a nice form header with the given title and returns the form. */
	public static ScrolledForm formHeader(IManagedForm mform, String title) {
		ScrolledForm form = mform.getForm();
		FormToolkit tk = mform.getToolkit();
		tk.getHyperlinkGroup().setHyperlinkUnderlineMode(
				HyperlinkSettings.UNDERLINE_HOVER);
		form.setText(title);
		form.getForm().setForeground(Colors.get(70, 70, 70));
		return form;
	}

	public static Composite formSection(Composite comp, FormToolkit tk,
			String label) {
		return formSection(comp, tk, label, null);
	}

	public static Composite formSection(Composite comp, FormToolkit tk,
			String label, String tooltip) {
		Section section = section(comp, tk, label);
		section.setToolTipText(tooltip);
		return sectionClient(section, tk);
	}

	public static Section section(Composite parent, FormToolkit tk,
			String label) {
		Section s = tk.createSection(parent,
				ExpandableComposite.TITLE_BAR
						| ExpandableComposite.FOCUS_TITLE
						| ExpandableComposite.EXPANDED
						| ExpandableComposite.TWISTIE);
		s.setTitleBarBackground(Colors.get(245, 245, 245));
		s.setTitleBarBorderColor(Colors.get(170, 170, 170));
		s.setTitleBarForeground(Colors.get(70, 70, 70));
		s.setToggleColor(Colors.get(70, 70, 70));
		gridData(s, true, false);
		s.setText(label);
		return s;
	}

	/**
	 * Creates a composite and sets it as section client of the given section.
	 * The created composite gets a 2-column grid-layout.
	 */
	public static Composite sectionClient(Section section,
			FormToolkit toolkit) {
		Composite composite = toolkit.createComposite(section);
		section.setClient(composite);
		gridLayout(composite, 2);
		return composite;
	}

	public static Composite formBody(ScrolledForm form, FormToolkit toolkit) {
		Composite body = form.getBody();
		GridLayout bodyLayout = new GridLayout();
		bodyLayout.marginRight = 10;
		bodyLayout.marginLeft = 10;
		bodyLayout.horizontalSpacing = 10;
		bodyLayout.marginBottom = 10;
		bodyLayout.marginTop = 10;
		bodyLayout.verticalSpacing = 10;
		bodyLayout.numColumns = 1;
		body.setLayout(bodyLayout);
		toolkit.paintBordersFor(body);
		gridData(body, true, true);
		return body;
	}

	public static GridLayout gridLayout(Composite composite, int columns) {
		return gridLayout(composite, columns, 10, 10);
	}

	public static GridLayout innerGrid(Composite composite, int columns) {
		return gridLayout(composite, columns, 5, 0);
	}

	public static GridLayout gridLayout(Composite composite, int columns,
			int spacing, int margin) {
		GridLayout layout = new GridLayout(columns, false);
		layout.verticalSpacing = spacing;
		layout.marginWidth = margin;
		layout.marginHeight = margin;
		layout.horizontalSpacing = spacing;
		if (composite == null)
			return layout;
		composite.setLayout(layout);
		return layout;
	}

	public static Composite formComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		gridLayout(composite, 2);
		return composite;
	}

	public static Composite formComposite(Composite parent,
			FormToolkit toolkit) {
		Composite composite = toolkit.createComposite(parent);
		gridLayout(composite, 2);
		return composite;
	}

	public static Button formCheckBox(Composite comp, FormToolkit tk,
			String label) {
		return formCheckBox(comp, tk, label, null);
	}

	public static Button formCheckBox(Composite comp, FormToolkit tk,
			String label, String tooltip) {
		formLabel(comp, tk, label, tooltip);
		Button button = tk != null
				? tk.createButton(comp, null, SWT.CHECK)
				: new Button(comp, SWT.CHECK);
		if (tooltip != null) {
			button.setToolTipText(tooltip);
		}
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		button.setLayoutData(gd);
		return button;
	}

	public static Text formText(Composite parent) {
		return formText(parent, null, null);
	}

	public static Text formText(Composite parent, String label) {
		return formText(parent, null, label);
	}

	public static Text formText(Composite comp, FormToolkit tk,
			String label) {
		return formText(comp, tk, label, null);
	}

	public static Text formText(
			Composite comp, FormToolkit tk,
			String label, String tooltip) {
		if (label != null) {
			formLabel(comp, tk, label, tooltip);
		}
		Text text = null;
		if (tk != null) {
			text = tk.createText(comp, null, SWT.BORDER);
		} else {
			text = new Text(comp, SWT.BORDER);
		}
		if (tooltip != null) {
			text.setToolTipText(tooltip);
		}
		gridData(text, true, false);
		return text;
	}

	public static Text formMultiText(Composite comp, FormToolkit tk) {
		return formMultiText(comp, tk, null);
	}

	public static Text formMultiText(Composite comp, FormToolkit tk,
			String label) {
		return formMultiText(comp, tk, label, null);
	}

	public static Text formMultiText(Composite comp, FormToolkit tk,
			String label, String tooltip) {
		if (label != null) {
			formLabel(comp, tk, label, tooltip);
		}
		int flags = SWT.BORDER | SWT.V_SCROLL | SWT.WRAP | SWT.MULTI;
		Text text = tk != null
				? tk.createText(comp, null, flags)
				: new Text(comp, flags);
		if (tooltip != null) {
			text.setToolTipText(tooltip);
		}
		GridData gd = gridData(text, true, false);
		gd.minimumHeight = 50;
		gd.heightHint = 50;
		gd.widthHint = 100;
		return text;
	}

	public static Combo formCombo(Composite comp, String label) {
		return formCombo(comp, null, label);
	}

	public static Combo formCombo(Composite comp, FormToolkit tk,
			String label) {
		return formCombo(comp, tk, label, null);
	}

	public static Combo formCombo(Composite comp, FormToolkit tk,
			String label, String tooltip) {
		formLabel(comp, tk, label, tooltip);
		Combo combo = new Combo(comp, SWT.READ_ONLY);
		if (tooltip != null) {
			combo.setToolTipText(tooltip);
		}
		gridData(combo, true, false);
		return combo;
	}

	public static Label formLabel(Composite comp, String label) {
		return formLabel(comp, null, label);
	}

	public static Label formLabel(Composite parent, FormToolkit tk,
			String label) {
		return formLabel(parent, tk, label, null);
	}

	public static Label formLabel(Composite parent, FormToolkit tk,
			String label, String tooltip) {
		Label lab = null;
		if (tk != null)
			lab = tk.createLabel(parent, label, SWT.NONE);
		else {
			lab = new Label(parent, SWT.NONE);
			lab.setText(label);
		}
		if (tooltip != null) {
			lab.setToolTipText(tooltip);
		}
		GridData gridData = gridData(lab, false, false);
		gridData.verticalAlignment = SWT.TOP;
		gridData.verticalIndent = 2;
		return lab;
	}

	public static Label filler(Composite parent, FormToolkit tk) {
		return formLabel(parent, tk, "");
	}

	public static Label filler(Composite parent) {
		return formLabel(parent, "");
	}

}
