/*
 * StemmingAnalyser.java
 * Project: EATool
 * Created on 09-Mar-2007
 *
 */
package uk.co.alvagem.dbview.model;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

/**
 * StemmingAnalyser is a Lucene Analyzer, based on a StandardAnalyzer, but
 * which includes a porter stemming filter.
 * 
 * @author rbp28668
 */
public class StemmingAnalyser extends Analyzer {

    /**
     * 
     */
    public StemmingAnalyser() {
        super();
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.analysis.Analyzer#tokenStream(java.lang.String, java.io.Reader)
     */
    public TokenStream tokenStream(String field, Reader reader) {
        StandardTokenizer tokenizer = new StandardTokenizer(reader);
        StandardFilter standardFilter = new StandardFilter(tokenizer);
        LowerCaseFilter lowerCaseFilter = new LowerCaseFilter(standardFilter);
        StopFilter stopFilter = new StopFilter(lowerCaseFilter,StopAnalyzer.ENGLISH_STOP_WORDS);
        PorterStemFilter porterStemFilter = new PorterStemFilter(stopFilter);
        return porterStemFilter;
    }
}
