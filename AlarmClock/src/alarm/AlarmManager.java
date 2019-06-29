package alarm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.swt.widgets.Display;

import util.NTPException;
import util.Util;


/**
 * This class manages alarms and is responsible for actually invoking them at
 * the right time
 * 
 * @author Raven
 *
 */
public class AlarmManager implements Serializable, IAlarmListener {

	private static final long serialVersionUID = 4283031660055197487L;

	public static final String PROGRAM_DIR = System.getProperty("user.home") + File.separator + ".AlarmClock";

	public static final String SAVE_PATH = PROGRAM_DIR + File.separator + "AlarmManager.ser";

	/**
	 * The manager instance
	 */
	protected static transient AlarmManager MANAGER;
	/**
	 * The comparator used for sorting the alarms. It will sort them based on the
	 * set date
	 */
	public static final Comparator<IAlarm> ALARM_COMPARATOR = new Comparator<IAlarm>() {

		@Override
		public int compare(IAlarm a1, IAlarm a2) {
			if (!a1.isActive()) {
				if (a2.isActive()) {
					return 1;
				}
			}
			if (!a2.isActive()) {
				if (a1.isActive()) {
					return -1;
				}
			}
			return a1.getAlarmDate().compareTo(a2.getAlarmDate());
		}
	};

	protected transient Thread alarmThread;

	/**
	 * A list of programmed alarms
	 */
	protected List<IAlarm> alarms;

	/**
	 * The currently active alarm
	 */
	private IAlarm currentAlarm;
	/**
	 * The lock used whenever an alarm or the alarm list is modified
	 */
	protected ReentrantLock alarmLock;

	/**
	 * A list of all alarm listeners
	 */
	protected transient List<IAlarmManagerListener> alarmListener;

	/**
	 * A counter for how often saving this manager has failed before
	 */
	protected transient int failedSaves;


	protected AlarmManager() {
		alarms = new ArrayList<IAlarm>();

		alarmLock = new ReentrantLock();

		failedSaves = 0;
	}

	/**
	 * Gets the current instance of the alarm manager
	 */
	public static final AlarmManager getManager() {
		initialize();

		return MANAGER;
	}

	/**
	 * Initializes the manager
	 */
	public static final void initialize() {
		File saveDir = new File(PROGRAM_DIR);

		if (!saveDir.exists()) {
			saveDir.mkdirs();
		}

		if (MANAGER == null) {
			File savedFile = new File(SAVE_PATH);
			if (savedFile.exists() && savedFile.length() > 0) {
				MANAGER = load();
				MANAGER.queueAlarms();
			} else {
				MANAGER = new AlarmManager();
			}
		}
	}

	public final void shutdown() {
		if (alarmThread != null) {
			// close the background thread
			alarmThread.interrupt();
		}
	}

	/**
	 * Adds the given alarm to this manager
	 * 
	 * @param alarm
	 *            The alarm to add
	 * @param notifyListener
	 *            Indicates whether the liostener should be notified about this
	 *            change
	 * @param reconfigure
	 *            Inidcates whether the alarms should be reconfigured automatically
	 */
	protected void addAlarm(IAlarm alarm, boolean notifyListener, boolean reconfigure) {
		synchronized (alarmLock) {
			alarms.add(alarm);
			alarm.addAlarmListener(this);
		}

		if (notifyListener) {
			notifyAlarmsChanged();
		}

		if (reconfigure) {
			reconfigureAlarms();
		}
	}

	/**
	 * Adds the given alarm to this manager
	 * 
	 * @param alarm
	 *            The alarm to add
	 */
	public void addAlarm(IAlarm alarm) {
		addAlarm(alarm, true, true);
	}

	/**
	 * Removes the given alarm from this manager
	 * 
	 * @param alarm
	 *            The alarm to remove
	 * @param notifyListener
	 *            Indicates whether the listener should be notified about this
	 *            change
	 * @param reconfigure
	 *            Indicates whether the alarms should be reconfigured automatically
	 */
	protected void removeAlarm(IAlarm alarm, boolean notifyListener, boolean reconfigure) {
		synchronized (alarmLock) {
			alarms.remove(alarm);
			alarm.removeAlarmListener(this);
		}

		if (notifyListener) {
			notifyAlarmsChanged();
		}

		if (reconfigure) {
			reconfigureAlarms();
		}
	}

	/**
	 * Removes the given alarm fro this manager
	 * 
	 * @param alarm
	 *            The alarm to remove
	 */
	public void removeAlarm(IAlarm alarm) {
		removeAlarm(alarm, true, true);
	}

	/**
	 * Adds the given alarm group to this manager
	 * 
	 * @param group
	 *            The group to add
	 */
	public void addGroup(AlarmGroup group) {
		synchronized (group) {
			for (IAlarm currentAlarm : group.getAlarms()) {
				addAlarm(currentAlarm, false, false);
			}
		}

		reconfigureAlarms();

		notifyAlarmsChanged();
	}

