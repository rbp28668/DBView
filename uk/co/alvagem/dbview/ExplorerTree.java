/*
 * ExplorerTree.java
 *
 * Created on 20 February 2002, 20:27
 */

package uk.co.alvagem.dbview;

import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import uk.co.alvagem.dbview.util.SettingsManager;

/**
 * Generic extension of JTree that ties popup menus to each node. The
 * popups are keyed by classname (only the last component of the classname
 * not the fully qualified name).
 * @author  rbp28668
 */
public class ExplorerTree extends JTree{
	private static final long serialVersionUID = 1L;


    /** Creates new ExplorerTree */
    public ExplorerTree() {
    }

    /** sets up right-click menus for the tree.  The menus are keyed by
     * the class name of the objects in the tree model.  The popup definitions 
     * are defined by the GUIBuilder.buildPopup method.
     * @param cfg is the configuration element holding the popup menu 
     * definitions.
     */
    public void setPopups(SettingsManager.Element cfg, ActionSet as) {
        actions = as;
        cfgPopups = cfg;
        
        addMouseListener (new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) { // right click
                    //System.out.println("Button 3");
                    TreePath tp = getPathForLocation(e.getX(),e.getY());
                    if(tp != null) {
                        setSelectionPath(tp);
                        Object node = ((DefaultMutableTreeNode)tp.getLastPathComponent()).getUserObject();
                        if(node != null) {
                            //System.out.println("Clicked on node " + strClass);
                            Class nodeClass = node.getClass();
                            JPopupMenu popup = null;
                            // work up the inheritance level looking for a match
                            while(popup == null && nodeClass != null) {
                                popup = GUIBuilder.buildPopup(actions, cfgPopups, nodeClass);
                                nodeClass = nodeClass.getSuperclass();
                            }
                            if(popup != null) {
                            	//System.out.println("Popup for " + strClass);
                                popup.show(getTree(), e.getX(), e.getY());
                            }
                        }
                    }
                }
            }
        });
    }
    
    /** gets the selected node (if any) from the tree
     * @return the selected node or null if none selected
     */
    public DefaultMutableTreeNode getSelectedNode() {
        TreePath tp = getSelectionPath();
        if(tp != null) {
            return (DefaultMutableTreeNode)tp.getLastPathComponent();
        }
        return null;
    }
    
//    /** test **/
//    public void onModelUpdate() {
//        clearToggledPaths();
//    }
    
    
    /** equivalent for "this" for inner classes */
    private JTree getTree() {
        return this;
    }
        
    /** description of popup menus */
    private SettingsManager.Element cfgPopups; 
    /** actions for menu-clicks */
    private ActionSet actions;
   
}
