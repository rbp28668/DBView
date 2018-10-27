/*
 * MenuBuilder.java
 *
 * Created on 23 January 2002, 22:55
 */

package uk.co.alvagem.dbview;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.ListIterator;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import uk.co.alvagem.dbview.util.SettingsManager;


/**
 * GUIBuilder provides various methods for building and configuring GUI elements
 * from SettingsManager sub-trees.
 * @author  rbp28668
 */
public class GUIBuilder {

    /** Creates new MenuBuilder */
    public GUIBuilder() {
    }

	/**
	 * Method buildMenuBar constructs a menu bar using the settings file to determine which
	 * items are to be in the menu bar.  This method may be called multiple times with different 
	 * action sets and different nodes in the settings file to build up functionality.
	 * @param menuBar is the menu bar to which menus should be added
	 * @param actions is the set of actions that should be used to provide the commands
	 * @param root is the element of the settings file that contains the structure of the
	 *  menu-bar
	 */
    static public void buildMenuBar(JMenuBar menuBar, ActionSet actions, SettingsManager.Element root) {
        ListIterator iterMenu = root.getChildren();
        while(iterMenu.hasNext()) {
            SettingsManager.Element menuElement = (SettingsManager.Element) iterMenu.next();
            if(menuElement.getName().compareTo("menu") != 0)
                throw new IllegalArgumentException("Invalid menu element in configuration");
            
            String display = menuElement.attributeRequired("display");

            JMenu jm = new JMenu();
            jm.setText(display);
            
            //System.out.println("New menu: "+ display);
            
            String mnemonic = menuElement.attribute("mnemonic");
            if(mnemonic != null)
                jm.setMnemonic(mnemonic.charAt(0));
            
            String image = menuElement.attribute("image");
            if(image != null) {
                // TODO - fix this
            }
            menuBar.add(jm);
                
            buildMenu(jm,actions,menuElement);
        }
    }
    
	/**
	 * Method buildToolbar constructs a toolbar using the settings file to determine which items are to 
	 * be in the toolbar.  This method may be called multiple times with different action sets and
	 * different nodes in the settings file to build up functionality.
	 * @param toolBar is the toolbar to which the menu items should be attached
	 * @param actions is the set of actions to use for this toolbar
	 * @param root is the element in the settings that contains the structure of the toolbar
	 */
    static public void buildToolbar(JToolBar toolBar, ActionSet actions, SettingsManager.Element root) {
        ListIterator iterButton = root.getChildren();
        while(iterButton.hasNext()) {
            SettingsManager.Element buttonElement = (SettingsManager.Element) iterButton.next();

            if(buttonElement.getName().compareTo("separator") == 0) {
                toolBar.addSeparator();
            } else if(buttonElement.getName().compareTo("button") == 0) {
                String actionName = buttonElement.attributeRequired("action");

                Action a = actions.getAction(actionName);
                if(a == null)
                    throw new IllegalArgumentException("No action to match " + actionName + " in toolbar configuration");

                String display = buttonElement.attribute("display");
                if(display != null)
                    a.putValue(Action.NAME,display);
            
                JButton jb = new JButton(a);
                toolBar.add(jb);
            }
        }
    }
    
	/**
	 * Method buildPopup creates a popup menu from an action set and a settings node that 
	 * describes the menu to be built.
	 * @param actions is the action set to use for the commands.
	 * @param root is the element that contains a set of named popup descriptions.
	 * @param select is the name of the popup we want from a set.
	 * @return PositionalPopup configured as the new popup menu..
	 */
    static public PositionalPopup buildPopup(ActionSet actions, SettingsManager.Element root, String select) {
        
        // root should have children with name "popup". Each of these children should have the
        // attribute "name" that says which popup.  Find the right one.....
        SettingsManager.Element ePopup = null;
        ListIterator iterPopup = root.getChildren();
        while(iterPopup.hasNext()) {
            SettingsManager.Element e = (SettingsManager.Element) iterPopup.next();
            String popupName = e.attributeRequired("name");
            if((e.getName().compareTo("popup") == 0)
                && (popupName.compareTo(select) == 0)) {
                ePopup = e;
                break;
           }
        }
        
        if(ePopup == null){
        	//System.out.println("No popup defined for " + select);
            return null;    // allows caller to fail silently - useful if keying 
                            // popup menus off a class name.
        }
        
        //System.out.println("Building popup for " + select);                            
        PositionalPopup popup = new PositionalPopup();
        ListIterator iterMenu = ePopup.getChildren();
        while(iterMenu.hasNext()) {
           SettingsManager.Element eMenu = (SettingsManager.Element) iterMenu.next();
           
            if(eMenu.getName().compareTo("separator") == 0) {
                popup.addSeparator();
            } else if(eMenu.getName().compareTo("menuitem") == 0) {
                JMenuItem jmi = buildMenuItem(actions,eMenu);
                popup.add(jmi);
            }
        }
        return popup;
    }

