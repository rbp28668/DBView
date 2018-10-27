/*
 * RecordExclusionTemplate.java
 * Project: DBView
 * Created on 7 Sep 2009
 *
 */
package uk.co.alvagem.dbview.model;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;

import uk.co.alvagem.dbview.util.XMLWriter;

/**
 * Class to hold criteria for eliminating records from the matching or indexing
 * process.  Exclusion criteria are defined by field names and regular expressions.
 * If a field value matches one of its regular expressions that record should
 * be rejected.  Note that this allows multiple patterns per field - this is easier
 * to manage for the user than one huge regexp.
 * RecordExclusionTemplate
 * 
 * @author rbp28668
 */
public class RecordExclusionTemplate {

    /** Field name -> regexp string */
    private List<Exclude> templates = new LinkedList<Exclude>();
    

    /**
     * @return the templates
     */
    public List<Exclude> getTemplates() {
        return Collections.unmodifiableList(templates);
    }

    /**
     * @param templates the templates to set
     */
    public void setTemplates(List<Exclude> templates) {
        this.templates.clear();
        this.templates.addAll(templates);
    }

    /**
     * Clears the exclusion list. 
     */
    public void clear() {
        this.templates.clear();
    }
    
    /**
     * Adds a new exclusion.
     * @param field
     * @param regexp
     */
    public void add(String field, String regexp){
        templates.add(new Exclude(field,regexp));
    }
    
    /**
     * Determines if the current row of the result set doesn't match any
     * of the exclusion patterns.
     * @param rs is the result set to test.
     * @return true if ok, false if rejected.
     * @throws SQLException if the resultset doesn't contain one of the fields.
     */
    public boolean accept(ResultSet rs) throws SQLException{

        for(Exclude e : templates){
            String value = rs.getString(e.getField());
            if(value == null){
                throw new SQLException("No field called " + e.getField());
            }
            
            Pattern p = e.getPattern();
            Matcher m = p.matcher(value);
            if(m.matches()){
                return false;
            }
        }
        
        return true;
    }

    /**
     * Determines if the lucene document doesn't match any
     * of the exclusion patterns.
     * @param doc is the document to test.
     * @return true if accepted, false if should be excluded.
     * @throws IllegalArgumentException if the document doesn't contain one of
     * the referenced fields.
     */
    public boolean accept(Document doc) {

        for(Exclude e : templates){
            String value = doc.get(e.getField());
            if(value == null) {
                throw new IllegalArgumentException("Document does not have a field called " + e.getField());
            }
            
            Pattern p = e.getPattern();
            Matcher m = p.matcher(value);
            if(m.matches()){
                return false;
            }
        }
        
        return true;
    }
    
    
    /**
     * Serialise as XML.
     * @param writer
     * @throws IOException
     */
    public void writeXML(XMLWriter writer) throws IOException {
        writer.startEntity("Exclusions");
        for(Exclude e : templates){
            e.writeXML(writer);
        }
        writer.stopEntity();
    }
    
    public RecordExclusionTemplate copy(){
        RecordExclusionTemplate copy = new RecordExclusionTemplate();
        for(Exclude e : templates){
            copy.templates.add(new Exclude(e));
        }

        return copy;
    }
    
    /**
     * Exclude
     * A single field/regexp pair. 
     * @author rbp28668
     */
    public static class Exclude {
        private String field;
        private Pattern pattern;
        
        private Exclude(Exclude e){
            this.field = e.field;
            this.pattern = e.pattern;
        }
        
        private Exclude(String field, String regex){
            this.field = field;
            this.pattern = Pattern.compile(regex);
        }
        
        /**
         * @return the field
         */
        public String getField() {
            return field;
        }
        /**
         * @param field the field to set
         */
        public void setField(String field) {
            this.field = field;
        }
        
        /**
         * @return the template
         */
        public String getTemplate() {
            return pattern.toString();
        }
        
        /**
         * @param template the template to set
         */
        public void setTemplate(String template) {
            this.pattern = Pattern.compile(template);
        }
        
        /**
         * @return the pattern
         */
        public Pattern getPattern() {
            return pattern;
        }
        
        /**
         * Serialise as XML.
         * @param writer
         * @throws IOException
         */
        private void writeXML(XMLWriter writer) throws IOException {
            writer.startEntity("Exclude");
            writer.textEntity("ExcludeField", getField());
            writer.textEntity("ExcludePattern", getTemplate());
            writer.stopEntity();
        }
        
    }
    
}
