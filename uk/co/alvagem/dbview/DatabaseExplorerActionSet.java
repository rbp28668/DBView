/**
 * 
 */
package uk.co.alvagem.dbview;

import java.awt.Cursor;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;

import uk.co.alvagem.dbview.model.Database;
import uk.co.alvagem.dbview.model.Databases;
import uk.co.alvagem.dbview.model.Table;

/**
 * @author bruce.porteous
 *
 */
public class DatabaseExplorerActionSet extends ActionSet {

	private DatabaseExplorer explorer;
	private Databases databases;
	private Frame frame;
	private DBView app;
	
	/**
	 * 
	 */
	public DatabaseExplorerActionSet(DBView app, DatabaseExplorer explorer, Databases databases) {
		super();
	
		this.explorer = explorer;
		this.databases = databases;
		this.app = app;
		this.frame = app.getCommandFrame();
		
		addAction("AddDatabase", actionAddDatabase);
		addAction("EditDatabase",actionEditDatabase);
		addAction("TestDatabase",actionTestDatabase);
		addAction("RemoveDatabase",actionRemoveDatabase);
		
		addAction("DatabaseProperties", actionDatabaseProperties);
		addAction("ShowTables", actionShowTables);
		addAction("RunQuery", actionRunQuery);
		
		// Tables
		addAction("TableProperties", actionTableProperties);
		addAction("TableIndices", actionTableIndices);
		addAction("TableForeignKeys", actionTableForeignKeys);
		
	}

    private final Action actionAddDatabase = new AbstractAction() {
		private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            try {
            	Database database = new Database();
            	DatabaseDialog editor = new DatabaseDialog(explorer, "Enter Database Parameters", database);
            	editor.setVisible(true);
            	if(editor.wasEdited()){
            		databases.addDatabase(database);
            	}
           } catch(Throwable t) {
                new ExceptionDisplay(frame,t);
            }
        }
    };   

    private final Action actionEditDatabase = new AbstractAction() {
		private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            try {
            	Database database = (Database)explorer.getSelectedItem();
            	DatabaseDialog editor = new DatabaseDialog(explorer, "Edit Database Parameters", database);
            	editor.setVisible(true);
            	databases.fireDatabaseEdited(database);
           } catch(Throwable t) {
                new ExceptionDisplay(frame,t);
            }
        }
    };   

    private final Action actionTestDatabase = new AbstractAction() {
		private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            try {
            	Database db = (Database)explorer.getSelectedItem();
            	Connection con = null;
            	try {
            		con = db.getConnection();
            		if(con != null) {
            			JOptionPane.showMessageDialog(explorer,"Connected to Database " + db.getName(),"Test Database", JOptionPane.INFORMATION_MESSAGE);
            		} else {
            			JOptionPane.showMessageDialog(explorer,"NOT Connected to Database " + db.getName(),"Test Database", JOptionPane.ERROR_MESSAGE);
            		}
            	} finally {
            		if(con != null){
            			con.close();
            		}
            	}
           } catch(Throwable t) {
                new ExceptionDisplay(frame,t);
            }
        }
    };   

    private final Action actionRemoveDatabase = new AbstractAction() {
		private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            try {
            	Database db = (Database)explorer.getSelectedItem();
            	databases.removeDatabase(db);
           } catch(Throwable t) {
                new ExceptionDisplay(frame,t);
            }
        }
    };   

    private final Action actionDatabaseProperties = new AbstractAction() {
		private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            try {
            	Database db = (Database)explorer.getSelectedItem();
            	DatabaseProperties props = new DatabaseProperties(frame,"Properties for " + db.getName(), db);
            	props.setVisible(true);
           } catch(Throwable t) {
                new ExceptionDisplay(frame,t);
            }
        }
    };   
    
    private final Action actionShowTables = new AbstractAction() {
		private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            try {
            	frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            	Connection con = null;
            	try {
	            	Database db = (Database)explorer.getSelectedItem();
	
	            	con = db.getConnection();
	            	DatabaseMetaData meta = con.getMetaData();
	            	
	    			String schema = "dbo";
	    			String[] types = new String[] {"TABLE","VIEW"};
	    			ResultSet rs = meta.getTables(null, schema, null,types);
	        		while(rs.next()){
	        			String tableName = rs.getString("TABLE_NAME");
	        			String tableType = rs.getString("TABLE_TYPE");
	        			//String remarks = rs.getString("REMARKS");
	        			
	        			int rowCount = getRowCount(db, schema, tableName);

	        			System.out.println(schema 
	        					+ "," + tableName 
	        					+ "," + tableType
	        					+ "," + rowCount
	        					//+ ", " + remarks
	        			);
	        		}
             	} finally {
                	frame.setCursor(Cursor.getDefaultCursor());
                	if(con != null){
                		con.close();
                	}
            	}
           } catch(Throwable t) {
                new ExceptionDisplay(frame,t);
            }
        }

		private int getRowCount(Database db, String schema, String tableName) throws SQLException {
			String query = "select count(*) from " + schema + "." + tableName;
			int rows = 0;
			Connection con = db.getConnection();
			try {
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(query);
				if(rs.next()){
					rows = rs.getInt(1);
				}
			} finally {
				con.close();
			}
			return rows;
		}
    };   
    
    private final Action actionRunQuery = new AbstractAction() {
		private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            try {
            	Database db = (Database)explorer.getSelectedItem();
            	QueryFrame query = new QueryFrame(app,db);
            	JDesktopPane desktop = app.getCommandFrame().getDesktop();
            	desktop.add(query);
            	query.moveToFront();
            	desktop.setSelectedFrame(query);
            	
           } catch(Throwable t) {
                new ExceptionDisplay(frame,t);
            }
        }
    };   

    private final Action actionTableProperties = new AbstractAction() {
		private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            try {
            	Table table = (Table)explorer.getSelectedItem();
            	TablePropertiesFrame display = new TablePropertiesFrame(table);
            	JDesktopPane desktop = app.getCommandFrame().getDesktop();
            	desktop.add(display);
            	display.moveToFront();
            	desktop.setSelectedFrame(display);
            	
           } catch(Throwable t) {
                new ExceptionDisplay(frame,t);
            }
        }
    };   

    private final Action actionTableIndices = new AbstractAction() {
		private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            try {
            	Table table = (Table)explorer.getSelectedItem();
            	TableIndicesFrame display = new TableIndicesFrame(table);
            	JDesktopPane desktop = app.getCommandFrame().getDesktop();
            	desktop.add(display);
            	display.moveToFront();
            	desktop.setSelectedFrame(display);
            	
           } catch(Throwable t) {
                new ExceptionDisplay(frame,t);
            }
        }
    };   

    private final Action actionTableForeignKeys = new AbstractAction() {
		private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            try {
            	Table table = (Table)explorer.getSelectedItem();
            	ForeignKeyFrame display = new ForeignKeyFrame(table);
            	JDesktopPane desktop = app.getCommandFrame().getDesktop();
            	desktop.add(display);
            	display.moveToFront();
            	desktop.setSelectedFrame(display);
            	
           } catch(Throwable t) {
                new ExceptionDisplay(frame,t);
            }
        }
    };   
    
    
  

}
