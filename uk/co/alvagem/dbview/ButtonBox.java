/*
 * ButtonBox.java
 *
 * Created on 02 February 2002, 21:25
 */

package uk.co.alvagem.dbview;

import java.awt.Dimension;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
/**
 * ButtonBox is a container class to provide a vertical strip of buttons.  
 * It is set up to make buttons drop to the bottom and is normally used 
 * to setup the OK and Cancel buttons in the bottom right corner of a
 * dialog.
 * @author  rbp28668
 */
public class ButtonBox extends Box {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** Creates new and empty ButtonBox */
    public ButtonBox() {
        super(BoxLayout.Y_AXIS);
        add(Box.createVerticalGlue()); // buttons drop to bottom of box
    }
    
	/**
	 * Method add adds a button with a strut to space it from the button
	 * above.  Buttons are added from top to bottom.
	 * @param btn
	 */
    public void add(JButton btn) {
        super.add(Box.createVerticalStrut(strutSize));
        super.add(btn);
    }

	/**
	 * @see java.awt.Component#doLayout().  
	 * Causes this container to lay out its components
	 */
    public void doLayout() {
        super.doLayout();
        int nButtons = getComponentCount();
        Dimension size = new Dimension(0,0);
        for(int i=0; i<nButtons; ++i){
            Component c = getComponent(i);
            Dimension d = c.getSize();
            if(d.width > size.width) size.width = d.width;
            if( ! (c instanceof Box.Filler)) size.height = d.height;
        }
        for(int i=0; i<nButtons; ++i) {
            getComponent(i).setSize(size);
        }
    }
    
    /** Size to leave between buttons */
    private int strutSize = 5;

}
