/**
 * 
 */
package uk.co.alvagem.dbview.model;


/**
 * Callback interface that allows async queries to report their progress.
 * @author bruce.porteous
 */
public interface QueryProgressCallback {

	/**
	 * Called at arbitrary intervals during a query to allow status updates.
	 * @param rows is the number of rows read so far.
	 */
	public void rowsRead(int rows);
	
	/**
	 * Called at the end of running a query - can update UI, close things etc.
	 * @param ex has any exception caused by running the query. Null if ok.
	 */
	public void complete(Exception ex);
}
