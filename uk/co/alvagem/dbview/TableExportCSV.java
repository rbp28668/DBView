/**
 * 
 */
package uk.co.alvagem.dbview;

import java.io.IOException;
import java.io.Writer;

import javax.swing.table.TableModel;

/**
 * Exports a table model in CSV format.
 * @author bruce.porteous
 *
 */
public class TableExportCSV {

	private boolean emitNulls = true;  // nulls as empty strings.
	private boolean emitHeaderRow = false; 
	/**
	 * 
	 */
	public TableExportCSV() {
		super();
	}

	/**
	 * Exports the table model to the given writer.
	 * @param writer
	 * @param table
	 * @throws IOException
	 */
	public void emitTable(Writer writer,  TableModel table) throws IOException{
		
		int columns = table.getColumnCount();
		
		if(emitHeaderRow){
			String[] headers = new String[columns];
			for(int i=0; i<columns; ++i){
				headers[i] = toTag(table.getColumnName(i));
			}
			emitRow(writer,headers);
		}
		
		Object[] row = new Object[columns];
		
		for(int iRow = 0; iRow < table.getRowCount(); ++iRow){
			for(int i=0; i<columns; ++i){
				row[i] = table.getValueAt(iRow,i);
			}
			emitRow(writer,row);
			
		}
	}
	
	/**
	 * Takes a table column name and converts it into a form suitable for an XML tag.
	 * @param columnName
	 * @return
	 */
	private String toTag(String columnName) {
		StringBuffer tag = new StringBuffer();
		boolean firstLetter = true;
		for(int i=0; i<columnName.length(); ++i){
			char ch = columnName.charAt(i);
			if(Character.isLetterOrDigit(ch)){
				if(firstLetter){
					ch = Character.toUpperCase(ch);
					firstLetter = false;
				} else {
					ch = Character.toLowerCase(ch);
				}
				tag.append(ch);
			} else {
				// Ignore this character but...
				firstLetter = true; // reset to produce CamelCase
			}
		}
		return tag.toString();
	}

	private void emitRow(Writer writer, Object[] row) throws IOException{
        for(int i=0; i<row.length; ++i){
        	if(i > 0){
        		writer.write(",");
        	}
        	Object o = row[i];
        	if(o != null || emitNulls){
        		emitCell(writer,o);
        	}
        }
        writer.write("\n");
		
	}
	
	private void emitCell(Writer writer,Object o) throws IOException{
		writer.write("\"");
        if(o != null) {
	        String value = o.toString();
	        writer.write(value);
        }
		writer.write("\"");

	}

	/**
	 * @return Returns the emitHeaderRow.
	 */
	public boolean isEmitHeaderRow() {
		return emitHeaderRow;
	}

	/**
	 * @param emitHeaderRow The emitHeaderRow to set.
	 */
	public void setEmitHeaderRow(boolean emitHeaderRow) {
		this.emitHeaderRow = emitHeaderRow;
	}

	/**
	 * @return Returns the emitNulls.
	 */
	public boolean isEmitNulls() {
		return emitNulls;
	}

	/**
	 * @param emitNulls The emitNulls to set.
	 */
	public void setEmitNulls(boolean emitNulls) {
		this.emitNulls = emitNulls;
	}

}
