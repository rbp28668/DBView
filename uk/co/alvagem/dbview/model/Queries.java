/**
 * 
 */
package uk.co.alvagem.dbview.model;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import uk.co.alvagem.dbview.util.XMLWriter;

/**
 * A collection of query.
 * @author bruce.porteous
 *
 */
public class Queries {

	private List<Query> queries = new LinkedList<Query>();
	/**
	 * 
	 */
	public Queries() {
		super();
	}
	
	public void addQuery(Query q){
		queries.add(q);
	}

	public void removeQuery(Query q){
		queries.remove(q);
	}

	public Collection<Query> getQueries(){
		return Collections.unmodifiableCollection(queries);
	}
	
	public void writeXML(XMLWriter writer) throws IOException {
		writer.startEntity("Queries");
		for(Iterator<Query> iter = queries.iterator(); iter.hasNext();){
			Query query = iter.next();
			query.writeXML(writer);
		}
		writer.stopEntity();
	}

	/**
	 * @return
	 */
	public boolean isEmpty() {
		return queries.isEmpty();
	}
	
	
}
