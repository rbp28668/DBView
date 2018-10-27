/**
 * 
 */
package uk.co.alvagem.dbview.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;

import org.xml.sax.Attributes;

import uk.co.alvagem.dbview.dedupe.CallbackManager;
import uk.co.alvagem.dbview.dedupe.DeDupeDetectionEventReceiver;
import uk.co.alvagem.dbview.dedupe.DeDupeException;
import uk.co.alvagem.dbview.util.FactoryBase;
import uk.co.alvagem.dbview.util.IXMLContentHandler;
import uk.co.alvagem.dbview.util.InputException;

/**
 * Factory class to de-serialise a database definition and its children from XML.
 * @author bruce.porteous
 *
 */
public class DatabaseFactory extends FactoryBase implements IXMLContentHandler {

	private CallbackManager templateCallbackManager;
	private Databases databases;
	private Database currentDatabase = null;
	private Query currentQuery = null;
	private SearchIndex currentIndex = null;
	private String text;
	private DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
	private String excludeField = null;
	private String excludePattern = null;
	private String callbackName = null;
	private boolean callbackEnabled = true;
	private Properties callbackProperties = null;
	private String callbackPropertyName = null;
	
	/**
	 * 
	 */
	public DatabaseFactory(Databases databases, CallbackManager templateCallbackManager) {
		super();
		this.templateCallbackManager = templateCallbackManager;
		this.databases = databases;
	}

	/* (non-Javadoc)
	 * @see uk.co.alvagem.dbview.util.IXMLContentHandler#startElement(java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String local, Attributes attrs)
			throws InputException {
		if(local.equals("Database")){
			if(currentDatabase != null){
				throw new InputException("Nested Databases in input XML");
			}
			
			String name = getString(attrs, "name");
			String driverClass = getString(attrs, "driver");
			String url = getString(attrs, "url");
			String serverName = getString(attrs, "server");
			int portNumber = getInt(attrs, "port" );
			String databaseName = getString(attrs, "database" );
			String userName = getString(attrs, "user");
			String password = getString(attrs, "pwd");
			String selectMethod = getString(attrs, "select");
			
			currentDatabase = new Database();
			currentDatabase.setName(name);
			currentDatabase.setDriverClass(driverClass);
			currentDatabase.setUrl(url);
			currentDatabase.setServerName(serverName);
			currentDatabase.setPortNumber(portNumber);
			currentDatabase.setDatabaseName(databaseName);
			currentDatabase.setUserName(userName);
			currentDatabase.setPassword(password);
			currentDatabase.setSelectMethod(selectMethod);
			
			databases.addDatabase(currentDatabase);
		} else if(local.equals("Query")){
			if(currentDatabase == null){
				throw new InputException("Query definition without enclosing database");
			}
			if(currentQuery != null){
				throw new InputException("Nested Query in input XML");
			}
			String name = getString(attrs, "name");
			currentQuery = new Query();
			currentQuery.setName(name);
		} else if(local.equals("SearchIndex")){
			if(currentDatabase == null){
				throw new InputException("SearchIndex definition without enclosing database");
			}
			if(currentIndex != null){
				throw new InputException("Nested SearchIndex in input XML");
			}
			String name = getString(attrs, "name");
			currentIndex = new SearchIndex();
			currentIndex.setName(name);
			try {
				currentIndex.setManager(templateCallbackManager.copy());
			} catch (DeDupeException e) {
				throw new InputException("Unable to create de-dupe callback manager",e);
			}

		} else if(local.equals("Exclusions")){
            if(currentIndex == null){
                throw new InputException("Exclusions must be a child of SearchIndex");
            }
		    currentIndex.getExclusions().clear();
		} else if(local.equals("DeDupeEventCallback")){
            if(currentIndex == null){
                throw new InputException("DeDupe event callbacks must be a child of SearchIndex");
            }
            String name = getString(attrs,"name");
            if(name == null){
            	throw new InputException("Missing name from de-dupe event callback");
            }
            callbackEnabled = "true".equals(getString(attrs,"enabled"));
			callbackName = name;
			callbackProperties = new Properties();
			
		} else if(local.equals("DeDupeEventProperty")){
            String name = getString(attrs,"name");
            if(name == null){
            	throw new InputException("Missing name from de-dupe event callback property");
            }
            callbackPropertyName = name;

		}

	}

	/* (non-Javadoc)
	 * @see uk.co.alvagem.dbview.util.IXMLContentHandler#endElement(java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String local) throws InputException {
		if(local.equals("Database")){
			currentDatabase = null;
		} else if(local.equals("Query")){
			currentQuery.setText(text);
			currentDatabase.getQueries().addQuery(currentQuery);
			currentQuery = null;
		} else if(local.equals("SearchIndex")){
			currentDatabase.getSearchIndices().addSearchIndex(currentIndex);
			currentIndex = null;
		} else if(local.equals("Selected")){
			currentIndex.getSelectedFields().add(text);
		} else if(local.equals("SearchExpression")){
			currentIndex.setText(text);
		} else if(local.equals("IndexPath")){
			currentIndex.setIndexPath(text);
		} else if(local.equals("IndexDate")){
			try {
				currentIndex.setIndexDate(df.parse(text));
			} catch (ParseException e) {
				throw new InputException(e.getMessage(),e); 
			}
		} else if(local.equals("SourceSQL")){
			currentIndex.setSourceSQL(text);
		} else if(local.equals("DeDupeLimit")){
			currentIndex.setDeDupeLimit(Integer.parseInt(text));
		} else if(local.equals("DeDupeThreshold")){
			currentIndex.setDeDupeThreshold(Float.parseFloat(text));
		} else if (local.equals("ExcludeField")){
		    excludeField = text;
        } else if (local.equals("ExcludePattern")){
            excludePattern = text;
        } else if (local.equals("Exclude")){
            currentIndex.getExclusions().add(excludeField,excludePattern);
        } else if(local.equals("DeDupeEventCallback")){
        	DeDupeDetectionEventReceiver rx = currentIndex.getManager().getReceivers().get(callbackName);
        	// Possible that the list of potential receivers has changed so rx may be null.
        	if(rx != null){
        		try {
					rx.setProperties(callbackProperties);
					currentIndex.getManager().setEnabled(callbackName, callbackEnabled);
				} catch (DeDupeException e) {
					throw new InputException("Unable to set properties for de-dupe callback " + callbackName, e);
				}
        	}
        	callbackName = null;
        	callbackProperties = null;
        } else if(local.equals("DeDupeEventProperty")){
        	if(callbackProperties == null){
        		throw new InputException("Missing properties");
        	}
        	if(callbackPropertyName == null){
        		throw new InputException("Missing property name");
        	}
        	callbackProperties.setProperty(callbackPropertyName, text);
        	callbackPropertyName = null;
        }



	}

	/* (non-Javadoc)
	 * @see uk.co.alvagem.dbview.util.IXMLContentHandler#characters(java.lang.String)
	 */
	public void characters(String str) throws InputException {
		text = str;
	}

}
