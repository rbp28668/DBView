/*
 * CommandActionSet.java
 *
 * Created on 23 January 2002, 22:49
 */

package uk.co.alvagem.dbview;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import uk.co.alvagem.dbview.util.SettingsManager;
import uk.co.alvagem.dbview.util.XMLFileFilter;


/**
 * CommandActionSet provides the ActionSet for the main menus.
 * @author Bruce.Porteous
 *
 */
public class CommandActionSet extends ActionSet {

	/** parent command frame */
    private CommandFrame frame;
    
    private DBView app;

    /** Creates new CommandActionSet */
    public CommandActionSet() {
        super();

        addAction("FileNew", actionFileNew);
        addAction("FileOpen", actionFileOpen);
        addAction("FileSave", actionFileSave);
        addAction("FileSaveAs", actionFileSaveAs);
        
        addAction("FileProperties",actionFileProperties);
        addAction("FileExit",actionFileExit);

        addAction("NotebookShow", actionNotebookShow);
        
        addAction("AddPlugin", actionAddPlugin);
        
		// -- Window --
		addAction("WindowPLAFMetal", actionWindowPLAFMetal);        
		addAction("WindowPLAFMotif", actionWindowPLAFMotif);        
		addAction("WindowPLAFWindows", actionWindowPLAFWindows);        

        // -- Help --
        addAction("HelpAbout", actionHelpAbout);
        
    }    

    public void setApp(DBView app){
        this.app = app;
        this.frame = app.getCommandFrame();
    }
    
    
    /** File new*/
    private final Action actionFileNew = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
            try {
            	app.reset();
            } catch(Throwable t) {
                new ExceptionDisplay(frame,t);
            }
        }
    };   

 
    /** File Open action*/
    private final Action actionFileOpen = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
            try {
                SettingsManager.Element cfg = app.getSettings().getOrCreateElement("/Files/XMLPath");
                String path = cfg.attribute("path");
                
                JFileChooser chooser = new JFileChooser();
                if(path == null) 
                    chooser.setCurrentDirectory( new File("."));
                else
                    chooser.setSelectedFile(new File(path));
                chooser.setFileFilter( new XMLFileFilter());

                if( chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                	frame.setCursor(Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
        			try {
        				path = chooser.getSelectedFile().getPath();
        				cfg.setAttribute("path",path);
        				app.reset();
        				app.setCurrentPath(path);
        				app.loadXML(path);
        			} finally {
        				frame.setCursor(Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
        			}
                }
            } catch(Throwable t) {
                new ExceptionDisplay(frame,t);
            }
        }
    };   

    /** File Save action - saves the repository as XML*/
    private final Action actionFileSave = new AbstractAction() {
		private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            try {
            	String path = app.getCurrentPath();
 
                if(path == null) {
                	JFileChooser chooser = new JFileChooser();
                    chooser.setCurrentDirectory( new File("."));
                    chooser.setFileFilter( new XMLFileFilter());

                    if( chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    	path = chooser.getSelectedFile().getPath();
                    	app.setCurrentPath(path);
                    }
                }
                if(path != null) {
                    app.saveXML(path);
                }
            } catch(Throwable t) {
                new ExceptionDisplay(frame,t);
            }
        }
    };   

    /** File Save As action - saves the repository as XML*/
    private final Action actionFileSaveAs = new AbstractAction() {
		private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            try {
                SettingsManager.Element cfg = app.getSettings().getOrCreateElement("/Files/XMLPath");
                String path = cfg.attribute("path");
 
                JFileChooser chooser = new JFileChooser();
                if(path == null) 
                    chooser.setCurrentDirectory( new File("."));
                else
                    chooser.setSelectedFile(new File(path));
                chooser.setFileFilter( new XMLFileFilter());

                if( chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    path = chooser.getSelectedFile().getPath();
                    cfg.setAttribute("path",path);
                    app.setCurrentPath(path);
                    app.saveXML(path);
                }
            } catch(Throwable t) {
                new ExceptionDisplay(frame,t);
            }
        }
    };   
    
    /** File Properties action - allows the user to edit the repository properties*/
    private final Action actionFileProperties = new AbstractAction() {
		private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            try {
            } catch(Throwable t) {
                new ExceptionDisplay(frame,t);
            }
        }
    };   

    /** File Exit action - terminates the application. */
    private final Action actionFileExit = new AbstractAction() {
		private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            //System.out.println("File Exit called.");
            if(JOptionPane.showConfirmDialog(null,"Exit application?","DBView",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
                fileExit();
            }
        }
    };   
    
    /**
     * Close the application.
     */
    public void fileExit(){
        app.dispose(); // tidy up
        System.exit(0);
    }

    /** Shows the notebook */
    private final Action actionNotebookShow = new AbstractAction() {
		private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
        	NotebookEditor editor = new NotebookEditor(app, "Notebook");
        	app.getCommandFrame().getDesktop().add(editor);
        	
        	editor.moveToFront();
        	editor.setVisible(true);
        }
    };   
    
    private final Action actionAddPlugin = new AbstractAction() {
		private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            try {
            	
            	CommandFrame frame = app.getCommandFrame();
            	String className = JOptionPane.showInputDialog(frame, "Class name of plugin?", "DBView", JOptionPane.QUESTION_MESSAGE);
            	if(className != null) {
            		app.getPlugins().registerPlugin(className);
            		updatePluginMenu(frame);
            	}
            } catch(Throwable t) {
                new ExceptionDisplay(frame,t);
            }
        }
    };   
 
    
    /** WindowPLAFMetal action - displays Metal look and feel */
    private final Action actionWindowPLAFMetal = new AbstractAction() {
		private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            try {
            	setPlaf("javax.swing.plaf.metal.MetalLookAndFeel");
           } catch(Throwable t) {
                new ExceptionDisplay(frame,t);
            }
        }
    };   
    /** WindowPLAFMotif action - displays Motif look and feel */
    private final Action actionWindowPLAFMotif = new AbstractAction() {
		private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            try {
            	setPlaf("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
           } catch(Throwable t) {
                new ExceptionDisplay(frame,t);
            }
        }
    };   
    /** WindowPLAFWindows action - displays Windows look and feel */
    private final Action actionWindowPLAFWindows = new AbstractAction() {
		private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            try {
            	setPlaf("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
           } catch(Throwable t) {
                new ExceptionDisplay(frame,t);
            }
        }
    };   
    
    /** private helper to set Pluggable Look & Feel (PLAF)
     * @param name is the PLAF name
     * @throws one of ClassNotFoundException, 
     *	IllegalAccessException, 
     *	InstantiationException, 
	 *   javax.swing.UnsupportedLookAndFeelException
     */ 
    private void setPlaf(String name) 
	throws ClassNotFoundException, 
	IllegalAccessException, 
	InstantiationException, 
    javax.swing.UnsupportedLookAndFeelException{
    	javax.swing.UIManager.setLookAndFeel(name);
    	SwingUtilities.updateComponentTreeUI(frame.getRootPane());
    }

    /**
	 * @param frame
	 */
	private void updatePluginMenu(CommandFrame frame) {
		for(Component component : frame.getContentPane().getComponents()){
			if(component instanceof QueryFrame){
				QueryFrame qf = (QueryFrame)component;
				qf.updatePluginMenu();
			}
		}
	}

	/** Help About action - displays the about box */
    private final Action actionHelpAbout = new AbstractAction() {
		private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            try {
                AboutBox about = new AboutBox();
                about.setVisible(true);
           } catch(Throwable t) {
                new ExceptionDisplay(frame,t);
            }
        }
    };   
    
 }
