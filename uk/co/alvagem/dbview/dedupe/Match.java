package uk.co.alvagem.dbview.dedupe;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;

/**
 * Describes match of records detected by de-duping.
 * @author bruce.porteous
 *
 */
public class Match {
	
	private Map<Integer,Float> matches;
	
	
	/**
	 * @param matches is the doc index -> match score map for a matched group.
	 */
	public Match(Map<Integer,Float> matches) {
		this.matches = matches;
	}


	
	/**
	 * @param reader
	 * @param reportFields
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	void displayMatch(PrintStream out, IndexReader reader, Collection<String> reportFields) throws CorruptIndexException, IOException {
		
		StringBuffer line = new StringBuffer();
		
		out.println();
		
		for(Map.Entry<Integer,Float> entry : matches.entrySet()){

			Document doc = reader.document(entry.getKey().intValue());
			
			line.delete(0,line.length());
			line.append('"');
			line.append(entry.getValue().floatValue());
			line.append('"');
			
			for(String field : reportFields){
				line.append(',');
				line.append('"');
				line.append( doc.get(field));
				line.append('"');
			}
			out.println(line.toString());
		}
	}

	static void printResultHeader(Collection<String>reportFields, PrintStream out){
		StringBuffer line = new StringBuffer();
		line.append("\"Score\"");
		for(String field : reportFields){
			line.append(',').append('"').append(field).append('"');
		}
		out.println(line.toString());
	}
	
}