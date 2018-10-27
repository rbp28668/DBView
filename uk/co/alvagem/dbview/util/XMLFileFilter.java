/**
 * 
 */
package uk.co.alvagem.dbview.util;


/**
 * XMLFileFilter provides a simple file filter that only accepts files
 * with the suffix xml.
 * @author Bruce.Porteous
 *
 */
public class XMLFileFilter extends BasicFileFilter {
    /**
	 * Constructor for the filter.
	 */
	public XMLFileFilter() {
        super(".xml", "XML Files");
    }
}        
