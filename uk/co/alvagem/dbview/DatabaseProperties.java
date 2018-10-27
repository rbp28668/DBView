/**
 * 
 */
package uk.co.alvagem.dbview;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import uk.co.alvagem.dbview.model.Database;

/**
 * Class to display the properties of a database.
 * @author bruce.porteous
 */
public class DatabaseProperties extends javax.swing.JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param parent
	 * @param title
	 */
	public DatabaseProperties(JDialog parent, String title, Database db) {
		super(parent, title);
		init(db);
        setLocationRelativeTo(parent); 
	}

	/**
	 * @param parent
	 * @param title
	 */
	public DatabaseProperties(Frame parent, String title, Database db) {
		super(parent, title);
		init(db);
        setLocationRelativeTo(parent); 
	}

	private void init(Database db){
		try {
			JButton ok = new JButton("OK");
	        ok.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                setVisible(false);
	                dispose();
	            }
	        });

			Connection con = db.getConnection();
			try {
				DatabaseMetaData meta = con.getMetaData();
				PropertiesPanel p = new PropertiesPanel(meta);
				getContentPane().add(p, BorderLayout.CENTER);
				getContentPane().add(ok, BorderLayout.SOUTH);
				pack();
			} finally {
				con.close();
			}
		} catch (SQLException e) {
			new ExceptionDisplay((Frame)getParent(),e);
		}

	}

	private class PropertiesPanel extends JPanel {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		PropertiesPanel(DatabaseMetaData meta) throws SQLException {
            
            GridBagLayout layout = new GridBagLayout();
            setLayout(layout);
            GridBagConstraints c = new GridBagConstraints();
            c.gridheight = 1;
            c.gridwidth = 1;
            c.weightx = 1.0f;
            c.weighty = 0.0f;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.LINE_START;
            c.ipadx = 10;
            c.insets = new Insets(5,2,5,2);

            
            setBorder(new StandardBorder());
            
            Map<String,String> p = new LinkedHashMap<String,String>();
            
            p.put("Database Product Name", meta.getDatabaseProductName());
            p.put("Database Product Version", meta.getDatabaseProductVersion());
            p.put("Database Major Version", Integer.toString(meta.getDatabaseMajorVersion()));
            p.put("Database Minor Version", Integer.toString(meta.getDatabaseMinorVersion()));
            p.put("Database URL", meta.getURL());
            p.put("Driver Name", meta.getDriverName());
            p.put("Driver Version", meta.getDriverVersion());
            p.put("Driver Major Version", Integer.toString(meta.getDriverMajorVersion()));
            p.put("Driver Minor Version", Integer.toString(meta.getDriverMinorVersion()));
            p.put("JDBC Major Version", Integer.toString(meta.getJDBCMajorVersion()));
            p.put("JDBC Minor Version", Integer.toString(meta.getJDBCMinorVersion()));
            
            
            for(Iterator<Map.Entry<String,String>> iter = p.entrySet().iterator(); iter.hasNext();){
                
                c.gridwidth = 1; // start of row.
                c.fill = GridBagConstraints.HORIZONTAL;
                
                Map.Entry<String,String> entry = iter.next();
                
                JLabel name = new JLabel(entry.getKey());
                layout.setConstraints(name,c);
                add(name);

                c.fill = GridBagConstraints.NONE;
                c.gridwidth = GridBagConstraints.REMAINDER; //end row
                
                JLabel value = new JLabel(entry.getValue());
                layout.setConstraints(value,c);
                add(value);
                
            }
            c.gridwidth = 1;
            c.weighty = 1.0f;
            JLabel padding = new JLabel();
            layout.setConstraints(padding,c);
            add(padding);
        }

	}
}
