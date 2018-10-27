/**
 * 
 */
package uk.co.alvagem.dbview.dedupe;

import java.io.IOException;
import java.io.PrintStream;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;

import uk.co.alvagem.dbview.model.RecordExclusionTemplate;
import uk.co.alvagem.dbview.model.SearchEngine;

/**
 * Uses a search engine to (try to!) identify duplicate records held in a
 * Lucene index.
 * @author bruce.porteous
 *
 */
public class DeDupeEngine {

	/** Query to do the matching with */
	private String queryText;
	
	/** Callback to report progress */
	private DeDupeProgressCallback callback;
	
	/** Search engine to do the underlying searching */
	private SearchEngine search;
	
	/** Names of Fields to use for reporting results */
	private HashSet<String> reportFields = new LinkedHashSet<String>();
	
	//private HashSet<Integer> processed = new HashSet<Integer>();
	private BitSet processed;
	
	//private List<Match> matches = new LinkedList<Match>();
	
	/** Only include results where the match score is above this threashold */
	private float threshold = 0.8f;
	
	/** Limit number of documents for testing.  0 suppresses and processes complete index */
	private int maxDocs = 0;
	
	/** Set true to randomly pick documents for sampling large database */
	private boolean randomSelect = false;
	
	/** Where to save the results to */
	private String resultsPath;
	
	
	/** Flag to terminate processing thread early */
	private boolean stop = false;
	
	/** event receivers to recieve matches */
	private List<DeDupeDetectionEventReceiver> receivers = new LinkedList<DeDupeDetectionEventReceiver>();
	
	/** determine which records should be excluded from the match. */
	private RecordExclusionTemplate exclude = new RecordExclusionTemplate();
	
	/**
	 * @param search
	 */
	public DeDupeEngine(SearchEngine search) {
		this.search = search;
	}

	
	/**
	 * @return the reportFields
	 */
	public Set<String> getReportFields() {
		return reportFields;
	}


	/**
	 * @param reportFields the reportFields to set
	 */
	public void setReportFields(Set<String> reportFields) {
		this.reportFields.clear();
		this.reportFields.addAll(reportFields);
	}


	/**
	 * @return the threshold
	 */
	public float getThreshold() {
		return threshold;
	}


	/**
	 * @param threshold the threshold to set
	 */
	public void setThreshold(float threshold) {
		this.threshold = threshold;
	}


	/**
	 * @return the maxDocs
	 */
	public int getMaxDocs() {
		return maxDocs;
	}


	/**
	 * @param maxDocs the maxDocs to set
	 */
	public void setMaxDocs(int maxDocs) {
		this.maxDocs = maxDocs;
	}

	
	public boolean isRandomSelect() {
		return randomSelect;
	}


	public void setRandomSelect(boolean randomSelect) {
		this.randomSelect = randomSelect;
	}


	/**
     * @return the exclude
     */
    public RecordExclusionTemplate getExclude() {
        return exclude;
    }


    /**
     * @param exclude the exclude to set
     */
    public void setExclude(RecordExclusionTemplate exclude) {
        this.exclude = exclude;
    }

    /**
     * Empties the list of event receivers.
     */
    public void clearReceivers(){
    	receivers.clear();
    }

    /**
     * Adds rx to the list of event receivers. The event receivers will get notification
     * of matches when the de-dupe is run.
     * @param rx is the DeDupeDetectionEventReceiver to add.
     */
    public void addReciever(DeDupeDetectionEventReceiver rx){
    	receivers.add(rx);
    }
    
    /**
	 * Looks for duplicates using a worker thread to do this as a background
	 * task.
	 * @param queryText
	 * @param outputPath
	 * @param callback
	 * @throws CorruptIndexException
	 * @throws IOException
	 * @throws ParseException
	 */
	public void findMatches(String queryText, String outputPath, DeDupeProgressCallback callback) throws CorruptIndexException, IOException, ParseException{
		
		this.queryText = queryText;
		this.resultsPath = outputPath;
		this.callback = callback;
		
		if(callback != null){
			callback.register(this);
		}
		
		Thread mainThread = new Thread( new Runnable() {

			public void run() {
				try {
					
					PrintStream out = new PrintStream(DeDupeEngine.this.resultsPath);

					Match.printResultHeader(reportFields, out);
					try {

						findMatches(out);

						//showMatches(out);
					} finally {
						out.close();
					}
					
				} catch (Exception e){
					if(DeDupeEngine.this.callback != null){
						DeDupeEngine.this.callback.error(e);
					}
				}
			}
			
		}, "Main de-dupe thread");
		mainThread.start();
	}

