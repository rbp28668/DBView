/**
 *
 * @author  rbp28668
 */

package uk.co.alvagem.dbview.util;

import org.xml.sax.Attributes;

public interface IXMLContentHandler {
    public void startElement(String uri, String local, Attributes attrs) throws InputException;
    public void endElement(String uri, String local) throws InputException;
    public void characters(String str) throws InputException;
}

