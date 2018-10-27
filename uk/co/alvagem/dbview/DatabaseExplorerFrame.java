/**
 * 
 */
package uk.co.alvagem.dbview;

import javax.swing.JInternalFrame;
import javax.swing.WindowConstants;

import uk.co.alvagem.dbview.util.SettingsManager;

/**
 * @author bruce.porteous
 *
 */
public class DatabaseExplorerFrame extends JInternalFrame {

	private static final long serialVersionUID = 1L;

	private DatabaseExplorer tree;
    private final static String SETTINGS_KEY = "/Windows/DatabaseExplorer";
    private SettingsManager settings;

	/**
	 * @param arg0
	 */
	public DatabaseExplorerFrame(DBView app, String title) {
		super(title);
		
		setTitle(title);
		setResizable(true);
		setMaximizable(false);
		setIconifiable(true);
		setClosable(false);
		
		settings = app.getSettings();
        GUIBuilder.loadBounds(this, settings, SETTINGS_KEY);
		
		tree = new DatabaseExplorer(app);
		
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane();
        scrollPane.setViewportView(tree);
        getContentPane().add(scrollPane);
        setVisible(true);
	}

	public void dispose() {
        GUIBuilder.saveBounds(this, settings, SETTINGS_KEY);
		tree.dispose();
		tree = null;
	}

}
