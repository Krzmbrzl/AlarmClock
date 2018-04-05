package ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * This class provides graphical representation of the current time in form of
 * an analog clock. It will scale to any given size.
 * 
 * @author Raven
 *
 */
public class AnalogClock extends Canvas
		implements PaintListener, DisposeListener {
	
	public static int DEFAULT_HEIGHT = 80;
	
	/**
	 * A coefficient used for calculating the base thickness all drawn lines are
	 * scaled to
	 */
	private double lineThicknessCoefficient;
	/**
	 * A coefficient used for calculating how long the orientation lines should
	 * be drawn
	 */
	private double orientationLineLengthCoefficient;
	/**
	 * The timer that is used in order to refresh the time display every second
	 */
	private Timer updateTimer;
	/**
	 * Indicates whether if the clock has a different height and width it should
	 * adapt to it and form an ovale instead of a circle
	 */
	private boolean scaleIndependent;
	/**
	 * A reference to the display thread this clock belongs to
	 */
	protected Display display;
	
	
	/**
	 * Creates a new clock on the given parent and with the given style
	 * 
	 * @param parent
	 *            The parent to add this clock to
	 * @param style
	 *            The style for this clock
	 * @param allowIndependentScaling
	 *            Indicates whether the clock should form an ovale if it's
	 *            height and width are different
	 */
	public AnalogClock(Composite parent, int style,
			boolean allowIndependentScaling) {
		super(parent, style);
		
		this.addPaintListener(this);
		
		display = Display.getCurrent();
		
		lineThicknessCoefficient = 1;
		
		orientationLineLengthCoefficient = 1;
		scaleIndependent = allowIndependentScaling;
		
		addDisposeListener(this);
		
		setVisible(true);
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		
		if (visible) {
			updateTimer = new Timer(false);
			updateTimer.scheduleAtFixedRate(new TimerTask() {
				
				@Override
				public void run() {
					// Make sure to call this mehtod from the correct thread
					display.asyncExec(new Runnable() {
						
						@Override
						public void run() {
							if (!AnalogClock.this.isDisposed()) {
								AnalogClock.this.redraw();
							} else {
								updateTimer.cancel();
							}
						}
					});
				}
			}, 1000 - Calendar.getInstance().get(Calendar.MILLISECOND), 1000);
		} else {
			if (updateTimer != null) {
				updateTimer.cancel();
			}
		}
	}
	
	@Override
	public void paintControl(PaintEvent e) {
		checkWidget();
		
		e.gc.setAntialias(SWT.ON);
		
		Rectangle clientArea = getClientArea();
		
		// clear previous paintings
		e.gc.fillRectangle(clientArea);
		
		Point center = new Point(clientArea.width / 2 + clientArea.x,
				clientArea.height / 2 + clientArea.y);
		
		drawFrame(e, center);
		drawPointers(e, center);
	}
	
	/**
	 * Draws the pointers of this clock
	 * 
	 * @param e
	 *            The paint event used to draw the pointers
	 * @param center
	 *            The center of the clock
	 */
	protected void drawPointers(PaintEvent e, Point center) {
		double radBase = (2 * Math.PI) / 60;
		
		Rectangle clientArea = getClientArea();
		double halfWidth = clientArea.width / 2.0;
		double halfHeight = clientArea.height / 2.0;
		
		if (!scaleIndependent) {
			// stretch the clock so it fills the smaller side
			halfHeight = halfWidth = Math.min(halfHeight, halfWidth);
		}
		
		Calendar cal = Calendar.getInstance();
		
		double hourAngle = radBase * 5
				* (cal.get(Calendar.HOUR) + cal.get(Calendar.MINUTE) / 60.0
						+ cal.get(Calendar.SECOND) / 3600.0);
		
		Point hourPointerEndCoordinates = new Point(
				center.x + (int) (Math.sin(hourAngle) * halfWidth * 0.75),
				center.y - (int) (Math.cos(hourAngle) * halfHeight * 0.75));
		
		// draw the hour pointer
		e.gc.setForeground(display.getSystemColor(SWT.COLOR_BLUE));
		e.gc.setLineWidth(calculateBaseLineThickness() * 2);
		e.gc.drawLine(center.x, center.y, hourPointerEndCoordinates.x,
				hourPointerEndCoordinates.y);
		
		
		double minuteAngle = radBase
				* (cal.get(Calendar.MINUTE) + cal.get(Calendar.SECOND) / 60.0);
		Point minutePointerEndCoordinates = new Point(
				center.x + (int) (Math.sin(minuteAngle) * halfWidth),
				center.y - (int) (Math.cos(minuteAngle) * halfHeight));
		
		// draw the minute pointer
		e.gc.setForeground(display.getSystemColor(SWT.COLOR_GREEN));
		e.gc.setLineWidth(calculateBaseLineThickness());
		e.gc.drawLine(center.x, center.y, minutePointerEndCoordinates.x,
				minutePointerEndCoordinates.y);
		
		double secondAngle = radBase
				* Calendar.getInstance().get(Calendar.SECOND);
		Point secondPointerEndCoordinates = new Point(
				center.x + (int) (Math.sin(secondAngle) * halfWidth),
				center.y - (int) (Math.cos(secondAngle) * halfHeight));
		
		// draw the minute pointer
		e.gc.setForeground(display.getSystemColor(SWT.COLOR_RED));
		e.gc.setLineWidth(calculateBaseLineThickness() / 2);
		e.gc.drawLine(center.x, center.y, secondPointerEndCoordinates.x,
				secondPointerEndCoordinates.y);
	}
	
	/**
	 * Draws the frame of the clock
	 * 
	 * @param e
	 *            The PaintEvent to use
	 * @param center
	 *            The center of the clock
	 */
	protected void drawFrame(PaintEvent e, Point center) {
		e.gc.drawOval(center.x - 3, center.y - 3, 6, 6);
		
		int counter = 5;
		
		// Draw the orientation lines
		for (Point currentPoint : getOrientationPoints(center)) {
			Point connectionVector = new Point(center.x - currentPoint.x,
					center.y - currentPoint.y);
			int scaleLength;
			
			if (counter == 5) {
				e.gc.setLineWidth(calculateBaseLineThickness());
				scaleLength = calculateOrientationLineLength();
				
				counter = 0;
			} else {
				e.gc.setLineWidth((int) (calculateBaseLineThickness() * 0.5));
				scaleLength = (int) (calculateOrientationLineLength() * 0.5);
			}
			
			// normalize Vector to respective length
			double length = Math.sqrt(Math.pow(connectionVector.x, 2)
					+ Math.pow(connectionVector.y, 2));
			connectionVector.x = (int) (connectionVector.x / length
					* scaleLength);
			connectionVector.y = (int) (connectionVector.y / length
					* scaleLength);
			
			e.gc.drawLine(currentPoint.x, currentPoint.y,
					currentPoint.x + connectionVector.x,
					currentPoint.y + connectionVector.y);
			
			counter++;
		}
	}
	
	/**
	 * Calculates the base line thickness according to the set
	 * {@link #lineThicknessCoefficient} and the current size of the control
	 */
	private int calculateBaseLineThickness() {
		int coef = Math.min(getClientArea().width, getClientArea().height);
		
		return (int) (lineThicknessCoefficient * (coef / 150.0));
	}
	
	/**
	 * Calculates the base line thickness according to the set
	 * {@link #orientationLineLengthCoefficient} and the current size of the
	 * control
	 */
	private int calculateOrientationLineLength() {
		int coef = Math.min(getClientArea().width, getClientArea().height);
		
		return (int) (orientationLineLengthCoefficient * (coef / 30.0));
	}
	
	/**
	 * Calculates the orientation points for the frame
	 * 
	 * @param center
	 *            The center of this clock
	 * @return 60 equal spaced points points on a circle witht the respective
	 *         radius {@link #halfHeight} and {@link #halfWidth}
	 */
	private List<Point> getOrientationPoints(Point center) {
		List<Point> list = new ArrayList<Point>();
		
		Rectangle clientArea = getClientArea();
		double halfWidth = clientArea.width / 2.0;
		double halfHeight = clientArea.height / 2.0;
		
		if (!scaleIndependent) {
			// stretch the clock so it fills the smaller side
			halfHeight = halfWidth = Math.min(halfHeight, halfWidth);
		}
		
		int amountOfPoints = 60;
		
		for (int i = 0; i < amountOfPoints; i++) {
			double currentRad = ((2 * Math.PI) / amountOfPoints) * i;
			list.add(new Point(
					center.x + (int) (Math.sin(currentRad) * halfWidth),
					center.y + (int) (Math.cos(currentRad) * halfHeight)));
		}
		
		return list;
	}
	
	@Override
	public Point computeSize(int wHint, int hHint) {
		checkWidget();
		
		// always prefer a square shape
		int size;
		if (wHint == SWT.DEFAULT || wHint < 0) {
			size = hHint;
		} else {
			if (hHint == SWT.DEFAULT || hHint < 0) {
				size = wHint;
			} else {
				size = Math.min(wHint, hHint);
			}
		}
		
		if (size == SWT.DEFAULT || size < 0) {
			size = 200;
		}
		
		return new Point(size, size);
	}
	
	@Override
	public void widgetDisposed(DisposeEvent e) {
		if (updateTimer != null) {
			updateTimer.cancel();
		}
	}
	
	/**
	 * Sets the coefficient for the line thickness of this clock's components
	 * 
	 * @param coef
	 *            The new coefficient to use
	 */
	public void setLineThicknessCoefficient(double coef) {
		lineThicknessCoefficient = Math.abs(coef);
	}
	
	/**
	 * Sets the coefficient for the line length of this clock's orientation
	 * lines
	 * 
	 * @param coef
	 *            The new coefficient to use
	 */
	public void setOrientationLineLengthCoefficient(double coef) {
		orientationLineLengthCoefficient = Math.abs(coef);
	}
	
	
}
