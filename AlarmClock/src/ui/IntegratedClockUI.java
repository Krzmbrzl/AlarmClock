package ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;

/**
 * A class providing support for arranging buttons or other elements around or
 * next to an analog clock
 * 
 * @author Raven
 *
 */
public class IntegratedClockUI extends FontInheritComposite implements ControlListener {
	
	/**
	 * The composite holding the contents for the left side
	 */
	protected FontInheritComposite left;
	/**
	 * The composite holding the contents for the right side
	 */
	protected FontInheritComposite right;
	/**
	 * The StackLayout for {@link #left}
	 */
	protected StackLayout leftStack;
	/**
	 * The StackLayout for {@link #right}
	 */
	protected StackLayout rightStack;
	/**
	 * The composite holding the buttons for the left side
	 */
	protected FontInheritComposite leftButtons;
	/**
	 * The conposite holding the buttons for the right side
	 */
	protected FontInheritComposite rightButtons;
	/**
	 * The clock component of this class
	 */
	protected AnalogClock clock;
	/**
	 * The display in which this widget exists
	 */
	protected Display display;
	/**
	 * The position(s) that should be hidden
	 */
	protected int hidden;
	/**
	 * A variable storing the previous hidden status before the auto-hide
	 * function has been called
	 */
	protected int hiddenBeforeAutoHide;
	
	/**
	 * Creates a new instance of this widget.
	 * 
	 * @param parent
	 *            The parent of this widget
	 * @param style
	 *            The style of this widget. With that it can be determined on
	 *            which side(s) a panel should be added on which buttons can be
	 *            created via {@link #createButton(String, int, Listener)}. Can
	 *            either be SWT.LEFT or SWT.RIGHT or both.
	 */
	public IntegratedClockUI(Composite parent, int style) {
		super(parent, style);
		
		display = Display.getCurrent();
		hidden = SWT.NONE;
		hiddenBeforeAutoHide = SWT.NONE;
		
		setFont(parent.getFont());
		
		addControlListener(this);
		
		initialize();
	}
	
	/**
	 * Initializes the components for this class
	 */
	protected void initialize() {
		boolean bothSides = (getStyle() & SWT.LEFT) == SWT.LEFT
				&& (getStyle() & SWT.RIGHT) == SWT.RIGHT;
		
		GridLayout layout = new GridLayout();
		layout.numColumns = (bothSides) ? 3 : 2;
		layout.horizontalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		layout.marginBottom = 10;
		layout.marginTop = 10;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		super.setLayout(layout);
		
		if (bothSides || (getStyle() & SWT.LEFT) == SWT.LEFT) {
			// only create if needed
			left = new FontInheritComposite(this, SWT.NONE);
			GridData leftData = new GridData();
			// leftData.grabExcessVerticalSpace = true;
			leftData.horizontalAlignment = SWT.FILL;
			leftData.verticalAlignment = SWT.FILL;
			left.setLayoutData(leftData);
			leftStack = new StackLayout();
			left.setLayout(leftStack);
			
			left.setFont(getFont());
			
			leftButtons = new FontInheritComposite(left, SWT.NONE);
			GridLayout grid = new GridLayout(1, false);
			grid.marginWidth = 0;
			leftButtons.setLayout(grid);
			
			leftButtons.setFont(getFont());
			
			leftStack.topControl = leftButtons;
		}
		
		clock = new AnalogClock(this, SWT.NO_BACKGROUND, false);
		GridData clockData = new GridData();
		clockData.horizontalAlignment = SWT.FILL;
		clockData.verticalAlignment = SWT.FILL;
		clock.setLayoutData(clockData);
		
		clock.setFont(getFont());
		clock.setLineThicknessCoefficient(1.5);
		clock.setOrientationLineLengthCoefficient(1.5);
		
		clock.addListener(SWT.MouseDown, new Listener() {
			
			@Override
			public void handleEvent(Event arg0) {
				// Toggle content
				if (hidden == SWT.NONE) {
					hiddenBeforeAutoHide = hidden;
					hide(SWT.LEFT | SWT.RIGHT);
				} else {
					if ((hidden & SWT.LEFT) == SWT.LEFT
							&& (hidden & SWT.RIGHT) == SWT.RIGHT) {
						hide(hiddenBeforeAutoHide);
					}
				}
			}
		});
		
		if (bothSides || (getStyle() & SWT.RIGHT) == SWT.RIGHT) {
			// onyl create if needed
			right = new FontInheritComposite(this, SWT.NONE);
			GridData rightData = new GridData();
			// rightData.grabExcessVerticalSpace = true;
			rightData.horizontalAlignment = SWT.FILL;
			rightData.verticalAlignment = SWT.FILL;
			right.setLayoutData(rightData);
			rightStack = new StackLayout();
			right.setLayout(rightStack);
			
			right.setFont(getFont());
			
			rightButtons = new FontInheritComposite(right, SWT.NONE);
			GridLayout grid = new GridLayout(1, false);
			grid.marginWidth = 0;
			rightButtons.setLayout(grid);
			
			rightButtons.setFont(getFont());
			
			rightStack.topControl = rightButtons;
		}
	}
	
