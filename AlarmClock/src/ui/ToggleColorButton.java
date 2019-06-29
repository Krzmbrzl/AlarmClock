package ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * This class represents a toggle button that indicates it's toggle status by
 * color (red or green)
 * 
 * @author Raven
 *
 */
// Use Composite instead of a Button as a super class as a Button might not
// allow a custom appearance
public class ToggleColorButton extends Composite implements PaintListener, Listener, ControlListener {
	/**
	 * The selection of this "button"
	 */
	private boolean selection;
	
	/**
	 * Indicates whether the mouse is currently hovering over this button
	 */
	private boolean mouseOverButton;


	/**
	 * Creates a new instance of this button
	 * 
	 * @param parent
	 *            The parent to group this parent to
	 * @param style
	 *            The style of the button. Should not contain any information about
	 *            the button type
	 */
	public ToggleColorButton(Composite parent, int style) {
		super(parent, style | SWT.TOGGLE | SWT.NO_BACKGROUND);

		setFont(parent.getFont());

		addPaintListener(this);
		addControlListener(this);
		addListener(SWT.MouseEnter, this);
		addListener(SWT.MouseExit, this);
		
		// Add itself as a mouse-down listener in order to emulate button behavior
		addListener(SWT.MouseDown, this);
		
		selection = false;
	}

	@Override
	protected void checkSubclass() {
		// do nothing
	}

	@Override
	public void paintControl(PaintEvent e) {
		Rectangle bounds = getBounds();

		e.gc.fillRectangle(new Rectangle(0, 0, bounds.width, bounds.height));

		double lineWidth = Math.min(bounds.width, bounds.height) / ((mouseOverButton) ? 13.0 : 15.0);

		if (isEnabled()) {
			if (getSelection()) {
				e.gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_GREEN));
			} else {
				e.gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_RED));
			}
		} else {
			e.gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_GRAY));
		}

		e.gc.fillOval((int) (lineWidth / 2), (int) (lineWidth / 2), (int) (bounds.width - lineWidth),
				(int) (bounds.height - lineWidth));

		e.gc.setLineWidth((int) lineWidth);
		if (isEnabled()) {
			e.gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
		} else {
			e.gc.setForeground(e.gc.getBackground());
		}
		e.gc.drawOval((int) (lineWidth / 2), (int) (lineWidth / 2), (int) (bounds.width - lineWidth),
				(int) (bounds.height - lineWidth));
	}
	
	public boolean getSelection() {
		return selection;
	}
	
	public void setSelection(boolean selection) {
		this.selection = selection;
	}

	@Override
	public void handleEvent(Event event) {
		if (event.type == SWT.MouseEnter) {
			mouseOverButton = true;
			this.redraw();
		} else {
			if (event.type == SWT.MouseExit) {
				mouseOverButton = false;
				this.redraw();
			} else {
				if (event.type == SWT.MouseDown) {
					// toggle selection
					setSelection(!getSelection());
					this.redraw();
				}
			}
		}
	}

	@Override
	public void controlMoved(ControlEvent e) {
	}

	@Override
	public void controlResized(ControlEvent e) {
		GridData data = (GridData) getLayoutData();
		data.widthHint = getSize().y;
		ToggleColorButton.this.update();
	}
}
