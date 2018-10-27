/**
 * 
 */
package uk.co.alvagem.dbview.model;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.co.alvagem.dbview.ForeignKeyColumn;
import uk.co.alvagem.dbview.IndexColumn;

/**
 * A database Table and container for its Columns.
 * @author bruce.porteous
 *
 */
public class Table {

	private String tableName;
	private TableTypes.Type type;
	private Schema parent;
	private List<Column> columns = null;
	private Map<String,Column> columnsByName = null;
	private List<IndexColumn> indexColumns = null;
	
	
	/**
	 * Private constructor - use Table.fromMeta factory method.
	 */
	private Table() {
		super();
	}

	public static Table fromMeta(Schema parent, ResultSet rs, TableTypes types) throws SQLException{
		Table t = new Table();
		t.parent = parent;
		t.tableName = rs.getString("TABLE_NAME");
		String typeName = rs.getString("TABLE_TYPE");
		t.type = types.getType(typeName);
		return t;
	}
	
	public void addColumns(DatabaseMetaData meta) throws SQLException{
		columns = new LinkedList<Column>();
		columnsByName = new HashMap<String,Column>();
		ResultSet rs = meta.getColumns(null, parent.getName(), tableName, null);
		while(rs.next()){
			Column c = Column.fromMeta(rs);
			columns.add(c);
			columnsByName.put(c.getColumnName(),c);
		}
		
		rs.close();
		
		// And work out primary keys.
		rs = meta.getPrimaryKeys(null, parent.getName(), tableName);
		while(rs.next()){
			String colName = rs.getString("COLUMN_NAME"); // String => column name
			short keySeq = rs.getShort("KEY_SEQ"); //  short => sequence number within primary key
			String pkName = rs.getString("PK_NAME"); // String => primary key name (may be null)
			Column c = columnsByName.get(colName);
			c.setPK(keySeq, pkName);
		}
		rs.close();
	}
	
	/**
	 * 
	 */
	private void addColumns() throws SQLException {
		Connection con = parent.getDatabase().getConnection();
		try {
			DatabaseMetaData meta = con.getMetaData();
			addColumns(meta);
		} finally {
			con.close();
		}
	}

	public void addIndexColumns(DatabaseMetaData meta) throws SQLException{
		indexColumns = new LinkedList<IndexColumn>();
		ResultSet rs = meta.getIndexInfo(null, parent.getName(), tableName, false, true);
		while(rs.next()){
			IndexColumn ic = IndexColumn.fromMeta(rs);
			indexColumns.add(ic);
		}
		rs.close();
	}
	
	/**
	 * 
	 */
	private void addIndexColumns() throws SQLException {
		Connection con = parent.getDatabase().getConnection();
		try {
			DatabaseMetaData meta = con.getMetaData();
			addIndexColumns(meta);
		} finally {
			con.close();
		}
	}
	
	
	/**
	 * @return Returns the parent.
	 */
	public Schema getParent() {
		return parent;
	}

	/**
	 * @return Returns the tableName.
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * @return Returns the type.
	 */
	public TableTypes.Type getType() {
		return type;
	}

	public boolean columnsLoaded(){
		return columns != null;
	}
	
	public Collection<Column> getColumns() throws SQLException{
		if(!columnsLoaded()){
			addColumns();
		}
		return Collections.unmodifiableCollection(columns);
	}

	public boolean indexColumnsLoaded(){
		return indexColumns != null;
	}
	
	public Collection<IndexColumn> getIndexColumns() throws SQLException{
		if(!indexColumnsLoaded()){
			addIndexColumns();
		}
		return Collections.unmodifiableCollection(indexColumns);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return tableName;
	}

	/**
	 * Gets a list of foreign key columns that reference this table.  Note that
	 * each all to this regenerates a new list from the database - the data is
	 * not cached.
	 * @return Collection of ForeignKeyColumn.  May be empty, never null.
	 * @throws SQLException 
	 */
	public Collection<ForeignKeyColumn> getFKColumns() throws SQLException {
		List<ForeignKeyColumn>fkList = new LinkedList<ForeignKeyColumn>();
		
		Connection con = parent.getDatabase().getConnection();
		try {
			DatabaseMetaData meta = con.getMetaData();
			
			ResultSet rs = meta.getImportedKeys(null, parent.getName(), tableName);
			while(rs.next()){
				ForeignKeyColumn fk = ForeignKeyColumn.fromMeta(true, rs);
				fkList.add(fk);
			}
			rs.close();
			
			rs = meta.getExportedKeys(null, parent.getName(), tableName);
			while(rs.next()){
				ForeignKeyColumn fk = ForeignKeyColumn.fromMeta(false, rs);
				fkList.add(fk);
			}
			rs.close();
			
		} finally {
			con.close();
		}
		
		return fkList;
	}
}
