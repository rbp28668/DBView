/**
 * 
 */
package uk.co.alvagem.dbview;

import java.sql.SQLException;

import uk.co.alvagem.dbview.model.Table;

/**
 * Class to display the foreign keys for a table as a Swing table.
 * @author bruce.porteous
  */
public class ForeignKeyFrame extends TableFrame {
	private static final long serialVersionUID = 1L;

	/**
	 * @throws SQLException 
	 * 
	 */
	public ForeignKeyFrame(Table table) throws SQLException {
		super(new ForeignKeyModel(table), "Foreign Keys for " + table.getParent().getName() + "." + table.getTableName() );
	}
	


}
