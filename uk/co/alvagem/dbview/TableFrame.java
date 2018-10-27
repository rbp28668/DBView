package uk.co.alvagem.dbview;


import java.sql.SQLException;

import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

/**
 * Class for displaying tables (with nothing else!). 
 * @author bruce.porteous
 *
 */
public class TableFrame extends JInternalFrame {
	private static final long serialVersionUID = 1L;

	/**
	 * @throws SQLException 
	 * 
	 */
	public TableFrame(TableModel model, String title) throws SQLException {
		super(title);

	    setResizable(true);
	    setMaximizable(true);
	    setIconifiable(true);
	    setClosable(true);
		
		JTable tab = new JTable(model) {
			private static final long serialVersionUID = 1L;
			public boolean getScrollableTracksViewportWidth() {
				return false;
			}
		};
		tab.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		JScrollPane scroll = new JScrollPane();
		scroll.setViewportView(tab);
		getContentPane().add(scroll);
		
		pack();
		setVisible(true);
	}
	


}
