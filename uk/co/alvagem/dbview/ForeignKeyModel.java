/**
 * 
 */
package uk.co.alvagem.dbview;

import java.sql.SQLException;
import java.util.Collection;

import javax.swing.table.AbstractTableModel;

import uk.co.alvagem.dbview.model.Table;

/**
 * Displays the foreign keys of a Table as a TableModel.
 * @author bruce.porteous
 *
 */
public class ForeignKeyModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	private String[] headings = {"PK-Table", "PK-Column", "FK-Table", "FK-Column", 
			"PK-Name", "FK-Name", "Key Seq", 
			"Update Rule", "Delete Rule","Deferability"};
	private int rowCount = 0;
	private ForeignKeyColumn[] rows;
	
	/**
	 * Initialises the table model with the index columns from the given table.
	 * @param table is the table to initialise from.
	 */
	public ForeignKeyModel(Table table) throws SQLException {
		super();
		
		Collection<ForeignKeyColumn> indices = table.getFKColumns();
		int count = indices.size();
		rows = indices.toArray(new ForeignKeyColumn[count]);
		rowCount = rows.length;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return rowCount;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return headings.length;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col) {
		ForeignKeyColumn fk = rows[row];
		switch (col) {
		case 0:	return fk.getPkTableName();
		case 1: return fk.getPkColumnName();
		case 2: return fk.getFkTableName();
		case 3: return fk.getFkColumnName();
		case 4: return fk.getPkName();
		case 5: return fk.getFkName();
		case 6: return Integer.toString(fk.getKeySeq());
		case 7: return fk.getUpdateRuleDesc();
		case 8: return fk.getDeleteRuleDesc();
		case 9: return fk.getDeferabilityDesc();
		default: return null;
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {
		return headings[column];
	}

}
