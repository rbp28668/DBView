package uk.co.alvagem.dbview;

import java.awt.Cursor;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Date;

import javax.swing.event.TableModelEvent;

import uk.co.alvagem.dbview.model.Database;
import uk.co.alvagem.dbview.model.Query;
import uk.co.alvagem.dbview.model.QueryProgressCallback;

/**
	 * A JPanel that fills each of the tabs in the query frame.  Manages the
	 * query text and results.
	 * @author bruce.porteous
	 *
	 */
	class SQLQueryPane extends QueryPane {
		/** Serialisation version */
		private static final long serialVersionUID = 1L;
		
		/** Named SQL query that drives this frame (or SearchIndex for search pane) */
		private Query query;
		
		/** Actual SQL run for this query.  User can over-type text once query has started
		 * and should the results be indexed, we want to know the query that actually
		 * produced the results */
		private String actualSQL;
		
		/** When the query was actually run */
		private Date queryDate;
		
		/**
		 * Creates a new QueryPane.
		 * @param index is used to set up the name of the Pane in the tabs so
		 * by default the panes are called Query 1, Query 2 etc.
		 */
		public SQLQueryPane(Query query){
			super(query.getName(), query.getText());
			this.query = query;
		}
		

		
		/**
		 * Gets the current query for this frame.
		 * @return this frame's query.
		 */
		public Query getQuery(){
			return query;
		}
		
		/* (non-Javadoc)
		 * @see uk.co.alvagem.dbview.QueryPane#getName()
		 */
		@Override
		public String getName() {
			return query.getName();
		}
		
		/* (non-Javadoc)
		 * @see java.awt.Component#setName(java.lang.String)
		 */
		@Override
		public void setName(String name) {
			getQuery().setName(name);
			super.setName(name);
		}

	
		
		/**
		 * Gets the actual SQL used ot run the query.
		 * @return the actualSQL
		 */
		public String getActualSQL() {
			return actualSQL;
		}


		/**
		 * Gets the date the query was run on.
		 * @return the queryDate
		 */
		public Date getQueryDate() {
			return queryDate;
		}



		/**
		 * Runs a SQL query.  If there are results they're shown in a table in
		 * the bottom frame.
		 * @param con is the database Connection to use to run the query.
		 * @throws SQLException
		 */
		public void runQuery(Connection con) throws SQLException{
			String queryText = getQueryText();

			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			try {
				Statement stmt = con.createStatement();
				setStatus("Running Query");
				
				long start = System.currentTimeMillis();
				boolean hasResults = stmt.execute(queryText);
				long finish = System.currentTimeMillis();
				
				if(hasResults){				
					setStatus("Query complete in " + (finish - start) + "mS");
				} else {
					int updateCount = stmt.getUpdateCount();
					setStatus("Updated " + updateCount + " row(s) in " + (finish - start) + "mS");
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
				
				if(hasResults) {
					actualSQL = queryText;
					queryDate = new Date();
					ResultSet rs = stmt.getResultSet();
					setData(rs, new QueryCallback(stmt, con, this));
				}
				
			} finally {
				setCursor(Cursor.getDefaultCursor());
			}
		}
		
		
		/**
		 * Sets the data to be displayed from a ResultSet. 
		 * @param rs is the ResultSet to provide the data to be displayed.
		 * @param callback is used to track progress of reading the data from
		 * the ResultSet.
		 * @throws SQLException
		 */
		private void setData(ResultSet rs, QueryProgressCallback callback) throws SQLException{
			QueryTableModel model = new QueryTableModel();
			model.setColumns(rs);
			setModel(model);
			model.setData(rs, callback);
		}

		
		/**
		 * Call at the end of a query to display it as complete.  Used
		 * by the QueryCallback.
		 * @param rows is the number of result rows read.
		 */
		public void setComplete(int rows){
			setStatus("Complete, read " + rows + " rows.");
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
			database.getQueries().removeQuery(query);
		}








	}