/*
 * XMLWriterSAX.java
 * Project: EATool
 * Created on 27-Dec-2005
 *
 */
package uk.co.alvagem.dbview.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.EmptyStackException;
import java.util.Properties;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;

import org.apache.xml.serializer.Method;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.AttributesImpl;

/**
 * XMLWriterSAX provides an XMLWriter implementation using SAX events. Depending on which constructor is called,
 * these events are either passed to a supplied ContentHandler (e.g. as input to an XSLT transform) or are passed
 * to a Serializer to be written to a Writer or OutputStream.
 * 
 * @author rbp28668
 */
public class XMLWriterSAX implements XMLWriter {

    private ContentHandler handler;
    private AttributesImpl attrs = null;
    private QName currentElement = null;
    private Stack elements = new Stack();
    private String nameSpaceURI = null;
    private String nameSpacePrefix = "";
    private OutputStream os = null;
    private Writer writer = null;

    /**
     * Creates a XMLWriterSAX tied to the writer for serializing XML.
     * @param writer is the Writer to serialize to.
     * @throws IOException
     */
    public XMLWriterSAX(Writer writer) throws IOException {
        super();

        this.writer = writer;
        
        Properties props = OutputPropertiesFactory.getDefaultMethodProperties(Method.XML);
        Serializer serializer = SerializerFactory.getSerializer(props);
        serializer.setWriter(writer);
        handler = serializer.asContentHandler();
    }

    /**
     * Creates a XMLWriterSAX tied to an OutputStream for serializing XML.
     * @param os is the OutputStream to serialize to.
     * @throws IOException
     */
    public XMLWriterSAX(OutputStream os) throws IOException {
        super();

        this.os = os;
        
        Properties props = OutputPropertiesFactory.getDefaultMethodProperties(Method.XML);
        props.setProperty(OutputKeys.INDENT,"yes");
        Serializer serializer = SerializerFactory.getSerializer(props);
        serializer.setOutputStream(os);
        handler = serializer.asContentHandler();
    }
    
    /**
     * Provides an XMLWriterSAX tied to a ContentHandler to allow XML to be serialized to raw
     * SAX events for further processing.
     * @param handler is the ContentHandler to receive the SAX events.
     */
    public XMLWriterSAX(ContentHandler handler){
        this.handler = handler;
    }
    
    /* (non-Javadoc)
     * @see alvahouse.eatool.util.XMLWriter#close()
     */
    public void close() throws IOException {
        if(writer != null){
            writer.close();
        }
        if(os != null){
            os.close();
        }
    }

    /* (non-Javadoc)
     * @see alvahouse.eatool.util.XMLWriter#startXML()
     */
    public void startXML() throws IOException {
        try {
            handler.startDocument();
        } catch (Exception e) {
            throw new IOException("Unable to write XML: " + e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see alvahouse.eatool.util.XMLWriter#stopXML()
     */
    public void stopXML() throws IOException {
        flush();
        try {
            handler.endDocument();
        } catch (Exception e) {
            throw new IOException("Unable to write XML: " + e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see alvahouse.eatool.util.XMLWriter#setNamespace(java.lang.String, java.lang.String)
     */
    public void setNamespace(String prefix, String ns) {
        nameSpacePrefix = prefix;
        nameSpaceURI = ns;
    }

    /* (non-Javadoc)
     * @see alvahouse.eatool.util.XMLWriter#startEntity(java.lang.String)
     */
    public void startEntity(String name) throws IOException {
        flush();
        try {
            attrs = new AttributesImpl();
            QName qName = new QName(nameSpaceURI, name, nameSpacePrefix);
            currentElement = qName;
            elements.push(qName);
        } catch (Exception e) {
            throw new IOException("Unable to write XML: " + e.getMessage());
        }
    }

    /*(non-Javadoc)
     * @see alvahouse.eatool.util.XMLWriter#stopEntity()
    */ 
    public void stopEntity() throws IOException, EmptyStackException {
        flush();
        try {
            currentElement = (QName)elements.pop();
            handler.endElement(currentElement.getNamespaceURI(), currentElement.getLocalPart(), currentElement.getPrefix() + ":" + currentElement.getLocalPart());
        } catch (Exception e) {
            throw new IOException("Unable to write XML: " + e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see alvahouse.eatool.util.XMLWriter#addAttribute(java.lang.String, java.lang.String)
     */
    public void addAttribute(String name, String value) throws IOException {
        attrs.addAttribute("",name,name,"CDATA",value);
    }

    /* (non-Javadoc)
     * @see alvahouse.eatool.util.XMLWriter#addAttribute(java.lang.String, int)
     */
    public void addAttribute(String name, int value) throws IOException {
        addAttribute(name, Integer.toString(value));   
    }

    /* (non-Javadoc)
     * @see alvahouse.eatool.util.XMLWriter#addAttribute(java.lang.String, boolean)
     */
    public void addAttribute(String name, boolean b) throws IOException {
        addAttribute(name, b ? "true" : "false");
    }

    /* (non-Javadoc)
     * @see alvahouse.eatool.util.XMLWriter#text(java.lang.String)
     */
    public void text(String text) throws IOException {
        flush();
        try {
            handler.characters(text.toCharArray(),0,text.length());
        } catch (Exception e) {
            throw new IOException("Unable to write XML: " + e.getMessage());
        }
    }

    /**
     * Creates any pending elements.  Elements are created then have attributes added but are not actually
     * serialized until this method is called.
     * @throws IOException
     */
    private void flush() throws IOException {
        if(attrs != null){
            try {
                handler.startElement(currentElement.getNamespaceURI(), currentElement.getLocalPart(), currentElement.getPrefix() + ":" + currentElement.getLocalPart(), attrs);
            } catch (Exception e) {
                throw new IOException("Unable to write XML: " + e.getMessage());
            }
            attrs = null;
        }
        
    }

    /* (non-Javadoc)
     * @see alvahouse.eatool.util.XMLWriter#textEntity(java.lang.String, java.lang.String)
     */
    public void textEntity(String name, String contents) throws IOException {
        startEntity(name);
        text(contents);
        stopEntity();

    }

}
