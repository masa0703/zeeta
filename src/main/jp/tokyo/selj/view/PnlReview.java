package jp.tokyo.selj.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import jp.tokyo.selj.dao.Output;

public class PnlReview extends JPanel {

	private static final long serialVersionUID = 1L;
	JLabel dspName = null;
	JLabel dspPath = null;
	JLabel dspMemo = null;
	private JLabel dspUser = null;
	private JLabel dspReviewDate = null;
	/**
	 * This is the default constructor
	 */
	public PnlReview() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.gridx = 0;
		gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints2.gridy = 0;
		dspReviewDate = new JLabel();
		dspReviewDate.setFont(new Font("Dialog", Font.PLAIN, 12));
		dspReviewDate.setText("JLabel");
		dspReviewDate.setBorder(new TitledBorder("date"));
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridx = 3;
		gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints1.weightx = 0.2;
		gridBagConstraints1.gridy = 0;
		dspUser = new JLabel();
		dspUser.setFont(new Font("Dialog", Font.PLAIN, 12));
		dspUser.setText("JLabel");
		dspUser.setBorder(new TitledBorder("creator"));
		GridBagConstraints pointerConstraints = new GridBagConstraints();
		pointerConstraints.gridx = 3;
		pointerConstraints.fill = GridBagConstraints.HORIZONTAL;
		pointerConstraints.gridwidth = 2;
		pointerConstraints.weightx = 0.5;
		pointerConstraints.gridy = 1;
		dspMemo = new JLabel();
		dspMemo.setFont(new Font("Dialog", Font.PLAIN, 12));
		dspMemo.setText("JLabel");
		dspMemo.setBorder(new TitledBorder("memo"));
		GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
		gridBagConstraints6.gridx = 0;
		gridBagConstraints6.gridwidth = 3;
		gridBagConstraints6.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints6.gridy = 1;
		dspPath = new JLabel();
		dspPath.setBorder(new TitledBorder("path"));
		dspPath.setFont(new Font("Dialog", Font.PLAIN, 12));
		dspPath.setText("JLabel");
		GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
		gridBagConstraints4.gridx = 2;
		gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints4.weightx = 0.5;
		gridBagConstraints4.gridwidth = 1;
		gridBagConstraints4.gridy = 0;
		dspName = new JLabel();
		dspName.setBorder(new TitledBorder("name"));
		dspName.setFont(new Font("Dialog", Font.PLAIN, 12));
		dspName.setBackground(Color.white);
		dspName.setForeground(new Color(51, 51, 51));
		dspName.setText("JLabel");
		this.setSize(505, 85);
		this.setLayout(new GridBagLayout());
		this.setBackground(new Color(198, 218, 238));
		this.add(dspName, gridBagConstraints4);
		this.add(dspPath, gridBagConstraints6);
		this.add(dspMemo, pointerConstraints);
		this.add(dspUser, gridBagConstraints1);
		this.add(dspReviewDate, gridBagConstraints2);
	}
	static Border BORDER_NO_WORK = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY);  //  @jve:decl-index=0:
	static Border BORDER_WORK = BorderFactory.createMatteBorder(4, 4, 4, 4, Color.ORANGE);  //  @jve:decl-index=0:
//	static Border BORDER_NO_WORK = BorderFactory.createEmptyBorder(0, 16, 0, 0);  //  @jve:decl-index=0:
//	static Border BORDER_WORK = BorderFactory.createMatteBorder(0, 16, 0, 0,
//			new ImageIcon(PnlOutput.class.getResource("/image/work.gif")));  //  @jve:decl-index=0:
	public void setOutput(Output output){
//		dspOutputId.setText(""+output.getOutputId());
//		dspName.setText(output.getName());
//		dspPath.setText(output.getPath());
//		dspMemo.setText(output.getMemo());
//		dspType.setText(output.getOutputType().getOutputTypeName());
//		dspUser.setText(output.getUserName());
//		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
//		dspReviewDate.setText(formatter.format(output.getNewDate()));
//		if(output.getWorkCount() > 0){
//			this.setBorder(BORDER_WORK);
//		}else{
//			this.setBorder(BORDER_NO_WORK);
//		}
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
