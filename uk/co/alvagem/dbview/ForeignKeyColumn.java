/**
 * 
 */
package uk.co.alvagem.dbview;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Describes (part of) a foreign key relationship.
 * @author bruce.porteous
 *
 */
public class ForeignKeyColumn {
	private boolean isImported;
	
	private String pkName;
	private String fkName;
	
	private String pkTableCat;
	private String pkTableSchem;
	private String pkTableName;
	private String pkColumnName;
	
	private String fkTableCat;
	private String fkTableSchem;
	private String fkTableName;
	private String fkColumnName;
	
	private int keySeq;
	private short updateRule;
	private short deleteRule;
	
	private short deferability;
	
	
	public static ForeignKeyColumn fromMeta(boolean isImported, ResultSet rs) throws SQLException{
		ForeignKeyColumn fk = new ForeignKeyColumn();
		fk.isImported = isImported;
		fk.pkTableCat = rs.getString("PKTABLE_CAT"); 		// String => primary key table catalog being imported (may be null)
		fk.pkTableSchem = rs.getString("PKTABLE_SCHEM"); 	//  String => primary key table schema being imported (may be null)
		fk.pkTableName = rs.getString("PKTABLE_NAME"); 		//  String => primary key table name being imported
		fk.pkColumnName = rs.getString("PKCOLUMN_NAME"); 	//  String => primary key column name being imported
		fk.fkTableCat = rs.getString("FKTABLE_CAT"); 		//  String => foreign key table catalog (may be null)
		fk.fkTableSchem = rs.getString("FKTABLE_SCHEM"); 	//  String => foreign key table schema (may be null)
		fk.fkTableName = rs.getString("FKTABLE_NAME"); 		//  String => foreign key table name
		fk.fkColumnName = rs.getString("FKCOLUMN_NAME"); 	//  String => foreign key column name
		fk.keySeq = rs.getShort("KEY_SEQ"); 				// short => sequence number within a foreign key
		fk.updateRule = rs.getShort("UPDATE_RULE"); 		// short => What happens to a foreign key when the primary key is updated:
															//		* importedNoAction - do not allow update of primary key if it has been imported
															//		* importedKeyCascade - change imported key to agree with primary key update
															//		* importedKeySetNull - change imported key to NULL if its primary key has been updated
															//		* importedKeySetDefault - change imported key to default values if its primary key has been updated
															//		* importedKeyRestrict - same as importedKeyNoAction (for ODBC 2.x compatibility) 
		fk.deleteRule = rs.getShort("DELETE_RULE"); 		// short => What happens to the foreign key when primary is deleted.
															//		* importedKeyNoAction - do not allow delete of primary key if it has been imported
															//		* importedKeyCascade - delete rows that import a deleted key
															//		* importedKeySetNull - change imported key to NULL if its primary key has been deleted
															//		* importedKeyRestrict - same as importedKeyNoAction (for ODBC 2.x compatibility)
															//		* importedKeySetDefault - change imported key to default if its primary key has been deleted 
		fk.fkName = rs.getString("FK_NAME"); 				// String => foreign key name (may be null)
		fk.pkName = rs.getString("PK_NAME"); 				// String => primary key name (may be null)
		fk.deferability = rs.getShort("DEFERRABILITY"); 	// short => can the evaluation of foreign key constraints be deferred until commit
															//		* importedKeyInitiallyDeferred - see SQL92 for definition
															//		* importedKeyInitiallyImmediate - see SQL92 for definition
															//		* importedKeyNotDeferrable - see SQL92 for definition 

		
		return fk;
	}


	/**
	 * @return the isImported
	 */
	public boolean isImported() {
		return isImported;
	}


	/**
	 * @return the pkName
	 */
	public String getPkName() {
		return pkName;
	}


	/**
	 * @return the fkName
	 */
	public String getFkName() {
		return fkName;
	}


	/**
	 * @return the pkTableCat
	 */
	public String getPkTableCat() {
		return pkTableCat;
	}


	/**
	 * @return the pkTableSchem
	 */
	public String getPkTableSchem() {
		return pkTableSchem;
	}


	/**
	 * @return the pkTableName
	 */
	public String getPkTableName() {
		return pkTableName;
	}


	/**
	 * @return the pkColumnName
	 */
	public String getPkColumnName() {
		return pkColumnName;
	}


	/**
	 * @return the fkTableCat
	 */
	public String getFkTableCat() {
		return fkTableCat;
	}


	/**
	 * @return the fkTableSchem
	 */
	public String getFkTableSchem() {
		return fkTableSchem;
	}


	/**
	 * @return the fkTableName
	 */
	public String getFkTableName() {
		return fkTableName;
	}


	/**
	 * @return the fkColumnName
	 */
	public String getFkColumnName() {
		return fkColumnName;
	}


	/**
	 * @return the keySeq
	 */
	public int getKeySeq() {
		return keySeq;
	}


	/**
	 * @return the updateRule
	 */
	public short getUpdateRule() {
		return updateRule;
	}

	public String getUpdateRuleDesc(){
		return ruleDesc(updateRule);
	}
	
	/**
	 * @return the deleteRule
	 */
	public short getDeleteRule() {
		return deleteRule;
	}

	public String getDeleteRuleDesc(){
		return ruleDesc(deleteRule);
	}

	private String ruleDesc(short rule){
		switch(rule){
		case DatabaseMetaData.importedKeyNoAction: return "No Action";
		case DatabaseMetaData.importedKeyCascade: return "Cascade";
		case DatabaseMetaData.importedKeySetNull: return "Set NULL";
		case DatabaseMetaData.importedKeySetDefault: return "Set Default";
		case DatabaseMetaData.importedKeyRestrict: return "Restrict";
		default: return "???";
		}
	}
	
	/**
	 * @return the deferability
	 */
	public short getDeferability() {
		return deferability;
	}
		
	public String getDeferabilityDesc(){
		switch(deferability){
		case DatabaseMetaData.importedKeyInitiallyDeferred: return "Initially Deferred";
		case DatabaseMetaData.importedKeyInitiallyImmediate: return "Initially Immediate";
		case DatabaseMetaData.importedKeyNotDeferrable: return "Not Deferrable";
		default: return "???";
		}
	}
}
