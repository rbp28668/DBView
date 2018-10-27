/*
 * XMLWriter.java
 * Project: EATool
 * Created on 27-Dec-2005
 *
 */
package uk.co.alvagem.dbview.util;

import java.io.IOException;
import java.util.EmptyStackException;

/**
 * XMLWriter
 * 
 * @author rbp28668
 */
public interface XMLWriter {
    /** closes the XML writer and any underlying writer
     * @throws IOException in the event of being unable to close
     */
    public abstract void close() throws IOException;

    /** initialises the XML stream, writing any header information.
     * @throws IOException if unable to initialise
     */
    public abstract void startXML() throws IOException;

    /** Finishes the XML stream, closing any output
     * @throws IOException in the event of any output problems
     */
    public abstract void stopXML() throws IOException;

    /** sets a namespace for the output.
     * @param prefix is the namespace prefix to use.
     * @param ns is the full namespace
     */
    public abstract void setNamespace(String prefix, String ns);

    /** starts an entity definition
     * @param name is the entity name 
     * @throws IOException in the event of any output problems
     */
    public abstract void startEntity(String name) throws IOException;

    /** completes an entity definition.
     * @throws IOException in the event of any output problems
     * @throws EmptyStackException if there is no corresponding startEntity call.
     */
    public abstract void stopEntity() throws IOException, EmptyStackException;

    /** adds an attribute to the currently open entity.
     * @param name is the attribute name
     * @param value is the attribute value
     * @throws IOException in the event of any output problems
     */
    public abstract void addAttribute(String name, String value)
            throws IOException;

    /** adds an attribute to the currently open entity.
     * @param name is the attribute name
     * @param value is the attribute value
     * @throws IOException in the event of any output problems
     */
    public abstract void addAttribute(String name, int value)
            throws IOException;

    /**
     * Adds a boolean attribute.
     * @param name is the attribute name.
     * @param b is the value to set in the attribute.
     * @throws IOException in the event of any output problems.
     */
    public abstract void addAttribute(String name, boolean b)
            throws IOException;

    /** writes text out to the body of a CDATA entity
     * @param text is the string to write
     * @throws IOException in the event of any output problems
     */
    public abstract void text(String text) throws IOException;

    /** writes a complete simple text entity (CDATA)
     * @param name is the entity name
     * @param contents is the entityc contents
     * @throws IOException in the event of any output problems
     */
    public abstract void textEntity(String name, String contents)
            throws IOException;
}