/**
 * 
 */
package uk.co.alvagem.dbview;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author bruce.porteous
 *
 */
public class DriverClassloader extends URLClassLoader {

	
	/** Singleton instance */
	private static DriverClassloader instance = null;
	
	private static final String JAR_PREFIX = "jar:file:///";
	
	/**
	 * Start with an empty list of URLs
	 */
	private DriverClassloader() {
		super(new URL[]{});
	}

	public static synchronized DriverClassloader getInstance(){
		if(instance == null) {
			instance = new DriverClassloader();
		}
		return instance;
	}

	public void addExtensionFolder(String path) throws IOException{
		File dir = new File(path);
		if(!dir.exists()){
			throw new IOException("Extension path " + path + " does not exist");
		}
		if(!dir.isDirectory()){
			throw new IOException("Extension path " + path + " must reference a directory");
		}
		
		String[] jarNames = findJars(dir);
		for(int i=0; i<jarNames.length; ++i){
			findJarClasses(dir,jarNames[i]);
		}
	}
	
	private String[] findJars(File dir){
		FilenameFilter filter = new FilenameFilter() {

			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
			
		};
		String[] files = dir.list(filter);
		return files;
	}

	private void findJarClasses(File dir, String jarName) throws IOException{
		File path = new File(dir,jarName);
		String urlPath = JAR_PREFIX + path.getCanonicalPath() + "!/";
		URL url = new URL(urlPath);
		addURL(url);
	}
	
}
