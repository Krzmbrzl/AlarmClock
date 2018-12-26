package alarm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.FactoryRegistry;
import javazoom.jl.player.advanced.AdvancedPlayer;
import util.Util;

public class MusicAlarm extends AbstractAlarm {

	private static final long serialVersionUID = 3141128512303573874L;

	/**
	 * The time it will take the music to fade in to full volume
	 */
	public static double MUSIC_FADE_IN_TIME = 30.0;
	/**
	 * The maximal music volume in %
	 */
	public static int MUSIC_MAX_VOLUME = 100;
	/**
	 * The amount of steps in which the music will be faded in
	 */
	public static int MUSIC_FADEIN_STEPS = 30;
	/**
	 * The timer used to fade in the music
	 */
	protected static transient Timer fadeTimer;

	/**
	 * The music source file or directory
	 */
	private File musicSource;
	/**
	 * The active player playing the music
	 */
	private transient AdvancedPlayer activePlayer;
	/**
	 * Indicates that the alarm has been terminated
	 */
	private AtomicBoolean terminated;


	public MusicAlarm(Date alarmDate, ERepetition repetition, File musicSource) throws Exception {
		super(alarmDate, repetition);

		try {
			checkMusicSource(musicSource);
		} catch (IllegalArgumentException e) {
			throw new Exception("The music dir does not contain any music!");
		}

		terminated = new AtomicBoolean(false);

		this.musicSource = musicSource;
	}

	/**
	 * Checks whether the given music source is valid
	 * 
	 * @param source
	 *            The source to check
	 */
	protected void checkMusicSource(File source) {
		if (!source.exists()) {
			throw new IllegalArgumentException("Music source does not exist!");
		}

		if (source.isDirectory()) {
			if (getMusicFiles(source).isEmpty()) {
				throw new IllegalArgumentException("The source directory does not contain music files!");
			}
		} else {
			if (!isMusicFile(source)) {
				throw new IllegalArgumentException("The source file is not a music file!");
			}
		}
	}

	/**
	 * Checks whether the given file is a music file
	 * 
	 * @param file
	 *            The file to check
	 */
	protected boolean isMusicFile(File file) {
		String fileExtension = Util.getFileExtension(file);

		if (fileExtension == null || (!fileExtension.toLowerCase().equals("mp3"))) {
			return false;
		}

		return true;
	}

	/**
	 * Gets the music files that re in the given directory
	 * 
	 * @param dir
	 *            The directory (and sub-directories) to check for music files
	 * @return A list containing all found music files
	 */
	protected List<File> getMusicFiles(File dir) {
		List<File> musicFiles = new ArrayList<File>();

		if (!dir.isDirectory()) {
			if (isMusicFile(dir)) {
				musicFiles.add(dir);

				return musicFiles;
			} else {
				throw new IllegalArgumentException(
						"The given file (" + dir.getAbsolutePath() + ") is neither a directory nor a music file!");
			}
		}

		for (File currentFile : dir.listFiles()) {
			if (currentFile.isFile()) {
				if (isMusicFile(currentFile)) {
					musicFiles.add(currentFile);
				}
			} else {
				musicFiles.addAll(getMusicFiles(currentFile));
			}
		}

		return musicFiles;
	}

