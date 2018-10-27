/**
 * 
 */
package uk.co.alvagem.dbview.model;

import uk.co.alvagem.dbview.DatabaseChangeEvent;


/**
 * Event listener interface to signal changes in databases.
 * @author bruce.porteous
 *
 */
public interface DatabaseEventListener {
    
    
    /**
     * Signals that all the databases have been updated (possibly deleted).
     * @param e
     */
    public void updated(DatabaseChangeEvent e);
    
    /**
     * Signals that a new database has been added.
     * @param e
     */
    public void databaseAdded(DatabaseChangeEvent e);
    
    /**
     * Signals that the database has been changed.
     * @param e
     */
    public void databaseChanged(DatabaseChangeEvent e);
    
    /**
     * Signals that a database has been deleted.S
     * @param e
     */
    public void databaseDeleted(DatabaseChangeEvent e);
}
