package alarm;

import java.io.Serializable;

/**
 * This interface describes an object that is listening to alarm changes
 * 
 * @author Raven
 *
 */
public interface IAlarmListener extends Serializable {
	
	/**
	 * Gets called whenver an alarm is invoked
	 * 
	 * @param alarm
	 *            The alarm that has been invoked
	 */
	public void alarmInvoked(IAlarm alarm);
	
	/**
	 * Gets called whenever the alarm changes
	 * 
	 * @param alarm
	 *            The alarm that has changed
	 */
	public void alarmChanged(IAlarm alarm);
}
