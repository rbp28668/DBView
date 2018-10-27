/**
 * 
 */
package uk.co.alvagem.dbview;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * Takes a list of field names and allows the user to select a subset.
 * @author bruce.porteous
 *
 */
public class FieldSelectionDialog extends BasicDialog {

	private static final long serialVersionUID = 1L;
	private PropertiesPanel panel;
	
	/**
	 * @param parent
	 * @param title
	 */
	public FieldSelectionDialog(JDialog parent, String title, Collection<String>fields, Set<String> selected) {
		super(parent, title);
		init(fields,selected);
	}

	/**
	 * @param parent
	 * @param title
	 */
	public FieldSelectionDialog(Component parent, String title, Collection<String>fields, Set<String> selected) {
		super(parent, title);
		init(fields,selected);
	}

	/**
	 * @param database
	 */
	private void init(Collection<String>fields,Set<String> selected) {
		
		setLayout(new BorderLayout());
		panel = new PropertiesPanel(fields, selected);
		
		JButton btnSetAll = new JButton("Set All");
		JButton btnClearAll = new JButton("Clear All");
		
		extendOKCancelPanel(btnSetAll);
		extendOKCancelPanel(btnClearAll);
		
		btnSetAll.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
				panel.setAll();
            }
        });

		btnClearAll.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
				panel.clearAll();
            }
        });


		add(getOKCancelPanel(),BorderLayout.EAST);
		add(panel,BorderLayout.CENTER);
		pack();
	}

	/* (non-Javadoc)
	 * @see uk.co.alvagem.dbview.BasicDialog#onOK()
	 */
	protected void onOK() {
		panel.onOK();
	}

	
	/* (non-Javadoc)
	 * @see uk.co.alvagem.dbview.BasicDialog#validateInput()
	 */
	protected boolean validateInput() {
		return panel.validateInput();
	}

	/**
	 * Gets the selected set of fields.
	 * @return a set containing the selected fields, maybe empty, never null.
	 */
	public Set<String> getSelected(){
		return panel.getSelected();
	}
	
	/**
	 * @author bruce.porteous
	 *
	 */
	private class PropertiesPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private Map<String,JCheckBox> checks;
		
	    public PropertiesPanel(Collection<String>fields,Set<String> selected) {
	        super();
	        
	        final int MAX_ROWS = 20;
	        
	        int nFields = fields.size();
	        int rows = nFields;
	        int cols = 1;
	        
	        if(nFields > MAX_ROWS){
	        	cols = (nFields + MAX_ROWS - 1) / MAX_ROWS;
	        	rows = MAX_ROWS;
	        }
	        
	        checks = new LinkedHashMap<String, JCheckBox>(2*nFields);

	        GridLayout grid = new GridLayout(rows,cols);
	        setLayout(grid);
	        
	        for( String field : fields){
	        	JCheckBox check = new JCheckBox(field);
	        	if(selected.contains(field)){
	        		check.setSelected(true);
	        	}
	        	add(check);
	        	checks.put(field,check);
	        }
	    }
	    
	    
	    /**
		 * 
		 */
		public void clearAll() {
			for(Map.Entry<String, JCheckBox> entry : checks.entrySet()){
				entry.getValue().setSelected(false);
			}
		}


		/**
		 * 
		 */
		public void setAll() {
			for(Map.Entry<String, JCheckBox> entry : checks.entrySet()){
				entry.getValue().setSelected(true);
			}
		}


		/**
	     * 
	     */
	    public void onOK(){
	    }
	    
		/**
		 * @return
		 */
		protected boolean validateInput() {
			return true;
		}
		
		/**
		 * Gets a set of selected entries.
		 * @return a set of selected entries.  May be empty, never null.
		 */
		public Set<String> getSelected(){
			Set<String> selected = new LinkedHashSet<String>();
			
			for(Map.Entry<String, JCheckBox> entry : checks.entrySet()){
				if(entry.getValue().isSelected()){
					selected.add(entry.getKey());
				}
			}
			return selected;
		}

		
	}
}
