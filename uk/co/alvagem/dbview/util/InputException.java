/*
 * InputException.java
 *
 * Created on 17 January 2002, 21:43
 */

package uk.co.alvagem.dbview.util;

/**
 *
 * @author  rbp28668
 */
public class InputException extends ChainedException {

    /**
     * Creates new <code>InputException</code> without detail message.
     */
    public InputException() {
    }


    /**
     * Constructs an <code>InputException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public InputException(String msg) {
        super(msg);
    }
    
    /** constructs an input exception chaining an existing exception
     * @param message is the message describing the problem
     * @param cause is the exception describing the cause of the problem
     */
    public InputException(String message, Throwable cause) {
        super(message,cause);
    }
}


