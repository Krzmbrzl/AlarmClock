package alarm;

import java.io.Serializable;
import java.util.Date;

public interface IAlarm extends Serializable {
	
	/**
	 * Gets the date the alarm is supposed to be invoked the next time
	 */
	public Date getAlarmDate();
	
	/**
	 * Gets the alarm date this alarm is programmed on ignoring any set temporyr
	 * alarm changes
	 */
	public Date getOriginalAlarmDate();
	
	/**
	 * Sets the alarm date for this alarm. Depending on the implementation it
	 * may be that the change is temporary. For a guaranteed permanent change
	 * you have to create a new alarm
	 * 
	 * @param date
	 *            The new alarm date to uses
	 */
	public void setAlarmDate(Date date);
	
	/**
	 * Adds the given listener to this alarm
	 * 
	 * @param listener
	 *            The alarm listener to add
	 */
	public void addAlarmListener(IAlarmListener listener);
	
	/**
	 * Removes the given listener from this alarm
	 * 
	 * @param listener
	 *            The alarm listener to remove
	 */
	public void removeAlarmListener(IAlarmListener listener);
	
	/**
	 * Invokes this alarm
	 */
	public void invoke();
	
	/**
	 * Terminates this alarm
	 */
	public void terminate();
	
	/**
	 * Checks whether this alarm is outdated. This is if the alarm has been
	 * invoked and is not set to repeat itself
	 */
	public boolean isOutDated();
	
	/**
	 * Gets the interval in which this alarm will repeat itself
	 */
	public ERepetition getRepetitionCycle();
	
	/**
	 * Asks thhis alarm to recalculate it's alarm date
	 */
	public void recalculateAlarmDate();
	
	/**
	 * Groups this alarm to the given alarm group
	 * 
	 * @param group
	 *            The alarm group this alarm should be grouped to
	 */
	public void group(AlarmGroup group);
	
	/**
	 * Ungroups this alarm from the alarm group it is currently in
	 */
	public void ungroup();
	
	/**
	 * Gets the group this alarm is associated with
	 * 
	 * @return The respective <code>AlarmGroup</code> or <code>null</code> if
	 *         none could be found
	 */
	public AlarmGroup getGroup();
	
	/**
	 * Changes this alarm temporary. That means it will change the alarm's date.
	 * If the alarm is repeating it will resume to the old alarm date cycle
	 * afterwards.
	 * 
	 * @param date
	 *            The new alarm date to use once
	 */
	public void temporaryChange(Date temporaryAlarmDate);
	
	/**
	 * Sets this alarm's activity status
	 * 
	 * @param active
	 *            Whether the alarm should be active
	 */
	public void setActive(boolean active);
	
	/**
	 * Checks whether this alarm is currently active
	 */
	public boolean isActive();
}
