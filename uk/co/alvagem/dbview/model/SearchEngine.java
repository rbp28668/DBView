/*
 * SearchEngine.java
 * Created on 21-Jun-2004
 * By Bruce.Porteous
 *
 */
package uk.co.alvagem.dbview.model;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.table.TableModel;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;


/**
 * SearchEngine is a wrapper round Lucene code to provide full-text search
 * facilities.
 * Note - now uses a custom analyser to include stemming.
 * @author Bruce.Porteous
 *
 */
public class SearchEngine {
	
	private Directory directory;
	private Analyzer analyzer;
	private final static String ROW_INDEX = "$ROWINDEX";
	private final static String ALL = "$ALL";
	private LoaderThread loaderThread = null;
	
	/** Allows rows to be excluded from an index */
	private transient RecordExclusionTemplate exclude = new RecordExclusionTemplate();
	
	
	/**
	 * 
	 */
	public SearchEngine() {
		super();
		analyzer = new StemmingAnalyser();		
	}
	
	public SearchEngine(String path) throws IOException {
		super();
		analyzer = new StemmingAnalyser();	
		directory = FSDirectory.getDirectory(path);
	}

	public Analyzer getAnalyzer(){
		return analyzer;
	}
	
	public void indexTable(TableModel model) throws IOException{
		indexTable(model, new RAMDirectory());

	}

	public void indexTable(TableModel model, String path) throws IOException{
		indexTable(model, FSDirectory.getDirectory(path));
	}

	/**
	 * Indexes a TableModel into the given directory.
	 * @param model is the table model to index.
	 * @param directory is the Lucene directory to create the index in.
	 * @throws IOException
	 */
	private void indexTable(TableModel model, Directory directory) throws IOException{
		if(model == null){
			throw new NullPointerException("Can't index a null table model");
		}

		this.directory = directory;
		
		IndexWriter index = new IndexWriter(directory, analyzer, true);
		int rows = model.getRowCount();
		for(int i=0; i<rows; ++i){
			Document doc = rowToDoc(model,i);
			if(exclude.accept(doc)){
			    index.addDocument(doc);
			}
		}
		index.optimize();
		index.close();
	}

	/**
	 * Takes a row of a TableModel and indexes it as a Lucene Document.  Non-null fields are tokenized
	 * stored and indexed.  The concatenation of all fields is tokenized and indexed.
	 * @param model is the source table model.
	 * @param rowIndex is the index of the row in the table model to process.
	 * @return Document containing the contents of this row.
	 */
	private Document rowToDoc(TableModel model, int rowIndex){
		Document doc = new Document();
		doc.add(new Field(ROW_INDEX,Integer.toString(rowIndex),Field.Store.YES, Field.Index.NO));
		StringBuffer all = new StringBuffer();
		int columns = model.getColumnCount();
		for(int i=0; i<columns; ++i ){
			Object o = model.getValueAt(rowIndex,i);
			if(o != null) {
				String value = o.toString();
				doc.add(new Field(model.getColumnName(i),value,Field.Store.YES, Field.Index.TOKENIZED));
				all.append(' ');
				all.append(value);
			}
		}
		doc.add(new Field(ALL,all.toString(),Field.Store.NO, Field.Index.TOKENIZED));
		
		return doc;
	}
	
	
	/**
	 * Indexes the results of a SQL query into a results set into the current directory
	 * Note that the data is stored as well as indexed which allows off-line searching.
	 * @param rs is the ResultSet to index.
	 * @param path is the path to the folder where the data is stored.
	 * @param callback is used to show the progress of indexing.
	 * @throws IOException
	 * @throws SQLException
	 */
	public void indexResults(ResultSet rs, QueryProgressCallback callback) throws IOException, SQLException{
		if(rs == null){
			throw new NullPointerException("Can't index a null ResultSet");
		}

		
		loaderThread = new LoaderThread(rs, callback);
		loaderThread.setName("Query Indexer");
		loaderThread.start();
	}

	
	public Searcher getSearcher() throws IOException{
        Searcher searcher = new IndexSearcher(directory);
        return searcher;
    }

