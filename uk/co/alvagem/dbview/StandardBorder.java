/*
 * StandardBorder.java
 *
 * Created on 19 April 2002, 08:50
 */

package uk.co.alvagem.dbview;

import javax.swing.border.TitledBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;

/**
 *
 * @author  rbp28668
 */
public class StandardBorder extends CompoundBorder{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** Creates new StandardBorder */
    public StandardBorder() {
        this("");
    }
    
    public StandardBorder(String title) {
        super( new TitledBorder(title), new EmptyBorder(10,5,10,5));
    }

}
