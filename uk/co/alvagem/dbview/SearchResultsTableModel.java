/**
 * 
 */
package uk.co.alvagem.dbview;

import java.io.IOException;
import java.util.Collection;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Searcher;

import uk.co.alvagem.dbview.model.SearchEngine;

/**
 * Provides a table model where the rows lucene Documents returned from a search.
 * @author bruce.porteous
 *
 */
public class SearchResultsTableModel extends AbstractTableModel {

	/** Serialisation version	 */
	private static final long serialVersionUID = 1L;
	
	private Vector<String> headers;
	
	private Vector<Document> rows;
 
	/**
	 * @throws IOException 
	 * @throws CorruptIndexException 
	 * @throws ParseException 
	 * 
	 */
	public SearchResultsTableModel(SearchEngine search, String queryString) throws CorruptIndexException, IOException, ParseException {
		super();
		
		IndexReader reader = search.getIndexReader();
		Collection fieldNames = reader.getFieldNames(IndexReader.FieldOption.ALL);
		headers = new Vector<String>(fieldNames);
		
		org.apache.lucene.search.Query query = search.parse(queryString);
		Searcher searcher = search.getSearcher();
		
		Hits hits = searcher.search(query);
		rows = new Vector<Document>(hits.length());
		for(int i=0; i<hits.length(); ++i){
			rows.add(hits.doc(i));
		}
		searcher.close();
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return rows.size();
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return headers.size();
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int colIndex) {
		Document doc = rows.elementAt(rowIndex);
		String fieldName = headers.elementAt(colIndex);
		return doc.get(fieldName);
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int arg0) {
		return headers.elementAt(arg0);
	}
	
	

}
