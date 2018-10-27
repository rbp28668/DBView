
package uk.co.alvagem.dbview.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Stack;

import javax.xml.transform.sax.SAXResult;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * XMLLoader
 * Wraps the Xerces SAX parser to give a simplified interface allowing different 
 * handlers to handle different elements.  By calling registerContent the handlers
 * are set up so that when a corresponding element arrives in the input that
 * handler is invoked and stays invoked to handle the content of any child elements.
 * Handlers will be appropriately nested.
 * @author  rbp28668
 */
public class XMLLoader extends DefaultHandler {
    
    private static final String PARSER_NAME = "org.apache.xerces.parsers.SAXParser";
    private XMLReader m_parser;
    private HashMap m_handlers = new HashMap();
    private IXMLContentHandler m_currentHandler = null;
    private Locator locator = null;
    private StringBuffer chars = new StringBuffer(256);
    private Stack handlerStack = new Stack();
    private IXMLContentHandler allHandler = null;
    
    /** Creates new XMLLoader  */
    public XMLLoader() {
        this(PARSER_NAME,null);
    }

    /** Creates new XMLLoader.
     * @param parserClass is the name of the class to use for the parser.   
     */
    public XMLLoader(String parserClass) {
        this(parserClass,null);
    }

    /** Creates new XMLLoader.
     * @param parserClass is the name of the class to use for the parser.  
     * @param transformPath is the path to use for a XSLT transform.  If null
     * then no transform will be used.
     */
    public XMLLoader(String parserClass, String transformPath) {
        try {
            m_parser = (XMLReader)Class.forName(parserClass).newInstance();
            if(transformPath == null){
                m_parser.setContentHandler(this);
            } else {
                XSLTTransform transform = new XSLTTransform(transformPath);
                SAXResult result = new SAXResult(this);
                m_parser.setContentHandler(transform.asContentHandler());
            }
            m_parser.setErrorHandler(this);
        }
        catch(Exception e) {
            throw new UnsupportedOperationException("Unable to setup XML Parser " + parserClass + ":" + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /**
     * Enables or disables validation
     * @param validate turns on validation if true, off if false 
     */    
    public void setValidation(boolean validate) {
        try {
            m_parser.setFeature( "http://xml.org/sax/features/validation", validate);
        }
        catch(Exception e) {
            throw new UnsupportedOperationException("Unable to set validation in XML Parser");
        }
    }
    
    /**
     * Turns namespace usage on or off
     * @param setNameSpaces turns on namespace handling if true, off if false.
     */    
    public void setNameSpaces(boolean setNameSpaces) {
        try {
            m_parser.setFeature( "http://xml.org/sax/features/namespaces", setNameSpaces );
        }
        catch(Exception e) {
            throw new UnsupportedOperationException("Unable to set namespaces in XML Parser");
        }
        
    }
    
    /**
     * Turns schema support on or off
     * @param setSchemaSupport enables schema support if true, disables if false */    
    public void setSchemaSupport(boolean setSchemaSupport) {
        try {
            m_parser.setFeature( "http://apache.org/xml/features/validation/schema", setSchemaSupport );
        }
        catch(Exception e) {
            throw new UnsupportedOperationException("Unable to set schema support in XML Parser");
        }
        
    }
    
    /**
     * Determines whether the parser will continue processing after a fatal 
     * error or not
     * @param bcontinue enables continued processing if true, disables if false.
     * @throws SAXNotRecognizedException
     * @throws SAXNotSupportedException  */    
    public void setContinue(boolean bcontinue) throws SAXNotRecognizedException, SAXNotSupportedException {
        m_parser.setFeature("http://apache.org/xml/features/continue-after-fatal-error",
        bcontinue);
    }
    
    /** sets up a locator that this handler can use to locate the 
     * position of any errors in the input document. 
     * @param locator is the locator to hold the current location.
     */
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }
    
    /**
     * Parses a stream obtained from a URI.  Events are fired in registered
     * handlers when matching entities are found in the XML.
     * @param uri identifies the data to parse
     */    
    public void parse(String uri) throws SAXException {
        try {
            m_parser.parse(uri);
        }
        catch(IOException e) {
            throw new InputException("Unable to read xml input ", e);
        }
    }

    /**
     * Parses an InputStream.  Events are fired in registered
     * handlers when matching entities are found in the XML.
     * @param stream provides the data to parse
     */    
    public void parse(InputStream stream) throws SAXException {
        try {
            m_parser.parse(new InputSource(stream));
        }
        catch(IOException e) {
            throw new InputException("Unable to read xml input ", e);
        }
    }
    
    /**
     * This registers a content handler to receive callbacks from a given
     * element and its children in the XML document
     * @param uri is the namespace URI of the elements to look for
     * @param local is the local element name (without namespace)
     * @param handler is the handler to handle the callbacks
     */    
    public void registerContent(String uri, String local, IXMLContentHandler handler) {
        String fullScope = uri + ":" + local;
        m_handlers.put(fullScope, handler);
    }

    /**
     * This registers a content handler to receive callbacks from a given
     * element and its children in the XML document
     * @param qName is the qName for import docs that don't use namespaces
     * @param handler is the handler to handle the callbacks
     */    
    public void registerContent(String qName, IXMLContentHandler handler) {
        m_handlers.put(qName, handler);
    }
    
    /**
     * Registers a single handler to handle all XML content
     * @param handler is the handler to use */    
    public void registerContent( IXMLContentHandler handler ) {
        m_currentHandler = handler;
    }
    
    /**
     * SAX Callback that passes the information down to the appropriate 
     * handlers (if any).
     * @param uri is the namespace URI
     * @param local is the element name (without namespace)
     * @param raw is the raw element name (prefix:element)
     * @param attrs are the attributes of the element 
     */    
    public void startElement(String uri, String local, String raw, Attributes attrs) {
        flushChars();


        IXMLContentHandler handler = lookupHandler(uri, local, raw);
        if(handler != null) {
            handlerStack.push(m_currentHandler);
            m_currentHandler = handler;
        }

        if(local.equals("")){
            local = raw;
        }

        if(m_currentHandler != null) {
            try {
                m_currentHandler.startElement(uri, local,attrs);
            }
            catch(Exception e) {
                throw new InputException("Unable to load XML" + getLocation(), e);
            }
        }
        
        if(allHandler != null){
            allHandler.startElement(uri,local,attrs);
        }

    }
    
    /**
     * @param uri
     * @param local
     * @param raw
     * @return
     */
    private IXMLContentHandler lookupHandler(String uri, String local, String raw) {
        String fullScope = raw;
        if(!uri.equals("")){
            fullScope = uri + ":" + local;            
        }
        IXMLContentHandler handler = (IXMLContentHandler)m_handlers.get(fullScope);
        return handler;
    }

    /**
     * SAX callback that ends an element.  Triggers an event in any current
     * handler and checks whether the current handler should remain current
     * @param uri is the namespace URI
     * @param local is the element name (without namespace)
     * @param raw is the raw element name (prefix:element)
     * @throws SAXException 
     */    
    public void endElement(String uri, String local, String raw) throws SAXException {
        flushChars();

        IXMLContentHandler handler = lookupHandler(uri,local,raw);

        if(local.equals("")){
            local = raw;
        }

        if(allHandler != null){
            allHandler.endElement(uri,local);
        }
        
        if(m_currentHandler != null) {
            try {
                m_currentHandler.endElement(uri, local);
            }
            catch(Exception e) {
                throw new InputException("Unable to load XML" + getLocation(), e);
            }
        }
        
        if(handler != null) {
            m_currentHandler = (IXMLContentHandler)handlerStack.pop();
        }
        
    }
    
    /**
     * SAX callback for content of an element
     * @param ch is the array of characters that contains the CDATA content
     * @param start is the start position in the array for valid content
     * @param length is the number of characters of valid content
     */    
    public void characters(char[] ch, int start, int length) {
        // System.out.println("Characters<" + new String(ch,start,length) + ">");
        if(m_currentHandler != null) {
            chars.append(ch, start, length);
        }
    }
    
    /**
     * flushes any characters to the output handler - implemented in case
     * characters arrive in multiple calls to characters(...)
     */
    private void flushChars(){
        if(m_currentHandler != null) {
	        String str = chars.toString().trim();
	        if(str.length() > 0) {
	            try {
	                m_currentHandler.characters(str);
	            }
	            catch(Exception e) {
	                throw new InputException("Unable to load XML" + getLocation(), e);
	            }
	        }
        }
        chars.delete(0,chars.length());
    }
   
    private String getLocation() {
        if(locator == null)
            return "";
        return "\n in " + locator.getSystemId() + 
            " line " + locator.getLineNumber() +
            " column " +  locator.getColumnNumber();
    }
    
//    /** Warning - reports a parse warning
//     * @param ex is an exception from the parser */
//    public void warning(SAXParseException ex) {
//        System.err.println("[Warning] "+
//        getLocationString(ex)+": "+
//        ex.getMessage());
//    }
//    
//    /** Error - reports a parse error
//     * @param ex is an exception from the parser  */
//    public void error(SAXParseException ex) {
//        System.err.println("[Error] "+
//        getLocationString(ex)+": "+
//        ex.getMessage());
//    }
//    
//    /** Fatal error - reports a fatal parse error
//     * @param ex is an exception from the parser
//     * @throws SAXException  */
//    public void fatalError(SAXParseException ex) throws SAXException {
//        System.err.println("[Fatal Error] "+
//        getLocationString(ex)+": "+
//        ex.getMessage());
//    }
//    
//    /** Returns a string of the location of the parse error
//     * @param ex is the error from the parser
//     * @returns the error string with the location of the parse error
//     */
//    private String getLocationString(SAXParseException ex) {
//        StringBuffer str = new StringBuffer();
//        
//        String systemId = ex.getSystemId();
//        if (systemId != null) {
//            int index = systemId.lastIndexOf('/');
//            if (index != -1)
//                systemId = systemId.substring(index + 1);
//            str.append(systemId);
//        }
//        str.append(':');
//        str.append(ex.getLineNumber());
//        str.append(':');
//        str.append(ex.getColumnNumber());
//        
//        return str.toString();
//        
//    }
    
    public IXMLContentHandler getAllHandler() {
        return allHandler;
    }
    public void setAllHandler(IXMLContentHandler allHandler) {
        this.allHandler = allHandler;
    }
}

