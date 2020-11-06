package jp.tokyo.selj.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.SimpleDateFormat;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import jp.tokyo.selj.dao.Output;

public class PnlOutput extends JPanel {

	private static final long serialVersionUID = 1L;
	JLabel dspOutputId = null;
	JLabel dspName = null;
	JLabel dspType = null;
	JLabel dspPath = null;
	private JLabel dspUser = null;
	private JLabel dspDate = null;
	/**
	 * This is the default constructor
	 */
	public PnlOutput() {
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
		gridBagConstraints2.gridx = 3;
		gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints2.gridy = 1;
		dspDate = new JLabel();
		dspDate.setFont(new Font("Dialog", Font.PLAIN, 12));
		dspDate.setText("JLabel");
		dspDate.setBorder(new TitledBorder("date"));
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridx = 3;
		gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints1.weightx = 0.2;
		gridBagConstraints1.gridy = 0;
		dspUser = new JLabel();
		dspUser.setFont(new Font("Dialog", Font.PLAIN, 12));
		dspUser.setText("JLabel");
		dspUser.setBorder(new TitledBorder("creator"));
		GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
		gridBagConstraints6.gridx = 0;
		gridBagConstraints6.gridwidth = 3;
		gridBagConstraints6.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints6.gridy = 1;
		dspPath = new JLabel();
		dspPath.setBorder(new TitledBorder("path"));
		dspPath.setFont(new Font("Dialog", Font.PLAIN, 12));
		dspPath.setText("JLabel");
		GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
		gridBagConstraints5.gridx = 1;
		gridBagConstraints5.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints5.weightx = 0.2;
		gridBagConstraints5.gridy = 0;
		dspType = new JLabel();
		dspType.setBorder(new TitledBorder("type"));
		dspType.setFont(new Font("Dialog", Font.PLAIN, 12));
		dspType.setText("JLabel");
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
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.gridy = 0;
		dspOutputId = new JLabel();
		dspOutputId.setText("JLabel");
		dspOutputId.setFont(new Font("Dialog", Font.PLAIN, 12));
		dspOutputId.setBorder(new TitledBorder("id"));
		this.setSize(505, 85);
		this.setLayout(new GridBagLayout());
		this.setBackground(new Color(198, 218, 238));
		this.setToolTipText("オレンジの枠はTreeNodeへリンクしている成果物を指します");
		this.add(dspOutputId, gridBagConstraints);
		this.add(dspName, gridBagConstraints4);
		this.add(dspType, gridBagConstraints5);
		this.add(dspPath, gridBagConstraints6);
		this.add(dspUser, gridBagConstraints1);
		this.add(dspDate, gridBagConstraints2);
	}
	static Border BORDER_NO_WORK = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY);  //  @jve:decl-index=0:
	static Border BORDER_WORK = BorderFactory.createMatteBorder(4, 4, 4, 4, Color.ORANGE);  //  @jve:decl-index=0:
//	static Border BORDER_NO_WORK = BorderFactory.createEmptyBorder(0, 16, 0, 0);  //  @jve:decl-index=0:
//	static Border BORDER_WORK = BorderFactory.createMatteBorder(0, 16, 0, 0,
//			new ImageIcon(PnlOutput.class.getResource("/image/work.gif")));  //  @jve:decl-index=0:
	public void setOutput(Output output){
		dspOutputId.setText(""+output.getOutputId());
		dspName.setText(output.getName());
		dspPath.setText(output.getPath());
//		dspMemo.setText(output.getMemo());
		dspType.setText(output.getOutputType().getOutputTypeName());
		dspUser.setText(output.getUserName());
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		dspDate.setText(formatter.format(output.getNewDate()));
		if(output.getWorkCount() > 0){
			this.setBorder(BORDER_WORK);
		}else{
			this.setBorder(BORDER_NO_WORK);
		}
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
