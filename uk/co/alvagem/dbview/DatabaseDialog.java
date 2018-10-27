/**
 * 
 */
package uk.co.alvagem.dbview;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import uk.co.alvagem.dbview.model.Database;

/**
 * @author bruce.porteous
 *
 */
public class DatabaseDialog extends BasicDialog {

	private static final long serialVersionUID = 1L;
	private PropertiesPanel panel;
	private Database database;
	
	/**
	 * @param parent
	 * @param title
	 */
	public DatabaseDialog(JDialog parent, String title, Database database) {
		super(parent, title);
		init(database);
	}

	/**
	 * @param parent
	 * @param title
	 */
	public DatabaseDialog(Component parent, String title, Database database) {
		super(parent, title);
		init(database);
	}

	/**
	 * @param database
	 */
	private void init(Database database) {
		this.database = database;
		setLayout(new BorderLayout());
		panel = new PropertiesPanel(database);
		
		add(getOKCancelPanel(),BorderLayout.EAST);
		add(panel,BorderLayout.CENTER);
		pack();
	}

	/* (non-Javadoc)
	 * @see uk.co.alvagem.dbview.BasicDialog#onOK()
	 */
	protected void onOK() {
		panel.onOK(database);
	}

	
	/* (non-Javadoc)
	 * @see uk.co.alvagem.dbview.BasicDialog#validateInput()
	 */
	protected boolean validateInput() {
		return panel.validateInput();
	}

	private class PropertiesPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		private JTextField name= new JTextField();
		private JTextField driverClass = new JTextField();
		private JTextField url= new JTextField();
		private JTextField serverName= new JTextField(); 
		private JTextField portNumber= new JTextField();
		private JTextField databaseName= new JTextField();
		private JTextField userName= new JTextField();
		private JTextField password= new JTextField();
		private JTextField selectMethod= new JTextField();

	    public PropertiesPanel(Database db) {
	        super();
	        
	        GridBagLayout grid = new GridBagLayout();
	        GridBagConstraints c = new GridBagConstraints();
	        
	        setLayout(grid);
	        
	        c.anchor = GridBagConstraints.LINE_START;
	        c.weighty = 1.0;
	        c.insets = new Insets(5,10,5,10);
	        
	        addRow("Connection Name",name,db.getName(),grid,c);
	        addRow("Driver Class", driverClass, db.getDriverClass(), grid, c);
	        addRow("URL",url,db.getUrl(),grid,c);
	        addRow("Server",serverName,db.getServerName(),grid,c);
	        addRow("Port", portNumber, Integer.toString(db.getPortNumber()),grid,c);
	        addRow("Database Name", databaseName,db.getDatabaseName(),grid,c);
	        addRow("User", userName,db.getUserName(), grid, c);
	        addRow("Password",password,db.getPassword(), grid,c);
	        addRow("Select Method", selectMethod, db.getSelectMethod(), grid,c);
	    }
	    
	    private void addRow(String text, JTextField textField, String value, GridBagLayout grid, GridBagConstraints c){
            JLabel label = new JLabel(text);
            textField.setText(value);
            textField.setColumns(40);
            
            c.gridwidth = GridBagConstraints.RELATIVE;
            grid.setConstraints(label,c);
            add(label);
            
            c.gridwidth = GridBagConstraints.REMAINDER;
            grid.setConstraints(textField,c);
            add(textField);
	    }
	    
	    public void onOK(Database db){
	    	db.setName(name.getText());
	    	db.setDriverClass(driverClass.getText());
	    	db.setUrl(url.getText());
	    	db.setServerName(serverName.getText());
	    	if(hasContents(portNumber)){
	    		db.setPortNumber(Integer.parseInt(portNumber.getText()));
	    	} else {
	    		db.setPortNumber(0);
	    	}
	    	db.setDatabaseName(databaseName.getText());
	    	db.setUserName(userName.getText());
	    	db.setPassword(password.getText());
	    	db.setSelectMethod(selectMethod.getText());
	    }
	    
		protected boolean validateInput() {

		
			if (!hasContents(name)) {
				JOptionPane.showMessageDialog(this, "You need to name the database", "DBView", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			if(!hasContents(driverClass)) {
				JOptionPane.showMessageDialog(this, "You need to specify the driver class", "DBView", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			if(!hasContents(url)){
				JOptionPane.showMessageDialog(this, "You need to specify a jdbc URL", "DBView", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			if(hasContents(portNumber) && !isIntegerContents(portNumber)){
				JOptionPane.showMessageDialog(this, "Port number must be integer", "DBView", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			// Depends on driver whether the rest is optional or not so,
		    // for the time being at least,
			// ignore them.
			/*
			&& hasContents(serverName)
			&& hasIntegerContents(portNumber)
			&& hasContents(databaseName)
			&& hasContents(userName)
			&& hasContents(password)
			&& hasContents(selectMethod);
			*/
			return true;
		}

		boolean hasContents(JTextField field){
	    	if(field.getText().trim().length() == 0) {
	    		field.requestFocusInWindow();
	    		return false;
	    	}
	    	return true;
		}

		boolean isIntegerContents(JTextField field){
			String text = field.getText().trim();
	    	try {
	    		Integer.parseInt(text);
	    	} catch (NumberFormatException nfx){
	    		field.requestFocus();
	    		return false;
	    	}
	    	return true;
		}
		
	}
}
