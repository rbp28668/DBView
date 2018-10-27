/**
 * 
 */
package uk.co.alvagem.dbview;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import uk.co.alvagem.dbview.dedupe.CallbackManager;
import uk.co.alvagem.dbview.model.DatabaseFactory;
import uk.co.alvagem.dbview.model.Databases;
import uk.co.alvagem.dbview.model.Notebook;
import uk.co.alvagem.dbview.util.InputException;
import uk.co.alvagem.dbview.util.OutputException;
import uk.co.alvagem.dbview.util.SettingsManager;
import uk.co.alvagem.dbview.util.XMLLoader;
import uk.co.alvagem.dbview.util.XMLWriter;
import uk.co.alvagem.dbview.util.XMLWriterSAX;


/**
 * @author bruce.porteous
 *
 */
public class DBView {

	//private WindowCoordinator windowCoordinator;
	private SettingsManager config;
	private SettingsManager settings;
	private CommandFrame commandFrame;
	
	
	private String configPath = "config.xml";
	private String settingsPath = "settings.xml";

	private String currentPath = null;	// path of currently loaded file.
    private final static String NAMESPACE = "http://alvagem.co.uk/dbview";

    private List<AppEventListener> listeners = new LinkedList<AppEventListener>();
	
	private final Databases databases = new Databases();
	private final Notebook notebook = new Notebook();
	private PluginManager plugins = new PluginManager();
	
