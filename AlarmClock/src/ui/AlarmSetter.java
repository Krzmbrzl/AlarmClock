package ui;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;

import alarm.AlarmGroup;
import alarm.AlarmManager;
import alarm.ERepetition;
import alarm.MusicAlarm;
import util.Util;

/**
 * This widget represents the graphical front end for programming an alarm
 * 
 * @author Raven
 *
 */
public class AlarmSetter extends FontInheritComposite {
	
	/**
	 * The time picker used for programming the alarm time
	 */
	protected TimePicker time;
	/**
	 * A list containing the days on which the alarm should be activated
	 */
	protected List<DayOfWeek> days;
	/**
	 * The toggle button indicating whether or not the alarm should be repeated
	 */
	protected ToggleColorButton repeat;
	/**
	 * The text field containing the name of the alarm
	 */
	protected FontInheritText nameText;
	/**
	 * The title of this widgets
	 */
	protected FontInheritLabel title;
	/**
	 * A map mapping a day to it's respective button
	 */
	protected Map<DayOfWeek, Button> dayButtons;
	
	
	public AlarmSetter(Composite parent, int style) {
		super(parent, style);
		
		days = new ArrayList<DayOfWeek>();
		dayButtons = new HashMap<DayOfWeek, Button>();
		
		initialize();
	}
	
	/**
	 * Initializes the components for this class
	 */
	protected void initialize() {
		GridLayout mainGrid = new GridLayout(1, false);
		mainGrid.marginHeight = 0;
		mainGrid.marginWidth = 0;
		super.setLayout(mainGrid);
		
		// Create title label
		title = new FontInheritLabel(this, SWT.CENTER);
		title.setText("Set Alarm");
		title.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		Util.magnifyFont(title, 1.5);
		
		
		// create content
		createContentComposite();
		
		// create composite for ok and cancel button
		FontInheritComposite buttonComp = new FontInheritComposite(this,
				SWT.NONE);
		buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		GridLayout bottomGrid = new GridLayout(3, false);
		bottomGrid.marginHeight = 0;
		bottomGrid.marginWidth = 0;
		buttonComp.setLayout(bottomGrid);
		
		// create the buttons themselves + spacer in bewteen
		Button leftButton = addBottomLeftButton(buttonComp);
		
		Canvas spacer = new Canvas(buttonComp, SWT.NONE);
		spacer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Button rightButton = addBottomRightButton(buttonComp);
		
		// set button layouts
		Point leftButtonSize = leftButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Point rightButtonSize = rightButton.computeSize(SWT.DEFAULT,
				SWT.DEFAULT);
		
		Point buttonSize = new Point(
				Math.max(leftButtonSize.x, rightButtonSize.x),
				Math.max(leftButtonSize.y, rightButtonSize.y));
		GridData buttonData = new GridData(SWT.CENTER, SWT.FILL, false, true);
		buttonData.heightHint = (int) (buttonSize.y * 1.05);
		buttonData.widthHint = (int) (buttonSize.x * 1.2);
		
		leftButton.setLayoutData(buttonData);
		rightButton.setLayoutData(buttonData);
	}
	
	/**
	 * Creates an alarm group and adds it to the {@link AlarmManager}
	 */
	protected AlarmGroup getAsAlarmGroup() {
		AlarmGroup group = new AlarmGroup(nameText.getText());
		
		int[] time = this.time.getTime();
		boolean repeat = this.repeat.getSelection() && this.repeat.isEnabled();
		
		DayOfWeek presentDay = DayOfWeek
				.of(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)).minus(1);
		
		boolean useNextDay = false;
		if (days.size() == 0) {
			days.add(presentDay);
			useNextDay = true;
		}
		
		// create a new alarm for each day
		for (DayOfWeek day : days) {
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, time[0]);
			cal.set(Calendar.MINUTE, time[1]);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			if (!shiftAlarmDate(day, cal, useNextDay)) {
				cal.set(Calendar.DAY_OF_WEEK, day.plus(1).getValue());
			}
			
