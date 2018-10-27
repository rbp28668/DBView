/**
 * 
 */
package uk.co.alvagem.dbview;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Properties;

import javax.swing.table.TableModel;
import javax.xml.transform.OutputKeys;

import org.apache.xml.serializer.Method;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author bruce.porteous
 *
 */
public class TableExportXML  {
    
    /** an empty attribute for use with SAX */
    private static final Attributes EMPTY_ATTR = new AttributesImpl();
    
    public final static String ROOT_TAG = "Export";
    public final static String ROW_TAG = "Row";
    private boolean emitNulls = false;
    private String rootTag = ROOT_TAG;
    private String rowTag = ROW_TAG;

    /**
	 * 
	 */
	public TableExportXML() {
		super();
	}

	/**
	 * @param writer
	 * @param table
	 * @throws IOException
	 */
	public void emitTable(Writer writer,TableModel table) throws IOException {
        Properties props = OutputPropertiesFactory.getDefaultMethodProperties(Method.XML);
        Serializer serializer = SerializerFactory.getSerializer(props);
        serializer.setWriter(writer);
        ContentHandler handler = serializer.asContentHandler();
        try {
			emitTable(handler, table);
		} catch (SAXException e) {
			throw new IOException(e.getMessage());
		}
    }
	
    /**
     * @param os
     * @param table
     * @throws IOException
     */
    public void emitTable(OutputStream os,TableModel table) throws IOException {
        
        Properties props = OutputPropertiesFactory.getDefaultMethodProperties(Method.XML);
        props.setProperty(OutputKeys.INDENT,"yes");
        Serializer serializer = SerializerFactory.getSerializer(props);
        serializer.setOutputStream(os);
        ContentHandler handler = serializer.asContentHandler();
        try {
			emitTable(handler, table);
		} catch (SAXException e) {
			throw new IOException(e.getMessage());
		}
    }

	/**
	 * @param ch
	 * @param table
	 * @throws SAXException
	 */
	public void emitTable(ContentHandler ch,  TableModel table) throws SAXException{
		
		int columns = table.getColumnCount();
		
		String[] headers = new String[columns];
		for(int i=0; i<columns; ++i){
			headers[i] = toTag(table.getColumnName(i));
		}
		
		Object[] row = new Object[columns];
		ch.startDocument();
        ch.startElement("","",rootTag,EMPTY_ATTR);

		for(int iRow = 0; iRow < table.getRowCount(); ++iRow){
			for(int i=0; i<columns; ++i){
				row[i] = table.getValueAt(iRow,i);
			}
			emitRow(ch,row,headers);
			
		}
        ch.endElement("","",rootTag);
		ch.endDocument();
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

	private void emitRow(ContentHandler ch, Object[] row, String[] headers) throws SAXException{
        ch.startElement("","",rowTag,EMPTY_ATTR);
        for(int i=0; i<row.length; ++i){
        	Object o = row[i];
        	if(o != null || emitNulls){
        		emitCell(ch,headers[i],o);
        	}
        }
        ch.endElement("","",rowTag);
		
	}
	
	private void emitCell(ContentHandler ch, String tag, Object o) throws SAXException{
        ch.startElement("","",tag,EMPTY_ATTR);
        if(o != null) {
	        String value = o.toString();
	        ch.characters(value.toCharArray(), 0, value.length( ));
        }
        ch.endElement("","",tag);

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

	/**
	 * @return Returns the rootTag.
	 */
	public String getRootTag() {
		return rootTag;
	}

	/**
	 * @param rootTag The rootTag to set.
	 */
	public void setRootTag(String rootTag) {
		this.rootTag = rootTag;
	}

	/**
	 * @return Returns the rowTag.
	 */
	public String getRowTag() {
		return rowTag;
	}

	/**
	 * @param rowTag The rowTag to set.
	 */
	public void setRowTag(String rowTag) {
		this.rowTag = rowTag;
	}

}
