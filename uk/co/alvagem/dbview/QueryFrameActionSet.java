/**
 * 
 */
package uk.co.alvagem.dbview;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;

import uk.co.alvagem.dbview.util.SettingsManager;

/**
 * ActionSet for the Query frames.
 * @author bruce.porteous
 *
 */
public class QueryFrameActionSet extends ActionSet {

	private QueryFrame frame;
	private DBView app;

	/**
	 * Creates the action set tied to a given app and query frame.
	 * @param app is the main application class.
	 * @param queryFrame is the queryFrame these actions act on.
	 */
	public QueryFrameActionSet(DBView app, QueryFrame queryFrame) {
		super();
		this.frame = queryFrame;
		this.app = app;

		addAction("RunQuery", actionRunQuery);
		addAction("NewQuery", actionNewQuery);
		addAction("RenameQuery", actionRenameQuery);
		addAction("DeleteQuery", actionDeleteQuery);
		addAction("ExportCSV", actionExportCSV);
		addAction("ExportXML", actionExportXML);
		addAction("SearchResults", actionSearchResults);
		addAction("IndexQuery", actionIndexQuery);
		addAction("BindToIndex", actionBindToIndex);
		addAction("ShowIndexFields",actionShowIndexFields);
		addAction("SelectResultFields",actionSelectResultFields);
		addAction("DetectDuplicates",actionDetectDuplicates);
		addAction("EditDataReceivers", actionEditDataReceivers);
        addAction("EditExclusions",actionEditExclusions);
		addAction("SetDuplicatesCount",actionSetDuplicatesCount);
		addAction("SetDuplicatesThreshold",actionSetDuplicatesThreshold);
		addAction("SetDuplicatesPath",actionSetDuplicatesPath);
		addAction("SetDuplicatesRandomSelect",actionSetDuplicatesRandomSelect);
	}