	private void findMatches(PrintStream out) throws DeDupeException, CorruptIndexException, IOException, ParseException{
		IndexReader reader = search.getIndexReader();
		int docCount = reader.numDocs();

		// possibly limit number 
		if(maxDocs > 0 && docCount > maxDocs){
			docCount = maxDocs;
		}

		if(callback != null){
			callback.started(docCount);
		}
		
		signalStart();  
		
		initProcessed(docCount);
		//processed = new HashSet<Integer>((docCount * 3) / 2);
		//matches.clear();
		
		QueryFactory queryFactory = new QueryFactory(queryText, search.getAnalyzer());
		
		Random random = null;
		if(randomSelect){
			random = new Random();
		}
		
		int matchCount = 0;
		int i;
		for(i = 0; i<docCount && !stop; ++i){
			
			Integer index;
			
			if(random == null){
				index = new Integer(i);
			} else {
				index = new Integer(random.nextInt(docCount));
			}
			
			
			if(isProcessed(index)){
				continue;
			}
			
			Match match = matchDocument(queryFactory, reader, index);
			if(match != null){
				match.displayMatch(out, reader, reportFields);
				signalMatch(match, reader);
				//matches.add(match);
				++matchCount;
			}
			if((i+1) % 10 == 0 && callback != null){
				callback.progress(i+1, matchCount);
			}
		}
		
		signalFinish();
		
		if(callback != null){
			callback.complete(i, matchCount);
		}
	}

	


	/**
	 * Looks for hits against a given document.  Hits are used as probe images to pick up any
	 * other duplicates.
	 * @param queryFactory
	 * @param reader
	 * @param index
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	private Match matchDocument(QueryFactory queryFactory, IndexReader reader, Integer index) throws IOException, ParseException{
		
		// Scores indexed by record index.  Inherently tracks the group of matches 
		// in the key set.
		Map<Integer, Float> group = new HashMap<Integer,Float>();
		
		// Record indices of records to process.
		List<Integer> toProcess = new LinkedList<Integer>();
		toProcess.add(index);
		
		while(!toProcess.isEmpty()){
			
			// Get the head of the queue for processing.
			index = toProcess.remove(0);
			
			if(isProcessed(index)){
				continue;
			}
			
			// Mark as processed so we don't do it again.
			setProcessed(index);
			
			Document doc = reader.document(index.intValue());
			
	         if(!exclude.accept(doc)){
	             continue;
	         }

			Query query = queryFactory.generateFrom(doc);
			
//			QueryInspector inspector = new QueryInspector();
//			inspector.inspect(query, 0);

			Searcher searcher = search.getSearcher();
			Hits hits = searcher.search(query);
			
			int hitCount = hits.length();
			for(int i=0; i<hitCount; ++i){
				float score = hits.score(i);
				if(score > threshold) {

					// Always expect to find probe document so ignore it.
					if(hits.id(i) == index.intValue()){
						continue;
					}
					
					if(!exclude.accept(reader.document(index.intValue()))){
					    continue;
					}
					
					Integer idxMatched = new Integer(hits.id(i));
					
					if(!isProcessed(idxMatched)){
						toProcess.add(idxMatched);
					}
					
					Float s1 = group.get(index);
					Float s2 = group.get(idxMatched);
					
					float f1 = (s1 == null) ? 0 : s1.floatValue();
					float f2 = (s2 == null) ? 0 : s2.floatValue();
					
					group.put(index, new Float(f1 + score));
					group.put(idxMatched, new Float(f2 + score));
						
				}
			}
			
		}
		
		Match match = null;
		if(group.size() >= 2){
			match = new Match(group);
		}
		return match;
	}

	/**
	 * @param docCount
	 */
	private void initProcessed(int docCount) {
		processed = new BitSet(docCount);
	}


	/**
	 * @param index
	 * @return
	 */
	private boolean isProcessed(Integer index) {
		boolean p = processed.get(index.intValue());
		//System.out.println("Record " + index.toString() + ((p)?" is " : " is not ") + "processed");
		return p;
	}


	/**
	 * @param index
	 */
	private void setProcessed(Integer index) {
		//System.out.println("Marking Record " + index.toString() + " processed");
		processed.set(index.intValue());
	}


	/**
	 * Signal to stop processing.
	 */
	public void stopProcessing() {
		stop = true;
	}

	private void signalStart() throws DeDupeException{
	    for(DeDupeDetectionEventReceiver rx : receivers) {
	        rx.start(reportFields);
	    }
	}
	
	private void signalMatch(Match match, IndexReader index) throws DeDupeException{
        for(DeDupeDetectionEventReceiver rx : receivers) {
            rx.onMatch(match, index, reportFields);
        }
	    
	}
	
	private void signalFinish() throws DeDupeException {
        for(DeDupeDetectionEventReceiver rx : receivers)  {
            rx.finish();
        }
	    
	}
	 	
}