	/**
	 * <b>Usage is prohibited</b> for this class!
	 */
	@Override
	public void setLayout(Layout layout) {
		throw new IllegalAccessError("No support for custom layouts!");
	}
	
	/**
	 * Creates a button on this widget. It can either be placed to the left or
	 * to the right of the clock widget. <br>
	 * The text size of the created button will be doubled in comparison to the
	 * default font size
	 * 
	 * @param text
	 *            The text of the button
	 * @param position
	 *            The button's position. Has to contain the placement
	 *            information where to place it relative to the clock widget
	 *            (Either SWT.LEFT or SWT.RIGHT)
	 * @param buttonListener
	 *            The listener that will be notified when the button is clicked
	 * @return The created button
	 */
	public Button createButton(String text, int position,
			Listener buttonListener) {
		checkWidget();
		checkPosition(position, true);
		
		FontInheritButton button = new FontInheritButton(((position & SWT.LEFT) == SWT.LEFT)
				? leftButtons : rightButtons, SWT.CENTER | SWT.PUSH);
		button.setText(text);
		button.setFont(((position & SWT.LEFT) == SWT.LEFT)
				? leftButtons.getFont() : rightButtons.getFont());
		button.addListener(SWT.Selection, buttonListener);
		button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		
		return button;
	}
	
	@Override
	public void controlResized(ControlEvent e) {
		updateWidget();
	}
	
	@Override
	public void controlMoved(ControlEvent e) {
	}
	
	/**
	 * Updates the size of this widget's components and re-layouts them
	 */
	public void updateWidget() {
		checkWidget();
		
		if (clock.isDisposed() || (left != null && left.isDisposed())
				|| (right != null && right.isDisposed())) {
			return;
		}
		
		GridLayout layout = (GridLayout) getLayout();
		int horizontalLayoutSpace = layout.horizontalSpacing * layout.numColumns
				+ layout.marginLeft + layout.marginRight;
		int verticalLayoutSpace = layout.marginTop + layout.marginBottom;
		
		boolean bothHidden = (hidden & SWT.LEFT) == SWT.LEFT
				&& (hidden & SWT.RIGHT) == SWT.RIGHT;
		
		// Make sure the respective components resize as well
		GridData componentData = (GridData) clock.getLayoutData();
		int size = (int) (Math.min(
				getClientArea().width - horizontalLayoutSpace,
				getClientArea().height - verticalLayoutSpace)
				* (bothHidden ? 1 : 0.85));
		componentData.heightHint = size;
		componentData.widthHint = size;
		
		if (!bothHidden) {
			if ((getStyle() & SWT.LEFT) == SWT.LEFT
					&& (getStyle() & SWT.RIGHT) == SWT.RIGHT
					&& hidden == SWT.NONE) {
				// both panels are visible
				componentData = (GridData) left.getLayoutData();
				componentData.heightHint = getClientArea().height
						- verticalLayoutSpace;
				componentData.widthHint = (getClientArea().width - size
						- horizontalLayoutSpace) / 2;
				
				componentData = (GridData) right.getLayoutData();
				componentData.heightHint = getClientArea().height
						- verticalLayoutSpace;
				componentData.widthHint = (getClientArea().width - size
						- horizontalLayoutSpace) / 2;
			} else {
				if ((getStyle() & SWT.LEFT) == SWT.LEFT
						&& (hidden & SWT.LEFT) != SWT.LEFT) {
					// only left panel should be visible
					componentData = (GridData) left.getLayoutData();
					componentData.heightHint = getClientArea().height
							- verticalLayoutSpace;
					componentData.widthHint = getClientArea().width - size
							- horizontalLayoutSpace;
				} else {
					if ((hidden & SWT.RIGHT) != SWT.RIGHT) {
						// only right panel should be visible
						componentData = (GridData) right.getLayoutData();
						componentData.heightHint = getClientArea().height
								- verticalLayoutSpace;
						componentData.widthHint = getClientArea().width - size
								- horizontalLayoutSpace;
					}
				}
			}
		}
		
		if (bothHidden) {
			// set the size of the left component in order to center the clock
			componentData = (GridData) left.getLayoutData();
			componentData.heightHint = getClientArea().height
					- verticalLayoutSpace;
			componentData.widthHint = (getClientArea().width - size
					- horizontalLayoutSpace) / 2;
			
			// hide panels
			left.setVisible(false);
			right.setVisible(false);
		} else {
			// hide respective panels
			if ((hidden & SWT.LEFT) == SWT.LEFT && left != null) {
				componentData = (GridData) left.getLayoutData();
				componentData.heightHint = 0;
				componentData.widthHint = 0;
				
				left.setVisible(false);
			} else {
				if (left != null) {
					left.setVisible(true);
				}
			}
			
			if ((hidden & SWT.RIGHT) == SWT.RIGHT && right != null) {
				componentData = (GridData) right.getLayoutData();
				componentData.heightHint = 0;
				componentData.widthHint = 0;
				
				right.setVisible(false);
			} else {
				if (right != null) {
					right.setVisible(true);
				}
			}
		}
		
		layout(true, true);
	}
	
