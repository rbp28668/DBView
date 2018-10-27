/**
 * 
 */
package uk.co.alvagem.dbview;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Describes a column in an index.
 * @author bruce.porteous
 *
 */
public class IndexColumn {
	
	private boolean isNonUnique;
	private String indexCatalog;
	private String indexName;
	private short type;
	private short ordinal;
	private String columnName;
	private String order;
	private int cardinality;
	private int pages;
	private String filterCondition;
	

	private IndexColumn(){
		
	}
	
	public static IndexColumn fromMeta(ResultSet rs) throws SQLException{
		IndexColumn ic = new IndexColumn();
	    ic.isNonUnique = rs.getBoolean("NON_UNIQUE"); 		// boolean => Can index values be non-unique. false when TYPE is tableIndexStatistic
	    ic.indexCatalog = rs.getString("INDEX_QUALIFIER"); 	// String => index catalog (may be null); null when TYPE is tableIndexStatistic
	    ic.indexName = rs.getString("INDEX_NAME"); 			// String => index name; null when TYPE is tableIndexStatistic
	    ic.type = rs.getShort("TYPE"); 						// short => index type:
	              											//* tableIndexStatistic - this identifies table statistics that are returned in conjuction with a table's index descriptions
	              											//* tableIndexClustered - this is a clustered index
	              											//* tableIndexHashed - this is a hashed index
	              											//* tableIndexOther - this is some other style of index 
	    ic.ordinal = rs.getShort("ORDINAL_POSITION"); 		// short => column sequence number within index; zero when TYPE is tableIndexStatistic
	    ic.columnName = rs.getString("COLUMN_NAME"); 		// String => column name; null when TYPE is tableIndexStatistic
	    ic.order = rs.getString("ASC_OR_DESC"); 			// String => column sort sequence, "A" => ascending, "D" => descending, may be null if sort sequence is not supported; null when TYPE is tableIndexStatistic
	    ic.cardinality = rs.getInt("CARDINALITY"); 			// int => When TYPE is tableIndexStatistic, then this is the number of rows in the table; otherwise, it is the number of unique values in the index.
	    ic.pages = rs.getInt("PAGES");						// int => When TYPE is tableIndexStatisic then this is the number of pages used for the table, otherwise it is the number of pages used for the current index.
	    ic.filterCondition = rs.getString("FILTER_CONDITION"); // String => Filter condition, if any. (may be null) 
		
		return ic;
	}

	/**
	 * @return the isNonUnique
	 */
	public boolean isNonUnique() {
		return isNonUnique;
	}

	/**
	 * @return the indexCatalog
	 */
	public String getIndexCatalog() {
		return indexCatalog;
	}

	/**
	 * @return the indexName
	 */
	public String getIndexName() {
		return indexName;
	}

	/**
	 * @return the type
	 */
	public short getType() {
		return type;
	}

	/**
	 * Decodes the type of the index to a string.
	 * @return
	 */
	public Object getTypeName() {
		switch(type) {
		case DatabaseMetaData.tableIndexStatistic: return "Statistic";
		case DatabaseMetaData.tableIndexClustered: return "Clustered";
		case DatabaseMetaData.tableIndexHashed: return "Hashed";
		case DatabaseMetaData.tableIndexOther: return "Other";
		default: return "";
		}
	}

	/**
	 * @return the ordinal
	 */
	public short getOrdinal() {
		return ordinal;
	}

	/**
	 * @return the columnName
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * @return the order
	 */
	public String getOrder() {
		return order;
	}

	/**
	 * @return the cardinality
	 */
	public int getCardinality() {
		return cardinality;
	}

	/**
	 * @return the pages
	 */
	public int getPages() {
		return pages;
	}

	/**
	 * @return the filterCondition
	 */
	public String getFilterCondition() {
		if(filterCondition == null){
			return "";
		}
		return filterCondition;
	}


	
}
