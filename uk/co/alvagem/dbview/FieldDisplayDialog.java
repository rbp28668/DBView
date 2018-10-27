/**
 * 
 */
package uk.co.alvagem.dbview;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Takes a list of field names and provides a read-only display.
 * @author bruce.porteous
 *
 */
public class FieldDisplayDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private PropertiesPanel panel;
	
	


	/**
	 * @param owner
	 * @param title
	 * @param fields
	 */
	public FieldDisplayDialog(Component parent, String title, Collection<String>fields) {
        super((Frame)null, title, true); // modal
        setLocationRelativeTo(parent); 
        getRootPane().setBorder(BasicDialog.dialogBorder);
		init(fields);
	}


	/**
	 * @param database
	 */
	private void init(Collection<String>fields) {
		
		setLayout(new BorderLayout());
		panel = new PropertiesPanel(fields);
		

		JButton OK = new JButton("OK");
		OK.addActionListener( new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				closeDialog();
			}
			
		});
		add(OK,BorderLayout.SOUTH);
		add(panel,BorderLayout.CENTER);
		pack();
	}

    /** Closes the dialog */
    protected void closeDialog() {
        setVisible(false);
        dispose();
    }
	

	
	/**
	 * @author bruce.porteous
	 *
	 */
	private class PropertiesPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		
	    public PropertiesPanel(Collection<String>fields) {
	        super();
	        
	        final int MAX_ROWS = 20;
	        
	        int nFields = fields.size();
	        int rows = nFields;
	        int cols = 1;
	        
	        if(nFields > MAX_ROWS){
	        	cols = (nFields + MAX_ROWS - 1) / MAX_ROWS;
	        	rows = MAX_ROWS;
	        }
	        
	        GridLayout grid = new GridLayout(rows,cols);
	        setLayout(grid);
	        
	        for( String field : fields){
	        	JLabel label  = new JLabel(field);
	        	add(label);
	        }
	    }
	    
	    

		
	}
}
