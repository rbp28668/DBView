/*
 * DeDupeDetectionEventReceiver.java
 * Project: DBView
 * Created on 7 Sep 2009
 *
 */
package uk.co.alvagem.dbview.dedupe;

import java.awt.Component;
import java.util.Properties;
import java.util.Set;

import org.apache.lucene.index.IndexReader;


/**
 * DeDupeDetectionEventReceiver defines classes that have an interest in the
 * results of a de-duping excercise.
 * 
 * @author rbp28668
 */
public interface DeDupeDetectionEventReceiver {

    public void edit(Component parent) throws DeDupeException;
    
    public Properties getProperties()  throws DeDupeException;
    
    public void setProperties(Properties props)  throws DeDupeException;
    
    public void start(Set<String>reportFields)  throws DeDupeException;
    
    public void onMatch(Match match, IndexReader index, Set<String>reportFields)  throws DeDupeException;
    
    public void finish() throws DeDupeException;
}
