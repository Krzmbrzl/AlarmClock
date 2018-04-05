package alarm;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * A simple alarm that implements all functions of an {@link IAlarm}
 * 
 * @author Raven
 *
 */
public abstract class AbstractAlarm implements IAlarm, Cloneable {

	/**
	 * The constant for a change activity
	 */
	protected final static int ACTIVITY_CHANGED = 0;
	/**
	 * The constant for a invoked activity
	 */
	protected final static int ACTIVITY_INVOKED = 1;


	private static final long serialVersionUID = 8051716558912275247L;

	/**
	 * The date this alarm is programmed to
	 */
	private Date alarmDate;
	/**
	 * The temporary alarm date to use instead of {@link #alarmDate} if it is set
	 */
	private Date tempAlarmDate;
	/**
	 * Indicates whether this alarm is currently active
	 */
	private boolean isActive;
	/**
	 * Indicates whether this alarm has been invoked yet
	 */
	private int invocations;
	/**
	 * Descibes the repetition cycle of this alarm
	 */
	private ERepetition repetitionCycle;
	/**
	 * The alarm group this alarm belongs to
	 */
	private AlarmGroup group;
	/**
	 * A list of alarm listeners
	 */
	protected List<IAlarmListener> listeners;


	/**
	 * Creates a new instance of this alarm
	 * 
	 * @param alarmDate
	 *            The date this alarm is set to
	 * @param repetition
	 *            How this alarm should repeat
	 */
	public AbstractAlarm(Date alarmDate, ERepetition repetition) {
		assert (alarmDate != null && repetition != null);
		// Don't allow the alarm to be set in the past
		assert (alarmDate.getTime() > Calendar.getInstance().getTimeInMillis());

		this.alarmDate = alarmDate;
		invocations = 0;
		isActive = true;

		listeners = new ArrayList<IAlarmListener>();

		repetitionCycle = repetition;
	}

	@Override
	public Date getAlarmDate() {
		// return the temporary alarm date
		if (temporaryAlarmActive()) {
			return tempAlarmDate;
		}

		return alarmDate;
	}

	/**
	 * Checks whether there is a temporary alarm date set for this alarm
	 */
	public boolean temporaryAlarmActive() {
		return tempAlarmDate != null;
	}

	@Override
	public Date getOriginalAlarmDate() {
		return alarmDate;
	}

	@Override
	public void invoke() {
		invocations++;
		tempAlarmDate = null;

		// run the alarm code in a new thread so that it can't delay other
		// alarms
		new Thread(new Runnable() {

			@Override
			public void run() {
				executeAlarm();
			}
		}).start();

		notifyListeners(ACTIVITY_INVOKED);
	}

	/**
	 * Gets called as soon as this alarm is invoked.
	 */
	protected abstract void executeAlarm();

	@Override
	public boolean isOutDated() {
		switch (getRepetitionCycle()) {
		case DAYLY:
		case WEEKLY:
			return false;
		case NONE:
			if (invocations > 0) {
				return true;
			} else {
				Date date = (tempAlarmDate == null || alarmDate.compareTo(tempAlarmDate) >= 0) ? alarmDate
						: tempAlarmDate;

				return date.compareTo(Calendar.getInstance().getTime()) < 0;
			}
		default:
			return false;

		}
	}

	@Override
	public ERepetition getRepetitionCycle() {
		return repetitionCycle;
	}

	@Override
	public void recalculateAlarmDate() {
		Instant inst = alarmDate.toInstant();

		TimeZone tz = TimeZone.getDefault();
		boolean DSTBefore = tz.inDaylightTime(new Date(inst.getEpochSecond()));

		switch (getRepetitionCycle()) {
		case DAYLY:
			inst = inst.plus(1, ChronoUnit.DAYS);
			break;
		case MINUTELY:
			inst = inst.plus(1, ChronoUnit.MINUTES);
			break;
		case HOURLY:
			inst = inst.plus(1, ChronoUnit.HOURS);
			break;
		case WEEKLY:
			inst = inst.plus(7, ChronoUnit.DAYS);
			break;
		default:
			try {
				throw new Exception("This alarm does not repeat and therefore can't recalculate it's alarm date!");
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}

		if (DSTBefore ^ tz.inDaylightTime(new Date(inst.toEpochMilli()))) {
			// account for time DST time changes -> reshift with the given amount so that
			// 6am stays 6am and won't get transformed to 5am or 7am
			inst = DSTBefore ? inst.plus(tz.getDSTSavings(), ChronoUnit.MILLIS)
					: inst.minus(tz.getDSTSavings(), ChronoUnit.MILLIS);
		}

		alarmDate = new Date(inst.toEpochMilli());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !obj.getClass().equals(this.getClass())) {
			return false;
		}

		AbstractAlarm alarm = (AbstractAlarm) obj;

		if (!alarm.getAlarmDate().equals(this.getAlarmDate())) {
			return false;
		}

		if (alarm.getRepetitionCycle() != this.getRepetitionCycle()) {
			return false;
		}

		return true;
	}

	@Override
	public void group(AlarmGroup group) {
		this.group = group;
	}

	@Override
	public void ungroup() {
		group = null;
	}

	@Override
	public AlarmGroup getGroup() {
		return group;
	}

	/**
	 * Checks whether this alarm is part of an alarm group
	 */
	public boolean isGrouped() {
		return getGroup() != null;
	}


	@Override
	public void temporaryChange(Date temporaryDate) {
		assert (temporaryDate != null);

		tempAlarmDate = temporaryDate;
	}

	@Override
	public void setActive(boolean active) {
		if (isActive != active) {
			isActive = active;

			notifyListeners(ACTIVITY_CHANGED);
		}
	}

	@Override
	public boolean isActive() {
		return isActive;
	}

	@Override
	public void addAlarmListener(IAlarmListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	@Override
	public void removeAlarmListener(IAlarmListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	/**
	 * Notifies all registered listeners about the given activity
	 * 
	 * @param activity
	 *            The activity that caused the listener notification
	 */
	protected void notifyListeners(int activity) {
		switch (activity) {
		case ACTIVITY_CHANGED:
			for (IAlarmListener currentListener : listeners) {
				currentListener.alarmChanged(this);
			}
			break;

		case ACTIVITY_INVOKED:
			for (IAlarmListener currentListener : listeners) {
				currentListener.alarmInvoked(this);
			}
			break;
		}
	}

	@Override
	public void setAlarmDate(Date date) {
		assert (date != null);

		tempAlarmDate = date;

		notifyListeners(ACTIVITY_CHANGED);
	}
}
