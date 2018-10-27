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

/**
 * A database Schema - acts as a container for its Tables.
 * @author bruce.porteous
 */
public class Schema {
	private static final long serialVersionUID = 1L;
	private String name;
	private List<Table> allTables;
	private Map<TableTypes.Type,List<Table>> byType;
	private Database database;

	/**
	 * Creates a new, empty schema.  Private so use fromMeta(Database,ResultSet) factory method to 
	 * create a new shema. 
	 */
	private Schema() {
		super();
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Factory method to create schemas.
	 * @param db is the source database.
	 * @param rs is a ResultSet created using DatabaseMetaData.getSchemas().  The Schema
	 * will be created from the current row.
	 * @return a new Schema initialised from the ResultSet.
	 * @throws SQLException
	 */
	public static Schema fromMeta(Database db, ResultSet rs) throws SQLException{
		Schema s = new Schema();
		s.database = db;
		s.name = rs.getString("TABLE_SCHEM");
		return s;
	}

	/**
	 * Loads up the tables for this schema.  Used to allow lazy loading - if tablesLoaded()
	 * returns false then one of the addTables methods must be called before getTables or
	 * getAllTables can be called.  Use this method if you have a current instance of
	 * DatabaseMetaData to hand, otherwise use addTables().
	 * @param meta
	 * @param types
	 * @throws SQLException
	 */
	public void addTables(DatabaseMetaData meta, TableTypes types) throws SQLException{
		allTables = new LinkedList<Table>();
		byType = new HashMap<TableTypes.Type,List<Table>>();

		ResultSet rs = meta.getTables(null, name, null, null);
		while(rs.next()){
			Table t = Table.fromMeta(this, rs, types);
			allTables.add(t);
			
			TableTypes.Type type = t.getType();
			List<Table> typeList = byType.get(type);
			if(typeList == null){
				typeList = new LinkedList<Table>();
				byType.put(type,typeList);
			}
			typeList.add(t);
		}
		rs.close();
	}
	
	/**
	 * Loads all the tables.
	 * @throws SQLException
	 */
	private void addTables() throws SQLException{
		Connection con = database.getConnection();
		try {
			DatabaseMetaData meta = con.getMetaData();
			addTables(meta,database.getTableTypes());
		} finally {
			con.close();
		}
	}
	
	/**
	 * Determines whether all the tables are loaded.  getAllTables() or getTables(TableTypes.Type type)
	 * both load tables on demand or tables can be explicitly preloaded using
	 * addTables(DatabaseMetaData, TableTypes).
	 * @return true if the tables are loaded, false otherwise.
	 */
	public boolean tablesLoaded(){
		return allTables != null;
	}

	/**
	 * Gets all the tables, irrespective of table type.
	 * @return a Collection of Table containing all the tables.  May be empty, never null.
	 * @throws SQLException
	 */
	public Collection<Table> getAllTables() throws SQLException{
		if(!tablesLoaded()){
			addTables();
		}
		return Collections.unmodifiableCollection(allTables);
	}
	
	/**
	 * Gets all the tables in this schema corresponding to a given table type.
	 * @param type is the type of tables to get.
	 * @return a Collection of Table.
	 * @throws SQLException
	 */
	public Collection<Table> getTables(TableTypes.Type type) throws SQLException{
		if(!tablesLoaded()){
			addTables();
		}
		// Chance that there are no tables of a given type:
		List<Table> typeList = byType.get(type);
		if(typeList == null){
			typeList = new LinkedList<Table>();
			byType.put(type,typeList);
		}

		return Collections.unmodifiableCollection(typeList);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return name;
	}

	/**
	 * @return Returns the database.
	 */
	public Database getDatabase() {
		return database;
	}
	
	
}
