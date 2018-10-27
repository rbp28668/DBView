/*
 * ProgressDisplay.java
 * Project: EATool
 * Created on 31-Mar-2006
 *
 */
package uk.co.alvagem.dbview;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * ProgressDisplay
 * 
 * @author rbp28668
 */
public class ProgressDisplay extends JInternalFrame  {

    private static final long serialVersionUID = 1L;
    private JProgressBar bar;
    private JTextField text;
    
    /**
     * 
     */
    public ProgressDisplay(JInternalFrame parent, String title) {
        super(title,false); 
        init();
    }


    private void init(){
        
        text = new JTextField();
        text.setColumns(40);
        text.setEditable(false);
        add(text,BorderLayout.NORTH);
        
        bar = new JProgressBar();
        bar.setIndeterminate(true);
        add(bar,BorderLayout.CENTER);
        pack();

        Dimension dlg = getPreferredSize();
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screen = tk.getScreenSize();
        setLocation((screen.width - dlg.width)/2, (screen.height - dlg.height)/2);

        setVisible(true);
        
    }
    
    /* (non-Javadoc)
     * @see alvahouse.eatool.repository.ProgressStatus#setRange(int, int)
     */
    public void setRange(int lower, int upper) {
        bar.setMinimum(lower);
        bar.setMaximum(upper);
    }

    /* (non-Javadoc)
     * @see alvahouse.eatool.repository.ProgressStatus#setPosition(int)
     */
    public void setPosition(int value) {
        bar.setValue(value);
    }

    /* (non-Javadoc)
     * @see alvahouse.eatool.repository.ProgressStatus#setTask(java.lang.String)
     */
    public void setTask(String name) {
        SwingUtilities.invokeLater( new TaskSetter(name));
    }
    
    private class TaskSetter implements Runnable {
        private final String name;
        
        TaskSetter(String name) {
            this.name = name;
        }
        public void run() {
            text.setText(name);
        }
        
    }

    /* (non-Javadoc)
     * @see alvahouse.eatool.repository.ProgressStatus#setIndeterminate(boolean)
     */
    public void setIndeterminate(boolean indeterminate) {
        bar.setIndeterminate(indeterminate);
    }


    /* (non-Javadoc)
     * @see alvahouse.eatool.repository.ProgressStatus#setComplete()
     */
    public void setComplete() {
        SwingUtilities.invokeLater( new Runnable(){
           public void run(){
               setVisible(false);
               ProgressDisplay.this.dispose();
           }
        });
    }

}
