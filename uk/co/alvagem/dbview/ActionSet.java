/*
 * ActionSet.java
 *
 * Created on 23 January 2002, 09:07
 */

package uk.co.alvagem.dbview;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;

/**
 * Container for all the actions accessible from the GUI.  This provides 
 * a set of named actions which allows access by logical name from a menu
 * or button.  The use of named actions allows actions to be bound to 
 * menu items dynamically.
 * @author  rbp28668
 */
public class ActionSet {

    /** Actions keyed by name */
    private HashMap<String,Action> m_actions = new HashMap<String,Action>();

    /** Creates new empty ActionSet */
    public ActionSet() {
    }

    /**
     * Looks up an action by name.
	 * @param localName is the name to use to look-up the action.
	 * @return the given action.
	 * @throws NullPointerException - if the given name is null.
	 * @throws IllegalStateException - if the given name doesn't correspond to a valid action.
	 */
	public Action getAction(String localName) {
		if(localName == null){
			throw new NullPointerException("Null name to getAction");
		}
		Action action = (Action)m_actions.get(localName);
		if(action == null){
			throw new IllegalStateException("Action " + localName + " not found");
		}
        return action;
    }

    /**
     * Adds a named action to the map.
	 * @param key is the String that identifies this action.
	 * @param action is the Action to store against the given key.
	 * @throws NullPointerException - if key is null.
	 * @throws NullPointerException - if action is null.
	 * @throws IllegalStateException - if there is already an action with the given key.
	 */
	public void addAction(String key, Action action) {
		if(key == null){
			throw new NullPointerException("Null key to addAction");
		}
		
		if(action == null){
			throw new NullPointerException("Null action to addAction");
		}
		
		if(m_actions.containsKey(key)){
			throw new IllegalStateException("Actions already contains action " + key);
		}
         m_actions.put(key,action);
    }

	/**
	 * Bulk addition of actions.
	 * @param actions is a map of name/Action pairs to be added.
	 */
	protected void addAll(Map<String,Action> actions){
	    m_actions.putAll(actions);
	}

}