	/**
	 * Method buildPopup creates a popup menu from an action set and a settings node that 
	 * describes the menu to be built. This is a convenience method to make it easy to match
	 * popup menus to the class of the object they need to operate on.  This uses
	 * the class name (with any package stripped off) to select the popup.
	 * @param actions is the action set to use for the commands.
	 * @param root is the element that contains a set of named popup descriptions.
	 * @param targetClass is the object which will select the appropriate popup by name.
	 * @return PositionalPopup with the new popup.
	 */
    static public PositionalPopup buildPopup(ActionSet actions, SettingsManager.Element root, Class targetClass){
        String strClass = targetClass.getName();
        int idx = strClass.lastIndexOf('.');    // full class name?
        if(idx != -1) {
            strClass = strClass.substring(idx+1);
        }
        idx = strClass.lastIndexOf('$');    // inner class?
        if(idx != -1) {
            strClass = strClass.substring(idx+1);
        }
        return GUIBuilder.buildPopup(actions, root, strClass);

    }
    
    /**
     * Builds a menu from a SettingsManager element.
     * @param menu is the menu to build (or add to)
     * @param actions is the set of named actions to use for the menu.
     * @param menuElement is the SettingsManager.Element that describes the menu.
     */
    static private void buildMenu(JMenu menu, ActionSet actions, SettingsManager.Element menuElement) {
        ListIterator iterMenu = menuElement.getChildren();
        while(iterMenu.hasNext()) {
            SettingsManager.Element menuItem = (SettingsManager.Element) iterMenu.next();

            if(menuItem.getName().compareTo("separator") == 0) {
                menu.addSeparator();
            } else if(menuItem.getName().compareTo("menuitem") == 0) {
                JMenuItem jmi = buildMenuItem(actions,menuItem);
                menu.add(jmi);
            }
        }
    }
    
    /**
     * This builds a single menu item from a SettingsManager Element.
     * @param actions is the set of actions to get a named action from.
     * @param menuItem is the Element that defines this menu item.
     * @return a new, configured JMenuItem.
     * @throws IllegalArgumentException if the required action cannot be found.
     */
    static private JMenuItem buildMenuItem( ActionSet actions, SettingsManager.Element menuItem) {
        String actionName = menuItem.attributeRequired("action");
        Action a = actions.getAction(actionName);
        if(a == null)
            throw new IllegalArgumentException("No action to match " + actionName + " in menu configuration");

        String display = menuItem.attribute("display");
        if(display != null)
            a.putValue(Action.NAME,display);
        else
            a.putValue(Action.NAME,actionName);

        //System.out.println("New menu item: "+ display);

        JMenuItem jmi = new JMenuItem(a);

        String accel = menuItem.attribute("accel");
        if(accel != null) {
            String accelmod = menuItem.attribute("accelmod");
            int modifier = 0;
            if(accelmod != null) {
                if(accelmod.indexOf("CTRL") != -1) modifier |= java.awt.Event.CTRL_MASK;
                if(accelmod.indexOf("SHIFT") != -1) modifier |= java.awt.Event.SHIFT_MASK;
                if(accelmod.indexOf("ALT") != -1) modifier |= java.awt.Event.ALT_MASK;
            }
            a.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(accel.charAt(0),modifier,false));
            jmi.setAccelerator(KeyStroke.getKeyStroke(accel.charAt(0),modifier,false));
        }

