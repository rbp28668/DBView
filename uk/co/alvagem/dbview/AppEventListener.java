/**
 * 
 */
package uk.co.alvagem.dbview;


/**
 * An application event listener that allows components to respond to key
 * events in the application lifecycle.
 * @author bruce.porteous
 *
 */
public interface AppEventListener {

	/**
	 * Signals to any listener that the application is about to save its state so
	 * now would be a good time to update that state from any open windows etc.
	 */
	public void aboutToSave();
	
	/**
	 * Asks the listener whether there are any unsaved changes.  Note this is not guaranteed
	 * to be called - another listener may already have flagged unsaved changes.
	 * @return true if there are unsaved changes.
	 */
	public boolean hasUnsaved();
	
}
