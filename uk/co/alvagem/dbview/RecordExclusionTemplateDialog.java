/*
 * RecordExclusionTemplateDialog.java
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
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import uk.co.alvagem.dbview.model.RecordExclusionTemplate;
import uk.co.alvagem.dbview.model.RecordExclusionTemplate.Exclude;

/**
 * RecordExclusionTemplateDialog is a dialog class to allow the user to edit a
 * set of exclusion records.  Each record consists of a field name and a regular
 * expression.
 * @see RecordExclusionTemplate.
 * @author rbp28668
 */
public class RecordExclusionTemplateDialog extends BasicDialog {

    private static final long serialVersionUID = 1L;
    private PropertiesPanel panel;
    private RecordExclusionTemplate exclude;
    private String[] fields = null;
    
    /**
     * @param parent
     * @param title
     */
    public RecordExclusionTemplateDialog(JDialog parent, String title, RecordExclusionTemplate exclude, String[] fields) {
        super(parent, title);
        init(exclude, fields);
    }

    /**
     * @param parent
     * @param title
     */
    public RecordExclusionTemplateDialog(Component parent, String title, RecordExclusionTemplate exclude, String[] fields) {
        super(parent, title);
        init(exclude,fields);
    }

    
    /**
     * @param database
     */
    private void init(RecordExclusionTemplate exclude, String[] fields) {
        this.exclude = exclude;
        this.fields = fields;
        
        setLayout(new BorderLayout());
        panel = new PropertiesPanel(exclude,fields);
        
        JButton addRow = new JButton("Add Row");
        extendOKCancelPanel(addRow);
        add(getOKCancelPanel(),BorderLayout.EAST);
        add(panel,BorderLayout.CENTER);
        pack();
        
        addRow.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                panel.addNewRow(RecordExclusionTemplateDialog.this.fields);
            }
            
        });
    }
    
    @Override
    protected void onOK() {
        exclude.clear();
        for(Row r : panel.getRows()){
            String field = r.getFieldText();
            String regex = r.getTemplate().getText().trim();
            exclude.add(field, regex);
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
        
        private String toMatch = "";

        private GridBagLayout layout = new GridBagLayout();

        PropertiesPanel(RecordExclusionTemplate exclude, String[] fields){
            setLayout(layout);
            
            setTitles(layout);

            for(Exclude e : exclude.getTemplates()){
                Row row = new Row(e, fields);
                addRow(row);
            }
            
            // If no fields then create a single empty row.
            if(exclude.getTemplates().isEmpty()){
            	Row newRow = new Row("","", fields);
            	addRow(newRow);
            }
        }
        
        public boolean validateInput() {
            for(Row row : rows){
                String field = row.getFieldText();
                if(field.length() == 0){
                    JOptionPane.showMessageDialog(this, "Missing field name", "DBView", JOptionPane.ERROR_MESSAGE);
                    row.getField().requestFocusInWindow();
                    return false;
                }
                String regex = row.getTemplate().getText().trim();
                if(regex.length() == 0){
                    JOptionPane.showMessageDialog(this, "Missing regular expression", "DBView", JOptionPane.ERROR_MESSAGE);
                    row.getTemplate().requestFocusInWindow();
                    return false;
                }
                
                try {
                    Pattern.compile(regex);
                } catch (PatternSyntaxException e) {
                    JOptionPane.showMessageDialog(this, e.getMessage(), "DBView", JOptionPane.ERROR_MESSAGE);
                    row.getTemplate().requestFocusInWindow();
                    return false;
                }
            }
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
        	
        	label = new JLabel("Field");
            Font font = label.getFont().deriveFont(Font.BOLD + Font.ITALIC);
            label.setFont(font);
        	c = new GridBagConstraints();
        	c.gridx = 0; c.gridy = 0; ;
            layout.setConstraints(label, c);
            add(label);
            
            label = new JLabel("Regular Expression");
            label.setFont(font);
        	c = new GridBagConstraints();
        	c.gridx = 1; c.gridy = 0; ;
            layout.setConstraints(label, c);
            add(label);
            
            label = new JLabel("Test");
            label.setFont(font);
        	c = new GridBagConstraints();
        	c.gridx = 2; c.gridy = 0; ;
            layout.setConstraints(label, c);
            add(label);
            
            label = new JLabel("Delete");
            label.setFont(font);
        	c = new GridBagConstraints();
        	c.gridx = 3; c.gridy = 0; ;
            layout.setConstraints(label, c);
            add(label);
        }


        /**
         * Adds a new empty row at the bottom of the panel.
         */
        public void addNewRow(String[] fields) {
            addRow(new Row(fields[0],"", fields));
            pack();
        }

        /**
         * @param row
         */
        private void addRow(Row row){
            rows.add(row);
            
            row.setConstraints(layout, rows.size());
            row.add(this);
            
            
            row.getTest().addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    testRow(e);
                }
                
            });
            
            row.getDelete().addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    deleteRow(e);
                }
                
            });
            
        }
        
        
        private void testRow(ActionEvent e){
            for(Row r : rows){
                if(r.getTest() == e.getSource()){
                    String regex = r.getTemplate().getText();
                    Pattern p = null;
                    try {
                        p = Pattern.compile(regex);
                    } catch (PatternSyntaxException ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "DBView", JOptionPane.ERROR_MESSAGE);
                    }
                    if(p != null){
                        String tm = JOptionPane.showInputDialog(this,"Match text",toMatch);
                        if(tm != null){
                        	toMatch = tm;
	                        Matcher m = p.matcher(toMatch);
	                        if(m.matches()){
	                            JOptionPane.showMessageDialog(this, "Matched", "DBView", JOptionPane.INFORMATION_MESSAGE);
	                        } else {
	                            JOptionPane.showMessageDialog(this, "Not Matched", "DBView", JOptionPane.INFORMATION_MESSAGE);
	                        }
                        }
                    }
                    break;
                }
            }
        }
        
        private void deleteRow(ActionEvent e){

            int idx = 0;
            int selected = -1;
            for(Row r : rows){
                if(r.getDelete() == e.getSource()){
                    selected = idx;
                    break;
                }
                ++idx;
            }
            
            if(selected != -1){
                Row toDelete = rows.remove(selected);
                toDelete.removeFrom(this);
            }

            int iRow = 1;
            for(Row r : rows){
            	r.setGridRow(layout, iRow++);
            }
            
            layout.invalidateLayout(this);
            pack();
            repaint();
            
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
        JComponent field;
        JTextField template;
        JButton test;
        JButton delete;

        /**
         * Creates a new row from an existing Exclude record using the
         * field and template values to initialise the text boxes.
         * @param e
         */
        Row(Exclude e, String[] fields){
            this(e.getField(),e.getTemplate(), fields);
        }
        
        /**
         * Creates a new row. 
         * @param field is the field text.
         * @param regex is the regular expression text.
         * @param fields is an array of the available fields.  May be null if not known.
         */
        Row(String field, String regex, String[] fields){
        	if(fields != null){
        		JComboBox combo = new JComboBox(fields);
        		combo.setEditable(false);
        		
        		String fl = field.toLowerCase();
        		for(int i=0; i<fields.length; ++i){
        			if(fields[i].toLowerCase().equals(fl)){
        				combo.setSelectedIndex(i);
        				break;
        			}
        		}
        		this.field = combo;
        	} else { // no fields list - nust use a text field.
        		JTextField text = new JTextField(field);
        		text.setColumns(10);
        		this.field = text;
        	}
            this.template = new JTextField(regex);
            this.template.setColumns(20);
            this.test = new JButton("Test");
            this.delete = new JButton("Delete");
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
            layout.setConstraints(field, c);
            
        	c = new GridBagConstraints();
        	c.gridx = 1; c.gridy = iRow; 
        	c.insets = new Insets(0,5,0,5);
        	layout.setConstraints(template, c);

        	c = new GridBagConstraints();
        	c.gridx = 2; c.gridy = iRow; 
        	c.insets = new Insets(2,10,2,10);
            layout.setConstraints(test, c);

        	c = new GridBagConstraints();
        	c.gridx = 3; c.gridy = iRow;
        	c.insets = new Insets(2,10,2,10);
            layout.setConstraints(delete, c);

           
        }
 
        void setGridRow(GridBagLayout layout, int iRow){
        	GridBagConstraints c;
            c = layout.getConstraints(field);
            c.gridy = iRow;
            layout.setConstraints(field, c);
            
        	c = layout.getConstraints(template);
        	c.gridy = iRow;
            layout.setConstraints(template, c);
        	
            c = layout.getConstraints(test);
            c.gridy = iRow;
            layout.setConstraints(test, c);
            
            c = layout.getConstraints(delete);
            c.gridy = iRow;
            layout.setConstraints(delete, c);
        }
        
        
        /**
         * Adds the row of components to a container - namely the panel.
         * @param container
         */
        void add(Container container){
            container.add(field);
            container.add(template);
            container.add(test);
            container.add(delete);
        }

        /**
         * Removes the complete row from the container to allow deletion of rows.
         * @param container
         */
        void removeFrom(Container container){
            container.remove(field);
            container.remove(template);
            container.remove(test);
            container.remove(delete);
        }

        
        /**
         * @return
         */
        public JComponent getField(){
        	return field;
        }
        
        /**
         * @return the field text
         */
        public String getFieldText() {
        	String text = null;
            if (field instanceof JComboBox) {
				JComboBox combo = (JComboBox) field;
				text = (String)combo.getSelectedItem();
			} else {
				JTextField tf = (JTextField) field;
				text = tf.getText();
			}
            return text.trim();
        }

        /**
         * @return the template
         */
        public JTextField getTemplate() {
            return template;
        }

        /**
         * @return the test
         */
        public JButton getTest() {
            return test;
        }

        /**
         * @return the delete
         */
        public JButton getDelete() {
            return delete;
        }
        
        
    }
    
}
