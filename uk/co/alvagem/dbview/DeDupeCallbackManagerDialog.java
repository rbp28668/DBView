/*
 * CallbackManagerDialog.java
 * Project: DBView
 * Created on 9 Sep 2009
 *
 */
package uk.co.alvagem.dbview;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import uk.co.alvagem.dbview.dedupe.CallbackManager;
import uk.co.alvagem.dbview.dedupe.DeDupeDetectionEventReceiver;

/**
 * CallbackManagerDialog is a dialog class to allow the user to edit a
 * set of exclusion records.  Each record consists of a field name and a regular
 * expression.
 * @see CallbackManager.
 * @author rbp28668
 */
public class DeDupeCallbackManagerDialog extends BasicDialog {

    private static final long serialVersionUID = 1L;
    private PropertiesPanel panel;
    private CallbackManager manager;
    //private String[] fields = null;
    
    /**
     * @param parent
     * @param title
     */
    public DeDupeCallbackManagerDialog(JDialog parent, String title, CallbackManager manager) {
        super(parent, title);
        init(manager);
    }

    /**
     * @param parent
     * @param title
     */
    public DeDupeCallbackManagerDialog(Component parent, String title, CallbackManager manager) {
        super(parent, title);
        init(manager);
    }

    
    /**
     * @param database
     */
    private void init(CallbackManager manager) {
        this.manager = manager;
        
        setLayout(new BorderLayout());
        panel = new PropertiesPanel(manager);
        
        add(getOKCancelPanel(),BorderLayout.EAST);
        add(panel,BorderLayout.CENTER);
        pack();
        
     }
    
    @Override
    protected void onOK() {
        for(Row r : panel.getRows()){
            String name = r.getName();
            boolean enabled = r.getEnabledState();
            manager.setEnabled(name, enabled);
        }
        
    }

    @Override
    protected boolean validateInput() {
        return panel.validateInput();
    }

    /**
     * Panel that contains the grid of controls.
     * @author bruce.porteous
     *
     */
    private class PropertiesPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        private Vector<Row> rows = new Vector<Row>();
        
        private GridBagLayout layout = new GridBagLayout();

        PropertiesPanel(CallbackManager manager){
            setLayout(layout);
            setTitles(layout);

            for(Map.Entry<String,DeDupeDetectionEventReceiver> entry : manager.getReceivers().entrySet()){
            	Row row = new Row(manager, entry.getKey(), true);
            	addRow(row);
            }
        }
        
        public boolean validateInput() {
            return true;
        }
        
        /**
         * Sets the constraints on all the items in the row.
         * @param layout
         * @param body
         * @param right
         */
        private void setTitles(GridBagLayout layout){
        	GridBagConstraints c;
        	JLabel label;
        	
        	label = new JLabel("Output");
            Font font = label.getFont().deriveFont(Font.BOLD + Font.ITALIC);
            label.setFont(font);
        	c = new GridBagConstraints();
        	c.gridx = 0; c.gridy = 0; ;
            layout.setConstraints(label, c);
            add(label);
            
            label = new JLabel("Enabled");
            label.setFont(font);
        	c = new GridBagConstraints();
        	c.gridx = 1; c.gridy = 0; ;
            layout.setConstraints(label, c);
            add(label);
            
            label = new JLabel("Edit");
            label.setFont(font);
        	c = new GridBagConstraints();
        	c.gridx = 2; c.gridy = 0; ;
            layout.setConstraints(label, c);
            add(label);
            
        }


 
        /**
         * @param row
         */
        private void addRow(Row row){
            rows.add(row);
            
            row.setConstraints(layout, rows.size());
            row.add(this);
            
        	row.getEdit().addActionListener( new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					try {
						JButton source = (JButton)e.getSource();
						// Find row.
						Row row = null;
						for(Row r : rows){
							if(r.getEdit() == source){
								row = r;
								break;
							}
						}
						if(row == null){
							throw new IllegalStateException("Impossible: Can't find source of edit");
						}
						String name = row.getName();
						manager.edit(name,DeDupeCallbackManagerDialog.this);
					} catch (Throwable t) {
						new ExceptionDisplay(DeDupeCallbackManagerDialog.this,t);
					}
				}
        		
        	});
            
         }
        
        
         
        
        Collection<Row> getRows(){
            return rows;
        }
    }
    
    /**
     * Row models a single row of data in the main panel.  
     * 
     * @author rbp28668
     */
    private static class Row{
    	private JLabel name;
    	private JCheckBox enable;
    	private JButton edit;
                

        /**
         * Creates a new row. 
         * @param field is the field text.
         * @param regex is the regular expression text.
         * @param fields is an array of the available fields.  May be null if not known.
         */
        Row(CallbackManager manager, String name, boolean enabled){
        	this.name = new JLabel(name);
        	this.enable = new JCheckBox();
        	this.enable.setSelected(enabled);
        	this.edit = new JButton("Edit");
        	
        }
        
        /**
		 * @return
		 */
		public String getName() {
			return name.getText();
		}

		/**
		 * @return
		 */
		public JButton getEdit(){
			return edit;
		}
		
		/**
         * Sets the constraints on all the items in the row.
         * @param layout
         * @param body
         * @param right
         */
        void setConstraints(GridBagLayout layout, int iRow){
        	GridBagConstraints c;
        	
        	c = new GridBagConstraints();
        	c.gridx = 0; c.gridy = iRow; 
        	c.insets = new Insets(0,0,0,5);
            layout.setConstraints(name, c);
            
        	c = new GridBagConstraints();
        	c.gridx = 1; c.gridy = iRow; 
        	c.insets = new Insets(0,5,0,5);
        	layout.setConstraints(enable, c);

        	c = new GridBagConstraints();
        	c.gridx = 2; c.gridy = iRow; 
        	c.insets = new Insets(0,5,0,5);
        	layout.setConstraints(edit, c);
        }
 
        
        
        /**
         * Adds the row of components to a container - namely the panel.
         * @param container
         */
        void add(Container container){
            container.add(name);
            container.add(enable);
            container.add(edit);
         }


        
        /**
         * @return
         */
        public boolean getEnabledState(){
        	return enable.isSelected();
        }
        
        
    }
    
}
