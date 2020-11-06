package jp.tokyo.selj.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.sql.Timestamp;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import jp.tokyo.selj.common.DateTextFormatter;
import jp.tokyo.selj.common.TextUndoHandler;
import jp.tokyo.selj.dao.Output;
import jp.tokyo.selj.dao.OutputType;
import jp.tokyo.selj.dao.OutputTypeDao;
import jp.tokyo.selj.dao.SelJDaoContainer;
import jp.tokyo.selj.model.MasterComboModel;

import org.apache.log4j.Logger;

public class PnlOutputDetail extends JPanel {
	Logger log = Logger.getLogger(this.getClass());  //  @jve:decl-index=0:

	private static final long serialVersionUID = 1L;

    protected TextUndoHandler nameUndoHandler_ = new TextUndoHandler();  //  @jve:decl-index=0:
    protected TextUndoHandler pathUndoHandler_ = new TextUndoHandler();  //  @jve:decl-index=0:
    protected TextUndoHandler memoUndoHandler_ = new TextUndoHandler();  //  @jve:decl-index=0:

	Output output_ = null;  //  @jve:decl-index=0:
	JLabel dspOutputId = null;
	JTextField inpName = null;
	JTextField inpPath = null;
	JComboBox inpOutputType = null;
	private JScrollPane jScrollPane = null;
	JTextArea inpMemo = null;
	JComboBox inpCreator = null;
	JFormattedTextField inpDate = null;
	private JPanel jPanel = null;
	/**
	 * This is the default constructor
	 */
	public PnlOutputDetail() {
		super();
		initialize();
	}
	public void setup(){
		//成果物種類リストの設定
		getInpOutputType().setModel(MasterComboModel.newOutputTypeComboBoxModel());
		getInpOutputType().getModel().setSelectedItem("");
		
		//作業者の設定
		getInpCreator().setModel(MasterComboModel.newUserComboBoxModel());
		getInpCreator().getModel().setSelectedItem("");
		
	    nameUndoHandler_.setup(getInpName());
	    pathUndoHandler_.setup(getInpPath());
	    memoUndoHandler_.setup(getInpMemo());

	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gbcPanel = new GridBagConstraints();
		gbcPanel.gridx = 0;
		gbcPanel.fill = GridBagConstraints.HORIZONTAL;
		gbcPanel.gridy = 0;
		GridBagConstraints gbcMemo = new GridBagConstraints();
		gbcMemo.fill = GridBagConstraints.BOTH;
		gbcMemo.gridy = 3;
		gbcMemo.weightx = 1.0;
		gbcMemo.weighty = 10.0;
		gbcMemo.gridwidth = 5;
		gbcMemo.gridx = 0;
		GridBagConstraints gbcPath = new GridBagConstraints();
		gbcPath.fill = GridBagConstraints.BOTH;
		gbcPath.gridy = 2;
		gbcPath.weightx = 1.0;
		gbcPath.anchor = GridBagConstraints.WEST;
		gbcPath.gridwidth = 4;
		gbcPath.weighty = 0.0;
		gbcPath.gridx = 0;
		GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
		gridBagConstraints6.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints6.gridy = 0;
		gridBagConstraints6.weightx = 1.0;
		gridBagConstraints6.anchor = GridBagConstraints.WEST;
		gridBagConstraints6.weighty = 0.0;
		gridBagConstraints6.gridwidth = 2;
		gridBagConstraints6.gridx = 3;
		GridBagConstraints gbcName = new GridBagConstraints();
		gbcName.fill = GridBagConstraints.HORIZONTAL;
		gbcName.gridy = 1;
		gbcName.weightx = 1.0;
		gbcName.anchor = GridBagConstraints.WEST;
		gbcName.weighty = 0.0;
		gbcName.gridwidth = 1;
		gbcName.gridx = 0;
		dspOutputId = new JLabel();
		dspOutputId.setText("JLabel");
		dspOutputId.setBorder(new TitledBorder("id"));
		this.setSize(597, 267);
		this.setLayout(new GridBagLayout());
		this.add(getInpName(), gbcName);
		this.add(getInpPath(), gbcPath);
		this.add(getJScrollPane(), gbcMemo);
		this.add(getJPanel(), gbcPanel);
	}

