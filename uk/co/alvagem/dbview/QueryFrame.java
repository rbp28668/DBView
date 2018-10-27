/**
 * 
 */
package uk.co.alvagem.dbview;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.table.TableModel;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;

import uk.co.alvagem.dbview.dedupe.DeDupeException;
import uk.co.alvagem.dbview.dedupe.DeDupeProgressCallback;
import uk.co.alvagem.dbview.model.Database;
import uk.co.alvagem.dbview.model.Queries;
import uk.co.alvagem.dbview.model.Query;
import uk.co.alvagem.dbview.model.RecordExclusionTemplate;
import uk.co.alvagem.dbview.model.SearchIndex;
import uk.co.alvagem.dbview.model.SearchIndices;
import uk.co.alvagem.dbview.util.SettingsManager;


/**
 * @author bruce.porteous
 *
 */
public class QueryFrame extends JInternalFrame implements AppEventListener {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final static String SETTINGS_KEY = "/Windows/DatabaseExplorer";
    private final static String MENU_CONFIG = "/QueryFrame/menus";
    private SettingsManager settings;
    private DBView app;
    private JTabbedPane tabs = new JTabbedPane();
    private Database database;
	private JMenuBar menuBar;

	

	/**
	 * Creates a new QueryFrame tied to a given database.
	 * @param app is the application.
	 * @param database is the database the queries apply to.
	 * @throws IOException 
	 */
	public QueryFrame(DBView app, Database database) throws IOException {
		super("Query " + database.getName());
		this.app = app;
		this.database = database;
		
	    setResizable(true);
	    setMaximizable(true);
	    setIconifiable(true);
	    setClosable(true);

		addInternalFrameListener(new WindowListener());

		app.addListener(this);
		
		settings = app.getSettings();
	    GUIBuilder.loadBounds(this, settings, SETTINGS_KEY);
		
        SettingsManager config = app.getConfig();
		SettingsManager.Element cfg = config.getElement(MENU_CONFIG);
		ActionSet actions = new QueryFrameActionSet(app,this);
		setMenuBar(cfg, actions);
	    
		
	    getContentPane().add(tabs);

	    Queries queries = database.getQueries();
	    if(queries.isEmpty()){
	    	addNewQuery();
	    } else {
		    for(Query query : queries.getQueries()){
			    addSQLQuery(query);
		    }
	    }

	    SearchIndices searchIndices = database.getSearchIndices();
	    for(SearchIndex searchIndex : searchIndices.getSearchIndices()){
		    addSearchQuery(searchIndex);
	    }

	    setVisible(true);
	}

    /**
     * Sets the menu bar from the configuration.
     * @param cfg is the configuration of the menus.
     * @param actions is the ActionSet containing the actions for the menu.
     */
    protected void setMenuBar(SettingsManager.Element cfg, ActionSet actions) {
        menuBar = new JMenuBar();
		GUIBuilder.buildMenuBar(menuBar, actions, cfg);
		buildPluginMenu(menuBar);
		setJMenuBar(menuBar);
    }

	/**
	 * Adds a new Query tab.  By default the queries are named Query 1, Query 2 etc. This
	 * checks existing names rather than just using an index as this copes with existing
	 * queries being read-in, merged, renamed etc.  This also adds it to the collection
	 * of queries of the database.
	 * @return the new Query.
	 */
	public Query addNewQuery(){
		
		Set<String> existingNames = new HashSet<String>();
		int count = tabs.getTabCount();
		for(int i=0; i<count; ++i){
			String title = tabs.getTitleAt(i);
			existingNames.add(title);
		}
		
		int idx = 0;
		String name = null;
		do {
			++idx;
			name = "Query " + idx;
		} while(existingNames.contains(name));
		
		Query query = new Query();
		query.setName(name );
		addSQLQuery(query);
		
		database.getQueries().addQuery(query);
		
		return query;
	}

	/**
	 * Adds a query tab from an existing query.
	 * @param query
	 */
	public SQLQueryPane addSQLQuery(Query query){
		SQLQueryPane pane = new SQLQueryPane(query);
		tabs.add(pane);
		tabs.setSelectedComponent(pane);
		return pane;
	}

	/**
	 * Adds a query tab from an existing query.
	 * @param query
	 * @throws IOException 
	 */
	public SearchQueryPane addSearchQuery(SearchIndex searchIndex) throws IOException{
		SearchQueryPane pane = new SearchQueryPane(searchIndex);
		tabs.add(pane);
		tabs.setSelectedComponent(pane);
		return pane;
	}

