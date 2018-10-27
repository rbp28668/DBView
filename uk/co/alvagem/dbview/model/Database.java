/**
 * 
 */
package uk.co.alvagem.dbview.model;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import uk.co.alvagem.dbview.DriverClassloader;
import uk.co.alvagem.dbview.util.XMLWriter;

/**
 * @author bruce.porteous
 *
 */
public class Database {

	private String name = null;
	private	String driverClassName = "net.sourceforge.jtds.jdbc.Driver";
	//String driverClass = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
	private String url = "jdbc:jtds:sqlserver://";
	private String serverName = "localhost"; 
	private int portNumber = 0;
	private String databaseName = "";
	private String userName = "";
	private String password = "";
	private String selectMethod = "";

	private TableTypes tableTypes = null;
	private List<Schema> schemas = null;
	
	/** Queries that are attached to this database */
	private Queries queries = new Queries();
	
	/** SearchIndices associated with this database */
	private SearchIndices searchIndices = new SearchIndices();
	
	/**
	 * 
	 */
	public Database() {
		super();
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the driverClass.
	 */
	public String getDriverClass() {
		return driverClassName;
	}

	/**
	 * @param driverClass The driverClass to set.
	 */
	public void setDriverClass(String driverClass) {
		this.driverClassName = driverClass;
	}

	/**
	 * @return Returns the databaseName.
	 */
	public String getDatabaseName() {
		return databaseName;
	}

	/**
	 * @param databaseName The databaseName to set.
	 */
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	/**
	 * @return Returns the password.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password The password to set.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return Returns the portNumber.
	 */
	public int getPortNumber() {
		return portNumber;
	}

	/**
	 * @param portNumber The portNumber to set.
	 */
	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}

	/**
	 * @return Returns the selectMethod.
	 */
	public String getSelectMethod() {
		return selectMethod;
	}

	/**
	 * @param selectMethod The selectMethod to set.
	 */
	public void setSelectMethod(String selectMethod) {
		this.selectMethod = selectMethod;
	}

	/**
	 * @return Returns the serverName.
	 */
	public String getServerName() {
		return serverName;
	}

	/**
	 * @param serverName The serverName to set.
	 */
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	/**
	 * @return Returns the url.
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url The url to set.
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return Returns the userName.
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName The userName to set.
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String toString(){
		return name;
	}
	
	public void writeXML(XMLWriter writer) throws IOException {
		writer.startEntity("Database");
		writer.addAttribute("name",name);
		writer.addAttribute("driver",driverClassName);
		writer.addAttribute("url",url);
		writer.addAttribute("server",serverName);
		writer.addAttribute("port", portNumber);
		writer.addAttribute("database", databaseName);
		writer.addAttribute("user",userName);
		writer.addAttribute("pwd",password);
		writer.addAttribute("select",selectMethod);
		queries.writeXML(writer);
		searchIndices.writeXML(writer);
		writer.stopEntity();
	}
	
	public Connection getConnection() throws SQLException{
		String connectionString = url;
		
		if(serverName != null && serverName.length() > 0){
			connectionString += serverName;
		}
		
		if(portNumber != 0){
			connectionString += ":"	+ portNumber;
		}
		//TODO - make properly generic.
		if(databaseName != null && databaseName.length() > 0){
			if(connectionString.contains("Cache")){
				connectionString += "/" + databaseName;
			} else if (connectionString.contains("oracle")){
				connectionString += ":" + databaseName; // aka SID
			} else {
				connectionString += ";databaseName=" + databaseName;
			}
		}
		if(selectMethod != null && selectMethod.length() > 0){
			connectionString += ";selectMethod=" + selectMethod;
		}
		//connectionString += ";";

		Driver drv = null;
		
		try {
			ClassLoader loader = DriverClassloader.getInstance();
			Class<?> driverClass =  Class.forName(driverClassName, true, loader);
			drv = (Driver) driverClass.newInstance();
			//System.out.println("Loaded " + drv.getClass().getCanonicalName());

		} catch(ClassNotFoundException ex) {
			throw new SQLException("Unable to find driver: " + driverClassName);
		} catch (InstantiationException ix) {
			throw new SQLException("Unable to instantiate driver: " + driverClassName);
		} catch (IllegalAccessException ix) {
			throw new SQLException("Unable to access driver: " + driverClassName);
		}
		
		Properties p = new Properties();
		
//		if(drv.acceptsURL(connectionString)) {
//			System.out.println("Connection string is valid");
////			DriverPropertyInfo info[] = drv.getPropertyInfo(connectionString, null);
////			for(DriverPropertyInfo i : info){
////				System.out.println(i.name + "(" + i.description + ")" + (i.required ? " is ":" is not ") + "required");
////			}
//		} else {
//			System.out.println("Connection string is not valid");
//		}
		
		if(userName != null){
			p.put("USER", userName);  // SQL Server, oracle in uppercase
			p.put("user", userName);	// Cache in lower case
		}
		if(password != null){
			p.put("PASSWORD", password);
			p.put("password", password);
		}

		// Optional for Oracle - TODO - allow default params.
        //p.put("defaultRowPrefetch", 100);

//		if(drv.acceptsURL(connectionString)) {
//			System.out.println("Connection string is valid");
//		}
//		
//		System.out.println("Connection String: " + connectionString );
		Connection con = null;
		con = drv.connect(connectionString, p);
//		if (con != null) {
//			System.out.println("Connected to " + con.getCatalog());
//		}
	
		// leave auto-commit set on.  The transaction handling in
		// SQLServerDB will turn it off if someone explicitly starts
		// a transaction.
		
		return con;
	}
	
	public boolean schemasLoaded(){
		return schemas != null;
	}
	
	public Collection<Schema> getSchemas() throws SQLException{
		if(!schemasLoaded()) {
			loadSchemas();
		}
		return  Collections.unmodifiableCollection(schemas);
	}

	public TableTypes getTableTypes() throws SQLException {
		if(!schemasLoaded()) {
			loadSchemas();
		}
		return  tableTypes;
	}

	/**
	 * Allow lazy loading of database schemas - this needs to be called before
	 * getSchemas or getTableTypes can be called.
	 * @throws SQLException
	 */
	private void loadSchemas() throws SQLException {
		Connection con = getConnection();
		List<Schema> newSchemas = new LinkedList<Schema>();
		try {
			DatabaseMetaData meta = con.getMetaData();
			ResultSet rs = meta.getSchemas();
			while(rs.next()){
				Schema schema = Schema.fromMeta(this,rs);
				newSchemas.add(schema);
			}
			rs.close();
			
			TableTypes newTableTypes = new TableTypes();
			newTableTypes.fromMeta(meta);
			
			schemas = newSchemas;
			tableTypes = newTableTypes;
		} finally {
			con.close();
		}
	}

	/**
	 * @return Returns the queries.
	 */
	public Queries getQueries() {
		return queries;
	}
	
	/**
	 * @return
	 */
	public SearchIndices getSearchIndices() {
		return searchIndices;
	}
}
