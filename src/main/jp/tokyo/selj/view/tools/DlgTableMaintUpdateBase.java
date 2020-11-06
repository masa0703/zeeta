package jp.tokyo.selj.view.tools;

import javax.swing.JPanel;
import java.awt.Frame;
import java.awt.BorderLayout;
import javax.swing.JDialog;

public class DlgTableMaintUpdateBase extends JDialog {

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	public void setup(){
		//
	}

	/**
	 * @param owner
	 */
	public DlgTableMaintUpdateBase(Frame owner) {
		super(owner);
		initialize();
	}
	/**
	 * @param owner
	 */
	public DlgTableMaintUpdateBase(JDialog owner) {
		super(owner, true);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(300, 200);
		this.setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
		}
		return jContentPane;
	}

}
