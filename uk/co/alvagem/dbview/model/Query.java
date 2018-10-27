/**
 * 
 */
package uk.co.alvagem.dbview.model;

import java.io.IOException;

import uk.co.alvagem.dbview.util.XMLWriter;

/**
 * A named SQL query.
 * @author bruce.porteous
 *
 */
public class Query {

	private String name = "";
	private String text = "";
	
	/**
	 * 
	 */
	public Query() {
		super();
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
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
		writer.startEntity("Query");
		writer.addAttribute("name",name);
		writer.text(text);
		writer.stopEntity();
	}

}