	/* (non-Javadoc)
	 * @see javax.swing.JInternalFrame#dispose()
	 */
	public void dispose() {
		updateQueries();
		app.removeListener(this);
	    GUIBuilder.saveBounds(this, settings, SETTINGS_KEY);
	    super.dispose();
	}

	private SQLQueryPane getSQLQuery(){
		SQLQueryPane queryPane = (SQLQueryPane)tabs.getSelectedComponent();
		return queryPane;
	}

	private SearchQueryPane getSearchQuery(){
		SearchQueryPane queryPane = (SearchQueryPane)tabs.getSelectedComponent();
		return queryPane;
	}
	
	/**
	 * Runs a query in the selected tab.
	 */
	public void runSelectedQuery() throws SQLException {
		SQLQueryPane queryPane = getSQLQuery();
		Connection con = database.getConnection();
		try {
			queryPane.runQuery(con);
		} catch (SQLException e) {
			con.close(); // closed by QueryCallback in normal operation.
			queryPane.setStatus(e.getMessage());
			throw e;
		}
	}

	/**
	 * Searches the selected tab.  If it's a normal query then it's indexed and a new
	 * search tab is added.  If it's already a search tab then the search is run on
	 * the existing index.
	 * @throws IOException 
	 * @throws ParseException 
	 * @throws DeDupeException 
	 */
	public void searchSelectedTab() throws IOException, ParseException, DeDupeException  {
		QueryPane selectedPane = (QueryPane)tabs.getSelectedComponent();
		
		boolean isSearch = selectedPane instanceof SearchQueryPane;
		
		String queryText;
		if(isSearch){
			queryText = selectedPane.getQueryText();
		} else {
			queryText = JOptionPane.showInputDialog(this,"Enter Search Text");
		}
		
		if(queryText != null && queryText.trim().length() > 0){

			selectedPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			try {
				if(isSearch){
					SearchQueryPane searchPane = getSearchQuery();
					searchPane.runSearch(queryText);
				} else { 
					// Not a search pane - create a new one
					SQLQueryPane sqlPane = getSQLQuery();
					
					SearchIndex searchIndex = new SearchIndex();
					searchIndex.setName("Search on " + selectedPane.getName() );
					searchIndex.setText(queryText);
					searchIndex.setIndexDate(sqlPane.getQueryDate());
					searchIndex.setSourceSQL(sqlPane.getActualSQL());
					searchIndex.setManager(app.getDeDupeCallbackManager().copy());
					
					SearchQueryPane pane = addSearchQuery(searchIndex);
					pane.indexQueryResults(selectedPane.getModel());
					pane.runSearch(queryText);
				}

			} finally {
				selectedPane.setCursor(Cursor.getDefaultCursor());
			}
		}
	}

	/**
	 * Uses the SQL query in the selected pane to build a Lucene index of the
	 * query results.  
	 * @throws IOException 
	 * @throws DeDupeException 
	 */
	public void indexSelectedQuery(String indexPath) throws SQLException, IOException, DeDupeException{
		
		SQLQueryPane selectedPane = getSQLQuery();
		String queryText = selectedPane.getQueryText();
		
		// Not a search pane - create a new one
		SearchIndex newQuery = new SearchIndex();
		newQuery.setName("Search Index on " + selectedPane.getName() );
		newQuery.setText(queryText);
		newQuery.setManager(app.getDeDupeCallbackManager().copy());
		
		SearchQueryPane pane = addSearchQuery(newQuery);
		Connection con = database.getConnection();
		pane.indexQuery(con, queryText, indexPath);
		
		database.getSearchIndices().addSearchIndex(newQuery);
	}
	
	
	/**
	 * Creates a new query pane bound to a given (disk-based) Lucene index.
	 * @param path is the path to the index.
	 * @throws IOException
	 * @throws DeDupeException 
	 */
	public void bindToIndex(String path) throws IOException, DeDupeException {
		SearchIndex searchIndex = new SearchIndex();
		searchIndex.setIndexPath(path);
		searchIndex.setName("Search index at " + path );
		searchIndex.setText("");
		searchIndex.setManager(app.getDeDupeCallbackManager().copy());
		SearchQueryPane pane = addSearchQuery(searchIndex);
		pane.attachToIndex(path);
		pane.initFieldNames();
		
		database.getSearchIndices().addSearchIndex(searchIndex);

	}
	
	/**
	 * Shows the fields on the selected index pane.
	 * @throws IOException 
	 * @throws CorruptIndexException 
	 */
	public void showIndexFields() throws CorruptIndexException, IOException {
		SearchQueryPane selectedPane = getSearchQuery();
		selectedPane.showIndexFields();
	}

