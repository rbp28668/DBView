/**
 * 
 */
package uk.co.alvagem.dbview.model;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import uk.co.alvagem.dbview.DatabaseChangeEvent;
import uk.co.alvagem.dbview.util.XMLWriter;

/**
 * Collection of database connections.
 * 
 * @author bruce.porteous
 * 
 */
public class Databases {

    private List<Database> databases = new LinkedList<Database>();
    private List<DatabaseEventListener> listeners = new LinkedList<DatabaseEventListener>();

    /**
     * 
     */
    public Databases() {
        super();
    }

    public void addDatabase(Database db) {
        if (db == null) {
            throw new NullPointerException("Can't add null database");
        }
        databases.add(db);
        fireDatabaseAdded(db);
    }

    public Collection<Database> getDatabases() {
        return Collections.unmodifiableCollection(databases);
    }

    public void removeDatabase(Database db) {
        databases.remove(db);
        fireDatabaseRemoved(db);
    }

    public void writeXML(XMLWriter writer) throws IOException {
        writer.startEntity("Databases");
        for (Database database : databases) {
            database.writeXML(writer);
        }
        writer.stopEntity();
    }

    /**
     * 
     */
    public void clear() {
        databases.clear();
        fireDatabasesUpdated();
    }

    public String toString() {
        return "Databases";
    }

    /**
     * Signal that the complete collection has been changed.
     */
    public void fireDatabasesUpdated() {
        DatabaseChangeEvent event = new DatabaseChangeEvent(this);
        for (DatabaseEventListener listener : listeners) {
            listener.updated(event);
        }
    }

    public void fireDatabaseAdded(Database database) {
        DatabaseChangeEvent event = new DatabaseChangeEvent(database);
        for (DatabaseEventListener listener : listeners) {
            listener.databaseAdded(event);
        }
    }

    /**
     * @param database
     */
    public void fireDatabaseEdited(Database database) {
        DatabaseChangeEvent event = new DatabaseChangeEvent(database);
        for (DatabaseEventListener listener : listeners) {
            listener.databaseChanged(event);
        }
    }

    public void fireDatabaseRemoved(Database database) {
        DatabaseChangeEvent event = new DatabaseChangeEvent(database);
        for (DatabaseEventListener listener : listeners) {
            listener.databaseDeleted(event);
        }
    }

    /**
     * @param model
     */
    public void addListener(DatabaseEventListener listener) {
        if (listener == null) {
            throw new NullPointerException("Can't add null listener");
        }
        listeners.add(listener);

    }

    /**
     * @param model
     */
    public void removeListener(DatabaseEventListener listener) {
        listeners.remove(listener);
    }

}
