/**
 * XMLWriterDirect helps write well-formed XML.  This provides direct writing to
 * a java.io.Writer without using SAX events etc.
 * @author  rbp28668
 */

package uk.co.alvagem.dbview.util;

import java.io.Writer;
import java.io.IOException;
import java.util.Stack;
import java.util.EmptyStackException;

public class XMLWriterDirect implements XMLWriter {
    
    private Writer m_out;
    
    private boolean m_entityIsOpen = false;
    
    private Stack m_entities = new Stack();
    private boolean mustInitNamespace = false;
    private String nameSpace;
    private String nameSpacePrefix = "";
    private String rawNameSpacePrefix;
    
    /** Creates new XMLWriterDirect  
     * @param out is the writer to output the XML to
     */
    public XMLWriterDirect(Writer out) {
        m_out = out;
    }
    
    /** closes the XML writer and any underlying writer
     * @throws IOException in the event of being unable to close
     */
    public void close() throws IOException {
        m_out.close();
    }
    
    /** initialises the XML stream, writing any header information.
     * @throws IOException if unable to initialise
     */
    public void startXML() throws IOException {
        m_out.write("<?xml version=\"1.0\"?>");
    }
    
    /** Finishes the XML stream, closing any output
     * @throws IOException in the event of any output problems
     */
    public void stopXML() throws IOException {
        terminateOpenEntity();
        m_out.close();
    }
    
    /** sets a namespace for the output.
     * @param prefix is the namespace prefix to use.
     * @param ns is the full namespace
     */
    public void setNamespace(String prefix, String ns) {
        nameSpacePrefix = prefix + ":";
        rawNameSpacePrefix = prefix;
        nameSpace = ns;
        mustInitNamespace = true;
    }
    
    /** starts an entity definition
     * @param name is the entity name 
     * @throws IOException in the event of any output problems
     */
    public void startEntity(String name) throws IOException {
        terminateOpenEntity();
        m_out.write("\n");
        m_out.write("<" + nameSpacePrefix + name ); // leave open for attributes
        m_entityIsOpen = true;
        m_entities.push(name);
        
        if(mustInitNamespace) {
            m_out.write(" xmlns:" + rawNameSpacePrefix + "=\"" + nameSpace + "\"");
            mustInitNamespace = false;
        }
    }

    /** completes an entity definition.
     * @throws IOException in the event of any output problems
     * @throws EmptyStackException if there is no corresponding startEntity call.
     */
    public void stopEntity() throws IOException, EmptyStackException {
        terminateOpenEntity();
        m_out.write("</" + nameSpacePrefix + (String)m_entities.pop() + ">");
        m_out.write("\n");
    }
    
    /** adds an attribute to the currently open entity.
     * @param name is the attribute name
     * @param value is the attribute value
     * @throws IOException in the event of any output problems
     */
    public void addAttribute(String name, String value) throws IOException {
        m_out.write(" " + name + "=\"" + escapeString(value) + "\"");
    }
    
	/** adds an attribute to the currently open entity.
	* @param name is the attribute name
	* @param value is the attribute value
	* @throws IOException in the event of any output problems
	*/
	public void addAttribute(String name, int value) throws IOException {
		addAttribute(name, Integer.toString(value));
	}

    /**
     * Adds a boolean attribute.
     * @param name is the attribute name.
     * @param b is the value to set in the attribute.
     * @throws IOException in the event of any output problems.
     */
    public void addAttribute(String name, boolean b) throws IOException {
        addAttribute(name, b ? "true" : "false");
    }
	
    /** writes text out to the body of a CDATA entity
     * @param text is the string to write
     * @throws IOException in the event of any output problems
     */
    public void text(String text) throws IOException {
        terminateOpenEntity();
        m_out.write(escapeString(text));
    }

    /** writes a complete simple text entity (CDATA)
     * @param name is the entity name
     * @param contents is the entityc contents
     * @throws IOException in the event of any output problems
     */
    public void textEntity(String name, String contents) throws IOException {
        terminateOpenEntity();
        m_out.write("<" + nameSpacePrefix + name + ">");
        m_out.write(escapeString(contents));
        m_out.write("</" + nameSpacePrefix + name + ">");
    }
    
    /** terminetes the start of any open entity.  Entities are kept open to allow
     * the addition of attributes until they are closed, text or child-elements
     * are written.
     * @throws IOException in the event of any output problems
     */
    private void terminateOpenEntity() throws IOException {
        if(m_entityIsOpen){
            m_out.write(">");
            m_entityIsOpen = false;
        }
    }
    
    /** escapes any string value puting appropriate escape sequences instead of 
     * the original characters.
     * @param text is the string to translate
     * @returns an escaped string
     */
    private String escapeString(String text) {
        StringBuffer sb = new StringBuffer(text);
        String escape = "\"<>&";
        String[] replace = {"&quot;", "&lt;", "&gt;", "&amp;"}; // must match entries in escape
        
        int nChars = sb.length();
        for(int iPos=0; iPos<nChars; ++iPos) {
            char ch = sb.charAt(iPos);
            int iEsc = escape.indexOf(ch);
            if(iEsc != -1) {                            // found something to replace
                sb.deleteCharAt(iPos);                  // so get rid of it
                sb.insert(iPos,replace[iEsc]);          // and patch in corresponding replacement string
                nChars = sb.length();
            }
        }
        return sb.toString();
    }

    
}