	/**
	 * Lets the user select the fields to be used in a search result.
	 * @throws IOException 
	 * @throws CorruptIndexException 
	 */
	public void selectSearchResultFields() throws CorruptIndexException, IOException{
		SearchQueryPane selectedPane = getSearchQuery();
		selectedPane.selectReportFields();
	}

	/**
	 * 
	 */
	public void setDuplicateCount() {
		SearchQueryPane selectedPane = getSearchQuery();
		selectedPane.setDuplicatesLimit();
	}

	/**
	 * 
	 */
	public void setDuplicatesThreshold() {
		SearchQueryPane selectedPane = getSearchQuery();
		selectedPane.setDuplicatesThreshold();
	}

	/**
	 * 
	 */
	public void setDuplicatesOutputPath() {
		SearchQueryPane selectedPane = getSearchQuery();
		selectedPane.setDuplicatesOutputPath();
	};   

	/**
	 * 
	 */
	public void setDuplicatesRandomSelect() {
		SearchQueryPane selectedPane = getSearchQuery();
		selectedPane.setDuplicatesRandomSelect();
	};   

	
	/**
	 * Runs the duplicate detection.
	 * @throws CorruptIndexException
	 * @throws IOException
	 * @throws ParseException
	 */
	public void detectDuplicates(DeDupeProgressCallback callback) throws CorruptIndexException, IOException, ParseException{
		SearchQueryPane selectedPane = getSearchQuery();
		selectedPane.detectDuplicates(callback);
	}
	
	
	/**
	 * Edits the exclusions template for this index.
	 */
	public void editExceptionTemplate(){
        try {
			SearchQueryPane selectedPane = getSearchQuery();
			SearchIndex index = selectedPane.getSearchIndex();
			RecordExclusionTemplate exclusions = index.getExclusions();
      
			Collection<String> fieldCollection = selectedPane.getFieldNames();
			String[] fields = fieldCollection.toArray(new String[fieldCollection.size()]);
			RecordExclusionTemplateDialog dialog = new RecordExclusionTemplateDialog(this,"Record Exclusions",exclusions, fields);
			dialog.setVisible(true);
		} catch (Throwable t) {
			new ExceptionDisplay(this,t);
		}
	}

	/**
	 * Edits the de-dupe event receivers for this index.
	 */
	public void editDeDupeReceivers(){
        try {
			SearchQueryPane selectedPane = getSearchQuery();
			SearchIndex index = selectedPane.getSearchIndex();
			DeDupeCallbackManagerDialog dialog = new DeDupeCallbackManagerDialog(this,"De Dupe Event Receivers", index.getManager());
			dialog.setVisible(true);
		} catch (Throwable t) {
			new ExceptionDisplay(this,t);
		}
	}
	
	/**
	 * Gets user input and renames the selected query tab.
	 */
	public void renameSelectedQuery(){
		QueryPane query = (QueryPane)tabs.getSelectedComponent();
		String name = JOptionPane.showInputDialog(this, "New name for query?", query.getName() );
		if(name != null){
			query.setName(name);
			tabs.setTitleAt(tabs.getSelectedIndex(), name);
		}
	}

	/**
	 * Deletes the selected query.
	 */
	public void deleteSelectedQuery() {
		QueryPane pane = (QueryPane)tabs.getSelectedComponent();
		tabs.remove(pane);
		pane.deleteFromDatabase(database);
	}

	/**
	 *  Updates the queries from the text in the tabs.
	 */
	public void updateQueries() {
		int nQueries = tabs.getTabCount();
		for(int i=0; i<nQueries; ++i){
			QueryPane pane = (QueryPane)tabs.getComponentAt(i);
			pane.getQuery().setText(pane.getQueryText());
		}
	}
	
	/**
	 * @param path
	 */
	public void exportSelectedCSV(String path, boolean exportNulls,boolean headerRow) throws IOException{
		QueryPane pane = (QueryPane)tabs.getSelectedComponent();
		TableModel model = pane.getModel();
		FileWriter writer = new FileWriter(path);
		try {
			TableExportCSV export = new TableExportCSV();
			export.setEmitNulls(exportNulls);
			export.setEmitHeaderRow(headerRow);
			export.emitTable(writer,model);
		} finally {
			writer.close();
		}
	}

	/**
	 * @param path
	 */
	public void exportSelectedXML(String path, boolean exportNulls, String rootTag, String rowTag) throws IOException {
		QueryPane pane = (QueryPane)tabs.getSelectedComponent();
		TableModel model = pane.getModel();
		FileWriter writer = new FileWriter(path);
		try {
			TableExportXML export = new TableExportXML();
			export.setEmitNulls(exportNulls);
			export.setRootTag(rootTag);
			export.setRowTag(rowTag);
			export.emitTable(writer,model);
		} finally {
			writer.close();
		}
	}
	
