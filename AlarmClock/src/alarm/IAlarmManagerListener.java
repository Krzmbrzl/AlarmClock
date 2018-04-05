package alarm;

/**
 * An interface describing a listener for an AlarmManager
 * @author Raven
 *
 */
public interface IAlarmManagerListener {
	
	/**
	 * Gets called whenver an alarm is invoked
	 * 
	 * @param alarm
	 *            The alarm that has been invoked
	 */
	public void alarmInvoked(IAlarm alarm);
	
	/**
	 * Gets called whenever the list of alarms have changed
	 */
	public void alarmsChanged();
}
