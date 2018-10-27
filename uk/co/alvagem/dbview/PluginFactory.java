/**
 * 
 */
package uk.co.alvagem.dbview;

import org.xml.sax.Attributes;

import uk.co.alvagem.dbview.util.FactoryBase;
import uk.co.alvagem.dbview.util.IXMLContentHandler;
import uk.co.alvagem.dbview.util.InputException;

/**
 * Loads plugins.
 * @author bruce.porteous
 */
public class PluginFactory extends FactoryBase implements IXMLContentHandler {

	private PluginManager manager;
	private StringBuffer text = new StringBuffer();
	/**
	 * 
	 */
	public PluginFactory(PluginManager manager) {
		super();
		this.manager = manager;
	}

	/* (non-Javadoc)
	 * @see uk.co.alvagem.dbview.util.IXMLContentHandler#startElement(java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String local, Attributes attrs)
			throws InputException {
		text.delete(0,text.length());
	}

	/* (non-Javadoc)
	 * @see uk.co.alvagem.dbview.util.IXMLContentHandler#endElement(java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String local) throws InputException {
		if(local.equals("Plugin")){
			try {
				manager.registerPlugin(text.toString());
			} catch (Exception e) {
				throw new InputException("Unable to create plugin " + text.toString(), e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see uk.co.alvagem.dbview.util.IXMLContentHandler#characters(java.lang.String)
	 */
	public void characters(String str) throws InputException {
		text.append(str);
	}

}