	/* (non-Javadoc)
	 * @see uk.co.alvagem.dbview.AppEventListener#aboutToSave()
	 */
	public void aboutToSave() {
		updateQueries();
	}

	/* (non-Javadoc)
	 * @see uk.co.alvagem.dbview.AppEventListener#hasUnsaved()
	 */
	public boolean hasUnsaved() {
		int nQueries = tabs.getTabCount();
		boolean unsaved = false;
		for(int i=0; i<nQueries; ++i){
			QueryPane pane = (QueryPane)tabs.getComponentAt(i);
			String queryText = pane.getQuery().getText();
			unsaved |= !queryText.equals(pane.getQueryText());
		}
		return unsaved;
	}

	
	
	/**
	 * @author bruce.porteous
	 *
	 */
	private class WindowListener extends InternalFrameAdapter{

		/* (non-Javadoc)
		 * @see javax.swing.event.InternalFrameAdapter#internalFrameActivated(javax.swing.event.InternalFrameEvent)
		 */
		@Override
		public void internalFrameActivated(InternalFrameEvent event) {
			JInternalFrame frame = event.getInternalFrame(); 
			frame.moveToFront();
			if(frame.getParent() instanceof  javax.swing.JDesktopPane) {
				javax.swing.JDesktopPane desktop = (javax.swing.JDesktopPane) frame.getParent();
				JInternalFrame[] frames = desktop.getAllFrames();
				for(int i=0; i<frames.length; ++i){
					if(frames[i] != frame){
						try {
							frames[i].setSelected(false);
						} catch (PropertyVetoException e) {
							// nop.
						}
					}
				}
			}
		}

		/* (non-Javadoc)
		 * @see javax.swing.event.InternalFrameAdapter#internalFrameClosing(javax.swing.event.InternalFrameEvent)
		 */
		@Override
		public void internalFrameClosing(InternalFrameEvent event) {
			event.getInternalFrame().dispose();
		}

		/* (non-Javadoc)
		 * @see javax.swing.event.InternalFrameAdapter#internalFrameDeiconified(javax.swing.event.InternalFrameEvent)
		 */
		@Override
		public void internalFrameDeiconified(InternalFrameEvent event) {
			event.getInternalFrame().moveToFront();
		}

		/* (non-Javadoc)
		 * @see javax.swing.event.InternalFrameAdapter#internalFrameOpened(javax.swing.event.InternalFrameEvent)
		 */
		@Override
		public void internalFrameOpened(InternalFrameEvent event) {
			event.getInternalFrame().moveToFront();
		}
		
	}

	private void buildPluginMenu(JMenuBar menuBar){
		
		final String PLUGIN_MENU_NAME = "Plugins";
		
		PluginManager mgr = app.getPlugins();
		if(!mgr.hasPlugins()){
			return;
		}
		
	    JMenu pluginMenu = null;
	    for(int i=0; i<menuBar.getMenuCount(); ++i){
	    	JMenu menu = menuBar.getMenu(i);
	    	if(menu.getText().equals(PLUGIN_MENU_NAME)){
	    		pluginMenu = menu;
	    		break;
	    	}
	    }

	    if(pluginMenu == null){
	    	pluginMenu =  new JMenu(PLUGIN_MENU_NAME);
	    	pluginMenu.setToolTipText("Plugin menu");
	    	menuBar.add(pluginMenu);
	    }

	    pluginMenu.removeAll();
	    
	    for(DBViewPlugin plugin : mgr.getPlugins()){
	    	JMenu menu = new JMenu(plugin.getName());
	    	pluginMenu.add(menu);
	    	
	    	Map<String,String> commands = plugin.getCommands();
	    	for(Map.Entry<String,String> entry : commands.entrySet()){
	    		CommandAction action = new CommandAction(plugin, entry.getKey()); 
	    		action.putValue(Action.NAME,entry.getKey());
	    		action.putValue(Action.SHORT_DESCRIPTION,entry.getValue());
	    		
	    		JMenuItem menuItem = new JMenuItem(action);
	    		menu.add(menuItem);
	    	}
	    }
	}

	public void updatePluginMenu(){
		buildPluginMenu(menuBar);
	}


	public class CommandAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		private DBViewPlugin plugin;
		private String command;
		
		public CommandAction(DBViewPlugin plugin, String command) {
			this.plugin = plugin;
			this.command = command;
		}
		
		public void actionPerformed(ActionEvent e) {
            try {
            	plugin.runCommand(command, database, QueryFrame.this);
            } catch(Throwable t) {
                new ExceptionDisplay(QueryFrame.this,t);
            }
        }
    }




}
