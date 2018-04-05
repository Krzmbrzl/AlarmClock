package ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class TriangleButton extends Button implements PaintListener, Listener {
	
	/**
	 * The color of the triangle
	 */
	private Color color;
	/**
	 * Indicates whether the mouse is currently hovering over this button
	 */
	private boolean mouseOverButton;
	
	public TriangleButton(Composite parent, int style) {
		super(parent, style | SWT.NO_BACKGROUND);
		
		setFont(parent.getFont());
		
		addPaintListener(this);
		addListener(SWT.MouseEnter, this);
		addListener(SWT.MouseExit, this);
		
		color = getBackground();
	}
	
	@Override
	public void paintControl(PaintEvent e) {
		e.gc.setAntialias(SWT.ON);
		
		int[] triangle = new int[6];
		Rectangle area = getBounds();
		
		// clear area
		e.gc.fillRectangle(new Rectangle(0, 0, area.width, area.height));
		
		if ((getStyle() & SWT.LEFT) == SWT.LEFT) {
			// face left
			triangle[0] = triangle[3] = 0;
			triangle[1] = area.height / 2;
			triangle[2] = triangle[4] = area.width;
			triangle[5] = area.height - 1;
		} else {
			if ((getStyle() & SWT.RIGHT) == SWT.RIGHT) {
				// face right
				triangle[0] = triangle[1] = triangle[2] = 0;
				triangle[3] = area.height - 1;
				triangle[4] = area.width;
				triangle[5] = area.height / 2;
			} else {
				if ((getStyle() & SWT.UP) == SWT.UP) {
					// face up
					triangle[0] = triangle[5] = 0;
					triangle[1] = triangle[3] = area.height - 1;
					triangle[2] = area.width;
					triangle[4] = area.width / 2;
				} else {
					// face down
					triangle[0] = triangle[1] = triangle[3] = 0;
					triangle[2] = area.width;
					triangle[4] = area.width / 2;
					triangle[5] = area.height - 1;
				}
			}
		}
		
		// draw new triangle
		if (isEnabled()) {
			e.gc.setBackground(getColor());
		} else {
			e.gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_GRAY));
		}
		e.gc.fillPolygon(triangle);
		
		// frame triangle if mouse is over
		if (isMouseOverButton()) {
			e.gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
			e.gc.setLineWidth(Math.min(
					Math.min(getBounds().width, getBounds().height) / 10, 1));
			e.gc.drawPolygon(triangle);
		}
	}
	
	/**
	 * Sets the color of this triangle. If it is currently visible it will be
	 * redrawn in order to refelct the changes
	 * 
	 * @param color
	 *            The color to use. <b>This color will not be disposed
	 *            automatically!</b>
	 */
	public void setColor(Color color) {
		this.color = color;
		
		if (isVisible()) {
			redraw();
		}
	}
	
	/**
	 * Gets the color of this triangle
	 */
	public Color getColor() {
		return color;
	}
	
	@Override
	protected void checkSubclass() {
		// do nothing
	}
	
	/**
	 * Checks whether the mouse is currently over this button
	 */
	public boolean isMouseOverButton() {
		return mouseOverButton;
	}
	
	@Override
	public void handleEvent(Event event) {
		if (event.type == SWT.MouseEnter) {
			mouseOverButton = true;
		} else {
			if (event.type == SWT.MouseExit) {
				mouseOverButton = false;
			}
		}
	}
}