	/**
	 * This method initializes InpName	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	public JTextField getInpName() {
		if (inpName == null) {
			inpName = new JTextField();
			inpName.setBorder(new TitledBorder("name"));
			inpName.setToolTipText("ファイルをドロップできます");
		}
		return inpName;
	}

	/**
	 * This method initializes inpPath	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	public JTextField getInpPath() {
		if (inpPath == null) {
			inpPath = new JTextField();
			inpPath.setBorder(new TitledBorder("path"));
			inpPath.setToolTipText("ファイルをドロップできます");
		}
		return inpPath;
	}

	/**
	 * This method initializes inpOutputType	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	public JComboBox getInpOutputType() {
		if (inpOutputType == null) {
			inpOutputType = new JComboBox();
			inpOutputType.setBorder(new TitledBorder("type"));
			inpOutputType.setEditable(false);
		}
		return inpOutputType;
	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getInpMemo());
			jScrollPane.setBorder(new TitledBorder("memo"));
		}
		return jScrollPane;
	}

	/**
	 * This method initializes inpMemo	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	public JTextArea getInpMemo() {
		if (inpMemo == null) {
			inpMemo = new JTextArea();
		}
		return inpMemo;
	}

	/**
	 * This method initializes inpCreator	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	public JComboBox getInpCreator() {
		if (inpCreator == null) {
			inpCreator = new JComboBox();
			inpCreator.setBorder(new TitledBorder("creator"));
			inpCreator.setEditable(true);
		}
		return inpCreator;
	}

	/**
	 * This method initializes inpDate	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	public JFormattedTextField getInpDate() {
		if (inpDate == null) {
			inpDate = new JFormattedTextField(new DateTextFormatter());
			inpDate.setBorder(new TitledBorder("date"));
		}
		return inpDate;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gb01 = new GridBagConstraints();
			gb01.fill = GridBagConstraints.HORIZONTAL;
			gb01.gridx = 0;
			gb01.gridy = 0;
			gb01.weightx = 0.1;
			gb01.weighty = 0.0;
			GridBagConstraints gb02 = new GridBagConstraints();
			gb02.fill = GridBagConstraints.HORIZONTAL;
			gb02.gridx = 1;
			gb02.gridy = 0;
			gb02.weightx = 0.5;
			gb02.weighty = 0.0;
			GridBagConstraints gb03 = new GridBagConstraints();
			gb03.fill = GridBagConstraints.HORIZONTAL;
			gb03.gridx = 2;
			gb03.gridy = 0;
			gb03.weightx = 0.5;
			gb03.weighty = 0.0;
			GridBagConstraints gb04 = new GridBagConstraints();
			gb04.gridx = 3;
			gb04.gridy = 0;
			gb04.weightx = 0.5;
			gb04.anchor = GridBagConstraints.WEST;
			gb04.weighty = 0.0;
			jPanel = new JPanel();
//			jPanel.setLayout(new BoxLayout(getJPanel(), BoxLayout.X_AXIS));
			jPanel.setLayout(new GridBagLayout());
			jPanel.add(dspOutputId, gb01);
			jPanel.add(getInpOutputType(), gb02);
			jPanel.add(getInpCreator(), gb03);
			jPanel.add(getInpDate(), gb04);
		}
		return jPanel;
	}
	public void resetOutputTypeList(){
		getInpOutputType().setModel(MasterComboModel.newOutputTypeComboBoxModel());
	}
	public void resetOutputTypeList(long workTypeId){
		OutputTypeDao outputTypeDao = (OutputTypeDao)SelJDaoContainer.SEL_DAO_CONT.getComponent(OutputTypeDao.class);
		List<OutputType> outputTypes = outputTypeDao.findByWorkTypeId(workTypeId);
		getInpOutputType().setModel(new DefaultComboBoxModel(outputTypes.toArray()));
	}

	public void setOutput(Output output) {
		output_ = output;
		dspOutputId.setText(""+output.getOutputId());
		inpCreator.getModel().setSelectedItem(output.getUserName());
		inpDate.setValue(output.getNewDate());
		inpMemo.setText(output.getMemo());
		inpName.setText(output.getName());
		inpOutputType.getModel().setSelectedItem(output.getOutputType());
		inpPath.setText(output.getPath());

		nameUndoHandler_.cleanup();
	    pathUndoHandler_.cleanup();
	    memoUndoHandler_.cleanup();
	}

	public Output getOutput() {
		output_.setUserName(
				(inpCreator.getModel().getSelectedItem()==null)? null:
//					((User)inpCreator.getModel().getSelectedItem()).getUserName()
					""+inpCreator.getModel().getSelectedItem()
		);

		output_.setNewDate( (Timestamp)inpDate.getValue() );
		output_.setMemo( inpMemo.getText() );
		output_.setName( inpName.getText() );
		output_.setOutputType( (OutputType)inpOutputType.getSelectedItem() );
		output_.setPath( inpPath.getText() );
//		output_.setPointer( inpPointer.getText() );
		return output_;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
