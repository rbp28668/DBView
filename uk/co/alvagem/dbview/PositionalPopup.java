/*
 * PositionalPopup.java
 * Project: EATool
 * Created on 14-Feb-2006
 *
 */
package uk.co.alvagem.dbview;

import javax.swing.JPopupMenu;

/**
 * PositionalPopup is a variation on a JPopupMenu that carries extra positional
 * information.  It is intended for popup menus that fire off actions where it 
 * is useful to know a (mouse) position so that new symbols etc can be placed at
 * a sensible place.
 * 
 * @author rbp28668
 */
public class PositionalPopup extends JPopupMenu {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int x;
    private int y;
    
    /**
     * 
     */
    public PositionalPopup() {
        super();
    }

    /**
     * @param arg0
     */
    public PositionalPopup(String arg0) {
        super(arg0);
    }

    public void setTargetPosition(int x, int y){
        this.x = x;
        this.y = y;
    }
    
    public int getTargetX(){
        return x;
    }
    
    public int getTargetY(){
        return y;
    }
    
}
