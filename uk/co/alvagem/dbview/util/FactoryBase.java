/*
 * FactoryBase.java
 *
 * Created on 19 January 2002, 16:08
 */

package uk.co.alvagem.dbview.util;

import org.xml.sax.Attributes;

/**
 * The base class for the various XML object factories.  This 
 * provides a number of utility classes for getting data from
 * the XML events.
 * @author  rbp28668
 */
public class FactoryBase  {

    /** Creates new FactoryBase */
    public FactoryBase() {
    }
    
    /**
     * @param attrs
     * @param name
     * @return
     * @throws InputException
     */
    protected String getString(Attributes attrs, String name) throws InputException{
    	String value = attrs.getValue(name);
    	if(value == null){
	        throw new InputException("Missing string value " + name);
	    }
    	return value;
    }
    
	/**
	 * Gets an int from a set of attributes.
	 * @param attrs is the set of attributes.
	 * @param name is the name of the attribute containing the int.
	 * @return the given int.
	 * @throws InputException if the attribute is missing or can't be converted.
	 */
	protected int getInt(Attributes attrs, String name) throws InputException{
	    String asText = attrs.getValue(name);
	    if(asText == null){
	        throw new InputException("Missing int value " + name);
	    }
	    int value = 0;
	    try {
            value = Integer.parseInt(asText);
        } catch (NumberFormatException e) {
            throw new InputException("Unable to convert value " + asText + " for " + name + " to int");
        }
	    return value;
	}
	/**
	 * Gets a float from a set of attributes.
	 * @param attrs is the set of attributes.
	 * @param name is the name of the attribute containing the float.
	 * @return the given float.
	 * @throws InputException if the attribute is missing or can't be converted.
	 */
	protected float getFloat(Attributes attrs, String name) throws InputException{
	    String asText = attrs.getValue(name);
	    if(asText == null){
	        throw new InputException("Missing float value " + name);
	    }
	    float value = 0.0f;
	    try {
            value = Float.parseFloat(asText);
        } catch (NumberFormatException e) {
            throw new InputException("Unable to convert value " + asText + " for " + name + " to float");
        }
	    return value;
	}
	
	

}
