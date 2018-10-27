/**
 * 
 */
package uk.co.alvagem.dbview;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * Provides a table model where the rows are a subset of rows of a parent table model.
 * Used to display the results of a search.
 * @author bruce.porteous
 *
 */
public class SubsetTableModel extends AbstractTableModel {

	/** Serialisation version	 */
	private static final long serialVersionUID = 1L;
	
	/** Parent table model - holds the real data */
	private TableModel parentModel;
	
	/** Identifies which rows in the parent table model to include in this one */
	private Vector<Integer> rowIndices;
	
	/**
	 * 
	 */
	public SubsetTableModel(TableModel parentModel, Vector<Integer> rowIndices) {
		super();
		this.parentModel = parentModel;
		this.rowIndices = rowIndices;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return rowIndices.size();
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return parentModel.getColumnCount();
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int colIndex) {
		Integer row = rowIndices.elementAt(rowIndex);
		return parentModel.getValueAt(row.intValue(),colIndex);
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int arg0) {
		return parentModel.getColumnName(arg0);
	}
	
	

}
