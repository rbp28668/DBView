/**
 * 
 */
package uk.co.alvagem.dbview;

import java.util.Map;

import javax.swing.JInternalFrame;

import uk.co.alvagem.dbview.model.Database;

/**
 * Interface to be implemented by DBView plugins.  These provide functionality that
 * it doesn't make sense to include in the core system - but which is useful nonetheless.
 * @author bruce.porteous
 *
 */
public interface DBViewPlugin {

	/**
	 * Gets the name of the plugin which can be used in a menu.
	 * @return the plugin's name.
	 */
	public String getName();
	
	
	/**
	 * Gets a map of the command names and their descriptions.
	 * @return a map of command names (keys) and descriptions (values).
	 */
	public Map<String,String> getCommands();
	
	
	/**
	 * Runs a command on the given database.
	 * @param commandName is one of the command names given by the getCommands() method.
	 * @param db is the database to use.
	 * @param frame is the Swing frame to use as parent for things like popups.
	 */
	public void runCommand(String commandName, Database db, JInternalFrame frame) throws Exception;
}
