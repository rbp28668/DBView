/*
 * ExceptionDisplay.java
 *
 * Created on 24 February 2002, 19:20
 */

package uk.co.alvagem.dbview;

import java.util.Set;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.Box;
/**
 * Provides a display of an exception in a standard dialog.
 * @author  rbp28668
 */
public class ExceptionDisplay extends javax.swing.JDialog{ 

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ExceptionDisplay(java.awt.Frame parent, Throwable t) {
        super(parent, "DBView", true); // modal
        setLocationRelativeTo(parent); 
        init(t);    
    }
    
    /** Creates new ExceptionDisplay */
    public ExceptionDisplay(javax.swing.JDialog parent, Throwable t) {
        super(parent, "DBView", true); // modal
        setLocationRelativeTo(parent); 
        init(t);
    }

    /** Creates new ExceptionDisplay */
    public ExceptionDisplay(java.awt.Component parent, Throwable t) {
        super((java.awt.Frame)null, "DBView", true); // modal
        setLocationRelativeTo(parent); 
        init(t);
    }
    
    private void init(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter writer = new PrintWriter(sw);

        String msg = t.getClass().getName();
        if(t.getMessage() != null)
            msg += "\n" + t.getMessage();
        
        t.printStackTrace(writer);
        // sw.getBuffer().toString() now has the stack trace.  We now want to 
        // make sure the stack trace only has unique lines where exceptions have
        // been chained.
        
        // Strip into lines
        StringTokenizer toks = new StringTokenizer(sw.getBuffer().toString(),"\n");
        LinkedList<String> tokList = new LinkedList<String>();
        while(toks.hasMoreTokens()) {
            tokList.addFirst(toks.nextToken()); // reverse order - bottom of stack first
        }
        
        // build an output list only containing unique lines back in normal order
        Set<String> exists = new HashSet<String>();
        LinkedList<String> output = new LinkedList<String>();
        for(Iterator<String> iter = tokList.iterator(); iter.hasNext(); ){
            String tok = iter.next();
            if(!exists.contains(tok)) {
                output.addFirst(tok);
                exists.add(tok);
            }
        }
        
        // and build an output string from the unique list
        StringBuffer outputBuff = new StringBuffer();
        for(Iterator iter = output.iterator(); iter.hasNext();){
            outputBuff.append((String)iter.next());
            if(iter.hasNext())
                outputBuff.append('\n');
        }
        
        
        // JTextPane for messages NORTH
        // optional JTextPane for stack trace CENTRE
        // buttons south

        getContentPane().setLayout(new BorderLayout());
        
        JTextArea messageDisplay = new JTextArea();
        messageDisplay.setBorder(new StandardBorder("Message"));
        messageDisplay.setText(msg);
        getContentPane().add(messageDisplay, BorderLayout.NORTH);
        
        JTextArea display = new JTextArea();
        stackDisplay = display;
        display.setBorder(new StandardBorder("Stack"));
        display.setText(outputBuff.toString());
        
        JScrollPane scroller = new JScrollPane();
        scroller.setViewportView(display);
        stackDisplay = scroller;
        
        btnMore = new JButton("More >>>");
        btnClose = new JButton("Close");
        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(btnMore);
        box.add(Box.createHorizontalStrut(10));
        box.add(btnClose);
        box.add(Box.createHorizontalStrut(10));
        getContentPane().add(box, BorderLayout.SOUTH);

                // OK Button
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setVisible(false);
                dispose();
            }
        });

        btnMore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if(moreShowing) { // button is less 
                    btnMore.setText("More >>>");
                    getContentPane().remove(stackDisplay);
                    moreShowing = false;
                } else {
                    btnMore.setText("Less <<<");
                    getContentPane().add(stackDisplay, BorderLayout.CENTER);
                    moreShowing = true;
                }
                pack();
            }
        });

        
        pack();
        setVisible(true);
    }

    private boolean moreShowing = false;
    JButton btnClose = null;
    JButton btnMore = null;
    Component stackDisplay = null;
}
