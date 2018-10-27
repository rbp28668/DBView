/*
 * OutputException.java
 *
 * Created on 25 February 2002, 09:56
 */

package uk.co.alvagem.dbview.util;

/**
 *
 * @author  rbp28668
 */
public class OutputException extends ChainedException {

    /** Creates new OutputException */
    public OutputException() {
    }
    /**
     * Constructs an <code>OutputException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public OutputException(String msg) {
        super(msg);
    }
    
    /** constructs an output exception chaining an existing exception
     * @param message is the message describing the problem
     * @param cause is the exception describing the cause of the problem
     */
    public OutputException(String message, Throwable cause) {
        super(message,cause);
    }
}
