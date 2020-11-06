package jp.tokyo.selj.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

public class PnlOutputOfWork extends JPanel {

	private static final long serialVersionUID = 1L;
	JLabel dspOutputId = null;
	JLabel dspName = null;
	JLabel dspType = null;
	JLabel dspPath = null;
	JLabel dspPointer = null;
	JLabel dspWorkType = null;
	/**
	 * This is the default constructor
	 */
	public PnlOutputOfWork() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints workTypeConstraints = new GridBagConstraints();
		workTypeConstraints.gridx = 0;
		workTypeConstraints.fill = GridBagConstraints.HORIZONTAL;
		workTypeConstraints.weightx = 0.1;
		workTypeConstraints.gridy = 0;
		dspWorkType = new JLabel();
		dspWorkType.setFont(new Font("Dialog", Font.BOLD, 14));
		dspWorkType.setText("");
		dspWorkType.setHorizontalAlignment(SwingConstants.CENTER);
		dspWorkType.setBorder(BorderFactory.createMatteBorder(2, 0, 2, 0, Color.gray));
		GridBagConstraints pointerConstraints = new GridBagConstraints();
		pointerConstraints.gridx = 0;
		pointerConstraints.fill = GridBagConstraints.HORIZONTAL;
		pointerConstraints.gridwidth = 4;
		pointerConstraints.gridy = 2;
		dspPointer = new JLabel();
		dspPointer.setFont(new Font("Dialog", Font.PLAIN, 12));
		dspPointer.setText("JLabel");
		dspPointer.setBorder(new TitledBorder("pointer"));
		GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
		gridBagConstraints6.gridx = 0;
		gridBagConstraints6.gridwidth = 4;
		gridBagConstraints6.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints6.gridy = 1;
		dspPath = new JLabel();
		dspPath.setBorder(new TitledBorder("path"));
		dspPath.setFont(new Font("Dialog", Font.PLAIN, 12));
		dspPath.setText("JLabel");
		GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
		gridBagConstraints5.gridx = 2;
		gridBagConstraints5.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints5.weightx = 0.1;
		gridBagConstraints5.gridy = 0;
		dspType = new JLabel();
		dspType.setBorder(new TitledBorder("type"));
		dspType.setFont(new Font("Dialog", Font.PLAIN, 12));
		dspType.setText("JLabel");
		GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
		gridBagConstraints4.gridx = 3;
		gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints4.weightx = 1.0;
		gridBagConstraints4.gridy = 0;
		dspName = new JLabel();
		dspName.setBorder(new TitledBorder("name"));
		dspName.setFont(new Font("Dialog", Font.PLAIN, 12));
		dspName.setBackground(Color.white);
		dspName.setForeground(new Color(51, 51, 51));
		dspName.setText("JLabel");
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.gridy = 0;
		dspOutputId = new JLabel();
		dspOutputId.setText("JLabel");
		dspOutputId.setFont(new Font("Dialog", Font.PLAIN, 12));
		dspOutputId.setBorder(new TitledBorder("id"));
		this.setSize(505, 125);
		this.setLayout(new GridBagLayout());
		this.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));
		this.setBackground(new Color(198, 218, 238));
		this.add(dspOutputId, gridBagConstraints);
		this.add(dspName, gridBagConstraints4);
		this.add(dspType, gridBagConstraints5);
		this.add(dspPath, gridBagConstraints6);
		this.add(dspPointer, pointerConstraints);
		this.add(dspWorkType, workTypeConstraints);
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
