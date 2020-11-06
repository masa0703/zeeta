package jp.tokyo.selj.common;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

public class DlgWait extends JDialog {

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JLabel dspMsg = null;

	/**
	 * @param owner
	 */
	public DlgWait(Frame owner) {
		super(owner);
		initialize();
	}
	public DlgWait(JDialog owner) {
		super(owner);
		initialize();
	}

	public void setMsg(String mes){
		dspMsg.setText(mes);
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(235, 67);
		this.setContentPane(getJContentPane());
		setLocationRelativeTo(getOwner());
		setUndecorated(true);
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			dspMsg = new JLabel();
			dspMsg.setText("ちょっとまってください");
			dspMsg.setBorder(new BevelBorder(BevelBorder.LOWERED));
			dspMsg.setHorizontalAlignment(SwingConstants.CENTER);
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(dspMsg, BorderLayout.CENTER);
			
		}
		return jContentPane;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
