/**
 * 
 */
package uk.co.alvagem.dbview;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * JTable for displaying results of queries.  It is NULL aware and also provides a
 * row header with row numbers.
 * @author bruce.porteous
 */
public class QueryTable extends JTable {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param arg0
	 */
	public QueryTable(TableModel model) {
		super(model);
		setWidths(model);
		setDefaultRenderer(Object.class, new NullAwareCellRenderer());
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setVisible(true);
	}

	/**
	 * Sets the column widths using the widths of the column names.
	 * @param model is the table model to use for the columns.
	 */
	private void setWidths(TableModel model){

		JTableHeader header = getTableHeader();
		Font font = header.getFont();
		FontMetrics metrics = header.getFontMetrics(font);
		
		int minWidth = metrics.stringWidth(" NULL ");
		int em = metrics.stringWidth("m");
		TableColumnModel columnModel = header.getColumnModel();
		
		int totalWidth = 0;
		int nColumns = columnModel.getColumnCount();
		for(int i=0; i<nColumns; ++i){
			String name = getColumnName(i);
			TableColumn column = columnModel.getColumn(i);
			int width = metrics.stringWidth(name) + 2* em;
			if(width < minWidth) {
				width = minWidth;
			}
			column.setPreferredWidth(width);
			totalWidth += width;
		}
		
		
	}


	/* (non-Javadoc)
	 * @see javax.swing.JTable#getScrollableTracksViewportWidth()
	 */
	public boolean getScrollableTracksViewportWidth() {
		return false;
	}
	
	/**
	 * Creates the row header for the table.
	 * @return a new TableRowHeader linked to this table.
	 */
	public TableRowHeader createRowHeader(){
		TableModel model = getModel();
		TableRowHeader header = new TableRowHeader(this, model);
		return header;
	}
	
	/**
	 * A CellRenderer that displays any null entries as <i>NULL</i>.
	 */
	private class NullAwareCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;
		private JLabel label;

		public Component getTableCellRendererComponent(JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
			if(value != null) {
				return super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
			} else {
				if(label == null) {
					label = new JLabel(" NULL ");
					Font font = label.getFont();
					font = font.deriveFont(Font.ITALIC);
					label.setFont(font);
				}
				if(isSelected){
					label.setOpaque(true);
					label.setBackground(table.getSelectionBackground());
				} else {
					label.setBackground(table.getBackground());
				}
				return label;
			}
		}
	}
	
	/**
	 * TableRowHeader is the component that implements the table row header strip down the
	 * left hand side with the row numbers.
	 * @author rbp28668
	 */
	public static class TableRowHeader extends JComponent implements ListSelectionListener {
		
	    /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private TableModel tableModel;
		private JTable table;
		private int width;
		private int labelHeight;
		private Font font;
		
		private TableRowHeader(JTable table, TableModel tableModel){
			
			this.table = table;
			this.tableModel = tableModel;
			
			width = 0;
			font = table.getFont();
			FontMetrics fontMetrics = table.getFontMetrics(font);
			width = fontMetrics.stringWidth("999999");
			labelHeight = fontMetrics.getAscent();
		}
		
		public Dimension getPreferredSize(){
			return new Dimension(width, tableModel.getRowCount() * table.getRowHeight());
		}
		
		public void paint(Graphics g){
			Graphics2D g2d = (Graphics2D)g;
			
			
			Rectangle clip = g2d.getClipBounds();
			int height = table.getRowHeight();
			int start = clip.y / height;
			int finish = 2+(clip.y + clip.height)/height;
			
			int yOffset = 1+ (height - labelHeight) / 2; // centre vertically.
			
			int iy = start * height;
			g2d.setFont(font);
			for(int i=start; i<finish; ++i){
				
				if(table.isRowSelected(i)){
				    g2d.setBackground(table.getSelectionBackground());
				} else {
				    g2d.setBackground(table.getBackground());
				}
				g2d.clearRect(0,iy,width,height);
				
				g2d.setColor(table.getGridColor());
				g2d.drawRect(0,iy-1,width-1,height);
				
				String text = Integer.toString(i);
				
				g2d.drawString(text, 2, iy-yOffset);
				
				iy += height;
			}
		}

        /* (non-Javadoc)
         * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
         */
        public void valueChanged(ListSelectionEvent arg0) {
            repaint();
        }
		
	}
	

}