	/**
	 * Sets the composite for the given side
	 * 
	 * @param comp
	 *            The composite to use for the given side
	 * @param position
	 *            On which side to use the given composite (SWT.LEFT or
	 *            SWT.RIGHT)
	 * @param addListener
	 *            Indicates whether the standard dispose listener should be
	 *            added to the new panel
	 */
	public void setPanel(Composite comp, int position, boolean addListener) {
		checkPosition(position, false);
		checkWidget();
		
		DisposeListener listener;
		
		if ((position & SWT.LEFT) == SWT.LEFT) {
			// switch to left
			comp.setParent(left);
			leftStack.topControl = comp;
			
			listener = new DisposeListener() {
				
				@Override
				public void widgetDisposed(DisposeEvent e) {
					leftStack.topControl = leftButtons;
					updateWidget();
				}
			};
		} else {
			// switch to right
			comp.setParent(right);
			rightStack.topControl = comp;
			
			listener = new DisposeListener() {
				
				@Override
				public void widgetDisposed(DisposeEvent e) {
					rightStack.topControl = rightButtons;
					updateWidget();
				}
			};
		}
		
		if (addListener) {
			comp.addDisposeListener(listener);
		}
		
		updateWidget();
	}
	
	/**
	 * Sets the composite for the given side. If the given panel is disposed the
	 * default panel will be used instead
	 * 
	 * @param comp
	 *            The composite to use for the given side
	 * @param position
	 *            On which side to use the given composite (SWT.LEFT or
	 *            SWT.RIGHT)
	 */
	public void setPanel(Composite comp, int position) {
		setPanel(comp, position, true);
	}
	
	/**
	 * Hides the panel(s) with the given position
	 * 
	 * @param position
	 *            The position of the panel to hide. Can be SWT.LEFT, SWT.RIGHT,
	 *            both of them or SWT.NONE to unhide everything
	 */
	public void hide(int position) {
		if (position != SWT.NONE) {
			checkPosition(position, true);
		}
		
		hidden = position;
		
		updateWidget();
	}
	
	/**
	 * Gets the available panels for the given position
	 * 
	 * @param position
	 *            The position the panels should be retrieved for. Can be
	 *            SWT.LEFT or SWT.RIGHT
	 * @return An array of controls that are set panels for the given position
	 */
	public Control[] getPanel(int position) {
		checkPosition(position, false);
		
		if ((position & SWT.LEFT) == SWT.LEFT) {
			return left.getChildren();
		} else {
			return right.getChildren();
		}
	}
	
	/**
	 * Checks the given position for it's validness. If it is considered invalid
	 * a InvalidArgumentException will be thrown
	 * 
	 * @param pos
	 *            The position of a button to check
	 * @param allowBoth
	 *            Indicates whether it is allowed to specify both left and right
	 *            at the same time
	 */
	protected void checkPosition(int pos, boolean allowBoth) {
		if ((pos & SWT.LEFT) != SWT.LEFT && (pos & SWT.RIGHT) != SWT.RIGHT) {
			throw new IllegalArgumentException(
					"The style of the button has to contain either SWT.LEFT or SWT.RIGHT!");
		}
		
		if (((getStyle() & SWT.LEFT) != SWT.LEFT || left == null)
				&& (pos & SWT.LEFT) == SWT.LEFT) {
			throw new IllegalArgumentException(
					"Specified position SWT.LEFT but the position hasn't been enabled "
							+ "for this widget (thorugh constructor)");
		}
		
		if (((getStyle() & SWT.RIGHT) != SWT.RIGHT || right == null)
				&& (pos & SWT.RIGHT) == SWT.RIGHT) {
			throw new IllegalArgumentException(
					"Specified position SWT.RIGHT but the position hasn't been enabled "
							+ "for this widget (thorugh constructor)");
		}
		
		if (!allowBoth && (pos & SWT.RIGHT) == SWT.RIGHT
				&& (pos & SWT.LEFT) == SWT.LEFT) {
			throw new IllegalArgumentException(
					"Specified two positions but only one was allowed!");
		}
	}
}