	@Override
	protected void executeAlarm() {
		terminated.set(false);
		List<File> musicFiles = getMusicFiles(musicSource);

		int failures = 0;

		if (fadeTimer == null) {
			try {
				startMusicFading();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		while (!terminated.get()) {
			try {
				activePlayer = new AdvancedPlayer(
						musicFiles.get(new Random().nextInt(musicFiles.size())).toURI().toURL().openStream(),
						FactoryRegistry.systemRegistry().createAudioDevice());

				activePlayer.play();
			} catch (JavaLayerException | IOException e) {
				e.printStackTrace();

				if (failures > 10) {
					// terminate on too many failures
					// TODO log
					terminate();
				}

				failures++;
			}
		}
	}

	/**
	 * starts fading in the music
	 * 
	 * @throws IOException
	 */
	protected void startMusicFading() throws IOException {
		// set volume to 0
		new ProcessBuilder(new String[] { "amixer", "-M", "set", "PCM", "--", "0%" }).start();

		fadeTimer = new Timer();
		TimerTask fadeTask = new TimerTask() {
			private int counter = 1;

			@Override
			public void run() {
				if (counter > MUSIC_FADEIN_STEPS) {
					// Stops the fade process
					fadeTimer.cancel();
					fadeTimer = null;
					return;
				}

				try {
					new ProcessBuilder(new String[] { "amixer", "-M", "set", "PCM", // TODO use different audio output
																					// name
							MUSIC_MAX_VOLUME / MUSIC_FADEIN_STEPS + "%+" }).start();
				} catch (IOException e) {
					e.printStackTrace();
				}

				counter++;
			}
		};

		fadeTimer.scheduleAtFixedRate(fadeTask, 0, (long) ((MUSIC_FADE_IN_TIME / MUSIC_FADEIN_STEPS) * 1000));
	}

	@Override
	public void terminate() {
		terminated.set(true);

		if (activePlayer != null) {
			synchronized (activePlayer) {
				activePlayer.close();
			}
		}
	}

	/**
	 * Gets the default music source directory which is a directory named
	 * "AlarmMusic" in the music directory or if that doesn't exist the music
	 * directory itself.
	 */
	public static File getDefaultMusicDir() {
		File musicDir = new File(System.getProperty("user.home") + File.separator + "Music");

		if (!musicDir.exists()) {
			throw new IllegalStateException("Music directory could not be found!");
		}

		File alarmDir = new File(musicDir.getAbsolutePath() + File.separator + "AlarmMusic");

		if (alarmDir.exists()) {
			return alarmDir;
		} else {
			return musicDir;
		}
	}

	/**
	 * Gets the currently active music player
	 * 
	 * @return The active player or <code>null</code> if none could be found
	 */
	protected AdvancedPlayer getActivePlayer() {
		return activePlayer;
	}

	/**
	 * Play the next song
	 */
	public void nextSong() {
		if (activePlayer != null) {
			activePlayer.close();
		}
	}

	protected void openAlarmShell() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				final Display display = Display.getDefault();

				AtomicBoolean selfCreated = new AtomicBoolean(display.getThread().equals(Thread.currentThread()));

				display.asyncExec(new Runnable() {

					@Override
					public void run() {
						final Shell shell = new Shell(display, SWT.ON_TOP);

						shell.setText("Alarm - " + getGroup().getName());
						GridLayout layout = new GridLayout(2, true);
						layout.verticalSpacing = 20;
						layout.marginWidth = 15;
						layout.marginHeight = 15;
						shell.setLayout(layout);

						Label title = new Label(shell, SWT.CENTER);
						title.setText("Alarm - " + getGroup().getName());
						title.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
						Util.magnifyFont(title, 2.5);

						Button terminate = new Button(shell, SWT.PUSH);
						terminate.setText("Terminate");
						terminate.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								shell.dispose();
							}
						});
						Util.magnifyFont(terminate, 2);
						terminate.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

						Button next = new Button(shell, SWT.PUSH);
						next.setText("Next");
						next.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								nextSong();
							}
						});
						Util.magnifyFont(next, 2);
						next.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

						shell.addDisposeListener(new DisposeListener() {

							@Override
							public void widgetDisposed(DisposeEvent e) {
								terminate();
							}
						});

						shell.pack();

						// center window
						Rectangle areaSize = display.getClientArea();
						shell.setLocation(new Point(areaSize.width / 2 - shell.getSize().x / 2,
								areaSize.height / 2 - shell.getSize().y / 2));

						shell.open();

						if (selfCreated.get()) {
							while (!display.isDisposed()) {
								if (!display.readAndDispatch()) {
									display.sleep();
								}
							}
						}
					}
				});
			}
		}).start();
	}
}
