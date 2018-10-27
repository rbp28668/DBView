/**
 * 
 */
package uk.co.alvagem.dbview.model;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import uk.co.alvagem.dbview.dedupe.CallbackManager;
import uk.co.alvagem.dbview.dedupe.DeDupeDetectionEventReceiver;
import uk.co.alvagem.dbview.util.XMLWriter;

/**
 * Tracks the data in a Search Index.  SearchIndices are always built from a SQL query.
 * @author bruce.porteous
 *
 */
public class SearchIndex  extends Query{
	/** Path to the search index folder in the filesystem. If null then the search index is in memory */
	private String indexPath;
	
	/** Date/time when the search index is created.  Important as it effectively snapshots the source data */
	private Date indexDate;
	
	/** Source SQL used to create the search index */
	private String sourceSQL;
	
	//private List<String> fieldNames = new LinkedList<String>();
	/** Which fields we want to display during de-duping */
	private Set<String> selectedFields = new LinkedHashSet<String>();
	
	/** Maximum number of records to process.  If 0 then no limit.  Used for testing */
	private int deDupeLimit = 0;
	
	/** Only consider as duplicate if the search score is above this threshold */
	private float deDupeThreshold = 0.8f;
	
	/** Randomly select records to process from the index.  Used for testing in conjunction
	 * with deDupeLimit to only process a subset of the full index */
	private boolean randomSelect;

	/** Define which records should be ignored */
	private RecordExclusionTemplate exclusions = new RecordExclusionTemplate();
	
//	/** Externalises state of de-dupe callbacks */
//	private Map<String,Properties>callbackState = new HashMap<String,Properties>();
	
	/** How to deal with the results of a de-dupe */
	private CallbackManager manager;
	
	
	/**
	 * @return the indexPath
	 */
	public String getIndexPath() {
		return indexPath;
	}
	/**
	 * @param indexPath the indexPath to set
	 */
	public void setIndexPath(String indexPath) {
		this.indexPath = indexPath;
	}
	/**
	 * @return the indexDate
	 */
	public Date getIndexDate() {
		return indexDate;
	}
	/**
	 * @param indexDate the indexDate to set
	 */
	public void setIndexDate(Date indexDate) {
		this.indexDate = indexDate;
	}

	/**
	 * Get the SQL used to build the index.
	 * @return the sourceSQL
	 */
	public String getSourceSQL() {
		return sourceSQL;
	}
	
	/**
	 * @param sourceSQL the sourceSQL to set
	 */
	public void setSourceSQL(String sourceSQL) {
		this.sourceSQL = sourceSQL;
	}
	
	
	/**
	 * @return the selectedFields
	 */
	public Set<String> getSelectedFields() {
		return selectedFields;
	}
	/**
	 * @param selectedFields the selectedFields to set
	 */
	public void setSelectedFields(Set<String> selectedFields) {
		this.selectedFields = selectedFields;
	}
	
	
	/**
	 * @return the deDupeLimit
	 */
	public int getDeDupeLimit() {
		return deDupeLimit;
	}
	/**
	 * @param deDupeLimit the deDupeLimit to set
	 */
	public void setDeDupeLimit(int deDupeLimit) {
		this.deDupeLimit = deDupeLimit;
	}
	/**
	 * @return the deDupeThreshold
	 */
	public float getDeDupeThreshold() {
		return deDupeThreshold;
	}
	/**
	 * @param deDupeThreshold the deDupeThreshold to set
	 */
	public void setDeDupeThreshold(float deDupeThreshold) {
		this.deDupeThreshold = deDupeThreshold;
	}
	
	
	/**
	 * @return
	 */
	public boolean isRandomSelect() {
		return randomSelect;
	}
	/**
	 * @param randomSelect
	 */
	public void setRandomSelect(boolean randomSelect) {
		this.randomSelect = randomSelect;
	}
	
	
	/**
     * @return the exclusions
     */
    public RecordExclusionTemplate getExclusions() {
        return exclusions;
    }
    /**
     * @param exclusions the exclusions to set
     */
    public void setExclusions(RecordExclusionTemplate exclusions) {
        this.exclusions = exclusions;
    }

    
	/**
	 * @return the manager
	 */
	public CallbackManager getManager() {
		return manager;
	}
	/**
	 * @param manager the manager to set
	 */
	public void setManager(CallbackManager manager) {
		this.manager = manager;
	}
	
//	/**
//	 * @param callbackName
//	 * @param callbackProperties
//	 */
//	public void addCallbackProperties(String callbackName,
//			Properties callbackProperties) {
//		callbackState.put(callbackName, callbackProperties);
//	}
    
    /* (non-Javadoc)
	 * @see uk.co.alvagem.dbview.Query#writeXML(uk.co.alvagem.dbview.util.XMLWriter)
	 */
	public void writeXML(XMLWriter writer) throws IOException {
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		writer.startEntity("SearchIndex");
		writer.addAttribute("name",getName());
		writer.textEntity("SearchExpression",getText());
		writer.textEntity("IndexPath", getIndexPath());
		if(getIndexDate() != null){
			writer.textEntity("IndexDate", df.format(getIndexDate()));
		}
		if(getSourceSQL() != null){
			writer.textEntity("SourceSQL", getSourceSQL());
		}
		
		for(String selected : selectedFields ){
			writer.textEntity("Selected", selected);
		}
		writer.textEntity("DeDupeLimit", Integer.toString(deDupeLimit));
		writer.textEntity("DeDupeThreshold", Float.toString(deDupeThreshold));
		writer.textEntity("DeDupeRandomSelect", Boolean.toString(randomSelect));
		
		exclusions.writeXML(writer);
		
		writeCallbackXML(writer);
		
		writer.stopEntity();
	}

	private void writeCallbackXML(XMLWriter writer) throws IOException {
		
		try {
			writer.startEntity("DeDupeCallbacks");
			for (Map.Entry<String, DeDupeDetectionEventReceiver> entry : manager.getReceivers()
					.entrySet()) {
				
				String name = entry.getKey();
				writer.startEntity("DeDupeEventCallback");
				writer.addAttribute("name", name);
				writer.addAttribute("enabled", manager.isEnabled(name));
				Properties props = entry.getValue().getProperties();
				for (String key : props.stringPropertyNames()) {
					String value = props.getProperty(key);

					writer.startEntity("DeDupeEventProperty");
					writer.addAttribute("name", value);
					writer.text(value);
					writer.stopEntity();
				}
				writer.stopEntity();
			}
			writer.stopEntity();
		} catch (Exception e) {
			throw new IOException("Unable to write de-dupe callback info",e);
		}
	}
	
}
