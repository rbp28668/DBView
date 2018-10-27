/**
 * 
 */
package uk.co.alvagem.dbview.model;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Models a single column in a table of the database.
 * @author bruce.porteous
 *
 */
public class Column {

	private String columnName; // "COLUMN_NAME"
	private int dataType;		// "DATA_TYPE"
	private String typeName;	// "TYPE_NAME"
	private int columnSize;		// "COLUMN_SIZE"
	private int decimalDigits;	// "DECIMAL_DIGITS"
	private int nullable;		// "NULLABLE"
	private boolean isPK = false; // True iff primary key column.
	private short pkIndex = 0;		// Position of column in PK
	private String pkName = "";		// name of PK.
	/**
	 * 
	 */
	private Column() {
		super();
	}

	public static Column fromMeta(ResultSet rs) throws SQLException{
		Column c = new Column();
		c.columnName = rs.getString("COLUMN_NAME");
		c.dataType = rs.getInt("DATA_TYPE");
		c.typeName = rs.getString("TYPE_NAME");
		c.columnSize = rs.getInt("COLUMN_SIZE");
		c.decimalDigits = rs.getInt("DECIMAL_DIGITS");
		c.nullable = rs.getInt("NULLABLE");
		return c;
	}

	/**
	 * @return Returns the columnName.
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * @return Returns the columnSize.
	 */
	public int getColumnSize() {
		return columnSize;
	}

	/**
	 * @return Returns the dataType.
	 */
	public int getDataType() {
		return dataType;
	}

	/**
	 * @return Returns the decimalDigits.
	 */
	public int getDecimalDigits() {
		return decimalDigits;
	}

	/**
	 * @return Returns the nullable.
	 */
	public int getNullable() {
		return nullable;
	}

	/**
	 * Returns a text description of the columns "nullability".
	 * @return YES, NO or blank for "don't know".
	 */
	public String isNullable() {
		switch(nullable){
		case DatabaseMetaData.attributeNoNulls: return "NO";
		case DatabaseMetaData.attributeNullable: return "YES";
		default: return "";
		}
	}
	
	/**
	 * @return Returns the typeName.
	 */
	public String getTypeName() {
		return typeName;
	}
	
	
	/**
	 * @return the isPK
	 */
	public boolean isPK() {
		return isPK;
	}

	/**
	 * @return the pkIndex
	 */
	public short getPkIndex() {
		return pkIndex;
	}

	/**
	 * @return the pkName
	 */
	public String getPkName() {
		return pkName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return columnName;
	}

	/**
	 * Sets the column as part of the primary key.
	 * @param keySeq is the position of the column in the key.
	 * @param pkName is the name (if any) of the column.
	 */
	public void setPK(short keySeq, String pkName) {
		isPK = true;
		this.pkIndex = keySeq;
		this.pkName = (pkName == null) ? "" : pkName; 
	}
	
}
