/**
 * 
 */
package uk.co.alvagem.dbview;

import java.util.EventObject;

/**
 * Used to signal changes in the databases.
 * @author bruce.porteous
 *
 */
public class DatabaseChangeEvent extends EventObject {
	private static final long serialVersionUID = 1L;


	/**
	 * @param arg0
	 */
	public DatabaseChangeEvent(Object arg0) {
		super(arg0);
	}

}
