/**
 * 
 */
package uk.co.alvagem.dbview;

import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.tree.DefaultMutableTreeNode;

import uk.co.alvagem.dbview.model.Databases;
import uk.co.alvagem.dbview.util.SettingsManager;

/**
 * @author bruce.porteous
 *
 */
public class DatabaseExplorer extends ExplorerTree  {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ActionSet actions;
    
    //private ExplorerTree tree;
    private DatabaseExplorerTreeModel treeModel;
    private final static String POPUPS_KEY = "/DatabaseExplorer/popups";

	/**
	 * @param arg0
	 */
	public DatabaseExplorer(DBView app) {



        Databases databases = app.getDatabases();
        treeModel = new DatabaseExplorerTreeModel(this, "Databases",databases);
        setModel(treeModel);
        addTreeWillExpandListener(treeModel);

        SettingsManager.Element cfg = app.getConfig().getElement(POPUPS_KEY);
        actions = new DatabaseExplorerActionSet(app, this,databases);
        setPopups(cfg,actions);

		MouseListener ml = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int selRow = getRowForLocation(e.getX(), e.getY());
				if (selRow != -1) {
					if (e.getClickCount() == 2) {

						DefaultMutableTreeNode node = getSelectedNode();
						if(node != null){
							try{
								// Object thingy = node.getUserObject();
								// Double-click handler here!
							} catch (Exception ex){
								new ExceptionDisplay((Frame)null,ex); // TODO
							}
						}
					}
				}
			}
		};
		addMouseListener(ml);

 	}


    /* (non-Javadoc)
     * @see javax.swing.JInternalFrame#dispose()
     */
    public void dispose() {
 
        //Main.getApp().getWindowCoordinator().removeFrame(this);
        treeModel.dispose();
        //super.dispose();
    }

//    /**
//     * Gets the node currently selected (if any).
//     * @return DefaultMutableTreeNode - the current node or null if none selected.
//     */
//    public DefaultMutableTreeNode getSelectedNode() {
//        return getSelectedNode();
//    }

	/**
	 * @return
	 */
	public Object getSelectedItem() {
		return getSelectedNode().getUserObject();
	}


	    

}
