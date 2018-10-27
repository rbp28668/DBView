/**
 * 
 */
package uk.co.alvagem.dbview;

import java.sql.SQLException;
import java.util.Collection;

import javax.swing.table.AbstractTableModel;

import uk.co.alvagem.dbview.model.Table;

/**
 * Displays the properties of columns of a Table as a TableModel.
 * @author bruce.porteous
 *
 */
public class TableIndicesModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	private String[] headings = {"Name", "Type", "Unique?", "Column", "Ordinal", "Ordering", "Cardinality", "Pages", "Filter"};
	private int rowCount = 0;
	private IndexColumn[] rows;
	
	/**
	 * Initialises the table model with the index columns from the given table.
	 * @param table is the table to initialise from.
	 */
	public TableIndicesModel(Table table) throws SQLException {
		super();
		
		Collection<IndexColumn> indices = table.getIndexColumns();
		int count = indices.size();
		rows = indices.toArray(new IndexColumn[count]);
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
		IndexColumn index = rows[row];
		switch (col) {
		case 0:	return index.getIndexName();
		case 1: return index.getTypeName();
		case 2: return index.isNonUnique() ? "N" : "Y"; // note inversion of not-unique.
		case 3: return index.getColumnName();
		case 4: return Short.toString(index.getOrdinal());
		case 5: return index.getOrder();
		case 6: return Integer.toString(index.getCardinality());
		case 7: return Integer.toString(index.getPages());
		case 8: return index.getFilterCondition();
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
