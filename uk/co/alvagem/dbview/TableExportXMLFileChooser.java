/**
 * 
 */
package uk.co.alvagem.dbview;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import uk.co.alvagem.dbview.util.XMLFileFilter;

/**
 * @author bruce.porteous
 *
 */
public class TableExportXMLFileChooser extends JFileChooser {

	private static final long serialVersionUID = 1L;
	private AccessoryPanel accessory;

	/**
	 * 
	 */
	public TableExportXMLFileChooser() {
		super();
        setDialogTitle("Select export path");
        setApproveButtonText("Export");
        setFileFilter( new XMLFileFilter());
        accessory = new AccessoryPanel();
        setAccessory(accessory);
	}

	public boolean exportNulls(){
		return accessory.exportNulls.isSelected();
	}

	public void setExportNulls(boolean export){
		accessory.exportNulls.setSelected(export);
	}

	public String getRootTag(){
		return accessory.rootTag.getText();
	}

	public void setRootTag(String rootTag){
		accessory.rootTag.setText(rootTag);
	}

	public String getRowTag(){
		return accessory.rowTag.getText();
	}

	public void setRowTag(String rootTag){
		accessory.rowTag.setText(rootTag);
	}
	
	private class AccessoryPanel extends JPanel{
		private static final long serialVersionUID = 1L;
		private JCheckBox exportNulls = new JCheckBox("Export Nulls");
		private JTextField rootTag = new JTextField(10);
		private JTextField rowTag = new JTextField(10);
		AccessoryPanel(){
			setBorder(new StandardBorder("Settings"));
			
			GridBagLayout gb = new GridBagLayout();
			setLayout(gb);
			GridBagConstraints c = new GridBagConstraints();
            c.gridheight = 1;
            c.gridwidth = 1;
            c.weightx = 1.0f;
            c.weighty = 0.0f;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.LINE_START;
            c.ipadx = 10;
            c.insets = new Insets(5,2,5,2);
			
			c.gridwidth = GridBagConstraints.REMAINDER; //end row
			gb.setConstraints(exportNulls,c);
			add(exportNulls);
			
            addTextField(gb, c, "Root Element", rootTag);
            addTextField(gb, c, "Row Element", rowTag);

		}
		
		private void addTextField(GridBagLayout gb, GridBagConstraints c, String labelText, JTextField field) {
			c.gridwidth = 1; // start of row.
            c.fill = GridBagConstraints.HORIZONTAL;

            JLabel label = new JLabel(labelText);
            gb.setConstraints(label,c);
            add(label);

            c.fill = GridBagConstraints.NONE;
            c.gridwidth = GridBagConstraints.REMAINDER; //end row
            
            gb.setConstraints(field,c);
            add(field);
		}
	}
	
}
