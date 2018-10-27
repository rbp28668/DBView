/**
 * 
 */
package uk.co.alvagem.dbview.dedupe;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Properties;
import java.util.Set;

import javax.swing.JFileChooser;

import org.apache.lucene.index.IndexReader;

/**
 * De-Dupe event receiver to write duplicates to a CSV file.
 * @author bruce.porteous
 *
 */
public class FileDeDupeReceiver implements DeDupeDetectionEventReceiver {

	private String resultsPath;
	private PrintStream out;
	
	public FileDeDupeReceiver(){
		resultsPath = System.getProperty("user.home");
		resultsPath += File.pathSeparator + "duplicates.csv";
	}
	
	/* (non-Javadoc)
	 * @see uk.co.alvagem.dbview.dedupe.DeDupeDetectionEventReceiver#edit()
	 */
	public void edit(Component parent )  throws DeDupeException{
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Duplicate Detection Output");
		chooser.setApproveButtonText("Set Output");
		chooser.setApproveButtonToolTipText("Set the output path for the duplicate detection");
		chooser.setSelectedFile(new File(resultsPath));
		int returnVal = chooser.showSaveDialog(parent);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	        resultsPath = chooser.getSelectedFile().getAbsolutePath();
	     }

	}

	/* (non-Javadoc)
	 * @see uk.co.alvagem.dbview.dedupe.DeDupeDetectionEventReceiver#finish()
	 */
	public void finish() throws DeDupeException {
		out.close();

	}

	/* (non-Javadoc)
	 * @see uk.co.alvagem.dbview.dedupe.DeDupeDetectionEventReceiver#getProperties()
	 */
	public Properties getProperties()  throws DeDupeException{
		Properties props = new Properties();
		props.put("OutputPath", resultsPath);
		return props;
	}

	/* (non-Javadoc)
	 * @see uk.co.alvagem.dbview.dedupe.DeDupeDetectionEventReceiver#onMatch(uk.co.alvagem.dbview.dedupe.Match, org.apache.lucene.index.IndexReader)
	 */
	public void onMatch(Match match, IndexReader index, Set<String>reportFields)  throws DeDupeException{
		try {
			match.displayMatch(out, index, reportFields);
		} catch (Exception e) {
			throw new DeDupeException("Unable to output duplicate information ", e);
		}
	}

	/* (non-Javadoc)
	 * @see uk.co.alvagem.dbview.dedupe.DeDupeDetectionEventReceiver#setProperties(java.util.Properties)
	 */
	public void setProperties(Properties props)  throws DeDupeException{
		String path = props.getProperty("OutputPath");
		if(path != null){
			resultsPath = path;
		}

	}

	/* (non-Javadoc)
	 * @see uk.co.alvagem.dbview.dedupe.DeDupeDetectionEventReceiver#start()
	 */
	public void start(Set<String>reportFields)  throws DeDupeException{
		try {
			out = new PrintStream(resultsPath);

			Match.printResultHeader(reportFields, out);
		} catch (FileNotFoundException e) {
			throw new DeDupeException("Unable to write de-dupe results", e);
		}

	}

}
