package uk.co.alvagem.dbview;

import java.awt.Cursor;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;

import uk.co.alvagem.dbview.dedupe.CallbackManager;
import uk.co.alvagem.dbview.dedupe.DeDupeDetectionEventReceiver;
import uk.co.alvagem.dbview.dedupe.DeDupeEngine;
import uk.co.alvagem.dbview.dedupe.DeDupeProgressCallback;
import uk.co.alvagem.dbview.model.Database;
import uk.co.alvagem.dbview.model.Query;
import uk.co.alvagem.dbview.model.QueryProgressCallback;
import uk.co.alvagem.dbview.model.SearchEngine;
import uk.co.alvagem.dbview.model.SearchIndex;

/**
	 * A JPanel that fills each of the tabs in the query frame.  Manages the
	 * query text and results.
	 * @author bruce.porteous
	 *
	 */
	class SearchQueryPane extends QueryPane {
		/** Serialisation version */
		private static final long serialVersionUID = 1L;
		
		
		/** Named SQL query that drives this frame (or SearchIndex for search pane) */
		private SearchIndex searchIndex;
		
		/** Search engine for searching results */
		private SearchEngine search;

		/** Where to save the results of a de-dupe to */
		private String resultsPath;
		
		
		/**
		 * Creates a new SearchQueryPane.
		 * @param searchIndex provides the parameters to configure the
		 * index.
		 * @throws IOException 
		 */
		public SearchQueryPane(SearchIndex searchIndex) throws IOException{
			super(searchIndex.getName(), searchIndex.getText());
			
			this.searchIndex = searchIndex;
			if(searchIndex.getIndexPath() != null){
				attachToIndex(searchIndex.getIndexPath());
			}
			
			String path = System.getProperty("user.home");
			File outputFile = new File(path,"duplicates.csv");
			resultsPath = outputFile.getAbsolutePath();
			
		}
		
		
		/**
		 * Gets the current SearchIndex for this frame.
		 * @return this frame's SearchIndex.
		 */
		SearchIndex getSearchIndex(){
			return searchIndex;
		}
		
		/* (non-Javadoc)
		 * @see uk.co.alvagem.dbview.QueryPane#getQuery()
		 */
		public Query getQuery() {
			return searchIndex;
		}
	
		/* (non-Javadoc)
		 * @see java.awt.Component#setName(java.lang.String)
		 */
		@Override
		public void setName(String name) {
			getSearchIndex().setName(name);
			super.setName(name);
		}

	
		
		/**
		 * Runs a lucene search once a search has been set up.
		 * @param query is the query string to run.
		 * @throws IOException
		 * @throws ParseException
		 */
		public void runSearch(String query) throws IOException, ParseException{
			setModel( new SearchResultsTableModel(search,query) );
		}

		/**
		 * Creates a Lucene index of the results of running the current SQL
		 * query.
		 * @param database is the database to run the query against.
		 * @param queryText is the SQL query text to use.
		 * @param indexPath is the path to the folder the index should be built in.
		 * @throws SQLException 
		 * @throws IOException 
		 */
		public void indexQuery(Connection con, String queryText, String indexPath) throws SQLException, IOException{
			
			try {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					Statement stmt = con.createStatement();
					setStatus("Indexing query results");
						
					long start = System.currentTimeMillis();
					boolean hasResults = stmt.execute(queryText);
					long finish = System.currentTimeMillis();
					
					if(hasResults){				
						setStatus("Query complete in " + (finish - start) + "mS");
					} else {
						throw new SQLException("Query does not return results");
					}
					
					SQLWarning warning = stmt.getWarnings();
					String text = null;
					while(warning != null){
						if(text == null){
							text = warning.getMessage();
						} else {
							text = text + " : " + warning.getMessage();
						}
						warning = warning.getNextWarning();
					}
					if(text != null){
						setStatus(text);
					}

					// Record the provenance of the index.
					SearchIndex si = getSearchIndex();
					si.setIndexDate(new Date(start));
					si.setSourceSQL(queryText);
					si.setIndexPath(indexPath);

					// Fire off the indexing - will run asynchronously
					ResultSet rs = stmt.getResultSet();
					search = new SearchEngine(indexPath);
					QueryProgressCallback callback = new SearchIndexCallback(stmt,con, this);
					search.indexResults(rs, callback);

					
				} catch (IOException e) {
					setStatus(e.getMessage());
					throw e;
				} finally {
					setCursor(Cursor.getDefaultCursor());
				}
			} catch (SQLException e) {
				 // closed by QueryCallback in normal operation.
				con.close();
				setStatus(e.getMessage());
				throw e;
			}
		}			
		
		
		/**
		 * Indexes a table model produced as a result of a query.
		 * @param sourceTable is the table model to index.
		 * @throws IOException
		 */
		public void indexQueryResults(TableModel sourceTable) throws IOException{
			if(!(searchIndex instanceof SearchIndex)) {
				throw new IllegalStateException("Can only index table model in search pane");
			}
			
			search = new SearchEngine();
			search.indexTable(sourceTable);
			initFieldNames();
		}
		
		/**
		 * Attaches to an initial index on disk.
		 * @param indexPath
		 * @throws IOException 
		 */
		public void attachToIndex(String indexPath) throws IOException{
			if(!(searchIndex instanceof SearchIndex)) {
				throw new IllegalStateException("Only attach index to search pane");
			}
			search = new SearchEngine(indexPath);
		}


		/**
		 * @throws CorruptIndexException
		 * @throws IOException
		 */
		public void initFieldNames() throws CorruptIndexException, IOException {
			Collection<String> names = getFieldNames();
			SearchIndex index = getSearchIndex();
			index.setSelectedFields(new LinkedHashSet<String>(names));
		}
		
		/**
		 * Gets a sorted list of field names stored in the index.  This list excludes any
		 * special names which start with $.
		 * @return sorted collection of String containing field names.
		 * @throws CorruptIndexException
		 * @throws IOException
		 */
		public Collection<String> getFieldNames() throws CorruptIndexException, IOException{
			IndexReader reader = search.getIndexReader();
			LinkedList<String> names = new LinkedList<String>(reader.getFieldNames(IndexReader.FieldOption.ALL));
			
			// Remove any names starting with $.  These are special fields.
			Set<String>toRemove = new HashSet<String>();
			for(String name : names){
				if(name.startsWith("$")){
					toRemove.add(name);
				}
			}
			names.removeAll(toRemove);
			Collections.sort(names);
			return names;

		}
		
		/**
		 * @throws IOException 
		 * @throws CorruptIndexException 
		 * 
		 */
		public void showIndexFields() throws CorruptIndexException, IOException {
			Collection<String> names = getFieldNames();
			FieldDisplayDialog dialog = new FieldDisplayDialog(this, "Index Fields", names);
			dialog.setVisible(true);
		}

		/**
		 * @throws ParseException 
		 * @throws IOException 
		 * @throws CorruptIndexException 
		 * 
		 */
		public void detectDuplicates(DeDupeProgressCallback callback) throws CorruptIndexException, IOException, ParseException {

			DeDupeEngine deDupe = new DeDupeEngine(search);
			
			deDupe.setMaxDocs(searchIndex.getDeDupeLimit());
			deDupe.setThreshold(searchIndex.getDeDupeThreshold());
			deDupe.setReportFields(searchIndex.getSelectedFields());
			deDupe.setRandomSelect(searchIndex.isRandomSelect());

			CallbackManager manager = searchIndex.getManager();
			deDupe.clearReceivers();
			for(Map.Entry<String,DeDupeDetectionEventReceiver> entry : manager.getReceivers().entrySet()){
				String name = entry.getKey();
				
				if(manager.isEnabled(name)){
					DeDupeDetectionEventReceiver rx = entry.getValue();
					deDupe.addReciever(rx);
				}
			}
			
			String queryText = getQueryText();
			System.out.println("Running duplicate detection");
			System.out.println("Query: " + queryText);
			System.out.println("Document Limit: " + deDupe.getMaxDocs());
			System.out.println("Threshold: " + deDupe.getThreshold());
			deDupe.findMatches(queryText, resultsPath, callback);
		}

		/**
		 * Lets the user select the fields for reporting.
		 * @throws IOException 
		 * @throws CorruptIndexException 
		 */
		public void selectReportFields() throws CorruptIndexException, IOException{
			FieldSelectionDialog dialog = new FieldSelectionDialog(this,"Select Fields", 
					getFieldNames(), searchIndex.getSelectedFields());
			dialog.setVisible(true);
			if(dialog.wasEdited()){
				searchIndex.setSelectedFields(dialog.getSelected());
			}
		}
		
		/**
		 * Call at the end of a query to display it as complete.  Used
		 * by the SearchIndexCallback.
		 * @param rows is the number of result rows read.
		 */
		public void setIndexComplete(int rows){
			setStatus("Complete, indexed " + rows + " rows.");
			if(getModel() != null){
				QueryTableModel model = (QueryTableModel)getModel();
				TableModelEvent event = new TableModelEvent(model);
				model.fireTableChanged(event);
				revalidate();
			}
		}




		/* (non-Javadoc)
		 * @see uk.co.alvagem.dbview.QueryPane#deleteFromDatabase(uk.co.alvagem.dbview.Database)
		 */
		@Override
		public void deleteFromDatabase(Database database) {
			database.getSearchIndices().removeSearchIndex(searchIndex);
		}




		/* (non-Javadoc)
		 * @see uk.co.alvagem.dbview.QueryPane#getName()
		 */
		@Override
		public String getName() {
			return searchIndex.getName();
		}


		/**
		 * Allows the user to set up the limit used for de-duping.
		 */
		public void setDuplicatesLimit() {
			int limit = searchIndex.getDeDupeLimit();
			String val = Integer.toString(limit);
			boolean valid = true;
			do {
				val = (String) JOptionPane.showInputDialog(this,"Enter processing limit","Duplicate Detection",JOptionPane.QUESTION_MESSAGE
						,null,null,val);
				if(val != null){
					try {
						limit = Integer.parseInt(val);
						valid = limit >= 0;
					} catch (NumberFormatException nfe){
						JOptionPane.showMessageDialog(this, "Please enter an integer of 0 or greater","DBView",JOptionPane.ERROR_MESSAGE);
						valid = false;
					}
					if(valid) {
						searchIndex.setDeDupeLimit(limit);
					}
				} else {  // val is null - user cancelled.
					valid = true;
				}
			} while (!valid);
		}


		/**
		 * Allows the user to select the threshold used for de-duping
		 */
		public void setDuplicatesThreshold() {
			float threshold = searchIndex.getDeDupeThreshold();
			String val = Float.toString(threshold);
			boolean valid = true;
			do {
				val = (String) JOptionPane.showInputDialog(this,"Enter threshold","Duplicate Detection",JOptionPane.QUESTION_MESSAGE
						,null,null,val);
				if(val != null){
					try {
						threshold = Float.parseFloat(val);
						valid = (threshold >= 0) && (threshold <= 1);
					} catch (NumberFormatException nfe){
						JOptionPane.showMessageDialog(this, "Please enter a threshold between 0 and 1","DBView",JOptionPane.ERROR_MESSAGE);
						valid = false;
					}
					if(valid){
						searchIndex.setDeDupeThreshold(threshold);
					}
				} else { // cancelled.
					valid = true;
				}
			} while (!valid);
		}


		/**
		 * Allows the user to select the path the de-dupe results
		 * are written to.
		 */
		public void setDuplicatesOutputPath() {

			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Duplicate Detection Output");
			chooser.setApproveButtonText("Set Output");
			chooser.setApproveButtonToolTipText("Set the output path for the duplicate detection");
			chooser.setSelectedFile(new File(resultsPath));
			int returnVal = chooser.showSaveDialog(this);
		    if(returnVal == JFileChooser.APPROVE_OPTION) {
		        resultsPath = chooser.getSelectedFile().getName();
		     }
		}

		/**
		 * Allows the user to select whether de-duping should randomly pick records
		 * for test sampling.
		 */
		public void setDuplicatesRandomSelect() {

			boolean isRandom = JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this,"Random Selection of records?","DBView - DeDup",JOptionPane.YES_NO_OPTION);
			searchIndex.setRandomSelect(isRandom);
		}


	}