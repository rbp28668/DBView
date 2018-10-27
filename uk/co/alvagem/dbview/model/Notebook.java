/**
 * 
 */
package uk.co.alvagem.dbview.model;

import java.io.IOException;

import uk.co.alvagem.dbview.util.XMLWriter;

/**
 * Keeps a notebook of text.
 * @author bruce.porteous
 *
 */
public class Notebook {

	private String text = "";
	
	/**
	 * 
	 */
	public Notebook() {
		super();
	}
	
	/**
	 * @return Returns the text.
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text The text to set.
	 */
	public void setText(String text) {
		this.text = text;
	}

	public void writeXML(XMLWriter writer) throws IOException {
		writer.startEntity("Notebook");
		writer.text(text);
		writer.stopEntity();
	}

    /**
     * Clears the notebook text.
     */
    public void clear() {
        this.text = "";
    }


}
