package ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import alarm.IListener;

/**
 * This is a graphical date picker that lets the user input a day, month and a
 * year of a date
 * 
 * @author Raven
 *
 */
public class DatePicker extends FontInheritComposite {
	
	/**
	 * The spinner for the day
	 */
	protected IntegerSpinner daySpinner;
	/**
	 * The spinner for the month
	 */
	protected IntegerSpinner monthSpinner;
	/**
	 * The spinner for the year
	 */
	protected IntegerSpinner yearSpinner;
	
	
	public DatePicker(Composite parent, int style) {
		super(parent, style);
		
		initialize();
	}
	
	protected void initialize() {
		GridLayout mainGrid = new GridLayout(5, false);
		mainGrid.marginHeight = 0;
		mainGrid.marginWidth = 0;
		super.setLayout(mainGrid);
		
		daySpinner = new IntegerSpinner(this, 1, 31, SWT.NONE);
		daySpinner
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		
		FontInheritLabel dot1 = new FontInheritLabel(this, SWT.NONE);
		dot1.setText(".");
		
		monthSpinner = new IntegerSpinner(this, 1, 12, SWT.NONE);
		monthSpinner
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		monthSpinner.addListener(new IListener() {
			
			@Override
			public void notify(Object ctx) {
				switch ((int) ctx) {
					case 2:
						daySpinner.setMax(28);
						break;
					case 1:
					case 3:
					case 5:
					case 7:
					case 8:
					case 10:
					case 12:
						daySpinner.setMax(31);
						break;
					default:
						daySpinner.setMax(30);
				}
			}
		});
		
		FontInheritLabel dot2 = new FontInheritLabel(this, SWT.NONE);
		dot2.setText(".");
		
		int currentYear = Instant.now()
				.atZone(Calendar.getInstance().getTimeZone().toZoneId())
				.get(ChronoField.YEAR);
		yearSpinner = new IntegerSpinner(this, currentYear, currentYear + 100,
				SWT.NONE);
		yearSpinner
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
	}
	
	/**
	 * Loads this date spinner with the given instant
	 * 
	 * @param date
	 *            The date that should be loaded into this spinner
	 */
	public void load(Instant date) {
		ZonedDateTime zDate = date
				.atZone(Calendar.getInstance().getTimeZone().toZoneId());
		
		daySpinner.setValue(zDate.get(ChronoField.DAY_OF_MONTH));
		monthSpinner.setValue(zDate.get(ChronoField.MONTH_OF_YEAR));
		yearSpinner.setValue(zDate.get(ChronoField.YEAR));
	}
	
	/**
	 * Gets the date this spinner currently represents
	 * 
	 * @throws ParseException
	 *             If the spinner is in an invalid state this exception will be
	 *             thrown
	 */
	public Date getDate() {
		Date date = null;
		try {
			String strDate = daySpinner.getValue() + "."
					+ monthSpinner.getValue() + "." + yearSpinner.getValue();
			
			if (strDate.subSequence(0, strDate.indexOf(".")).length() == 1) {
				strDate = "0" + strDate;
			}
			
			if (strDate.subSequence(strDate.indexOf(".") + 1,
					strDate.lastIndexOf(".")).length() == 1) {
				strDate = strDate.substring(0, strDate.indexOf(".") + 1) + "0"
						+ strDate.substring(strDate.indexOf(".") + 1);
			}
			
			date = new SimpleDateFormat("dd.MM.yyyy").parse(strDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return date;
	}
}
