/**
 * 
 */
package uk.co.alvagem.dbview.dedupe;

/**
 * Interface to report progress during a duplication detection run.
 * @author bruce.porteous
 */
public interface DeDupeProgressCallback {

	
	/**
	 * Called just before processing starts.
	 * @param records is the total number of records to be processed.
	 */
	public void started(int records);
	
	/**
	 * Determines progress by reporting the number of records processed
	 * and the number of matches found so far.
	 * @param processed is the number of records processed.
	 * @param matches is the number of match groups found.
	 */
	public void progress(int processed, int matches);
	
	/**
	 * Signals completion of the process.
	 * @param processed is the number of records processed.
	 * @param matches is the number of match groups found.
	 */
	public void complete(int processed, int matches);
	
	/**
	 * Registers the engine to allow it to be controlled during a run.
	 * @param engine
	 */
	public void register(DeDupeEngine engine);

	/**
	 * Signals that something bad has happened during processing.
	 * @param e contains the exception detail.
	 */
	public void error(Exception e);
}