	/**
	 * Removes the given alarm group from this manager
	 * 
	 * @param group
	 *            the group to remove
	 */
	public void removeGroup(AlarmGroup group) {
		synchronized (group) {
			for (IAlarm currentAlarm : group.getAlarms()) {
				removeAlarm(currentAlarm, false, false);
			}
		}

		reconfigureAlarms();

		notifyAlarmsChanged();
	}

	/**
	 * Gets a list of all alarms that this manager currently holds
	 */
	public List<IAlarm> getAlarms() {
		synchronized (alarmLock) {
			return alarms;
		}
	}

	/**
	 * Reconfigures the set alarms
	 */
	protected void reconfigureAlarms() {
		boolean requeueAlarms = false;

		removeOutdatedAlarms();

		synchronized (alarmLock) {
			if (alarms.size() == 0) {
				if (alarmThread != null) {
					// make sure there the currently queued alarm is being removed as well
					alarmThread.interrupt();
				}

				return;
			}

			if (alarms.size() == 1) {
				requeueAlarms = !alarms.get(0).equals(currentAlarm);
			} else {
				requeueAlarms = !alarms.get(0).equals(currentAlarm);
			}
		}

		if (requeueAlarms) {
			queueAlarms();
		}
	}

	/**
	 * Queues the current alarms for execution. This will create the
	 * {@link #alarmThread} which will sleep until the time for the first alarm in
	 * the list is reached. Afterwards it will be invoked and the list will be
	 * updated if necessary. This loop will continue until the Thread is interrupted
	 * or there are no more alarms.<br>
	 * This method will also interrupt {@link #alarmThread} if it does already exist
	 * and will create a new one instead
	 */
	protected void queueAlarms() {
		if (alarmThread != null) {
			// Stop the thread in order to requeue
			alarmThread.interrupt();
		}

		alarmThread = new Thread(new Runnable() {

			@Override
			public void run() {
				// loop as long as there are alarms
				alarmLock.lock();

				// counter for how often the system has failed to retrieve the
				// time
				int timeFailures = 0;

				while (alarms.size() > 0 && alarms.get(0).isActive()) {
					try {
						if (Thread.currentThread().isInterrupted()) {
							throw new InterruptedException();
						}
						currentAlarm = alarms.get(0);

						if (currentAlarm == null) {
							// something went wrong
							break;
						}

						long currentTime;
						try {
							currentTime = Util.getNTPTime();
							timeFailures = 0;
						} catch (NTPException e1) {
							e1.printStackTrace();

							if (timeFailures > 180) { // Try for half an hour
								// TODO: add note that the alarm is being
								// invoked because the time couldn't be
								// retrieved
								currentTime = currentAlarm.getAlarmDate().getTime();
							} else {
								timeFailures++;

								// the time could not be retrieved -> continue
								// until it is
								Thread.sleep(10000); // Try again after ten
														// seconds
								continue;
							}
						}

						long timeDiff = currentAlarm.getAlarmDate().getTime() - currentTime;

						if (timeDiff > 0) {
							// Don't lock while sleeping
							alarmLock.unlock();

							Thread.sleep(timeDiff);

							alarmLock.lock();
						} else {
							if (timeDiff < 1000 * 60 * 120) {
								// if time diff is greater than 120 minutes ->
								// abort alarm (using "<" because of negative
								// numbers)
								rescheduleCurrentAlarm();
								continue;
							}
						}

						if (currentAlarm == null) {
							try {
								throw new Exception("Current alarm is null!");
							} catch (Exception e) {
								e.printStackTrace();

								break;
							}
						}

						// the respective time has passed
						currentAlarm.invoke();
						notifyAlarmInvoked(currentAlarm);

						rescheduleCurrentAlarm();
					} catch (InterruptedException e) {
						if (alarmLock.isHeldByCurrentThread()) {
							alarmLock.unlock();
						}
						return;
					}
				}

				// The cycle has been interrupted or there are no more
				// alarms
				if (alarms.size() > 0 && alarms.get(0).isActive()) {
					if (alarmLock.isHeldByCurrentThread()) {
						alarmLock.unlock();
					}
					reconfigureAlarms();
				} else {
					if (alarmLock.isHeldByCurrentThread()) {
						alarmLock.unlock();
					}
				}
			}
		});

		alarmThread.start();
	}

	/**
	 * Reschedules the current alarm. This method gets called when the current alarm
	 * has been invoked (or cancelled because it was way to late
	 */
	protected void rescheduleCurrentAlarm() {
		if (currentAlarm.getRepetitionCycle() != ERepetition.NONE) {
			currentAlarm.recalculateAlarmDate();
			reconfigureAlarms();
			notifyAlarmsChanged();
		} else {
			if (currentAlarm.isOutDated()) {
				// remove outdated alarm
				removeAlarm(currentAlarm);
			}
		}
	}

	/**
	 * Gets the currently active alarm
	 */
	public IAlarm getActiveAlarm() {
		return currentAlarm;
	}

	/**
	 * Adds the given alarm listener to this manager
	 * 
	 * @param listener
	 *            The listener to add
	 */
	public void addAlarmManagerListener(IAlarmManagerListener listener) {
		assert (listener != null);

		synchronized (getAlarmListener()) {
			getAlarmListener().add(listener);
		}

		save();
	}

