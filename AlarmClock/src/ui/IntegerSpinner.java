package ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import alarm.IListener;

public class IntegerSpinner extends FontInheritComposite {
	
	/**
	 * The current value of this spinner
	 */
	protected int value;
	/**
	 * The maximal value of this spinner
	 */
	protected int max;
	/**
	 * The minimal value of this spinner
	 */
	protected int min;
	/**
	 * The text field for representing the value of this spinner
	 */
	protected FontInheritText valueField;
	/**
	 * The color used for the spinner arrows
	 */
	protected Color arrowColor;
	/**
	 * The up-button
	 */
	protected TriangleButton up;
	/**
	 * The down-button
	 */
	protected TriangleButton down;
	/**
	 * A list of listeners registered to this spinner
	 */
	protected List<IListener> listeners;
	
	
	/**
	 * Creates a new instance of this spinner
	 * 
	 * @param parent
	 *            The parent of this spinner
	 * @param min
	 *            The minimal value iof this spinner
	 * @param max
	 *            the maximal value fo this spinner
	 * @param style
	 *            The style of this spinner
	 */
	public IntegerSpinner(Composite parent, int min, int max, int style) {
		super(parent, style);
		
		this.max = max;
		this.min = min;
		
		listeners = new ArrayList<IListener>();
		
		setFont(parent.getFont());
		
		initialize();
		setArrowColor(getDisplay().getSystemColor(SWT.COLOR_GREEN));
	}
	
	/**
	 * Initializes the components for this class
	 */
	protected void initialize() {
		GridLayout mainGrid = new GridLayout(1, false);
		mainGrid.marginHeight = 0;
		mainGrid.marginWidth = 0;
		super.setLayout(mainGrid);
		
		
		up = new TriangleButton(this, SWT.UP);
		
		valueField = new FontInheritText(this,
				SWT.CENTER | SWT.READ_ONLY | SWT.NO_BACKGROUND);
		valueField.setFont(getFont());
		
		GridData valueData = new GridData(SWT.FILL, SWT.FILL, true, false);
		valueData.widthHint = (int) (valueField.getFont().getFontData()[0]
				.getHeight()
				* Math.max(String.valueOf(max).length(),
						String.valueOf(min).length())
				* 1.2);
		valueData.heightHint = (int) (valueField.getFont().getFontData()[0]
				.getHeight() * 2.5);
		valueField.setLayoutData(valueData);
		
		down = new TriangleButton(this, SWT.DOWN);
		
		
		// configure arrow layout
		GridData arrowData = new GridData(SWT.FILL, SWT.FILL, true, false);
		arrowData.heightHint = (int) (valueData.heightHint / 1.5);
		arrowData.widthHint = valueData.widthHint;
		
		down.setLayoutData(arrowData);
		up.setLayoutData(arrowData);
		
		
		// Add listener
		valueField.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				String text = valueField.getText();
				
				int length = Math.max(String.valueOf(max).length(),
						String.valueOf(min).length());
				
				if (text.length() < length) {
					// Misformat -> correct it
					while (text.length() < length) {
						
						text = "0" + text;
					}
					
					valueField.setText(text);
					return;
				}
				
				value = Integer.parseInt(text);
				
				notifyListeners();
			}
		});
		
		up.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				value++;
				
				if (value > max) {
					value = min;
				}
				
				setValue(value);
				
				notifyListeners();
			}
		});
		
		down.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				value--;
				
				if (value < min) {
					value = max;
				}
				
				setValue(value);
				
				notifyListeners();
			}
		});
		
		setValue(min);
	}
	
	/**
	 * Gets the current value of this spinner
	 */
	public int getValue() {
		return value;
	}
	
	/**
	 * Sets the current value for this spinner
	 * 
	 * @param value
	 *            The new value of this spinner. Has to be in range of min and
	 *            max in order to get processed
	 */
	public void setValue(int value) {
		valueField.setText(String.valueOf(value));
	}
	
	/**
	 * Sets the arrow color for this spinner
	 * 
	 * @param color
	 *            The new color to use
	 */
	public void setArrowColor(Color color) {
		arrowColor = color;
		
		up.setColor(arrowColor);
		up.redraw();
		down.setColor(arrowColor);
		down.redraw();
	}
	
	/**
	 * Gets the color of the arrows of this spinner
	 */
	public Color getArrowColor() {
		return arrowColor;
	}
	
	@Override
	public Point computeSize(int wHint, int hHint) {
		GC gc = new GC(valueField);
		gc.setFont(valueField.getFont());
		
		Point maxPoint = gc.textExtent(String.valueOf(max));
		Point minPoint = gc.textExtent(String.valueOf(min));
		
		Point preferredSize = new Point(Math.max(maxPoint.x, minPoint.x),
				Math.max(maxPoint.y, minPoint.y));
		
		if ((wHint & SWT.DEFAULT) != SWT.DEFAULT) {
			preferredSize.x = Math.max(preferredSize.x, wHint);
		}
		
		if ((hHint & SWT.DEFAULT) != SWT.DEFAULT) {
			preferredSize.y = Math.max(preferredSize.y, hHint);
		}
		
		return preferredSize;
	}
	
	/**
	 * Adds the given listener to this spinner. The listener will be notifed
	 * about value changes of this spinner with the new value as the
	 * notification context.
	 * 
	 * @param listener
	 *            The listener to add
	 */
	public void addListener(IListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}
	
	/**
	 * Removes the given listener to this spinner
	 * 
	 * @param listener
	 *            The listener to remove
	 */
	public void removeListener(IListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}
	
	/**
	 * Notifes the registered listeners that the valiue of this spinner has
	 * changed
	 */
	protected void notifyListeners() {
		synchronized (listeners) {
			for (IListener currentListener : listeners) {
				currentListener.notify(value);
			}
		}
	}
	
	/**
	 * Sets the maximum value for this spinner
	 * 
	 * @param max
	 *            The new maximum to use
	 */
	protected void setMax(int max) {
		if (max < min) {
			throw new IllegalArgumentException(
					"The specified maximum is smaller than the set minimum!");
		}
		
		this.max = max;
		
		if (value > max) {
			setValue(max);
		}
	}
	
	/**
	 * Sets the minimum value for this spinner
	 * 
	 * @param min
	 *            The new minimum to use
	 */
	protected void setMin(int min) {
		if (max < min) {
			throw new IllegalArgumentException(
					"The specified minimum is bigger than the set maximum!");
		}
		
		this.min = min;
		
		if (value < min) {
			setValue(min);
		}
	}
	
}