	/** Run the currently selected query */
	private final Action actionRunQuery = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			try {
				frame.runSelectedQuery();
			} catch(Throwable t) {
				new ExceptionDisplay(frame,t);
			}
		}
	};

	/** Create a new query tab */
	private final Action actionNewQuery = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			try {
				frame.addNewQuery();
			} catch(Throwable t) {
				new ExceptionDisplay(frame,t);
			}
		}
	};   

	/** Renames the current query */
	private final Action actionRenameQuery = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			try {
				frame.renameSelectedQuery();
			} catch(Throwable t) {
				new ExceptionDisplay(frame,t);
			}
		}
	};   

	/** Deletes the current query */
	private final Action actionDeleteQuery = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			try {
				frame.deleteSelectedQuery();
			} catch(Throwable t) {
				new ExceptionDisplay(frame,t);
			}
		}
	};   

	/** Exports the query results to a CSV file */
	private final Action actionExportCSV = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			try {
				SettingsManager.Element cfg = app.getSettings().getOrCreateElement("/Files/CSVExport");
				String path = cfg.attribute("path");
				String strExportNulls = cfg.attribute("exportNulls");
				String strRowHeaders = cfg.attribute("rowHeaders");

				TableExportCSVFileChooser chooser = new TableExportCSVFileChooser();
				chooser.setExportNulls("true".equals(strExportNulls));
				chooser.setHeaderRow("true".equals(strRowHeaders));

				if(path == null) 
					chooser.setCurrentDirectory( new File("."));
				else
					chooser.setSelectedFile(new File(path));

				if( chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
					frame.setCursor(Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
					try {
						path = chooser.getSelectedFile().getPath();
						boolean exportNulls = chooser.exportNulls();
						boolean headerRow = chooser.headerRow();
						cfg.setAttribute("path",path);
						cfg.setAttribute("exportNulls",Boolean.toString(exportNulls));
						cfg.setAttribute("rowHeaders",Boolean.toString(headerRow));
						frame.exportSelectedCSV(path,exportNulls,headerRow);
					} finally {
						frame.setCursor(Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
					}
				}
			} catch(Throwable t) {
				new ExceptionDisplay(frame,t);
			}
		}
	};   

	/** Exports the current query results as XML */
	private final Action actionExportXML = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			try {
				SettingsManager.Element cfg = app.getSettings().getOrCreateElement("/Files/XMLExport");
				String path = cfg.attribute("path");
				String strExportNulls = cfg.attribute("exportNulls");
				String rootTag = cfg.attribute("rootTag");
				if(rootTag == null){
					rootTag = TableExportXML.ROOT_TAG;
				}
				String rowTag = cfg.attribute("rowTag");
				if(rowTag == null){
					rowTag = TableExportXML.ROW_TAG;
				}

				TableExportXMLFileChooser chooser = new TableExportXMLFileChooser();
				chooser.setExportNulls("true".equals(strExportNulls));
				chooser.setRootTag(rootTag);
				chooser.setRowTag(rowTag);

				if(path == null) 
					chooser.setCurrentDirectory( new File("."));
				else
					chooser.setSelectedFile(new File(path));

				if( chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
					frame.setCursor(Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
					try {
						path = chooser.getSelectedFile().getPath();
						boolean exportNulls = chooser.exportNulls();
						rootTag = chooser.getRootTag();
						rowTag = chooser.getRowTag();
						cfg.setAttribute("path",path);
						cfg.setAttribute("exportNulls",Boolean.toString(exportNulls));
						cfg.setAttribute("rootTag",rootTag);
						cfg.setAttribute("rowTag",rowTag);
						frame.exportSelectedXML(path,exportNulls,rootTag,rowTag);
					} finally {
						frame.setCursor(Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
					}
				}
			} catch(Throwable t) {
				new ExceptionDisplay(frame,t);
			}
		}
	};   

	/** Allows the user to search the current query results */
	private final Action actionSearchResults = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			try {
				frame.searchSelectedTab();
			} catch(Throwable t) {
				new ExceptionDisplay(frame,t);
			}
		}
	};   

	/** Allows the user to search the current query results */
	private final Action actionIndexQuery = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			try {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY );
				int returnVal = chooser.showOpenDialog(frame);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					String indexPath = chooser.getSelectedFile().getCanonicalPath();
					frame.indexSelectedQuery(indexPath);
				}

			} catch(Throwable t) {
				new ExceptionDisplay(frame,t);
			}
		}
	};   

	/** Allows the user to search the current query results */
	private final Action actionBindToIndex = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			try {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY );
				int returnVal = chooser.showOpenDialog(frame);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					String indexPath = chooser.getSelectedFile().getCanonicalPath();
					frame.bindToIndex(indexPath);
				}

			} catch(Throwable t) {
				new ExceptionDisplay(frame,t);
			}
		}
	};   


	/** Shows the fields stored in the index */
	private final Action actionShowIndexFields = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			try {
				frame.showIndexFields();

			} catch(Throwable t) {
				new ExceptionDisplay(frame,t);
			}
		}
	};   

	/** Shows the fields stored in the index */
	private final Action actionSelectResultFields = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			try {
				frame.selectSearchResultFields();

			} catch(Throwable t) {
				new ExceptionDisplay(frame,t);
			}
		}
	};   

	/** Shows the fields stored in the index */
	private final Action actionDetectDuplicates = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			try {
				DeDupeProgressFrame progress = new DeDupeProgressFrame();
				app.getCommandFrame().getDesktop().add(progress);
				progress.setVisible(true);
				progress.toFront();
				frame.detectDuplicates(progress);

			} catch(Throwable t) {
				new ExceptionDisplay(frame,t);
			}
		}
	}; 
	
	/** Edits the data receivers */
	private final Action actionEditDataReceivers = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			try {
				frame.editDeDupeReceivers();
			} catch(Throwable t) {
				new ExceptionDisplay(frame,t);
			}
		}
	};   
	
	/** Edits the criteria for records to be excluded from the index or search */
	private final Action actionEditExclusions = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			try {
				frame.editExceptionTemplate();
			} catch(Throwable t) {
				new ExceptionDisplay(frame,t);
			}
		}
	};   

	/** Sets the count of records to process in duplicate finding */
	private final Action actionSetDuplicatesCount = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			try {
				frame.setDuplicateCount();

			} catch(Throwable t) {
				new ExceptionDisplay(frame,t);
			}
		}
	};   

	/** Sets the threshold to use detecting duplicates */
	private final Action actionSetDuplicatesThreshold = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			try {
				frame.setDuplicatesThreshold();

			} catch(Throwable t) {
				new ExceptionDisplay(frame,t);
			}
		}
		
		
	};   
    
	/** Shows the fields stored in the index */
	private final Action actionSetDuplicatesPath = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			try {
				frame.setDuplicatesOutputPath();
			} catch(Throwable t) {
				new ExceptionDisplay(frame,t);
			}
		}
	};   
	
	/** Shows the fields stored in the index */
	private final Action actionSetDuplicatesRandomSelect = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			try {
				frame.setDuplicatesRandomSelect();
			} catch(Throwable t) {
				new ExceptionDisplay(frame,t);
			}
		}
	};   
}