	/** CallbackManager to act as a template for all the search indices */
	private CallbackManager deDupeCallbacks = new CallbackManager();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DBView dbview = new DBView();
		dbview.run(args);
	}

	/**
	 * 
	 */
	public DBView() {
		super();
	}

	public void dispose(){
		settings.save(settingsPath);
	}
	
	/**
	 * @return Returns the commandFrame.
	 */
	public CommandFrame getCommandFrame() {
		return commandFrame;
	}

	/**
	 * @return Returns the config.
	 */
	public SettingsManager getConfig() {
		return config;
	}

	/**
	 * @return Returns the configPath.
	 */
	public String getConfigPath() {
		return configPath;
	}

	/**
	 * @return Returns the settings.
	 */
	public SettingsManager getSettings() {
		return settings;
	}

	/**
	 * @return Returns the settingsPath.
	 */
	public String getSettingsPath() {
		return settingsPath;
	}

	
	/**
	 * @return the plugins
	 */
	public PluginManager getPlugins() {
		return plugins;
	}

	private void run(String[] args){
		try {


			// Setup basic configuration files
			// Configuration file should be in the jar file.  If it's missing
			// then the app cannot set up its menus so die gracefully.
			config = new SettingsManager(); // configuration
			InputStream stream = getClass().getResourceAsStream(configPath);
			if(stream == null)
				throw new IOException("Missing Config file: " + configPath);
			config.load(stream);
			
			
			// Settings file however is in user-land.  The first time through
			// there may not be a settings file.
			settings = new SettingsManager(); // user settings
			stream = null;
			String dir = System.getProperty("user.home");
			if(dir != null){
				File file = new File(dir,".dbviewrc.xml");
				settingsPath = file.getCanonicalPath();
				if(file.exists()){
				    stream = new FileInputStream(file);
				}
			}
			
			if(stream != null) {
				settings.load(stream);
			} else {
				settings.setEmptyRoot("DBViewSettings");
			}



			// Init the GUI
			//windowCoordinator = new EAToolWindowCoordinator();
			CommandActionSet actions = new CommandActionSet();
			commandFrame = new CommandFrame("Database View", actions, config);
			actions.setApp(this);
			
			DatabaseExplorerFrame explorer = new DatabaseExplorerFrame(this,"Databases");
			commandFrame.getDesktop().add(explorer);
			commandFrame.setVisible(true);

			// Load up any de-dupe receivers from the configuration
			deDupeCallbacks.initialise(config);
			
			// Load any files specified on the command line.
			int nArgs = args.length;
			if (nArgs > 0) {
				for (int i = 0; i < nArgs; ++i) {
					//System.out.println("Parsing " + args[i]);
					loadXML(args[i]);
				}
			}
			
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
		}
		
	}

	/**
	 * @param string
	 */
	public void loadXML(String path) {
        XMLLoader loader = new XMLLoader();
        loader.setNameSpaces(true);
        
        // Set up handlers for the different objects.
        
        DatabaseFactory databaseHandler = new DatabaseFactory(databases,deDupeCallbacks);
		loader.registerContent(NAMESPACE,"Database",databaseHandler);

		NotebookFactory notebookHandler = new NotebookFactory(notebook);
		loader.registerContent(NAMESPACE,"Notebook",notebookHandler);
		
		PluginFactory pluginHandler = new PluginFactory(plugins);
		loader.registerContent(NAMESPACE,"Plugin",pluginHandler);
		
        try {
            loader.parse(path);
        }
        catch(SAXParseException e) {
            throw new InputException("SAX Parse Exception. Line " + e.toString() +
                " at line " + e.getLineNumber() + ", column " + e.getColumnNumber(), e);
        }
        catch(SAXException e) {
            throw new InputException("Problem parsing xml file: " + e.getMessage(),e);
        }
        
        
        //System.out.println("**Loaded XML");
		
	}
	
	public void saveXML(String path){
		try {
			aboutToSave();
	        XMLWriter writer = new XMLWriterSAX(new FileOutputStream(path));
	        try{
	            writer.startXML();
	            writer.setNamespace("dbv",NAMESPACE);
	            writer.startEntity("DBView");
	 
	    		databases.writeXML(writer);
	    		notebook.writeXML(writer);
	    		plugins.writeXML(writer);
	            writer.stopEntity();
	            writer.stopXML();
	        } finally {
	            writer.close();
	        }
	    }
	    catch(Exception e) {
	        throw new OutputException("problem saving xml file: " + e.getMessage(),e);
	    }
	}

	/**
	 * Adds a listener to receive application events.
	 * @param listener is the listener to add.
	 */
	public void addListener(AppEventListener listener){
		listeners.add(listener);
	}
	
	/**
	 * Removes a listener which will no longer receive application events.
	 * @param listener is the listener to remove.
	 * @return true if the listener was registered.
	 */
	public boolean removeListener(AppEventListener listener){
		return listeners.remove(listener);
	}
	
	/**
	 * Signals to any listener that the application is about to save its state so
	 * now would be a good time to update that state from any open windows etc.
	 */
	public void aboutToSave() {
		for(Iterator<AppEventListener> iter = listeners.iterator(); iter.hasNext();){
			AppEventListener listener = iter.next();
			listener.aboutToSave();
		}
	}
	
	/**
	 * Asks the listeners whether there are any unsaved changes.  Note this is not guaranteed
	 * to call all listeners - another listener may already have flagged unsaved changes.
	 * @return true if there are unsaved changes.
	 */
	public boolean hasUnsaved(){
		for(Iterator<AppEventListener> iter = listeners.iterator(); iter.hasNext();){
			AppEventListener listener = iter.next();
			if(listener.hasUnsaved()){
				return true;
			}
		}
		return false;
	}

//	/**
//	 * @return Returns the tabs.
//	 */
//	public JTabbedPane getTabs() {
//		return tabs;
//	}

	/**
	 * 
	 */
	public void reset() {
		databases.clear();
		notebook.clear();
		plugins.clear();
		
        JDesktopPane desktop = getCommandFrame().getDesktop();
        JInternalFrame[] frames = desktop.getAllFrames();
        for(int i=0; i<frames.length; ++i){
            if(!(frames[i] instanceof DatabaseExplorerFrame)){
                desktop.remove(frames[i]);
                frames[i].dispose();
            }
        }
        desktop.repaint();

		
	}

	/**
	 * @return
	 */
	public Databases getDatabases() {
		return databases;
	}

	/**
	 * @return
	 */
	public Notebook getNotebook() {
		return notebook;
	}
	
	/**
	 * @return Returns the currentPath.
	 */
	public String getCurrentPath() {
		return currentPath;
	}

	/**
	 * @param currentPath The currentPath to set.
	 */
	public void setCurrentPath(String currentPath) {
		this.currentPath = currentPath;
	}

	/**
	 * @return
	 */
	public CallbackManager getDeDupeCallbackManager() {
		return deDupeCallbacks;
	}
}