			try {
				group.addAlarm(new MusicAlarm(cal.getTime(),
						(repeat) ? ERepetition.WEEKLY : ERepetition.NONE,
						MusicAlarm.getDefaultMusicDir()));
			} catch (Exception e) {
				// Don't add the alarm
				// TODO: notify
				e.printStackTrace();
			}
		}
		
		return group;
	}
	
	/**
	 * Shifts the alarm date if necessary. For example it sets the week_in_year
	 * attribute corresponding to the given target day assuming that the day for
	 * the alarm has to be in the future or at least the current day. This
	 * method assumes that the desired time has already been set.
	 * 
	 * @param target
	 *            The target day to match
	 * @param cal
	 *            The calendar to apply this info to
	 * @param useNextDay
	 *            Indicates whether the next day should be used instead of the
	 *            same day next week if there are adjustments to make
	 * @return Whether the day has been corrected by this method
	 */
	private boolean shiftAlarmDate(DayOfWeek target, Calendar cal,
			boolean useNextDay) {
		if (target.plus(1).getValue() < cal.get(Calendar.DAY_OF_WEEK)) {
			cal.set(Calendar.WEEK_OF_YEAR, cal.get(Calendar.WEEK_OF_YEAR) + 1);
			return false;
		}
		
		if (target.plus(1).getValue() == cal.get(Calendar.DAY_OF_WEEK)) {
			Calendar presentCal = Calendar.getInstance();
			
			int presentHour = presentCal.get(Calendar.HOUR_OF_DAY);
			int presentMinute = presentCal.get(Calendar.MINUTE);
			int setHour = cal.get(Calendar.HOUR_OF_DAY);
			int setMinute = cal.get(Calendar.MINUTE);
			
			if (presentHour > setHour
					|| (presentHour == setHour && presentMinute > setMinute)) {
				// week has to be increased as the set time on the current day
				// has already passed
				if (useNextDay) {
					// simply use the next day
					cal.set(Calendar.DAY_OF_YEAR,
							cal.get(Calendar.DAY_OF_YEAR) + 1);
					return true;
				} else {
					// use the same day next week
					cal.set(Calendar.WEEK_OF_YEAR,
							cal.get(Calendar.WEEK_OF_YEAR) + 1);
					return false;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Creates the composite that will hold the content associated with
	 * configuring the alarm
	 */
	protected void createContentComposite() {
		FontInheritComposite comp = new FontInheritComposite(this, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.marginTop = 5;
		layout.verticalSpacing = 10;
		layout.horizontalSpacing = 5;
		comp.setLayout(layout);
		
		
		// Create name label
		FontInheritLabel nameLabel = new FontInheritLabel(comp, SWT.NONE);
		nameLabel.setText("Name:");
		
		// Create name field
		nameText = new FontInheritText(comp, SWT.BORDER);
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		nameText.setText("Unnamed");
		
		// Create time label
		FontInheritLabel timeLabel = new FontInheritLabel(comp, SWT.NONE);
		timeLabel.setText("Time:");
		
		// Create time selection
		time = new TimePicker(comp, SWT.NONE);
		
		
		// Create Day label
		FontInheritLabel dayLabel = new FontInheritLabel(comp, SWT.NONE);
		dayLabel.setText("Day:");
		
		// Create day selection
		FontInheritComposite daySelection = new FontInheritComposite(comp,
				SWT.NONE);
		daySelection
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		// Create day buttons
		for (DayOfWeek day : DayOfWeek.values()) {
			FontInheritButton btn = new FontInheritButton(daySelection,
					SWT.TOGGLE);
			btn.setText(day.getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
			btn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
			
			dayButtons.put(day, btn);
			
			btn.addListener(SWT.Selection, new Listener() {
				
				@Override
				public void handleEvent(Event event) {
					if (btn.getSelection()) {
						days.add(day);
						btn.setForeground(btn.getDisplay()
								.getSystemColor(SWT.COLOR_BLUE));
					} else {
						days.remove(day);
						btn.setForeground(btn.getDisplay()
								.getSystemColor(SWT.COLOR_BLACK));
					}
					
					if (days.size() == 0) {
						repeat.setEnabled(false);
					} else {
						repeat.setEnabled(true);
					}
				}
			});
		}
		
		// Create composite for day buttons
		GridLayout dayLayout = new GridLayout(4, true);
		daySelection.setLayout(dayLayout);
		
		// Create repeat label
		FontInheritLabel repeatLabel = new FontInheritLabel(comp, SWT.NONE);
		repeatLabel.setText("Repeat:");
		
		// Create Repeat selection
		repeat = new ToggleColorButton(comp, SWT.NONE);
		repeat.setEnabled(false);
		repeat.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
	}
	
	/**
	 * <b>Usage is prohibited</b> for this class!
	 */
	@Override
	public void setLayout(Layout layout) {
		throw new IllegalAccessError("No support for custom layouts!");
	}
	
	/**
	 * Creates the button that will be placed in the bottom left
	 * 
	 * @param parent
	 *            The parent of the new button
	 */
	protected Button addBottomLeftButton(Composite parent) {
		FontInheritButton cancelButton = new FontInheritButton(parent,
				SWT.PUSH);
		cancelButton.setText("Cancel");
		
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AlarmSetter.this.dispose();
			}
		});
		
		return cancelButton;
	}
	
	/**
	 * Creates the button that will be placed in the bottom right
	 * 
	 * @param parent
	 *            The parent of the new button
	 */
	protected Button addBottomRightButton(Composite parent) {
		FontInheritButton okButton = new FontInheritButton(parent, SWT.PUSH);
		okButton.setText("OK");
		
		okButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				AlarmManager.getManager().addGroup(getAsAlarmGroup());
				
				AlarmSetter.this.dispose();
			}
		});
		
		return okButton;
	}
}
