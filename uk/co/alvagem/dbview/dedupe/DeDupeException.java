/**
 * 
 */
package uk.co.alvagem.dbview.dedupe;

/**
 * @author bruce.porteous
 *
 */
public class DeDupeException extends Exception {

	/**
	 * 
	 */
	public DeDupeException() {
	}

	/**
	 * @param message
	 */
	public DeDupeException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public DeDupeException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DeDupeException(String message, Throwable cause) {
		super(message, cause);
	}

}
