/**
 * 
 */
package uk.co.alvagem.dbview.model;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import uk.co.alvagem.dbview.util.XMLWriter;

/**
 * Collection of SearchIndex associated with a database.
 * @author bruce.porteous
 *
 */
public class SearchIndices {

	private List<SearchIndex> searchIndices = new LinkedList<SearchIndex>();
	/**
	 * 
	 */
	public SearchIndices() {
		super();
	}
	
	public void addSearchIndex(SearchIndex searchIndex){
		searchIndices.add(searchIndex);
	}

	public void removeSearchIndex(SearchIndex searchIndex){
		searchIndices.remove(searchIndex);
	}

	public Collection<SearchIndex> getSearchIndices(){
		return Collections.unmodifiableCollection(searchIndices);
	}
	
	public void writeXML(XMLWriter writer) throws IOException {
		writer.startEntity("SearchIndices");
		for(SearchIndex searchIndex : searchIndices){
			searchIndex.writeXML(writer);
		}
		writer.stopEntity();
	}

	/**
	 * @return
	 */
	public boolean isEmpty() {
		return searchIndices.isEmpty();
	}
	
	
}
