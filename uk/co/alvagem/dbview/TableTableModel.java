/**
 * 
 */
package uk.co.alvagem.dbview;

import java.sql.SQLException;
import java.util.Collection;

import javax.swing.table.AbstractTableModel;

import uk.co.alvagem.dbview.model.Column;
import uk.co.alvagem.dbview.model.Table;

/**
 * Displays the properties of columns of a Table as a TableModel.
 * @author bruce.porteous
 *
 */
public class TableTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	private String[] headings = {"Name", "Type", "Length", "Digits", "Nulls?", "PK?", "PK Seq", "PK Name"};
	private int rowCount = 0;
	private Column[] rows;
	
	/**
	 * 
	 */
	public TableTableModel(Table table) throws SQLException {
		super();
		
		Collection<Column> columns = table.getColumns();
		int count = columns.size();
		rows = columns.toArray(new Column[count]);
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
		Column column = rows[row];
		switch (col) {
		case 0:	return column.getColumnName();
		case 1: return column.getTypeName();
		case 2: return Integer.toString(column.getColumnSize());
		case 3: return Integer.toString(column.getDecimalDigits());
		case 4: return column.isNullable();
		case 5: return column.isPK() ? "Y" : "";
		case 6: return column.isPK() ? Short.toString(column.getPkIndex()) : "";
		case 7: return column.getPkName();  // blank if not PK anyway.
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
