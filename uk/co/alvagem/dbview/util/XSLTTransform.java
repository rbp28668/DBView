/*
 * XSLTTransform.java
 * Project: EATool
 * Created on 28-Dec-2005
 *
 */
package uk.co.alvagem.dbview.util;

import java.io.IOException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.ContentHandler;

/*
 * 
 * XSLTTransform dramatis personae:
 * 
 * TransformerFactory
 * A TransformerFactory instance can be used to create Transformer and Templates objects.
 * 
 * Transformer
 * An instance of this abstract class can transform a source tree into a result tree.
 * An instance of this class can be obtained with the TransformerFactory.newTransformer method. This instance may then be used to process XML from a variety of sources and write the transformation output to a variety of sinks.
 * An object of this class may not be used in multiple threads running concurrently. Different Transformers may be used concurrently by different threads.
 * A Transformer may be used multiple times. Parameters and output properties are preserved across transformations.
 * 
 * Templates
 * An object that implements this interface is the runtime representation of processed transformation instructions.
 * Templates must be threadsafe for a given instance over multiple threads running concurrently, and may be used multiple times in a given session.
 *
 * SAXTransformerFactory 
 * This class extends TransformerFactory to provide SAX-specific factory methods. It provides two types of ContentHandlers, one for creating Transformers, the other for creating Templates objects.
 * If an application wants to set the ErrorHandler or EntityResolver for an XMLReader used during a transformation, it should use a URIResolver to return the SAXSource which provides (with getXMLReader) a reference to the XMLReader.
 * 
 * TransformerHandler
 * extends ContentHandler, LexicalHandler, DTDHandler
 * A TransformerHandler listens for SAX ContentHandler parse events and transforms them to a Result.
 * 
 * SAXResult
 * Acts as an holder for a transformation Result. 
 * @author rbp28668
 */

/**
 * XSLTTransform accepts SAXEvents (via its asContentHandler method) and outputs transformed
 * XML to the given Result.  Use by creating with a suitable XSLT template (will give the
 * identity transform if null), setting up the Result for the output transform with
 * <code>setResult(Result)</code> and then supplying it with SAX events though the
 * <code>ContentHandler</code> obtained with <code>asContentHandler()</code> 
 * 
 * @author rbp28668
 */
public class XSLTTransform {

    private TransformerHandler handler;
    
    /**
     * Creates an XSLTTransform for a given XSLT transform.
     * @param templatePath is the path to the XSLT transform.
     */
    public XSLTTransform(String templatePath) throws IOException{
        super();

		try {
            TransformerFactory tf = TransformerFactory.newInstance();
            if(!tf.getFeature(SAXTransformerFactory.FEATURE)) {
            	throw new UnsupportedOperationException("Stylesheets cannot accept SAX events");
            }

            //tf.setErrorListener(new TransformErrorHandler());
            //Templates templates = tf.newTemplates(source);

            SAXTransformerFactory saxtf = (SAXTransformerFactory)tf;

            if(templatePath != null){
                // TODO - check file exists.
                Source source = new StreamSource(templatePath);
                source.setSystemId(templatePath);
                handler = saxtf.newTransformerHandler(source);
            } else {
                handler = saxtf.newTransformerHandler();
            }
            
            Transformer trans = handler.getTransformer();
            trans.setOutputProperty(OutputKeys.INDENT,"yes");
            //trans.setOutputProperty(OutputKeys.METHOD,"xml");
            trans.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
            
        } catch (TransformerConfigurationException e) {
            throw new IOException("Unable to set transformer configuration " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new IOException("Unable to setup transform " + e.getMessage());
        } catch (TransformerFactoryConfigurationError e) {
            throw new IOException("Unable to set transformer factory configuration " + e.getMessage());
        }
		
    }

    /**
     * Gets a ContentHandler that should sink the SAX events that describe the XML to be
     * transformed.
     * @return a ContentHandler.
     */
    public ContentHandler asContentHandler(){
        return handler;
    }

    /**
     * Sets the Result that should be used for the output of the transform.  Use StreamResult
     * to write to a file, SAXResult to pass on the SAX events.
     * @param result is the output to use.
     */
    public void setResult(Result result){
        handler.setResult(result);
    }
    
    /**
     * Sets the system id to identify where the input events are coming from.
     * This allows relative operations such as XSL import/include to work properly.
     * @param id is the system id to set. 
     */
    public void setSystemID(String id){
        handler.setSystemId(id);
    }
    

}
