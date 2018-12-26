package starter;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import alarm.AlarmManager;
import alarm.IAlarm;
import alarm.IAlarmManagerListener;
import alarm.MusicAlarm;
import ui.AlarmList;
import ui.AlarmManagerUI;
import ui.AlarmSetter;
import ui.AnalogClock;
import ui.IntegratedClockUI;
import util.Util;

public class Starter {
	/**
	 * The font height that should be used
	 */
	public static final int fontHeight = 18;
	/**
	 * The timer used to schedule the display-off action
	 */
	protected static Timer displayOffTimer = new Timer();
	/**
	 * How long the display should stay on if nothing happens
	 */
	protected static int displayOffDelay = 8000;
	/**
	 * Indicates whether the display has been turned off
	 */
	protected static AtomicBoolean displayTurnedOff = new AtomicBoolean(false);
	/**
	 * The used clock UI
	 */
	protected static IntegratedClockUI ui;
	
	
	static class AlarmListener implements IAlarmManagerListener {
		
		private int fontSize;
		
		public AlarmListener(int fontSize) {
			this.fontSize = fontSize;
		}
		
		@Override
		public void alarmInvoked(IAlarm alarm) {
			if (!(alarm instanceof MusicAlarm)) {
				return;
			}
			MusicAlarm musicAlarm = (MusicAlarm) alarm;
			
			Display display = Display.getDefault();
			display.asyncExec(new Runnable() {
				
				@Override
				public void run() {
					Shell parentShell = null;
					
					for (Shell currentShell : display.getShells()) {
						if (currentShell.getLayout() instanceof StackLayout) {
							parentShell = currentShell;
							break;
						}
					}
					
					if (parentShell == null) {
						// no GUI -> abort
						return;
					}
					
					if (parentShell.getLayout() instanceof StackLayout) {
						StackLayout layout = (StackLayout) parentShell
								.getLayout();
						// store old top control for recreation purpose
						Control oldTopControl = layout.topControl;
						
						// assemble font to use
						FontData[] data = parentShell.getFont().getFontData();
						data[0].setHeight(fontSize);
						Font font = new Font(display, data);
						
						// create the shell for the alarm
						Shell alarmShell = new Shell(parentShell, SWT.NO_TRIM);
						alarmShell.setLocation(parentShell.getLocation());
						alarmShell.setSize(parentShell.getSize());
						alarmShell.setCursor(parentShell.getCursor());
						GridLayout mainGrid = new GridLayout(2, true);
						mainGrid.marginHeight = 0;
						mainGrid.marginWidth = 0;
						mainGrid.marginLeft = 10;
						alarmShell.setLayout(mainGrid);
						
						// create the clock
						AnalogClock clock = new AnalogClock(alarmShell,
								SWT.NONE, false);
						clock.setLayoutData(
								new GridData(SWT.FILL, SWT.FILL, true, true));
						
						// create composite for the alarm controls
						Composite alarmControls = new Composite(alarmShell,
								SWT.NONE);
						alarmControls.setLayoutData(
								new GridData(SWT.FILL, SWT.FILL, true, true));
						GridLayout grid = new GridLayout(2, true);
						grid.verticalSpacing = 80;
						grid.marginHeight = 0;
						grid.marginWidth = 0;
						grid.marginTop = 10;
						alarmControls.setLayout(grid);
						
						// create title of the alarm
						Label title = new Label(alarmControls, SWT.CENTER);
						title.setFont(font);
						title.setText("Alarm - " + alarm.getGroup().getName());
						title.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
								true, false, 2, 1));
						
						// create terminate button
						Button terminateButton = new Button(alarmControls,
								SWT.PUSH);
						terminateButton.setFont(font);
						terminateButton.setText("Terminate");
						terminateButton.setLayoutData(
								new GridData(SWT.FILL, SWT.FILL, true, false));
						final Shell helper = parentShell;
						terminateButton.addListener(SWT.Selection,
								new Listener() {
									
									@Override
									public void handleEvent(Event arg0) {
										musicAlarm.terminate();
										display.asyncExec(new Runnable() {
											
											@Override
											public void run() {
												layout.topControl = oldTopControl;
												helper.layout();
												alarmShell.dispose();
											}
										});
									}
								});
								
						// create next button
						Button nextButton = new Button(alarmControls, SWT.PUSH);
						nextButton.setFont(font);
						nextButton.setText("Next");
						nextButton.setLayoutData(
								new GridData(SWT.FILL, SWT.FILL, true, false));
						nextButton.addListener(SWT.Selection, new Listener() {
							
							@Override
							public void handleEvent(Event arg0) {
								musicAlarm.nextSong();
							}
						});
						
						// switch the alarm shell as the new top control
						layout.topControl = alarmShell;
						
						alarmShell.open();
						
						parentShell.layout();
					}
				}
			});
		}
		
		@Override
		public void alarmsChanged() {
			// display next alarm time
			final List<IAlarm> alarms = AlarmManager.getManager().getAlarms();
			
			Display display = Display.getDefault();
			
			display.asyncExec(new Runnable() {
				
				@Override
				public void run() {
					Shell parentShell = null;
					
					for (Shell currentShell : display.getShells()) {
						if (currentShell.getLayout() instanceof StackLayout) {
							parentShell = currentShell;
							break;
						}
					}
					
					if (parentShell == null) {
						return;
					}
					
					Shell shell = new Shell(display,
							SWT.NO_TRIM | SWT.NO_BACKGROUND);
					shell.setLayout(new FillLayout());
					
					FontData[] data = parentShell.getFont().getFontData();
					data[0].setHeight(fontSize);
					Font font = new Font(display, data);
					
					shell.setFont(font);
					
					Label timeLabel = new Label(shell, SWT.NO_BACKGROUND);
					timeLabel.setFont(font);
					Util.magnifyFont(timeLabel, 0.8);
					
					String message = "No alarms set";
					if (alarms.size() > 0 && alarms.get(0).isActive()) {
						message = "Next alarm: " + alarms.get(0).getAlarmDate();
					}
					timeLabel.setText(message);
					
					shell.pack();
					
					Rectangle area = parentShell.getClientArea();
					Point location = parentShell.getLocation();
					
					shell.setLocation(
							area.width / 2 + location.x - shell.getSize().x / 2,
							location.y + shell.getSize().y);
					
					shell.open();
					
					display.timerExec(5000, new Runnable() {
						
						@Override
						public void run() {
							shell.close();
							shell.dispose();
						}
					});
				}
			});
		}
		
	}
	
	
	public static void main(String[] args) {
		boolean debug = false;
		
		for (String currentArg : args) {
			if (currentArg.equals("debug")) {
				debug = true;
			}
		}
		
		AlarmManager.initialize();
		AlarmListener alarmListener = new AlarmListener(fontHeight);
		AlarmManager.getManager().addAlarmManagerListener(alarmListener);
		
		final Display display = Display.getDefault();
		
		final Shell topLevelShell;
		
		if (debug) {
			topLevelShell = new Shell(display);
		} else {
			topLevelShell = new Shell(display, SWT.NO_TRIM);
			
			// hide cursor
			Color white = display.getSystemColor(SWT.COLOR_WHITE);
			Color black = display.getSystemColor(SWT.COLOR_BLACK);
			PaletteData palette = new PaletteData(
					new RGB[] { white.getRGB(), black.getRGB() });
			ImageData sourceData = new ImageData(16, 16, 1, palette);
			sourceData.transparentPixel = 0;
			Cursor cursor = new Cursor(display, sourceData, 0, 0);
			
			topLevelShell.setCursor(cursor);
		}
		
		topLevelShell.setText("AlarmClock");
		StackLayout layout = new StackLayout();
		topLevelShell.setLayout(layout);
		
		FontData[] fontData = topLevelShell.getFont().getFontData();
		fontData[0].setHeight(fontHeight);
		topLevelShell.setFont(new Font(display, fontData));
		
		
		ui = new IntegratedClockUI(topLevelShell, SWT.LEFT | SWT.RIGHT);
		
		layout.topControl = ui;
		
		ui.createButton("Set Alarm", SWT.LEFT, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				AlarmSetter setter = new AlarmSetter(ui, SWT.NONE);
				setter.addDisposeListener(new DisposeListener() {
					
					@Override
					public void widgetDisposed(DisposeEvent e) {
						ui.hide(SWT.NONE);
					}
				});
				ui.setPanel(setter, SWT.LEFT);
				ui.hide(SWT.RIGHT);
			}
		});
		
		ui.createButton("Mode", SWT.LEFT, new Listener() {
			
			@Override
			public void handleEvent(Event arg0) {
				// TODO add option to set the mode of the alarm clock
				// (enable/disable)
			}
		}).setEnabled(false);
		
		ui.createButton("Manage alarms", SWT.RIGHT, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				AlarmManagerUI manager = new AlarmManagerUI(topLevelShell,
						SWT.NONE);
				manager.addDisposeListener(new DisposeListener() {
					
					@Override
					public void widgetDisposed(DisposeEvent e) {
						ui.hide(SWT.NONE);
					}
				});
				ui.setPanel(manager, SWT.RIGHT);
				ui.hide(SWT.LEFT);
			}
		});
		
		ui.createButton("List alarms", SWT.RIGHT, new Listener() {
			
			@Override
			public void handleEvent(Event arg0) {
				AlarmList list = new AlarmList(topLevelShell, SWT.NONE);
				
				list.addDisposeListener(new DisposeListener() {
					
					@Override
					public void widgetDisposed(DisposeEvent e) {
						ui.hide(SWT.NONE);
					}
				});
				ui.setPanel(list, SWT.RIGHT);
				ui.hide(SWT.LEFT);
			}
		});
		
		ui.createButton("Next alarm", SWT.RIGHT, new Listener() {
			
			@Override
			public void handleEvent(Event arg0) {
				// pretend that the alarms have changed in order for the next
				// alarm to be displayed
				alarmListener.alarmsChanged();
			}
		});
		
		if (!debug) {
			Rectangle bounds = Display.getCurrent().getBounds();
			topLevelShell.setSize(new Point(bounds.width, bounds.height));
			topLevelShell.setMaximized(true);
			topLevelShell.setFullScreen(true);
			
			setUpDisplaySaver(display);
		}
		
		topLevelShell.open();
		
		while (!topLevelShell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
		display.dispose();
		
		displayOffTimer.cancel();
	}
	
	/**
	 * Sets up all the stuff for shutting down the screen if it is not needed
	 * 
	 * @param display
	 *            The used display
	 */
	protected static void setUpDisplaySaver(Display display) {
		displayOffTimer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				try {
					new ProcessBuilder(
							new String[] { "xset", "dpms", "force", "off" })
									.start();
					displayTurnedOff.set(true);
					display.asyncExec(new Runnable() {
						
						@Override
						public void run() {
							ui.setEnabled(false);
						}
					});
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}, displayOffDelay);
		
		
		Listener mouseListener = new Listener() {
			
			@Override
			public void handleEvent(Event e) {
				if (displayTurnedOff.get()) {
					// don't process the event as the screen has been black till
					// then
					displayTurnedOff.set(false);
				} else {
					display.asyncExec(new Runnable() {
						
						@Override
						public void run() {
							ui.setEnabled(true);
						}
					});
				}
				
				displayOffTimer.cancel();
				displayOffTimer = new Timer();
				displayOffTimer.schedule(new TimerTask() {
					
					@Override
					public void run() {
						try {
							new ProcessBuilder(new String[] { "xset", "dpms",
									"force", "off" }).start();
							displayTurnedOff.set(true);
							display.asyncExec(new Runnable() {
								
								@Override
								public void run() {
									ui.setEnabled(false);
								}
							});
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}, displayOffDelay);
			}
		};
		
		display.addFilter(SWT.MouseDown, mouseListener);
		display.addFilter(SWT.MouseMove, mouseListener);
	}
	
}
