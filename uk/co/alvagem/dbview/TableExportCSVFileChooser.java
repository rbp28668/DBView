/**
 * 
 */
package uk.co.alvagem.dbview;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import uk.co.alvagem.dbview.util.BasicFileFilter;

/**
 * @author bruce.porteous
 *
 */
public class TableExportCSVFileChooser extends JFileChooser {
	private static final long serialVersionUID = 1L;

	private AccessoryPanel accessory;
	
	/**
	 * 
	 */
	public TableExportCSVFileChooser() {
		super();
        setDialogTitle("Select export path");
        setApproveButtonText("Export");
        setFileFilter( new BasicFileFilter(".csv","Comma Separated Variable files"));
        accessory = new AccessoryPanel();
        setAccessory(accessory);
	}

	public boolean exportNulls(){
		return accessory.exportNulls.isSelected();
	}

	public void setExportNulls(boolean export){
		accessory.exportNulls.setSelected(export);
	}
	

	public boolean headerRow(){
		return accessory.headerRow.isSelected();
	}
	
	public void setHeaderRow(boolean header){
		accessory.headerRow.setSelected(header);
	}
	
	private class AccessoryPanel extends JPanel{
		private static final long serialVersionUID = 1L;
		private JCheckBox exportNulls = new JCheckBox("Export Nulls");
		private JCheckBox headerRow = new JCheckBox("Add Header Row");
		
		AccessoryPanel(){
			setBorder(new StandardBorder("Settings"));
			setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
			add(exportNulls);
			add(headerRow);
		}
	}

}