	public Query parse(String queryString) throws IOException, ParseException{
		if(queryString == null){
			throw new NullPointerException("Null query in search");
		}
		
		QueryParser parser = new QueryParser(ALL,analyzer);	
		Query query = parser.parse(queryString);

		return query;
    }

	public IndexReader getIndexReader() throws CorruptIndexException, IOException{
		return IndexReader.open(directory);
	}

	/**
	 * @param queryString
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	public Vector<Integer> searchForRows(String queryString) throws IOException, ParseException{
		if(queryString == null){
			throw new NullPointerException("Null query in search");
		}
		
		QueryParser parser = new QueryParser(ALL,analyzer);	
		Query query = parser.parse(queryString);
		
		Vector<Integer> results = runQuery(query);
		
		return results;
	}

	
	/**
     * @param query
     * @return
     * @throws IOException
     */
    private Vector<Integer> runQuery(Query query) throws IOException {
        Searcher searcher = new IndexSearcher(directory);
		Hits hits = searcher.search(query);
		
		Vector<Integer> results = new Vector<Integer>();
		int nResults = hits.length();
		for(int i=0; i<nResults; ++i){
			Document doc = hits.doc(i);
			Field indexField = doc.getField(ROW_INDEX);
			results.add(Integer.valueOf(indexField.stringValue()));
		}

		searcher.close();
        return results;
    }



    /**
     * @param e
     * @throws IOException
     */
    public void addRow(TableModel model, int rowIndex) throws IOException {
        IndexWriter index = new IndexWriter(directory, analyzer, false);
        Document doc = rowToDoc(model,rowIndex);
        index.addDocument(doc);
        index.optimize();
        index.close();
    }

	/**
	 * LoaderThread runs the indexing job asynchronously.
	 * @author bruce.porteous
	 *
	 */
	private class LoaderThread extends Thread {

		private ResultSet rs;
		private QueryProgressCallback callback;

		private Exception exception;
		
		public LoaderThread(ResultSet rs ,QueryProgressCallback callback){
			this.rs = rs;
			this.callback = callback;
		}
		
		public void run() {
		    
		    // Make copy in case user decides to edit during a long run.
		    RecordExclusionTemplate exclude = SearchEngine.this.exclude.copy();
		    
			int rowIndex = 0;
			try {
				
				ResultSetMetaData meta = rs.getMetaData();
				IndexWriter index = new IndexWriter(directory, analyzer, true);
				while(rs.next()){
					Document doc = rowToDoc(rs, rowIndex, meta);
					if(exclude.accept(doc)){
					    index.addDocument(doc);
					}
					++rowIndex;
					if(callback != null) {
						callback.rowsRead(rowIndex);
					}
				}
				index.optimize();
				index.close();
				
				rs.close();
			} catch (SQLException e) {
				exception = e;
			} catch (IOException e) {
				exception = e;
			}
			
			if(callback != null) {
				callback.rowsRead(rowIndex);
				callback.complete(exception);
			}
		}
		
		/**
		 * Converts a single row of the ResultSet into a Lucene Document that can be stored in an index.
		 * @param rs
		 * @param rowIndex
		 * @param meta
		 * @return
		 * @throws SQLException
		 */
		private Document rowToDoc(ResultSet rs, int rowIndex, ResultSetMetaData meta) throws SQLException{
			Document doc = new Document();
			doc.add(new Field(ROW_INDEX,Integer.toString(rowIndex),Field.Store.YES, Field.Index.NO));
			StringBuffer all = new StringBuffer();
			int columns = meta.getColumnCount();
			for(int i=1; i<=columns; ++i ){
				Object o = rs.getObject(i);
				if(o != null) {
					String value = o.toString();
					doc.add(new Field(meta.getColumnName(i),value,Field.Store.YES, Field.Index.TOKENIZED));
					all.append(' ');
					all.append(value);
				}
			}
			doc.add(new Field(ALL,all.toString(),Field.Store.NO, Field.Index.TOKENIZED));
			
			return doc;
		}

	}


}
