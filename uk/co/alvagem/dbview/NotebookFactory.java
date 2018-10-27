/**
 * 
 */
package uk.co.alvagem.dbview;

import org.xml.sax.Attributes;

import uk.co.alvagem.dbview.model.Notebook;
import uk.co.alvagem.dbview.util.FactoryBase;
import uk.co.alvagem.dbview.util.IXMLContentHandler;
import uk.co.alvagem.dbview.util.InputException;

/**
 * Loads a singleton notebook.
 * @author bruce.porteous
 */
public class NotebookFactory extends FactoryBase implements IXMLContentHandler {

	private Notebook notebook;
	private StringBuffer text = new StringBuffer();
	/**
	 * 
	 */
	public NotebookFactory(Notebook notebook) {
		super();
		this.notebook = notebook;
	}

	/* (non-Javadoc)
	 * @see uk.co.alvagem.dbview.util.IXMLContentHandler#startElement(java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String local, Attributes attrs)
			throws InputException {
	}

	/* (non-Javadoc)
	 * @see uk.co.alvagem.dbview.util.IXMLContentHandler#endElement(java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String local) throws InputException {
	}

	/* (non-Javadoc)
	 * @see uk.co.alvagem.dbview.util.IXMLContentHandler#characters(java.lang.String)
	 */
	public void characters(String str) throws InputException {
		text.append(str);
		notebook.setText(text.toString());
	}

}
