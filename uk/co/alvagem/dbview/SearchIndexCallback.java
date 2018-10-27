package uk.co.alvagem.dbview;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.SwingUtilities;

import org.apache.lucene.index.CorruptIndexException;

import uk.co.alvagem.dbview.model.QueryProgressCallback;



/**
 * @author bruce.porteous
 *
 */
class SearchIndexCallback implements QueryProgressCallback {

	private Statement stmt;
	private Connection con;
	private SearchQueryPane pane;
	private Exception ex;
	private long timestamp;
	private int rowsRead;
	private static final long UPDATE_FREQUENCY = 250; // mS
	
	
	/**
	 * Creates a new QueryCallback.
	 * @param stmt is the statement to close when complete.
	 * @param con is the database connection to close when complete.
	 * @param pane is the QueryPane that should display status updates.
	 */
	SearchIndexCallback(Statement stmt, Connection con, SearchQueryPane pane){
		this.stmt = stmt;
		this.con = con;
		this.pane = pane;
		// Initialise timestamp to regulate frequency of status updates.
		timestamp = System.currentTimeMillis();
	}
	
	public void rowsRead(int rows){
		this.rowsRead = rows;
		
		long now = System.currentTimeMillis();
		if(now - timestamp > UPDATE_FREQUENCY){
			timestamp = now;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					pane.setStatus("Indexed " + rowsRead + " rows.");
				}
			});
		}
	}
	
	/* (non-Javadoc)
	 * @see uk.co.alvagem.dbview.QueryProgressCallback#complete(java.sql.SQLException)
	 */
	public void complete(Exception ex) {
		try {
			stmt.close();
			con.close();
		} catch (SQLException e) {
			if(ex == null){
				ex = e;
			}
		}
		if(ex != null){
			this.ex = ex;
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				if(SearchIndexCallback.this.ex == null) {
					pane.setIndexComplete(rowsRead);
					try {
						pane.initFieldNames();
					} catch (CorruptIndexException e) {
						SearchIndexCallback.this.ex = e;
					} catch (IOException e) {
						SearchIndexCallback.this.ex = e;
					}
				} 

				if(SearchIndexCallback.this.ex != null) {
					pane.setStatus(SearchIndexCallback.this.ex.getMessage());
					new ExceptionDisplay(pane, SearchIndexCallback.this.ex);
				}
			}
			
		});
	}

}