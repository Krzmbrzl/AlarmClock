package ui;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import alarm.AlarmManager;
import alarm.IAlarm;
import alarm.IAlarmManagerListener;

public class AlarmList extends FontInheritComposite
		implements IAlarmManagerListener {
	
	/**
	 * The list used to display the alarms
	 */
	protected FontInheritList alarmList;
	/**
	 * The button that closes the list view
	 */
	protected FontInheritButton cancelButton;
	/**
	 * The button that allows the selected alarm to be avtivated/deactivated
	 */
	protected FontInheritButton activateButton;
	/**
	 * The button that allows the alarm to be changed
	 */
	protected FontInheritButton changeButton;
	
	/**
	 * Maps the list index to the respectve alarm
	 */
	protected HashMap<Integer, IAlarm> alarmMap;
	/**
	 * The currently selected alarm
	 */
	protected IAlarm selectedAlarm;
	
	
	public AlarmList(Composite parent, int style) {
		super(parent, style);
		
		alarmMap = new HashMap<Integer, IAlarm>();
		
		initialize();
	}
	
	/**
	 * Initializes the components of this composite
	 */
	protected void initialize() {
		GridLayout mainGrid = new GridLayout(1, false);
		mainGrid.marginHeight = 0;
		mainGrid.marginWidth = 0;
		super.setLayout(mainGrid);
		
		alarmList = new FontInheritList(this, SWT.SINGLE | SWT.RIGHT);
		alarmList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		alarmList.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				selectedAlarm = alarmMap.get(alarmList.getSelectionIndex());
				
				activateButton.setEnabled(selectedAlarm != null);
				changeButton.setEnabled(selectedAlarm != null);
				
				if (selectedAlarm != null) {
					activateButton.setSelection(!selectedAlarm.isActive());
					activateButton.notifyListeners(SWT.Selection, null);
				}
			}
		});
		
		FontInheritComposite buttonComp = new FontInheritComposite(this,
				SWT.NONE);
		buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		GridLayout buttonGrid = new GridLayout(3, true);
		buttonGrid.marginHeight = 0;
		buttonGrid.marginWidth = 0;
		buttonGrid.horizontalSpacing = 30;
		buttonComp.setLayout(buttonGrid);
		
		initializeButtons(buttonComp);
		
		updateList();
		
		AlarmManager.getManager().addAlarmManagerListener(this);
	}
	
	/**
	 * Updates the list content with the respective alarms
	 */
	protected void updateList() {
		// store current selection
		IAlarm selectedAlarm = (alarmList.getSelectionIndex() >= 0)
				? alarmMap.get(alarmList.getSelectionIndex()) : null;
		
		int selectionIndex = -1;
		
		SimpleDateFormat formatter = new SimpleDateFormat(
				"EEE HH:mm   (dd. MMMM)");
		
		alarmList.removeAll();
		
		// list alarms
		for (IAlarm currentAlarm : AlarmManager.getManager().getAlarms()) {
			alarmList.add(((currentAlarm.getGroup() == null) ? ""
					: currentAlarm.getGroup().getName() + " - ")
					+ formatter.format(currentAlarm.getAlarmDate())
					+ ((currentAlarm.isActive()) ? "" : " -- Inactive"));
			
			alarmMap.put(alarmList.getItemCount() - 1, currentAlarm);
			
			if (currentAlarm.equals(selectedAlarm)) {
				selectionIndex = alarmList.getItemCount() - 1;
			}
		}
		
		if (selectionIndex >= 0) {
			alarmList.select(selectionIndex);
		} else {
			if (alarmList.getItemCount() > 0) {
				alarmList.select(0);
			}
		}
		
		alarmList.notifyListeners(SWT.Selection, null);
	}
	
	/**
	 * Creates and initializes the three bottom buttons
	 * 
	 * @param parent
	 *            The parent composite for the buttons
	 */
	protected void initializeButtons(Composite parent) {
		cancelButton = new FontInheritButton(parent, SWT.PUSH | SWT.CENTER);
		cancelButton.setText("Cancel");
		cancelButton
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		activateButton = new FontInheritButton(parent, SWT.TOGGLE | SWT.CENTER);
		activateButton.setText("Deactivate");
		activateButton
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		changeButton = new FontInheritButton(parent, SWT.PUSH | SWT.CENTER);
		changeButton.setText("Change");
		changeButton
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		
		cancelButton.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				AlarmList.this.dispose();
			}
		});
		
		activateButton.addListener(SWT.Selection, new Listener() {
			private AtomicBoolean selection;
			
			@Override
			public void handleEvent(Event event) {
				activateButton.setText((activateButton.getSelection())
						? "Activate" : "Deactivate");
				
				if (selection == null) {
					selection = new AtomicBoolean(
							!activateButton.getSelection());
				}
				
				if (selection.get() != activateButton.getSelection()) {
					selection.set(activateButton.getSelection());
					
					selectedAlarm.setActive(!activateButton.getSelection());
					
					updateList();
				}
			}
		});
		
		changeButton.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				Control searchHelper = getParent();
				
				while (!(searchHelper instanceof IntegratedClockUI)) {
					searchHelper = searchHelper.getParent();
				}
				
				IntegratedClockUI ui = (IntegratedClockUI) searchHelper;
				
				AlarmChanger changer = new AlarmChanger(ui, SWT.NONE,
						selectedAlarm);
				
				changer.addDisposeListener(new DisposeListener() {
					
					@Override
					public void widgetDisposed(DisposeEvent e) {
						if (!AlarmList.this.isDisposed()) {
							ui.setPanel(AlarmList.this, SWT.RIGHT);
							
							if (!alarmList.isDisposed()) {
								updateList();
							}
						}
					}
				});
				
				ui.setPanel(changer, SWT.RIGHT, false);
				ui.hide(SWT.LEFT);
			}
		});
	}
	
	@Override
	public void alarmInvoked(IAlarm alarm) {
		// ignore
	}
	
	@Override
	public void alarmsChanged() {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				updateList();
			}
		});
	}
	
	@Override
	public void dispose() {
		AlarmManager.getManager().removeAlarmManagerListener(this);
		
		super.dispose();
	}
	
}
