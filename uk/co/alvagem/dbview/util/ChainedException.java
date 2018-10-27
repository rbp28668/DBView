/*
 * ChainedException.java
 *
 * Created on 24 February 2002, 15:11
 */

package uk.co.alvagem.dbview.util;

/**
 *
 * @author  rbp28668
 */
public class ChainedException extends RuntimeException {

    /** Creates new ChainedException */
    public ChainedException() {
        super();
    }
    
    public ChainedException(String message) {
        super(message);
    }
    
    public ChainedException(String message, Throwable cause) {
        super(message);
        this.cause = cause;
    }
    
    public Throwable getCause() {
        return cause;
    }
    
    public String getMessage() {
        String superMsg = super.getMessage();
        String causeMsg = null;
        if(cause != null) {
            causeMsg = cause.getMessage();
        }
        if((superMsg == null) && (causeMsg == null))
            return null;
        
        if((superMsg != null) && (causeMsg == null))
            return superMsg;
        
        if((superMsg == null) && (causeMsg != null))
            return causeMsg;
        
        return superMsg + System.getProperty("line.separator") + causeMsg;
    }
    
    public void printStrackTrace() {
        super.printStackTrace();
        if(cause != null) {
            cause.printStackTrace();
        }
    }
    
    public void printStackTrace(java.io.PrintStream ps) {
        super.printStackTrace(ps);
        if(cause != null) {
            cause.printStackTrace(ps);
        }
    }
    
    public void printStackTrace(java.io.PrintWriter pw) {
        super.printStackTrace(pw);
        if(cause != null) {
            cause.printStackTrace(pw);
        }
    }
    private Throwable cause = null;

}
