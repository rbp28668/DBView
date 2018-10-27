/*
 * ExplorerTreeModel.java
 *
 * Created on 20 February 2002, 20:27
 */

package uk.co.alvagem.dbview;

import java.util.HashMap;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Base utilities for the various tree based explorers.  Primarily these deal
 * with node lookup.  The explorers respond the change events where they are 
 * notified of changes to the data they represent.  However, the changes need
 * to be reflected in the node structure of the tree model.  Hence the provision
 * of node lookup given the user object in the node.
 * @author  rbp28668
 */
public class ExplorerTreeModel extends javax.swing.tree.DefaultTreeModel {
	private static final long serialVersionUID = 1L;
    /** used to map uuids of objects to the tree node that references
     * those objects.  Needed to allow the event handler to easily
     * work out how to manipulate the tree in respond to a meta model
     * or model change event
     */
    HashMap<Object,DefaultMutableTreeNode> nodeLookup = new HashMap<Object,DefaultMutableTreeNode>(); 

    
    /** Creates new ExplorerTreeModel */
    public ExplorerTreeModel(String rootTitle) {
        super(new DefaultMutableTreeNode(rootTitle));
    }

    /** registers a tree node so that it can be keyed found quickly given
     * an event involving a particular repository item.
     * @param node is the node to register.
     * @param item is the repository item that keys it.
     */
    protected void registerNode(DefaultMutableTreeNode node, Object item) {
        nodeLookup.put(item, node);
    }
    
    /** looks up a tree node given an associated repository item.
     * @param item is the repository item we want the node for.
     * @return the tree node or null if this item is not in the model.
     */
    protected DefaultMutableTreeNode lookupNodeOf(Object item) {
        return nodeLookup.get(item);
    }
    
    /** removes the registration for a repository item.
     * @param item is the repository item to remove the node registration for.
     */
    protected void removeNodeOf(Object item) {
        nodeLookup.remove(item);
    }
    
    /**
     * Removes all the registrations. 
     */
    protected void removeAll(){
        nodeLookup.clear();
    }
    

}
