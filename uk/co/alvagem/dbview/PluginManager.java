/**
 * 
 */
package uk.co.alvagem.dbview;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import uk.co.alvagem.dbview.util.XMLWriter;

/**
 * @author bruce.porteous
 *
 */
public class PluginManager {

	private Map<String,DBViewPlugin> plugins = new LinkedHashMap<String,DBViewPlugin>();

	public void registerPlugin(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
		
		ClassLoader loader = DriverClassloader.getInstance();
		Class<?> pluginClass =  Class.forName(className, true, loader);
		if(!DBViewPlugin.class.isAssignableFrom(pluginClass)){
			throw new IllegalArgumentException(className + " does not implement " + DBViewPlugin.class.getCanonicalName());
		}
		
		DBViewPlugin plugin = (DBViewPlugin) pluginClass.newInstance();
		
		if(plugins.containsKey(plugin.getName())) {
			throw new IllegalStateException("Plugin manager already contains " + plugin.getName());
		}
		
		plugins.put(plugin.getName(), plugin);
		
	}



	private class Command {
		String actionName;
		DBViewPlugin plugin;
		String command;
	}


	/**
	 * @param writer
	 * @throws IOException 
	 */
	public void writeXML(XMLWriter writer) throws IOException {
		writer.startEntity("Plugins");
		for(DBViewPlugin plugin : plugins.values()){
			writer.textEntity("Plugin", plugin.getClass().getCanonicalName());
		}
		writer.stopEntity();
	}


	/**
	 * @return
	 */
	public boolean hasPlugins() {
		return !plugins.isEmpty();
	}


	/**
	 * @return
	 */
	public Collection<DBViewPlugin> getPlugins() {
		return plugins.values();
	}


    /**
     * Clears all existing plugins.
     */
    public void clear() {
        plugins.clear();        
    }
	
	
}
