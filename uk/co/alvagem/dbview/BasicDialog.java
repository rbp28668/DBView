/*
 * BasicDialog.java
 *
 * Created on 09 February 2002, 14:24
 */

package uk.co.alvagem.dbview;

import java.awt.Component;
import java.awt.Frame;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;


/**
 * BasicDialog provides a base class from which all the dialogs
 * in the tool are derived from.  This gives 2 aspects - a common
 * point for changing the basic dialog type and common services 
 * and layout.
 * @author  rbp28668
 */
public abstract class BasicDialog extends javax.swing.JDialog{

	public final static Border dialogBorder = new EmptyBorder(15,10,15,10);
	public final static Border componentBorder = new EmptyBorder(7,5,7,5);

	/** Standard OK/Cancel panel */
    private ButtonBox box = new ButtonBox();    
    /** true if the OK button was clicked, false if cancelled*/
    private boolean edited = false;
    private javax.swing.JButton btnOK;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnHelp;
    
    /** set true if the ok, cancel panel is fully initialised.*/
    private boolean okCancelInit = false;

    /** Creates new BasicDialog */
    public BasicDialog(javax.swing.JDialog parent, String title ) {
        super(parent, title, true); // modal
        setLocationRelativeTo(parent); 
        getRootPane().setBorder(dialogBorder);

    }
    /** Creates new BasicDialog */
    public BasicDialog(Component parent, String title ) {
        super((Frame)null, title, true); // modal
        setLocationRelativeTo(parent); 
        getRootPane().setBorder(dialogBorder);
    }
    
   
	/**
	 * Method initBox does the full initialisation of the
	 * OK/Cancel panel. This is delayed until the panel is
	 * accessed via getOKCancelPanel() to allow extra buttons
	 * to be added above OK & Cancel.
	 */
    private void initBox() {
        btnOK = new javax.swing.JButton("OK");
        btnCancel = new javax.swing.JButton("Cancel");
        btnHelp = new javax.swing.JButton("Help");
        box.add(btnOK);
        box.add(btnCancel);
        box.add(btnHelp);
        
        // OK Button
        btnOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
				fireOK();
            }
        });
        

        // Cancel Button
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeDialog();
            }
        });

        // Help Button
        btnHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showHelp();
            }
        });
        
        okCancelInit = true;
    }

	/**
	 * Method wasEdited returns whether the user clicked on OK and 
	 * the dialog contents were valid.
	 * @return boolean
	 */
    public boolean wasEdited() {
        return edited;
    }
    
	/**
	 * Method onOK is called when the user clicks the OK button and
	 * the dialog contains valid input (determined by validateInput()).
	 * This must be over-ridden in the sub-class.
	 */
    protected abstract void onOK();
    
	/**
	 * Method validateInput checks whether the dialog contains
	 * valid input or not. It must be implemented by the sub-class.
	 * @return boolean, true if the input is valid, false otherwise.
	 */
    protected abstract boolean validateInput();
    
    /** Closes the dialog */
    protected void closeDialog() {
        setVisible(false);
        dispose();
    }

	/**
	 * allows subclasses to simulate pressing the OK button.  This
	 * allows dialogs to implement double click for select & close.
	 */
	protected void fireOK() {
		if(validateInput()) {
			onOK();
			edited = true;
			closeDialog();
		}
	}

    /**
     * Show the dialog specific help.
     */
    private void showHelp() {
    }

	/**
	 * Method extendOKCancelPanel allows extra buttons to be added above
	 * the OK and Cancel buttons.
	 * @param extra
	 * @throws IllegalStateException
	 */
	protected void extendOKCancelPanel(javax.swing.JButton extra)
	throws IllegalStateException {
		if(okCancelInit) {
			throw new IllegalStateException("Can only extend OK/Cancel panel before first access");	
		}
		box.add(extra);
	}
	
	/**
	 * Method getOKCancelPanel gets a reference to the OK/Cancel
	 * button panel. If not already initialised it adds the OK and
	 * Cancel buttons on the bottom of the box. Any buttons that need
	 * to be added to the panel above the OK/Cancel buttons should
	 * be added by calling @see extendOKCancelPanel(javax.swing.JButton extra)
	 * before calling this method.
	 * @return ButtonBox
	 */
    protected ButtonBox getOKCancelPanel() {
    	if(!okCancelInit) {
    		initBox();
    	}
        return box;
    }
    
    
}
