/*
 * NotebookEditor.java
 * Project: EATool
 * Created on 05-Mar-2006
 *
 */
package uk.co.alvagem.dbview;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;

import uk.co.alvagem.dbview.util.SettingsManager;


/**
 * NotebookEditor provides a simple editor for writing scripts.
 * 
 * @author rbp28668
 */
public class NotebookEditor extends JInternalFrame implements AppEventListener {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private EditorPane display;
    private DBView app;
    private NotebookEditorActionSet actions = new NotebookEditorActionSet(this);
    
    private final static String WINDOW_SETTINGS = "/Windows/NotebookEditor";
	private static final String MENU_CONFIG = "/NotebookEditor/menus";

    /**
     * 
     */
    public NotebookEditor(DBView app) {
        super();
        init(app);
    }

    /**
     * @param arg0
     */
    public NotebookEditor(DBView app,String arg0) {
        super(arg0);
        init(app);
    }

    private void init(DBView app){
    	this.app = app;
        setResizable(true);
        setMaximizable(true);
        setIconifiable(true);
        setClosable(true);
        
        SettingsManager settings = app.getSettings();
        GUIBuilder.loadBounds(this,settings,WINDOW_SETTINGS);

        SettingsManager config = app.getConfig();

        display = new EditorPane();
        actions.addTextActions(display);
        
        display.setText(app.getNotebook().getText());
        app.addListener(this);
        
        JMenuBar menuBar = new JMenuBar();
        SettingsManager.Element cfg = config.getElement(MENU_CONFIG);
        GUIBuilder.buildMenuBar(menuBar, actions, cfg);
        setJMenuBar(menuBar);

        JScrollPane scrollPane = new JSizedScroll(display);
        getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);
        
        updateKeymap(display);
        
    }
    
    /**
     * @param display
     */
    private void updateKeymap(EditorPane display) {
        Keymap map = JTextComponent.addKeymap("NextPrevMap",display.getKeymap());
        bindKey(map, KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK, DefaultEditorKit.nextWordAction);
        bindKey(map, KeyEvent.VK_LEFT,  InputEvent.CTRL_MASK, DefaultEditorKit.previousWordAction);
        bindKey(map, KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK|InputEvent.SHIFT_MASK, DefaultEditorKit.selectionNextWordAction);
        bindKey(map, KeyEvent.VK_LEFT,  InputEvent.CTRL_MASK|InputEvent.SHIFT_MASK, DefaultEditorKit.selectionPreviousWordAction);
    }

    /**
     * Binds an Action to a keystroke in the given Keymap.
     * @param map is the Keymap to add the binding to.
     * @param key is the virtual key code to make up the keystroke.
     * @param mask is the keymask to make up the keystroke.
     * @param actionName is the name of the action to bind.
     */
    private void bindKey(Keymap map, int key, int mask, String actionName) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(key,mask, false);
        map.addActionForKeyStroke(keyStroke,actions.getAction(actionName));
    }


    public void dispose() {
        SettingsManager settings = app.getSettings();
        GUIBuilder.saveBounds(this,settings, WINDOW_SETTINGS);
        setVisible(false);
        app.removeListener(this);
        app.getCommandFrame().getDesktop().remove(this);
        app.getCommandFrame().repaint();
        super.dispose();
    }

    JEditorPane getEditPane(){
        return display;
    }
    
	/* (non-Javadoc)
	 * @see uk.co.alvagem.dbview.AppEventListener#aboutToSave()
	 */
	public void aboutToSave() {
		String text = display.getText();
		app.getNotebook().setText(text);
	}

	/* (non-Javadoc)
	 * @see uk.co.alvagem.dbview.AppEventListener#hasUnsaved()
	 */
	public boolean hasUnsaved() {
		String text = display.getText();
		return !text.equals(app.getNotebook().getText());
	}
    
    private class EditorPane extends JEditorPane{
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public EditorPane() {
            super();
            setEditable(true);
            setContentType("text/plain");
            
            Font font = new Font("Lucida Console", Font.PLAIN,12);
            setFont(font);
        }
        
    }

    private class JSizedScroll extends JScrollPane {
        
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		JSizedScroll(JComponent component){
            super(component);
            Dimension d = new Dimension(200,300);
            super.setPreferredSize(d);
            
        }
    }

}
