/**
 * 
 */
package uk.co.alvagem.dbview.model;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author bruce.porteous
 *
 */
public class TableTypes {

	private Map<String,Type> types = new HashMap<String,Type>();
	
	/**
	 * 
	 */
	public TableTypes() {
		super();
	}
	
	public void fromMeta(DatabaseMetaData meta) throws SQLException{
		ResultSet rs = meta.getTableTypes();
		while(rs.next()){
			String typeName = rs.getString("TABLE_TYPE");
			Type type = new Type(typeName);
			types.put(typeName, type);
		}
		rs.close();
	}
	
	public Type getType(String typeName) throws SQLException{
		if(!types.containsKey(typeName)){
			throw new SQLException("Invalid table type name " + typeName);
		}
		return types.get(typeName);
	}
	
	public Collection<Type> getTypes() {
		return types.values();
	}
	
	public static class Type {
		private String typeName;
		
		private Type(String typeName){
			this.typeName = typeName;
		}
		
		public String toString(){
			return typeName;
		}
		
	}

}
