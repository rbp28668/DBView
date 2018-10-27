/*
 * SettingsManager.java
 *
 * Created on 23 January 2002, 20:16
 */

package uk.co.alvagem.dbview.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/** SettingsManager manages a configuration tree loaded in from
 * a XML file.  The file is converted into a tree of Elements
 * where each Element has a (possibly empty) set of name-value
 * attributes.
 *
 * @author rbp28668
 */
public class SettingsManager {

    /** Creates new SettingsManager */
    public SettingsManager() {
    }

    /** Loads the settings from an xml file
     * @param uri is the URI of the settings xml file
     */    
    public void load(String uri) {
        XMLLoader loader = new XMLLoader();

        Handler h = new Handler();
        loader.registerContent(h);

        try {
            loader.parse(uri);
        }
        catch(SAXException e) {
            throw new IllegalArgumentException("Problem parsing xml file: " + uri + e.getMessage());
        }
    }

    /** Loads the settings from an xml file
     * @param uri is the URI of the settings xml file
     */    
    public void load(InputStream stream) {
        XMLLoader loader = new XMLLoader();

        Handler h = new Handler();
        loader.registerContent(h);

        try {
            loader.parse(stream);
        }
        catch(SAXException e) {
            throw new IllegalArgumentException("Problem parsing xml stream: " + e.getMessage());
        }
    }

	public void setEmptyRoot(String name) {
		root = new Element(name);
	}
	
    public void save(String uri) {
        try {
            XMLWriter writer = new XMLWriterDirect(new FileWriter(uri));
            writer.startXML();
            getRoot().writeXML(writer);
            writer.stopXML();
        }
        catch(Exception e) {
            throw new IllegalArgumentException("propblem saving xml file: " + e.getMessage());
        }
        
    }
    /** Gets the root element of the settings tree
     * @return the root element (never null)
     */    
    public Element getRoot() {
        return root;
    }
    
    /** This gets an element using an XPath like expression.  This
     * matches only one element in the settings tree.  The expression
     * is of the form /name/name/name etc. where each name is matched
     * against the appropriate level in the tree.
     * @param path is the simplified path expression to identify the required node
     * @return the Element that matches the path expression.
     */    
    public Element getElement(String path) {
        StringTokenizer tokens = new StringTokenizer(path,"/");
        Element here = root;
        while(tokens.hasMoreTokens()) {
            String key = tokens.nextToken();
            here = here.findChild(key);
            if(here == null)
                throw new IllegalArgumentException("Unable to find " + path + " in settings");
        }
        return here;
    }

    /** This gets an element using an XPath like expression.  This
     * matches only one element in the settings tree.  The expression
     * is of the form /name/name/name etc. where each name is matched
     * against the appropriate level in the tree.  If the element is not
     * found it is created.
     * @param path is the simplified path expression to identify the required node
     * @return the Element that matches the path expression.
     */    
    public Element getOrCreateElement(String path) {
        StringTokenizer tokens = new StringTokenizer(path,"/");
        Element here = root;
        while(tokens.hasMoreTokens()) {
            String key = tokens.nextToken();
            Element child = here.findChild(key);
            if(child == null) {
                child = new Element(key);
                here.addElement(child);
            }
            here = child;
        }
        return here;
    }
    
    private class Handler implements IXMLContentHandler {
        
        /** callback from IXMLContentHandler
         * @param str is the element text
         */        
        public void characters(String str) {}
        
        /** callback from IXMLContentHandler specifies the end of an element
         * This uses it to reset the current node so that future nodes
         * are attached to the correct parent
         * @param uri is the namespace uri (should be blank)
         * @param local is the element name
         */        
        public void endElement(String uri, String local) {
            current = (Element)currentElementStack.pop();
        }
        
