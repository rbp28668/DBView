package uk.co.alvagem.dbview;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;

import uk.co.alvagem.dbview.model.Database;
import uk.co.alvagem.dbview.model.Query;

/**
	 * A JPanel that fills each of the tabs in the query frame.  Manages the
	 * query text and results.
	 * @author bruce.porteous
	 *
	 */
	/**
	 * @author bruce.porteous
	 *
	 */
	public abstract class QueryPane extends JPanel {
		/** Serialisation version */
		private static final long serialVersionUID = 1L;
		
		/** Split pane to hold query on top, results on bottom panes */
		private JSplitPane splitter;
		
		/** Status window at bottom of pane */
		private JTextField statusText;
		
		/** Main query text editing pane */
		private JTextArea queryText;
		
		/** Table model containing results of query or search */
		private TableModel model;
		
		/** Table for displaying current results table model */
		private QueryTable table;
		
		/** For scrolling results */
		private JScrollPane scrollPane;
		

		/**
		 * Creates a new QueryPane.
		 * @param index is used to set up the name of the Pane in the tabs so
		 * by default the panes are called Query 1, Query 2 etc.
		 */
		public QueryPane(String name, String initialQuery){
			
			splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			queryText = new JTextArea();
			queryText.setRows(10);
			queryText.setColumns(40);
			queryText.setText(initialQuery);
			splitter.setTopComponent(queryText);
			
	        scrollPane = new JScrollPane();
	        splitter.setBottomComponent(scrollPane);
	        
	        statusText = new JTextField();
	        
	        setLayout(new BorderLayout());
	        add(splitter, BorderLayout.CENTER);
	        add(statusText, BorderLayout.SOUTH);
	        
			super.setName(name);
		}
		

		/**
		 * Gets the current table model containing results.
		 * @return the current table model. May be null if no queries run.
		 */
		public TableModel getModel() {
			return model;
		}

		/**
		 * Gets the text from the query pane.
		 * @return the query text.
		 */
		public String getQueryText(){
			return queryText.getText();
		}
		
		/**
		 * Sets the text in the query pane.
		 * @param text is the query text to set.
		 */
		public void setQueryText(String text) {
			queryText.setText(text);
		}
		
		/**
		 * Subclasses should know how to detach themselves from the
		 * database.
		 * @param database
		 */
		public abstract void deleteFromDatabase(Database database);
	
		/* (non-Javadoc)
		 * @see java.awt.Component#getName()
		 */
		public abstract String getName();
		
		/* (non-Javadoc)
		 * @see java.awt.Component#setName(java.lang.String)
		 */
		public void setName(String name){
			super.setName(name);
		}

		public abstract Query getQuery();
		
		/**
		 * Sets a new table model for the results ensuring that the correct
		 * table and scrollbars are set to display it.
		 * @param model is the table model to set.
		 */
		protected void setModel(TableModel model) {
			//TableSorter sorter = new TableSorter(model);
			table = new QueryTable(model);
			//sorter.setTableHeader(table.getTableHeader());
	        scrollPane.setViewportView(table);

	        QueryTable.TableRowHeader rowHeader = table.createRowHeader();
	        table.getSelectionModel().addListSelectionListener(rowHeader);
	        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			scrollPane.setRowHeaderView(rowHeader);
			this.model = model;
		}
		
		/**
		 * Sets the status text at the bottom of this pane.
		 * @param text is the status text to set.
		 */
		public void setStatus(String text){
			statusText.setText(text);
		}

	}