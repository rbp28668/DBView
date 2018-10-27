/*
 * AboutBox.java
 *
 * Created on 09 April 2002, 21:36
 */

package uk.co.alvagem.dbview;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.border.TitledBorder;
import javax.swing.JTextArea;
import javax.swing.Box;

/**
 * Displays the system about box.
 * @author  rbp28668
 */
public class AboutBox extends javax.swing.JDialog{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** Creates new AboutBox */
    public AboutBox() {
        setTitle("About DBView");
        getContentPane().setLayout(new BorderLayout());
        
        Box box = Box.createHorizontalBox();
        
        btnOK = new javax.swing.JButton("OK");
        btnMoreLess = new javax.swing.JButton("More >>");
        
        box.add(Box.createHorizontalGlue());
        box.add(btnMoreLess);
        box.add(Box.createHorizontalStrut(5));
        box.add(btnOK);
        
        getContentPane().add(box, BorderLayout.SOUTH);
        
        // OK Button
        btnOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeDialog();
            }
        });
        
        // More/Less button
        btnMoreLess.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if(expanded) {
                    getContentPane().remove(envInfo);
                    btnMoreLess.setText("More >>");
                } else {
                    getContentPane().add(envInfo,BorderLayout.CENTER);
                    btnMoreLess.setText("Less <<");
                }
                
                expanded = !expanded;
                pack();
            }
        });
        
        JTextArea aboutInfo = new JTextArea();
        aboutInfo.append("Database Viewer\n");
        aboutInfo.append("Version 0.1\n");
        aboutInfo.append("Written by R Bruce Porteous\n");
        aboutInfo.append("\n");
        aboutInfo.append("This product includes software developed by the\n");
        aboutInfo.append("Apache Software Foundation (http://www.apache.org/)");
        aboutInfo.setFont(new Font("Times New Roman",Font.BOLD,14));

        aboutInfo.setBorder(new TitledBorder("DBView"));
        getContentPane().add(aboutInfo,BorderLayout.NORTH);
        
        for(int idx=0; idx<propertyKeys.length; idx+=2) {
            envInfo.append(propertyKeys[idx+1] + " " + System.getProperty(propertyKeys[idx]) + '\n');
        }
        envInfo.setBorder(new TitledBorder("Environment"));

        pack();
        
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screen = tk.getScreenSize();
        Dimension size = getSize();
        int x = (screen.width - size.width)/2;
        int y = (screen.height - size.height) / 3; // bias towards top.
        setLocation(x,y);

    }

    /** Closes the dialog */
    private void closeDialog() {
        setVisible(false);
        dispose();
    }

    private javax.swing.JButton btnOK;
    private javax.swing.JButton btnMoreLess;
    private JTextArea envInfo = new JTextArea();
    private boolean expanded = false;
    
    private static final String[] propertyKeys = new String[] {
        "java.version", "Java Runtime Environment version ",
        "java.vendor", "Java Runtime Environment vendor ",
        "java.home", "Java installation directory ",
        "java.vm.version", "Java Virtual Machine implementation version ",
        "java.vm.vendor", "Java Virtual Machine implementation vendor ",
        "java.vm.name", "Java Virtual Machine implementation name ",
        "os.name", "Operating system name ",
        "os.arch", "Operating system architecture ",
        "os.version", "Operating system version ",
        "user.name", "User's account name ",
        "user.home", "User's home directory ",
        "user.dir", "User's current working directory ",
        "java.io.tmpdir","Default temporary directory "};
    
   
}
