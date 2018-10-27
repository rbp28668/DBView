/**
 * 
 */
package uk.co.alvagem.dbview;

import java.sql.SQLException;

import uk.co.alvagem.dbview.model.Table;

/**
 * Class to display the properties of a table as a grid of columns.
 * @author bruce.porteous
  */
public class TablePropertiesFrame extends TableFrame {
	private static final long serialVersionUID = 1L;

	/**
	 * @throws SQLException 
	 * 
	 */
	public TablePropertiesFrame(Table table) throws SQLException {
		super(new TableTableModel(table), table.getParent().getName() + "." + table.getTableName());
	}
	


}
