/**
 * 
 */
package uk.co.alvagem.dbview;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import uk.co.alvagem.dbview.model.QueryProgressCallback;

/**
 * Table model for results retrieved from a ResultSet.  Note that this sets everything
 * as string.
 * @author bruce.porteous
 */
public class QueryTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int columnCount = 0;
	private String[] headings = new String[0];
	private ResultSetMetaData meta;
	
	private Vector<String[]> rows = new Vector<String[]>();
	private int rowsRead = 0;
	
	private LoaderThread loaderThread = null;
	
	public QueryTableModel(){
		
	}
	
	
	/**
	 * 
	 */
	public QueryTableModel(ResultSet rs, QueryProgressCallback callback) throws SQLException {
		super();
		setColumns(rs);
		setData(rs,callback);
	}
	
	/**
	 * @param rs
	 * @throws SQLException
	 */
	public void setColumns(ResultSet rs) throws SQLException{
		meta = rs.getMetaData();
		
		columnCount = meta.getColumnCount();
		headings = new String[columnCount];
		for(int i=0; i<columnCount; ++i){
			headings[i] = meta.getColumnLabel(i+1);
		}
	}

	/**
	 * @param rs
	 * @throws SQLException
	 */
	public void setData(ResultSet rs, QueryProgressCallback callback) throws SQLException{
		readDataThreaded(rs, callback);
	}
	
//	/**
//	 * @throws SQLException 
//	 * 
//	 */
//	private void readData(QueryProgressCallback callback) throws SQLException {
//		rowsRead = 0;
//		while(rs.next()){
//			Object[] row = new Object[columnCount];
//			for(int i=0; i<columnCount; ++i){
//				row[i] = rs.getObject(i+1);
//			}
//			synchronized(rows){
//				rows.add(row);
//			}
//			++rowsRead;
//		}
//		rs.close();
//		
//	}

	/**
	 * @throws SQLException 
	 * 
	 */
	private void readDataThreaded(ResultSet rs, QueryProgressCallback callback) throws SQLException {
		
		loaderThread = new LoaderThread(rs, callback);
		loaderThread.setName("Query Loader");
		loaderThread.start();
	}
		
	private class LoaderThread extends Thread {

		private ResultSet rs;
		private QueryProgressCallback callback;

		private Exception exception;
		private final static int CHUNK = 10;
		
		public LoaderThread(ResultSet rs ,QueryProgressCallback callback){
			this.rs = rs;
			this.callback = callback;
		}
		
		public void run() {
			rowsRead = 0;
			int firstRow = 0;
			try {
				
				ResultSetMetaData meta = rs.getMetaData();
				
				while(rs.next()){
					String[] row = new String[columnCount];
					for(int i=0; i<columnCount; ++i){
						if(meta.getColumnType(i+1) == Types.CLOB){
							Clob clob = rs.getClob(i+1);
							row[i] = ClobToString(clob);
						} else {
							//row[i] = rs.getObject(i+1);
							row[i] = rs.getString(i+1);
						}
					}
					synchronized(rows){
						rows.add(row);
					}
					++rowsRead;
					callback.rowsRead(rowsRead);
					
					if(rowsRead % CHUNK == 0){
						fireTableRowsInserted(firstRow, rowsRead-1);
						firstRow = rowsRead;
					}
				}
				rs.close();
			} catch (SQLException e) {
				exception = e;
			} catch (IOException e) {
				exception = e;
			}
			callback.rowsRead(rowsRead);
			callback.complete(exception);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	public String getColumnName(int col) {
		return headings[col];
	}


	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		//System.out.println("Table has " + rowsRead + " rows");
		return rowsRead;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		//System.out.println("Table has " + columnCount + " columns");
		return columnCount;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public String getValueAt(int rowIndex, int columnIndex) {
		String[] row;
		synchronized(rows){
			row = rows.get(rowIndex);
		}
		//System.out.println("Value at " + rowIndex + "," + columnIndex + " is " + row[columnIndex]);
		return row[columnIndex];
	}

	private String ClobToString(Clob cl) throws IOException, SQLException   {
      if (cl == null) { 
        return  "";
      }
          
      StringBuffer strOut = new StringBuffer();
            
      BufferedReader br = new BufferedReader(cl.getCharacterStream());

      String aux;
      while ((aux=br.readLine())!=null)
             strOut.append(aux);

      return strOut.toString();
    }
}
