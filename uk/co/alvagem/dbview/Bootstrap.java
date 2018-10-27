/**
 * 
 */
package uk.co.alvagem.dbview;

import java.io.File;
import java.lang.reflect.Method;

/**
 * @author bruce.porteous
 *
 */
public class Bootstrap {

	/**
	 * 
	 */
	public Bootstrap() {
		super();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {

			DriverClassloader loader = DriverClassloader.getInstance();

			String dir = System.getProperty("user.dir");
			File currentDir = new File(dir);
			
			File driverDir = new File(currentDir,"drivers");
			if(driverDir.exists() && driverDir.isDirectory()){
				loader.addExtensionFolder(driverDir.getAbsolutePath());
			}

			File extensionDir = new File(currentDir,"extensions");
			if(extensionDir.exists() && extensionDir.isDirectory()){
				loader.addExtensionFolder(extensionDir.getAbsolutePath());
			}
			
			Class dbViewClass = loader.loadClass(DBView.class.getCanonicalName());
			
			Class[] formalParams = new Class[]{String[].class};
			Method main = dbViewClass.getMethod("main", formalParams);
			Object[] actualParams = new Object[]{args};
			main.invoke(null,actualParams);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