	/**
	 * Removes the given alarm listener from this manager
	 * 
	 * @param listener
	 *            The listener to remove
	 */
	public void removeAlarmManagerListener(IAlarmManagerListener listener) {
		synchronized (getAlarmListener()) {
			getAlarmListener().remove(listener);
		}

		save();
	}

	/**
	 * Gets all set alarms as groups.<br>
	 * If an alarm is part of a group the respective group is returned otherwise the
	 * alarm will be added to it's own unnamed group
	 */
	public List<AlarmGroup> getAllAlarmsAsGroups() {
		List<AlarmGroup> groups = new ArrayList<AlarmGroup>();

		for (IAlarm currentAlarm : getAlarms()) {
			AlarmGroup currentGroup = currentAlarm.getGroup();

			if (currentGroup == null) {
				currentGroup = new AlarmGroup("");
				currentGroup.addAlarm(currentAlarm);

				groups.add(currentGroup);
			} else {
				if (!groups.contains(currentGroup)) {
					groups.add(currentGroup);
				}
			}
		}

		return groups;
	}

	/**
	 * Notifies the registered alarm listeners about the invokation of the given
	 * alarm in a new thread
	 * 
	 * @param alarm
	 *            The alarm that has been invoked
	 */
	protected void notifyAlarmInvoked(IAlarm alarm) {
		synchronized (getAlarmListener()) {
			if (getAlarmListener().size() == 0) {
				return;
			}
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				synchronized (getAlarmListener()) {
					for (IAlarmManagerListener currentListener : getAlarmListener()) {
						currentListener.alarmInvoked(alarm);
					}
				}
			}
		}).start();
	}

	/**
	 * Notifies the registered alarm listeners about changes in the alarm list
	 */
	protected void notifyAlarmsChanged() {
		save();

		synchronized (getAlarmListener()) {
			if (getAlarmListener().size() == 0) {
				return;
			}

			for (IAlarmManagerListener currentListener : getAlarmListener()) {
				currentListener.alarmsChanged();
			}
		}
	}

	/**
	 * Saves this manager to disk
	 */
	protected void save() {
		try {
			alarmLock.lock();
			// write to temp-file first
			String tempName = SAVE_PATH + ".new.tmp";

			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(tempName));

			out.writeObject(this);

			out.close();

			// Try to load the manager from the serialized file in order to check its
			// integrity
			AlarmManager manager = load(tempName);

			if (manager == null) {
				// Something has gone wrong during serialization
				failedSaves++;

				if (failedSaves <= 3) {
					this.save();
				} else {
					// It failed 3 times already -> abort the program and the UI
					Display.getCurrent().close();
					throw new RuntimeException("Unable to save the alarms");
				}

				return;
			}

			failedSaves = 0;

			// if the manager could be successfully deserialized, the temp-file seems okay
			// -> overwrite actual file
			File tempFile = new File(tempName);
			Files.move(tempFile.toPath(), new File(SAVE_PATH).toPath(), StandardCopyOption.REPLACE_EXISTING );

			tempFile.delete();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			alarmLock.unlock();
		}
	}

	/**
	 * Loads the saved manager from disk (default path). Note that this method
	 * assumes that the saved file does exist!
	 */
	protected static AlarmManager load() {
		return load(SAVE_PATH);
	}

	/**
	 * Loads the saved manager from disk. Note that this method assumes that the
	 * saved file does exist!
	 * 
	 * @param path
	 *            The path to load from
	 */
	protected static AlarmManager load(String path) {
		AlarmManager manager = null;
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));
			manager = (AlarmManager) in.readObject();
			in.close();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}

		return manager;
	}

	/**
	 * Clears the list of alarm listeners
	 */
	public void clearListener() {
		synchronized (alarmLock) {
			getAlarmListener().clear();
		}
	}

	@Override
	public void alarmInvoked(IAlarm alarm) {
		// ignore
	}

	@Override
	public void alarmChanged(IAlarm alarm) {
		if (alarm.equals(currentAlarm)) {
			alarms.sort(ALARM_COMPARATOR);
			queueAlarms();
		} else {
			reconfigureAlarms();
		}

		notifyAlarmsChanged();
	}

	/**
	 * Removes all outdated alarms from the alarm list
	 */
	protected void removeOutdatedAlarms() {
		alarmLock.lock();

		ArrayList<IAlarm> alarmsCopy = new ArrayList<IAlarm>(alarms);
		int removals = 0;

		for (int i = 0; i < alarmsCopy.size(); i++) {
			if (alarmsCopy.get(i).isOutDated()) {
				alarms.remove(i - removals);
				removals++;
			}
		}

		alarms.sort(ALARM_COMPARATOR);

		alarmLock.unlock();
	}

	/**
	 * Gets the list of listeners attached to this AlarmManager. If there is no list
	 * initialized yet a new one will be created
	 * 
	 * @return The respective list
	 */
	protected List<IAlarmManagerListener> getAlarmListener() {
		if (alarmListener == null) {
			alarmListener = new ArrayList<IAlarmManagerListener>();
		}

		return alarmListener;
	}
}
