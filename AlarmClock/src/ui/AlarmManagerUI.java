package ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import alarm.AlarmGroup;
import alarm.AlarmManager;
import util.Util;

public class AlarmManagerUI extends FontInheritComposite {
	
	/**
	 * A map mapping the list index to the respective alarm group
	 */
	protected Map<Integer, AlarmGroup> groupList;
	/**
	 * The list displaying the alarms
	 */
	protected FontInheritList list;
	/**
	 * The button for opening the edit mode
	 */
	protected FontInheritButton editButton;
	
	
	public AlarmManagerUI(Composite parent, int style) {
		super(parent, style);
		
		groupList = new HashMap<Integer, AlarmGroup>();
		
		setFont(parent.getFont());
		
		initialize();
		updateList();
	}
	
	/**
	 * Initializes the components for this class
	 */
	protected void initialize() {
		GridLayout mainGrid = new GridLayout(1, false);
		mainGrid.marginHeight = 0;
		mainGrid.marginWidth = 0;
		super.setLayout(mainGrid);
		
		FontInheritLabel title = new FontInheritLabel(this, SWT.CENTER);
		title.setText("Manage alarms");
		title.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		title.setFont(getFont());
		Util.magnifyFont(title, 1.5);
		
		list = new FontInheritList(this, SWT.NONE);
		
		list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		list.setFont(getFont());
		
		
		// create composite for edit and cancel button
		FontInheritComposite buttonComp = new FontInheritComposite(this, SWT.NONE);
		buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		GridLayout bottomGrid = new GridLayout(3, false);
		bottomGrid.marginHeight = 0;
		bottomGrid.marginWidth = 0;
		buttonComp.setLayout(bottomGrid);
		
		buttonComp.setFont(getFont());
		
		// create the buttons themselves + spacer in bewteen
		FontInheritButton cancelButton = new FontInheritButton(buttonComp, SWT.PUSH);
		cancelButton.setText("Cancel");
		
		Point buttonSize = cancelButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		GridData buttonData = new GridData(SWT.CENTER, SWT.FILL, false, true);
		buttonData.heightHint = (int) (buttonSize.y *1.05);
		buttonData.widthHint = (int) (buttonSize.x *1.2);
		
		cancelButton.setLayoutData(buttonData);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AlarmManagerUI.this.dispose();
			}
		});
		
		Canvas spacer = new Canvas(buttonComp, SWT.NONE);
		spacer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		editButton = new FontInheritButton(buttonComp, SWT.PUSH);
		editButton.setText("EDIT");
		editButton.setLayoutData(buttonData);
		editButton.setFont(getFont());
		
		editButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				AlarmGroup selectedGroup = groupList
						.get(list.getSelectionIndex());
				
				Control searchHelper = getParent();
				
				while (!(searchHelper instanceof IntegratedClockUI)) {
					searchHelper = searchHelper.getParent();
				}
				
				IntegratedClockUI ui = (IntegratedClockUI) searchHelper;
				
				AlarmEditer editer = new AlarmEditer(ui, SWT.NONE,
						selectedGroup, AlarmManagerUI.this);
				
				editer.addDisposeListener(new DisposeListener() {
					
					@Override
					public void widgetDisposed(DisposeEvent e) {
						if (!AlarmManagerUI.this.isDisposed()) {
							ui.setPanel(AlarmManagerUI.this, SWT.RIGHT);
						}
					}
				});
				
				ui.setPanel(editer, SWT.RIGHT, false);
				ui.hide(SWT.LEFT);
			}
		});
	}
	
	/**
	 * <b>Usage is prohibited</b> for this class!
	 */
	@Override
	public void setLayout(Layout layout) {
		throw new IllegalAccessError("No support for custom layouts!");
	}
	
	/**
	 * Updates the list with the alarms from the alarm manager
	 */
	public void updateList() {
		checkWidget();
		
		list.removeAll();
		for (AlarmGroup currentGroup : AlarmManager.getManager()
				.getAllAlarmsAsGroups()) {
			list.add(currentGroup.getName());
			groupList.put(list.getItemCount() - 1, currentGroup);
		}
		
		editButton.setEnabled(list.getItemCount() > 0);
	}
}