        /** callback from IXMLContentHandler this starts a new Element in
         * the settings tree
         * @param uri is the namespace in the source xml (should be blank)
         * @param local is the element name
         * @param attrs are the attributes for this element
         */        
        public void startElement(String uri, String local, Attributes attrs) {
            Element ne = new Element(local);
            ne.addAttributes(attrs);

            if(root == null) {
                //System.out.println("Setting " + ne.getName() + " as root node");
                root = ne;
            }
            if(current != null) {
                //System.out.println("Adding " + ne.getName() + " to " + current.getName());
                current.addElement(ne);
            }
            currentElementStack.push(current); // even if null
            current = ne;
        }
    }
    
    /** This is the main node class for the settings tree.
     */    
    public class Element {
        /** Creates a new element with a given name
         * @param nm is the element name for this node
         */        
        public Element(String nm) {
            name = nm;
        }
        
        /** Gets the name of the element
         * @return the element name
         */        
        public String getName() {
            return name;
        }
    
        public String toString() {
            return name;
        }
        
        public void setName(String n) {
            name = n;
        }
        
        /** Looks up an attribute of the element by attribute name
         * @param key is the name of the attribute to get
         * @return the attribute or null if the name is not found
         */        
        public String attribute(String key) {
            return (String) attributes.get(key);
        }

         /** Looks up an attribute of the element by attribute name
         * but throws an exception if the attribute is not found
         * @param key is the name of the attribute to get
         * @return the attribute
         */        
        public String attributeRequired(String key) {
            String val = (String) attributes.get(key);
            if(val == null)
                throw new IllegalArgumentException("Missing attribute " + key + " in settings entry " + getName());
            return val ;
        }

         /** Adds an attibute to the attribue list of the element
         * @param key is the name of the attribute 
         * @param value is the value of the attribute
         */        
        public void setAttribute(String key, String value) {
            attributes.put(key,value);
        }
        
        /** Gets a ListIterator that can be used to enumerate all the
         * child Elements of this Element
         * @return A ListIterator that can iterate all the child Elements
         */        
        public ListIterator getChildren() {
            return children.listIterator();
        }
        
        /** Gets the number of children 
         * @return the child count
         */
        public int getChildCount() {
            return children.size();
        }
        /** Get the set of keys for attributes
         * @return iterator for a set of keys
         */        
        public Iterator getAttributeKeys() {
            return attributes.keySet().iterator();
        }
        /** Find the a given child Element by name
         * @param key is the name of the child Element to search for
         * @return the corresponding child element or null if no match
         */        
        public Element findChild(String key) {
            ListIterator iter = getChildren();
            while(iter.hasNext()) {
                Element here = (Element) iter.next();
                if(here.name.compareTo(key) == 0) {
                    return here;
                }
            }
            return null;
        }
        
        /** Adds attributes from the SAX attributes
         * @param attrs are the SAX attributes
         */        
        void addAttributes(Attributes attrs) {
            int nAttributes = attrs.getLength();
            for(int i=0; i<nAttributes; ++i) {
                attributes.put( attrs.getLocalName(i), attrs.getValue(i));
            }
        }
        
        /** Adds an element to the tail of the list of children
         * @param child is the child Element to add
         */        
        void addElement(Element child) {
            children.addLast(child);
        }

        /**
         * Writes the Element out as XML
         * @param out is the XMLWriterDirect to write the XML to
         */
        public void writeXML(XMLWriter out) throws IOException {
            out.startEntity(getName());
            Iterator iter = attributes.entrySet().iterator();
            while(iter.hasNext()) {
                Map.Entry entry = (Map.Entry)iter.next();
                out.addAttribute((String)entry.getKey(), (String)entry.getValue());
            }
            iter = children.iterator();
            while(iter.hasNext()){
                ((Element)iter.next()).writeXML(out);
            }
            out.stopEntity();
        }
        /** map of string attributes indexed by name
         */        
        HashMap attributes = new HashMap();
        /** ordered list of child Elements
         */        
        LinkedList children = new LinkedList();
        /** name of this element
         */        
        String name;
    }
    
    /** The root element of the settings tree
     */    
    Element root = null;
    /** The current element of the settings tree.  This is the element
     * that will be the parent of any new elements.
     */    
    Element current = null;
    /** track the current element
     */    
    Stack currentElementStack = new Stack();
}
