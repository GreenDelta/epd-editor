package app.editors;

import app.M;
import app.Tooltips;
import app.rcp.Icon;
import app.util.UI;
import epd.model.Version;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import java.util.function.Consumer;

public class VersionField {

	private Version version;
	private Label label;
	private Consumer<String> consumer;

	public VersionField(Composite parent, FormToolkit toolkit) {
		version = new Version();
		render(parent, toolkit);
	}

	private void render(Composite parent, FormToolkit toolkit) {
		UI.formLabel(parent, toolkit,
			M.DataSetVersion, Tooltips.All_DataSetVersion);
		Composite composite = toolkit.createComposite(parent);
		GridLayout layout = UI.gridLayout(composite, 3);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		label = toolkit.createLabel(composite, "00.00.000");
		new VersionLink(composite, toolkit, VersionLink.MAJOR);
		new VersionLink(composite, toolkit, VersionLink.MINOR);
	}

	public void setVersion(String v) {
		version = Version.fromString(v);
		label.setText(version.toString());
	}

	public void onChange(Consumer<String> consumer) {
		this.consumer = consumer;
	}

	private class VersionLink extends HyperlinkAdapter {

		static final int MAJOR = 1;
		static final int MINOR = 2;
		private final int type;
		private final ImageHyperlink link;

		private Image hoverIcon = null;
		private Image icon = null;

		public VersionLink(Composite parent, FormToolkit toolkit, int type) {
			this.type = type;
			link = toolkit.createImageHyperlink(parent, SWT.TOP);
			link.addHyperlinkListener(this);
			configureLink();
		}

		private void configureLink() {
			var tooltip = type == MAJOR
				? M.UpdateMajorVersion
				: M.UpdateMinorVersion;
			hoverIcon = Icon.UP.img();
			icon = Icon.UP_DISABLED.img();
			link.setToolTipText(tooltip);
			link.setActiveImage(hoverIcon);
			link.setImage(icon);
		}

		@Override
		public void linkActivated(HyperlinkEvent e) {
			if (version == null || label == null)
				return;
			if (type == MAJOR)
				version.incMajor();
			else
				version.incMinor();
			if (consumer != null)
				consumer.accept(version.toString());
			label.setText(version.toString());
		}

		@Override
		public void linkEntered(HyperlinkEvent e) {
			link.setImage(hoverIcon);
		}

		@Override
		public void linkExited(HyperlinkEvent e) {
			link.setImage(icon);
		}
	}

}
