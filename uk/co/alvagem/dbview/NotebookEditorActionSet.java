/*
 * NotebookEditorActionSet.java
 * Project: EATool
 * Created on 05-Mar-2006
 *
 */
package uk.co.alvagem.dbview;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

/**
 * NotebookEditorActionSet
 * 
 * @author rbp28668
 */
public class NotebookEditorActionSet extends ActionSet {

    private NotebookEditor editor;
    /**
     * 
     */
    public NotebookEditorActionSet(NotebookEditor editor) {
        super();
        this.editor = editor;
        
		addAction("NotebookClose", actionNotebookClose);
		addAction("NotebookGotoLine",actionNotebookGotoLine);
    }
    
    /* Note - text actions for JTextComponent are:
     * insert-content
		delete-previous
		delete-next
		set-read-only
		set-writable
		cut-to-clipboard
		copy-to-clipboard
		paste-from-clipboard
		page-up
		page-down
		selection-page-up
		selection-page-down
		selection-page-left
		selection-page-right
		insert-break
		beep
		caret-forward
		caret-backward
		selection-forward
		selection-backward
		caret-up
		caret-down
		selection-up
		selection-down
		caret-begin-word
		caret-end-word
		selection-begin-word
		selection-end-word
		caret-previous-word
		caret-next-word
		selection-previous-word
		selection-next-word
		caret-begin-line
		caret-end-line
		selection-begin-line
		selection-end-line
		caret-begin-paragraph
		caret-end-paragraph
		selection-begin-paragraph
		selection-end-paragraph
		caret-begin
		caret-end
		selection-begin
		selection-end
		default-typed
		insert-tab
		select-word
		select-line
		select-paragraph
		select-all
		unselect
		toggle-componentOrientation
		dump-model
     */
    public void addTextActions(JTextComponent text){
        Action[] actions = text.getActions();
        for(int i=0; i<actions.length; ++i){
            String name = (String)actions[i].getValue(Action.NAME);
            addAction(name,actions[i]);
        }
    }

	private final Action actionNotebookClose = new AbstractAction() {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			try {
			    editor.dispose();
			} catch(Throwable t) {
				new ExceptionDisplay(editor,t);
			}
		}
	};
	
 		
		private final Action actionNotebookGotoLine = new AbstractAction() {
			  
			private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					try {
					    JEditorPane editPane = editor.getEditPane();

					    
					    PlainDocument doc = (PlainDocument)editPane.getDocument();
					    
					    
					    Element root = doc.getDefaultRootElement();

					    String caption = "Enter line number between 1 and " + Integer.toString(root.getElementCount());
					    String lineText = JOptionPane.showInputDialog(editor,caption,"DBView",JOptionPane.QUESTION_MESSAGE);
					    if(lineText != null){
						    int lineNumber = Integer.parseInt(lineText);
						    if(lineNumber < 1){
						        lineNumber = 1; 
						    } else if (lineNumber > root.getElementCount()){
						        lineNumber = root.getElementCount();
						    }
					        Element line = root.getElement(lineNumber-1);
					        int offset = line.getStartOffset();
					        editPane.setCaretPosition(offset);
					    }
					    
					} catch(Throwable t) {
						new ExceptionDisplay(editor,t);
					}
				}
			    
			};

}
