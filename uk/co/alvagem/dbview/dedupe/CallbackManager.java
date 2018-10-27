/**
 * 
 */
package uk.co.alvagem.dbview.dedupe;

import java.awt.Component;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import uk.co.alvagem.dbview.DriverClassloader;
import uk.co.alvagem.dbview.util.SettingsManager;
import uk.co.alvagem.dbview.util.SettingsManager.Element;

/**
 * Manages a collection of named DeDupeDetectionEventReceiver.
 * @author bruce.porteous
 *
 */
public class CallbackManager {

	private Map<String,DeDupeDetectionEventReceiver> receivers = new HashMap<String,DeDupeDetectionEventReceiver>();
	private Map<String, Boolean> enabled = new HashMap<String,Boolean>();
	
	public void initialise(SettingsManager settings) throws DeDupeException{
		Element root = settings.getElement("/DeDupe");
		if(root == null){
			return;
		}
		
		for(Iterator<?> iter = root.getChildren(); iter.hasNext();){
			Element child = (Element)iter.next();
			if(child.getName().equals("EventReceiver")){
				String name = child.attributeRequired("name");
				String className = child.attributeRequired("class");
				
				// Load the class - first using the default classloader and if that
				// fails
				Class<?> c = null;
				try {
					c = Class.forName(className);
				} catch (ClassNotFoundException e) {
					// NOP - not a problem at this point in time.
				}
				if(c == null){
					try {
						c = DriverClassloader.getInstance().loadClass(className);
					} catch (ClassNotFoundException e) {
						throw new DeDupeException("Unable to find DeDupe Receiver class " + className);
					}
				}
				
				if(!DeDupeDetectionEventReceiver.class.isAssignableFrom(c)){
					throw new DeDupeException(className + " is not a DeDupeDetectionEventReceiver");
				}
				
				
				DeDupeDetectionEventReceiver receiver  = null;
				try {
					receiver = (DeDupeDetectionEventReceiver)c.newInstance();
				} catch (InstantiationException e) {
					throw new DeDupeException("Unable to create " + className, e);
				} catch (IllegalAccessException e) {
					throw new DeDupeException("Not allowed to create " + className, e);
				}
				
				receivers.put(name, receiver);
				enabled.put(name, new Boolean(true));
			}
		}
		
	}
	
	public Map<String,DeDupeDetectionEventReceiver> getReceivers(){
		return Collections.unmodifiableMap(receivers);
	}

	/**
	 * @param name
	 * @param enabled
	 */
	public void setEnabled(String name, boolean enabled) {
		this.enabled.put(name, new Boolean(enabled));
		
	}
	
	/**
	 * @param name
	 * @return
	 */
	public boolean isEnabled(String name){
		Boolean e = enabled.get(name);
		if(e == null){
			throw new IllegalArgumentException(name + " is not a known DeDupe callback");
		}
		return e.booleanValue();
	}

	/**
	 * Edits the named callback.
	 * @param name
	 */
	public void edit(String name, Component parent) throws DeDupeException {
		DeDupeDetectionEventReceiver rx = receivers.get(name);
		if(rx == null){
			throw new IllegalArgumentException(name + " is not a known DeDupe callback");
		}
		
		rx.edit(parent);
	}

	/**
	 * Equivalent of clone except returns a CallbackManager.
	 * @return a copy of this manager.
	 * @throws DeDupeException 
	 */
	public CallbackManager copy() throws  DeDupeException {
		CallbackManager copy = new CallbackManager();
		
		try {
			for(Map.Entry<String,DeDupeDetectionEventReceiver> entry : receivers.entrySet()){
				String name = entry.getKey();
				DeDupeDetectionEventReceiver rx = entry.getValue();
				DeDupeDetectionEventReceiver newrx = rx.getClass().newInstance();
				Properties props = new Properties();
				props.putAll(rx.getProperties());
				newrx.setProperties(props);
				copy.receivers.put(name, newrx);
				copy.enabled.put(name, enabled.get(name));
			}
		} catch (InstantiationException e) {
			throw new DeDupeException("Unable to create copy of de dupe event callback manager",e);
		} catch (IllegalAccessException e) {
			throw new DeDupeException("Unable to create copy of de dupe event callback manager",e);
		}
		

		return copy;
	}
	

}
