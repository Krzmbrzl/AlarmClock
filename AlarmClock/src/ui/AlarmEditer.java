package ui;

import java.time.DayOfWeek;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

import alarm.AlarmGroup;
import alarm.AlarmManager;
import alarm.ERepetition;

public class AlarmEditer extends AlarmSetter {
	
	/**
	 * The alarm group this widget should edit
	 */
	protected AlarmGroup editGroup;
	/**
	 * The alarm manager ui this editer has been opened from
	 */
	protected AlarmManagerUI managerUI;
	
	
	public AlarmEditer(Composite parent, int style, AlarmGroup group,
			AlarmManagerUI ui) {
		super(parent, style);
		
		editGroup = group;
		managerUI = ui;
		
		loadData();
	}
	
	@Override
	protected void initialize() {
		super.initialize();
		
		title.setText("Edit alarm");
	}
	
	/**
	 * Loads the data of the edited group into the respective fields
	 */
	protected void loadData() {
		nameText.setText(editGroup.getName());
		time.setTime(editGroup.getHourAndMinute());
		
		Iterator<Entry<DayOfWeek, Button>> it = dayButtons.entrySet()
				.iterator();
		
		List<DayOfWeek> activeDays = editGroup.getDays();
		
		while (it.hasNext()) {
			Entry<DayOfWeek, Button> entry = it.next();
			if (activeDays.contains(entry.getKey())) {
				entry.getValue().setSelection(true);
				entry.getValue().notifyListeners(SWT.Selection, new Event());
			} else {
				entry.getValue().setSelection(false);
			}
		}
		
		repeat.setSelection(editGroup.getRepetition() != ERepetition.NONE
				&& editGroup.getRepetition() != null);
	}
	
	@Override
	protected Button addBottomRightButton(Composite parent) {
		FontInheritButton okButton = new FontInheritButton(parent, SWT.PUSH);
		okButton.setText("OK");
		
		okButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				AlarmGroup group = getAsAlarmGroup();
				
				// switch the old alarm with the new one if there are changes
				if (!editGroup.equals(group)) {
					AlarmManager.getManager().removeGroup(editGroup);
					AlarmManager.getManager().addGroup(group);
				}
				
				if (managerUI != null) {
					managerUI.updateList();
				}
				
				AlarmEditer.this.dispose();
			}
		});
		
		return okButton;
	}
	
	@Override
	protected Button addBottomLeftButton(Composite parent) {
		FontInheritButton deleteButton = new FontInheritButton(parent, SWT.PUSH);
		deleteButton.setText("Delete");
		
		deleteButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// remove old group
				AlarmManager.getManager().removeGroup(editGroup);
				
				if (managerUI != null) {
					managerUI.updateList();
				}
				
				AlarmEditer.this.dispose();
			}
		});
		
		return deleteButton;
	}
	
}
