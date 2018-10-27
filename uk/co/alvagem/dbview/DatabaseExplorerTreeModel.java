/**
 * 
 */
package uk.co.alvagem.dbview;

import java.awt.Component;
import java.awt.Cursor;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import uk.co.alvagem.dbview.model.Column;
import uk.co.alvagem.dbview.model.Database;
import uk.co.alvagem.dbview.model.DatabaseEventListener;
import uk.co.alvagem.dbview.model.Databases;
import uk.co.alvagem.dbview.model.Schema;
import uk.co.alvagem.dbview.model.Table;
import uk.co.alvagem.dbview.model.TableTypes;

/**
 * Provides the model of the databases/database/schemas/tables/columns hierarchy for exploring the
 * databases.
 * @author bruce.porteous
 */
public class DatabaseExplorerTreeModel extends ExplorerTreeModel implements DatabaseEventListener, TreeWillExpandListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DefaultMutableTreeNode root = null;
	private Databases databases;
	private Component container;
	
	/**
	 * @param rootTitle
	 */
	public DatabaseExplorerTreeModel(Component container, String rootTitle, Databases databases) {
		super(rootTitle);
		this.databases = databases;
		this.container = container;
		
		databases.addListener(this);

		root = (DefaultMutableTreeNode)getRoot();
		root.setUserObject( databases );
		
		initModel();
	}

	/**
	 * 
	 */
	public void dispose() {
		databases.removeListener(this);
	}

	   /**
	 * Method initModel builds the tree model from the mappings.
	 */
    private void initModel() {
        int idx = 0;
        for(Iterator iter = databases.getDatabases().iterator();iter.hasNext();){
            Database db = (Database)iter.next();
            addDatabaseNode((MutableTreeNode)getRoot(),db,idx++);
        }
    }

    /**
     * @param node
     * @param db
     * @param idx
     */
    private int addDatabaseNode(MutableTreeNode parent, Database db, int idx) {
        DefaultMutableTreeNode tnDatabase = new ContainerNode(db);
        insertNodeInto(tnDatabase,parent,idx);
        registerNode(tnDatabase,db);
        return idx;
    }
    
    private int addDatabaseChildren(MutableTreeNode parent, Database db) throws SQLException {
        int idx = 0;
        for(Iterator iter = db.getSchemas().iterator();iter.hasNext();){
            Schema schema = (Schema)iter.next();
            addSchemaNode(parent,schema,idx++);
        }
        return idx;
    }
    
    
	/**
	 * @param parent
	 * @param schema
	 * @param idx
	 */
	private void addSchemaNode(MutableTreeNode parent, Schema schema, int idx) {
        DefaultMutableTreeNode tnSchema = new ContainerNode(schema);
        insertNodeInto(tnSchema,parent,idx);
        registerNode(tnSchema, schema);
	}

    private int addSchemaChildren(MutableTreeNode parent, Schema schema) throws SQLException{
        int idx = 0;
        Database db = schema.getDatabase();
        for(Iterator iter = db.getTableTypes().getTypes().iterator();iter.hasNext();){
            TableTypes.Type type = (TableTypes.Type)iter.next();
            TypeProxy proxy = new TypeProxy(type,schema);
            addTypeNode(parent,proxy,idx++);
        }
        return idx;
    }
    

	
	/**
	 * @param parent
	 * @param type
	 * @param i
	 */
	private void addTypeNode(MutableTreeNode parent, TypeProxy type, int idx) {
        DefaultMutableTreeNode tnType = new ContainerNode(type);
        insertNodeInto(tnType,parent,idx);
        registerNode(tnType, type);
	}
	
	private int addTableTypeChildren(MutableTreeNode parent,TypeProxy proxy) throws SQLException{
        int idx = 0;
        Collection tables = proxy.getSchema().getTables(proxy.getType());
        for(Iterator iter = tables.iterator();iter.hasNext();){
            Table table = (Table)iter.next();
            addTableNode(parent,table,idx++);
        }
        return idx;
	}

	private void addTableNode(MutableTreeNode parent, Table table, int idx) {
        DefaultMutableTreeNode tnTable = new ContainerNode(table);
        insertNodeInto(tnTable,parent,idx);
        registerNode(tnTable, table);
	}

	private int addTableChildren(MutableTreeNode parent, Table table) throws SQLException{
		int idx = 0;
		for(Iterator iter = table.getColumns().iterator(); iter.hasNext();){
			Column column = (Column)iter.next();
			addColumnNode(parent, column, idx++);
		}
		return idx;
	}

	/**
	 * @param parent
	 * @param column
	 * @param idx
	 */
	private void addColumnNode(MutableTreeNode parent, Column column, int idx) {
        DefaultMutableTreeNode tnColumn = new DefaultMutableTreeNode(column);
        insertNodeInto(tnColumn,parent,idx);
        registerNode(tnColumn, column);
	}

	/* (non-Javadoc)
	 * @see uk.co.alvagem.dbview.DatabaseEventListener#updated(uk.co.alvagem.dbview.DatabaseChangeEvent)
	 */
	public void updated(DatabaseChangeEvent e) {
        root.removeAllChildren();
        initModel();
        nodeStructureChanged(root);
	}

	/* (non-Javadoc)
	 * @see uk.co.alvagem.dbview.DatabaseEventListener#DatabaseAdded(uk.co.alvagem.dbview.DatabaseChangeEvent)
	 */
	public void databaseAdded(DatabaseChangeEvent e) {
        Database db = (Database)e.getSource();
        addDatabaseNode(root,db,root.getChildCount());
	}

	/* (non-Javadoc)
	 * @see uk.co.alvagem.dbview.DatabaseEventListener#DatabaseChanged(uk.co.alvagem.dbview.DatabaseChangeEvent)
	 */
	public void databaseChanged(DatabaseChangeEvent e) {
        Database database = (Database)e.getSource();
        DefaultMutableTreeNode tn = lookupNodeOf(database);
        if(tn != null) {
            nodeChanged(tn);
        }
	}

	/* (non-Javadoc)
	 * @see uk.co.alvagem.dbview.DatabaseEventListener#DatabaseDeleted(uk.co.alvagem.dbview.DatabaseChangeEvent)
	 */
	public void databaseDeleted(DatabaseChangeEvent e) {
        Database database = (Database)e.getSource();
        DefaultMutableTreeNode tn = lookupNodeOf(database);
        if(tn != null) {
            removeNodeFromParent(tn);
            removeNodeOf(database);
        }
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.TreeWillExpandListener#treeWillExpand(javax.swing.event.TreeExpansionEvent)
	 */
	public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
		
		TreePath path = event.getPath();
		Object last = path.getLastPathComponent();
		//Source is uk.co.alvagem.dbview.ExplorerTree
		//Last is javax.swing.tree.DefaultMutableTreeNode
		
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)last;
		Object dbItem = node.getUserObject();
		//System.out.println("Expanding a " + dbItem);
		
		container.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try {
			if(dbItem instanceof Database) {
				if(node.getChildCount() == 0){
					Database db = (Database)dbItem;
					addDatabaseChildren(node,db);
				}
			} else if( dbItem instanceof Schema) {
				if(node.getChildCount() == 0){
					Schema schema = (Schema)dbItem;
					addSchemaChildren(node,schema);
				}
			} else if (dbItem instanceof TypeProxy) {
				TypeProxy proxy = (TypeProxy)dbItem;
				if(node.getChildCount() == 0){
					addTableTypeChildren(node,proxy);
				}
				
			} else if (dbItem instanceof Table) {
				Table table = (Table)dbItem;
				if(node.getChildCount() == 0){
					addTableChildren(node,table);
				}
				
			} else if (dbItem instanceof Column) {
				// Nop - can't expand further.
			}
		} catch (Exception e) {
			// TODO - report error
			System.out.println(e.getMessage());
			throw new ExpandVetoException(event);
		} finally {
			container.setCursor(Cursor.getDefaultCursor());
		}
		//System.out.println("Expanded");
		
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.TreeWillExpandListener#treeWillCollapse(javax.swing.event.TreeExpansionEvent)
	 */
	public void treeWillCollapse(TreeExpansionEvent arg0) throws ExpandVetoException {
		
	}


	private class ContainerNode extends DefaultMutableTreeNode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ContainerNode(Object obj){
			super(obj);
		}
		public boolean getAllowsChildren() {
			return true;
		}
		
		public boolean isLeaf() {
			return false;
		}
	}
	
	/**
	 * Proxy for Table Type.  Also contains the schema so that when the node is expanded the tables
	 * can be accessed.
	 */
	private class TypeProxy {
		private TableTypes.Type type;
		private Schema schema;
		
		TypeProxy(TableTypes.Type type, Schema schema){
			this.type = type;
			this.schema = schema;
		}
		public String toString(){
			return type.toString();
		}
		/**
		 * @return Returns the schema.
		 */
		public Schema getSchema() {
			return schema;
		}
		/**
		 * @return Returns the type.
		 */
		public TableTypes.Type getType() {
			return type;
		}
		
	}
}
