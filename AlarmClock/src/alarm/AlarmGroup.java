package alarm;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AlarmGroup implements Serializable {
	
	private static final long serialVersionUID = 2454405866754603793L;
	
	/**
	 * A list of alarms that are members of this group. In order to be part of
	 * this Group the time of the alarms has to be the same
	 */
	private List<IAlarm> members;
	/**
	 * The name of this group
	 */
	private String name;
	/**
	 * An Array containing the times for this group
	 */
	private int[] times;
	/**
	 * The repetition type of the alarms associated with this group
	 */
	private ERepetition repetition;
	
	
	public AlarmGroup(String name) {
		members = new ArrayList<IAlarm>();
		
		this.name = name;
	}
	
	/**
	 * Adds the given alarm to this group. In order for this to be possible the
	 * alarm has to have the same alarm time (not day) as the rest of the group
	 * 
	 * @param alarm
	 *            The alarm to add
	 * @return Whether the alarm has been added successfully
	 */
	public boolean addAlarm(IAlarm alarm) {
		if (!checkAlarm(alarm)) {
			return false;
		}
		
		members.add(alarm);
		alarm.group(this);
		
		return true;
	}
	
	/**
	 * removes the given alarm from this group
	 * 
	 * @param alarm
	 *            The alarm to remove
	 */
	public void removeAlarm(IAlarm alarm) {
		members.remove(alarm);
		alarm.ungroup();
		
		if (members.size() == 0) {
			// reset times
			times = null;
			repetition = null;
		}
	}
	
	/**
	 * Gets all alarms associated with this group
	 */
	public List<IAlarm> getAlarms() {
		return new ArrayList<IAlarm>(members);
	}
	
	/**
	 * Sets the name of this group
	 * 
	 * @param name
	 *            The new group name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the name of this group
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Checks whether the given alarm suits in this group
	 * 
	 * @param alarm
	 *            The alarm to check
	 * @return Whether or not the alarm suits to this group
	 */
	protected boolean checkAlarm(IAlarm alarm) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(alarm.getOriginalAlarmDate());
		
		int hours = cal.get(Calendar.HOUR_OF_DAY);
		int minutes = cal.get(Calendar.MINUTE);
		int seconds = cal.get(Calendar.SECOND);
		
		if (times == null) {
			times = new int[3];
			
			// set times
			times[0] = hours;
			times[1] = minutes;
			times[2] = seconds;
			repetition = alarm.getRepetitionCycle();
			
			return true;
		} else {
			// compare times
			return hours == times[0] && minutes == times[1]
					&& seconds == times[2]
					&& alarm.getRepetitionCycle().equals(repetition);
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !obj.getClass().equals(this.getClass())) {
			return false;
		}
		
		AlarmGroup compare = (AlarmGroup) obj;
		
		if (!compare.getName().equals(getName())) {
			return false;
		}
		
		return getAlarms().equals(compare.getAlarms());
	}
	
	/**
	 * Gets the time of this group (hour, minute, second)
	 */
	public int[] getTimes() {
		return times;
	}
	
	/**
	 * Gets the hour and minute of this group
	 */
	public int[] getHourAndMinute() {
		return new int[] { times[0], times[1] };
	}
	
	/**
	 * Gets the repetition of this group
	 */
	public ERepetition getRepetition() {
		return repetition;
	}
	
	/**
	 * Gets a list of days this group will ring on
	 */
	public List<DayOfWeek> getDays() {
		List<DayOfWeek> days = new ArrayList<DayOfWeek>();
		
		for (IAlarm currentAlarm : getAlarms()) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(currentAlarm.getAlarmDate());
			
			days.add(DayOfWeek.of(cal.get(Calendar.DAY_OF_WEEK)).minus(1));
			
		}
		
		return days;
	}
	
}
