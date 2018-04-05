package ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

/**
 * This widget allows the user to specify a time in day
 * 
 * @author Raven
 *
 */
public class TimePicker extends Composite {
	
	/**
	 * This spinner for the hours
	 */
	protected IntegerSpinner hours;
	/**
	 * The spinner for the minutes
	 */
	protected IntegerSpinner minutes;
	
	
	/**
	 * Creates a new instance of this time picker
	 * 
	 * @param parent
	 *            The parent to use
	 * @param style
	 *            The style to use
	 */
	public TimePicker(Composite parent, int style) {
		super(parent, style);
		
		setFont(parent.getFont());
		
		initialize();
	}
	
	/**
	 * Initializes the components for this class
	 */
	protected void initialize() {
		GridLayout mainGrid = new GridLayout(3, false);
		mainGrid.marginHeight = 0;
		mainGrid.marginWidth = 0;
		super.setLayout(mainGrid);
		
		GridData spinnerData = new GridData(SWT.FILL, SWT.FILL, true, true);
		
		hours = new IntegerSpinner(this, 0, 23, SWT.NONE);
		hours.setLayoutData(spinnerData);
		
		FontInheritLabel seperator = new FontInheritLabel(this, SWT.NONE);
		seperator.setText(":");
		
		minutes = new IntegerSpinner(this, 0, 59, SWT.NONE);
		minutes.setLayoutData(spinnerData);
	}
	
	/**
	 * <b>Usage is prohibited</b> for this class!
	 */
	@Override
	public void setLayout(Layout layout) {
		throw new IllegalAccessError("No support for custom layouts!");
	}
	
	/**
	 * Gets the time set in this time picker
	 * 
	 * @return An array representing the set time. The first entry represents
	 *         the hours the second entry the minutes
	 */
	public int[] getTime() {
		return new int[] { hours.getValue(), minutes.getValue() };
	}
	
	/**
	 * Sets the time for this picker
	 * 
	 * @param time
	 *            An array representing the new time. The first entry are the
	 *            hours and the second entry are the minutes
	 */
	public void setTime(int[] time) {
		if (time.length != 2) {
			throw new IllegalArgumentException(
					"Time array has to be of length 2!");
		}
		
		hours.setValue(time[0]);
		minutes.setValue(time[1]);
	}
	
}
