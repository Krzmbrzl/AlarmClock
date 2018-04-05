package alarm;

/**
 * An interface describing a minimal listener class
 * 
 * @author Raven
 *
 */
public interface IListener {
	
	/**
	 * Gets called when this listener should be notified
	 * 
	 * @param ctx
	 *            The context of this notification. May be <code>null</code>
	 */
	public void notify(Object ctx);
}
