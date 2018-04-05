package ui;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Calendar;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import alarm.IAlarm;

public class AlarmChanger extends AlarmSetter {
	/**
	 * The alarm that should be changed
	 */
	protected IAlarm alarm;
	/**
	 * The DatePicker used to display and edit the date of the alarm
	 */
	protected DatePicker datePicker;
	/**
	 * The TimePicker that lets the user pick the respective time
	 */
	protected TimePicker timePicker;
	
	
	public AlarmChanger(Composite parent, int style, IAlarm alarm) {
		super(parent, style);
		
		assert (alarm != null);
		
		this.alarm = alarm;
		
		datePicker.load(alarm.getAlarmDate().toInstant());
		
		ZonedDateTime time = alarm.getAlarmDate().toInstant()
				.atZone(Calendar.getInstance().getTimeZone().toZoneId());
		
		timePicker.setTime(new int[] { time.get(ChronoField.HOUR_OF_DAY),
				time.get(ChronoField.MINUTE_OF_HOUR) });
	}
	
	@Override
	protected void initialize() {
		super.initialize();
		
		title.setText("Change alarm");
	}
	
	@Override
	protected void createContentComposite() {
		FontInheritComposite comp = new FontInheritComposite(this, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.marginTop = 20;
		layout.marginLeft = 10;
		layout.verticalSpacing = 50;
		layout.horizontalSpacing = 30;
		comp.setLayout(layout);
		
		// date
		FontInheritLabel dayLabel = new FontInheritLabel(comp, SWT.NONE);
		dayLabel.setText("Date:");
		
		datePicker = new DatePicker(comp, SWT.NONE);
		datePicker.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		
		// time
		FontInheritLabel timeLabel = new FontInheritLabel(comp, SWT.NONE);
		timeLabel.setText("Time:");
		
		timePicker = new TimePicker(comp, SWT.NONE);
	}
	
	@Override
	protected Button addBottomRightButton(Composite parent) {
		FontInheritButton okButton = new FontInheritButton(parent, SWT.PUSH);
		okButton.setText("OK");
		
		okButton.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				// save the alarm changes
				Calendar cal = Calendar.getInstance();
				cal.clear();
				cal.setTime(datePicker.getDate());
				
				cal.set(Calendar.HOUR_OF_DAY, timePicker.getTime()[0]);
				cal.set(Calendar.MINUTE, timePicker.getTime()[1]);
				
				alarm.setAlarmDate(cal.getTime());
				
				AlarmChanger.this.dispose();
			}
		});
		
		return okButton;
	}
}