        String tooltip = menuItem.attribute("tooltip");
        if(tooltip != null) {
            a.putValue(Action.SHORT_DESCRIPTION,tooltip);
            jmi.setToolTipText(tooltip);
        }

        String mnemonic = menuItem.attribute("mnemonic");
        if(mnemonic != null)
            jmi.setMnemonic(mnemonic.charAt(0));
        
        return jmi;
    }
    
	/**
	 * Method loadBounds loads bounds for a component from a settings node.  The component's
	 * position and size are set to these bounds.
	 * @see Eatool.GUI.GUIBuilder#saveBounds
	 * @param frame is the component to be set
	 * @param root is the node containing the bounds
	 */
    static public void loadBounds(java.awt.Component frame, SettingsManager.Element root) {
 
        // default with magic numbers loosely based on screen size
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        int ixSize = size.width/3;
        int iySize = size.height/2;
        int ixLoc = size.width/5;
        int iyLoc = size.height/5;
 
        // Saved values?
        String sxSize = root.attribute("width");
        String sySize = root.attribute("height");
        String sxLoc = root.attribute("xlocation");
        String syLoc = root.attribute("ylocation");
        
 
        // Try to decode saved values
        if(sxSize != null && sySize != null) {
            try {
                ixSize = Integer.decode(sxSize).intValue();
                iySize = Integer.decode(sySize).intValue();
            }
            catch(Exception e) { /* NOP-fail quietly */ }
        }
        if(sxLoc != null && syLoc !=null) {
            try {
                ixLoc = Integer.decode(sxLoc).intValue();
                iyLoc = Integer.decode(syLoc).intValue();
            }
            catch(Exception e) { /* NOP-fail quietly */ }
        }
        
        // Ensure window isn't bigger than physical screen.
        if(ixSize > size.width){
            ixSize = size.width;
        }
        if(iySize > size.height){
            iySize = size.height;
        }
        
        // And make sure it's all on screen
        if( ixLoc + ixSize > size.width){
            ixLoc = size.width - ixSize;
        } else if (ixLoc < 0) {
            ixLoc = 0;
        }
        
        if( iyLoc + iySize > size.height){
            iyLoc = size.height - iySize;
        } else if (iyLoc < 0) {
            iyLoc = 0;
        }
        
        
        frame.setSize(ixSize,iySize);
        frame.setLocation(ixLoc,iyLoc);

    }

	/**
	 * Method loadBounds loads bounds for a component from a settings node.  The component's
	 * position and size are set to these bounds.
	 * @see Eatool.GUI.GUIBuilder#saveBounds
	 * @param frame is the component to be set
	 */
    static public void loadBounds(java.awt.Component frame, SettingsManager settings, String key) {
        // Look for saved settings
        SettingsManager.Element cfg = settings.getOrCreateElement(key);
        GUIBuilder.loadBounds(frame,cfg);
    }
    
	/**
	 * Method saveBounds saves the bouunds for a component to a settings node.  This allows
	 * a window's size and position to be saved between invocations.
	 * @param frame is the component to save the bounds for
	 * @param root is the settings node to use for the bounds.
	 */
    static public void saveBounds(java.awt.Component frame, SettingsManager.Element root) {
        Point location = frame.getLocation();
        root.setAttribute("xlocation",String.valueOf(location.x));
        root.setAttribute("ylocation",String.valueOf(location.y));
        
        Dimension size = frame.getSize();
        root.setAttribute("width",String.valueOf(size.width));
        root.setAttribute("height",String.valueOf(size.height));
    }

	/**
	 * Method saveBounds saves the bouunds for a component to a settings node.  This allows
	 * a window's size and position to be saved between invocations.
	 * @param frame is the component to save the bounds for
	 * @param key is the key to the settings node to use for the bounds.
	 */
    static public void saveBounds(java.awt.Component frame, SettingsManager settings, String key) {
        // Look for saved settings
        SettingsManager.Element cfg = settings.getOrCreateElement(key);
        GUIBuilder.saveBounds(frame,cfg);
    }
    
}
