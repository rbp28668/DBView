/**
 * 
 */
package uk.co.alvagem.dbview.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * BasicFileFilter provides a simple file filter that only accepts files
 * with the suffix xml.
 * @author Bruce.Porteous
 *
 */
public class BasicFileFilter extends FileFilter {

    private String extension;
    private String description;
    /**
	 * Constructor for the filter.
	 */
	public BasicFileFilter(String extension, String description) {
        super();
        this.extension = extension;
        this.description = description;
    }
    
    /* (non-Javadoc)
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 * Accepts .xml files
	 */
	public boolean accept(File f) {
        return f.isDirectory() || f.getName().toLowerCase().endsWith(extension);
    }
    
    /* (non-Javadoc)
	 * @see javax.swing.filechooser.FileFilter#getDescription()
	 */
	public String getDescription() {
        return description;
    }
}    

