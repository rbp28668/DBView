/**
 * 
 */
package uk.co.alvagem.dbview;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import uk.co.alvagem.dbview.dedupe.DeDupeEngine;
import uk.co.alvagem.dbview.dedupe.DeDupeProgressCallback;

/**
 * @author bruce.porteous
 *
 */
public class DeDupeProgressFrame extends JInternalFrame implements
		DeDupeProgressCallback {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long start;
	private int target;
	private int processed;
	private int matched;
	private Exception exception = null;
	private boolean completed = false;
	private DeDupeEngine engine = null;
	
	private JLabel progressText = new JLabel();
	private JLabel hitsText = new JLabel();
	private JButton controlButton = new JButton("Stop");
	
	public DeDupeProgressFrame(){
		
		setTitle("Duplicate Detection");
		setClosable(false);
		setMaximizable(false);
		setIconifiable(false);
		setResizable(false);
		setLocation(200,200);
		
		Font font = getContentPane().getFont(); 
		FontMetrics fm = getFontMetrics(font);
		Dimension d = new Dimension(fm.charWidth('m') * 30, fm.getHeight());
		progressText.setPreferredSize(d);
		hitsText.setPreferredSize(d);
		
		setLayout(new BorderLayout());
		add(progressText,BorderLayout.NORTH);
		add(hitsText,BorderLayout.CENTER);
		add(controlButton, BorderLayout.SOUTH);
		pack();
		setVisible(true);
		
		controlButton.addActionListener( new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				if(completed){
					setVisible(false);
					dispose();
				} else {
					if(engine != null){
						engine.stopProcessing();
						controlButton.setText("Close");
						completed = true;
					}
				}
				
			}
			
		});
	}
	
	/* (non-Javadoc)
	 * @see uk.co.alvagem.dbview.DeDupeProgressCallback#complete(int, int)
	 */
	public void complete(int processed, int matches) {
		synchronized(this){
			this.processed = processed;
			this.matched = matches;
			this.completed = true;
		}
		
		SwingUtilities.invokeLater( new Runnable() {

			public void run() {
				synchronized(DeDupeProgressFrame.this) {
					progressText.setText("Completed " 
							+ DeDupeProgressFrame.this.processed
							+ " records");
					hitsText.setText("Found " 
							+ DeDupeProgressFrame.this.matched
							+ " groups " );
					controlButton.setText("Close");
				}
			}
			
		});
	}

	/* (non-Javadoc)
	 * @see uk.co.alvagem.dbview.DeDupeProgressCallback#progress(int, int)
	 */
	public void progress(int processed, int matches) {
		synchronized(this){
			this.processed = processed;
			this.matched = matches;
		}
		
		SwingUtilities.invokeLater( new Runnable() {

			public void run() {
				synchronized(DeDupeProgressFrame.this) {
					progressText.setText("Processed " 
							+ DeDupeProgressFrame.this.processed
							+ " out of " 
							+ DeDupeProgressFrame.this.target);
					hitsText.setText("Found " 
							+ DeDupeProgressFrame.this.matched
							+ " groups " );
				}
			}
			
		});
	}

	/* (non-Javadoc)
	 * @see uk.co.alvagem.dbview.DeDupeProgressCallback#started(int)
	 */
	public void started(int records) {
		synchronized(this) {
			start = System.currentTimeMillis();
			target = records;
			completed = false;
		}
	}
	
	/* (non-Javadoc)
	 * @see uk.co.alvagem.dbview.DeDupeProgressCallback#error(java.lang.Exception)
	 */
	public void error(Exception e) {
		synchronized(this){
			this.exception = e;
		}
		
		SwingUtilities.invokeLater( new Runnable() {

			public void run() {
				synchronized(DeDupeProgressFrame.this) {
					new ExceptionDisplay(DeDupeProgressFrame.this,exception);
				}
			}
			
		});
	}
	
	/* (non-Javadoc)
	 * @see uk.co.alvagem.dbview.DeDupeProgressCallback#register(uk.co.alvagem.dbview.DeDupeEngine)
	 */
	public void register(DeDupeEngine engine) {
		this.engine = engine;
	}

	


}
