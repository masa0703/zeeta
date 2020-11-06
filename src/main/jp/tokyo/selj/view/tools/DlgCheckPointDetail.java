package jp.tokyo.selj.view.tools;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import jp.tokyo.selj.common.AppException;
import jp.tokyo.selj.common.NumericTextFormatter;
import jp.tokyo.selj.dao.CheckPoint;
import jp.tokyo.selj.dao.CheckPointDao;
import jp.tokyo.selj.dao.OutputType;
import jp.tokyo.selj.dao.SelJDaoContainer;
import jp.tokyo.selj.model.MasterComboModel;
import jp.tokyo.selj.view.ActTransactionBase;

import org.apache.log4j.Logger;
import org.seasar.framework.container.S2Container;
import java.awt.Insets;

public class DlgCheckPointDetail extends DlgTableMaintUpdateBase {
	Logger log = Logger.getLogger(this.getClass());

	private JPanel jPanel = null;
	private JFormattedTextField inpSEQ = null;
	private JPanel jPanel1 = null;
	private JPanel jPanel2 = null;
	private JButton cmdCommit = null;
	
	S2Container daoCont_ = SelJDaoContainer.SEL_DAO_CONT;  //  @jve:decl-index=0:
	boolean isUpdate_ = false;
	CheckPointDao checkPointDao_ = null;  //  @jve:decl-index=0:
	CheckPoint checkPoint_ = null;  //  @jve:decl-index=0:
	//new, update兼用
	private class ActNewOrUpdate extends ActTransactionBase {
		protected Component getOwnerComponent(){
			return DlgCheckPointDetail.this;
		}
		public ActNewOrUpdate(){
			super();
			putValue(Action.NAME, "commit");
			putValue(Action.SHORT_DESCRIPTION, "新規登録または更新");
		}
		public void actionPerformed2(ActionEvent e) {
			checkPoint_.setSEQ(  ((Number)getInpSEQ().getValue()).intValue()  );
			checkPoint_.setCheckCont( getInpCheckCont().getText() );
			checkPoint_.setOutputType( (OutputType)getInpOutputType().getSelectedItem());
			checkPoint_.setDeleteFlg( getInpDeleteFlg().isSelected() );
			checkPoint_.check();
			if(isUpdate_){
				//更新
				checkPointDao_.update(checkPoint_);
			}else{
				//新規追加
				checkPointDao_.insert(checkPoint_);
			}
			getOwnerComponent().setVisible(false);
			//一旦トランザクションを終了しないとmodel_.executeQuery()がタイムアウトになる
			postProc();
			preProc();
			//JTableをリフレッシュする
			((DlgCheckPointAndPropTypeMaint)getOwner()).refreshCheckPointTable();

			//MasterComboModelを更新する
			MasterComboModel.refreshCheckState();
		}
	}
	//delete用
	ActDelete actDelete_ = new ActDelete();  //  @jve:decl-index=0:

	private JTextField inpCheckPointId = null;

	private JComboBox inpOutputType = null;

	private JTextField inpCheckCont = null;

	private JCheckBox inpDeleteFlg = null;

	private JPanel jPanel3 = null;

	private class ActDelete extends ActTransactionBase {
		protected Component getOwnerComponent(){
			return DlgCheckPointDetail.this;
		}
		public ActDelete(){
			super();
			putValue(Action.NAME, "delete");
			putValue(Action.SHORT_DESCRIPTION, "削除します");
		}
		public void actionPerformed2(ActionEvent e) {
			String strId = e.getActionCommand();
			//削除
			checkPointDao_.deleteById(Long.parseLong(strId)); 
			//一旦トランザクションを終了しないとmodel_.executeQuery()がタイムアウトになる
			postProc();
			preProc();
			//JTableをリフレッシュする
			((DlgCheckPointAndPropTypeMaint)getOwner()).refreshCheckPointTable();

			getOwnerComponent().setVisible(false);
		}
	}

	void setCheckPoint(CheckPoint checkPoint) {
		getInpCheckPointId().setText(""+checkPoint.getCheckPointId());
		getInpSEQ().setValue(checkPoint.getSEQ());
		getInpCheckCont().setText(checkPoint.getCheckCont());
		getInpDeleteFlg().setSelected(checkPoint.isDeleteFlg());
		
		if( checkPoint.getOutputType() == null){
			//作業種類のポジショニング
			DefaultComboBoxModel cmodel = (DefaultComboBoxModel)getInpOutputType().getModel();
			for(int i=0; i < cmodel.getSize(); i++){
				OutputType outputype = (OutputType)cmodel.getElementAt(i);
				if(outputype.getOutputTypeId() == checkPoint.getOutputTypeId()){
					checkPoint.setOutputType(outputype);
					break;
				}
			}
		}
		getInpOutputType().getModel().setSelectedItem(checkPoint.getOutputType());

	}	
	public DlgCheckPointDetail(JDialog owner) {
		super(owner);
		initialize();
	}
	public void setup(){
		super.setup();
		checkPointDao_ = (CheckPointDao) daoCont_.getComponent(CheckPointDao.class);
		getCmdCommit().setAction(new ActNewOrUpdate());
		getInpOutputType().setModel(MasterComboModel.newOutputTypeComboBoxModel());
	}
	public void newCheckPoint(long outputTypeId /* 初期値 */){
		checkPoint_ = new CheckPoint();
		checkPoint_.setOutputTypeId(outputTypeId);
		setCheckPoint(checkPoint_);
		isUpdate_ = false;
	}
	public void loadCheckPoint(long id){
		checkPoint_ = checkPointDao_.findById(id); 
		if(checkPoint_ == null){
			throw new AppException("チェック項目ID="+id+" は、既に削除されています");
		}
		setCheckPoint(checkPoint_);
		isUpdate_ = true;
	}
	public void deleteCheckPoint(long id){
		actDelete_.actionPerformed(new ActionEvent(this, 0, ""+id));
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setSize(new Dimension(756, 186));
        this.setContentPane(getJPanel());
        this.setTitle("check point");
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setLayout(new BorderLayout());
			jPanel.add(getJPanel1(), BorderLayout.CENTER);
			jPanel.add(getJPanel2(), BorderLayout.SOUTH);
		}
		return jPanel;
	}

	/**
	 * This method initializes inpSEQ	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JFormattedTextField getInpSEQ() {
		if (inpSEQ == null) {
			inpSEQ = new JFormattedTextField(new NumericTextFormatter());
			inpSEQ.setBorder(new TitledBorder("SEQ"));
			inpSEQ.setPreferredSize(new Dimension(60, 42));
		}
		return inpSEQ;
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.anchor = GridBagConstraints.WEST;
			gridBagConstraints11.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints11.gridy = 0;
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.fill = GridBagConstraints.BOTH;
			gridBagConstraints21.gridy = 1;
			gridBagConstraints21.weightx = 1.0;
			gridBagConstraints21.weighty = 1.0;
			gridBagConstraints21.gridwidth = 1;
			gridBagConstraints21.gridx = 0;
			jPanel1 = new JPanel();
			jPanel1.setLayout(new GridBagLayout());
			jPanel1.add(getInpCheckCont(), gridBagConstraints21);
			jPanel1.add(getJPanel3(), gridBagConstraints11);
		}
		return jPanel1;
	}

	/**
	 * This method initializes jPanel2	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			jPanel2 = new JPanel();
			jPanel2.setLayout(new FlowLayout());
			jPanel2.add(getCmdCommit());
		}
		return jPanel2;
	}

	/**
	 * This method initializes cmdCommit	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdCommit() {
		if (cmdCommit == null) {
			cmdCommit = new JButton();
		}
		return cmdCommit;
	}
	/**
	 * This method initializes inpCheckPointId	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getInpCheckPointId() {
		if (inpCheckPointId == null) {
			inpCheckPointId = new JTextField();
			inpCheckPointId.setBorder(new TitledBorder("id"));
			inpCheckPointId.setPreferredSize(new Dimension(60, 42));
			inpCheckPointId.setEditable(false);
		}
		return inpCheckPointId;
	}
	/**
	 * This method initializes inpOutputType	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getInpOutputType() {
		if (inpOutputType == null) {
			inpOutputType = new JComboBox();
			inpOutputType.setPreferredSize(new Dimension(200, 52));
			inpOutputType.setBorder(new TitledBorder("output type"));
		}
		return inpOutputType;
	}
	/**
	 * This method initializes inpCheckCont	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JTextField getInpCheckCont() {
		if (inpCheckCont == null) {
			inpCheckCont = new JTextField();
			inpCheckCont.setBorder(new TitledBorder("check"));
		}
		return inpCheckCont;
	}
	/**
	 * This method initializes inpDeleteFlg	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getInpDeleteFlg() {
		if (inpDeleteFlg == null) {
			inpDeleteFlg = new JCheckBox();
			inpDeleteFlg.setText("delete flag");
		}
		return inpDeleteFlg;
	}
	/**
	 * This method initializes jPanel3	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel3() {
		if (jPanel3 == null) {
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints3.gridy = 0;
			gridBagConstraints3.weightx = 0.5;
			gridBagConstraints3.anchor = GridBagConstraints.WEST;
			gridBagConstraints3.gridx = 3;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints2.gridx = 2;
			gridBagConstraints2.gridy = 0;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.insets = new Insets(5, 3, 5, 2);
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.fill = GridBagConstraints.NONE;
			gridBagConstraints1.gridx = 1;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.weightx = 0.0;
			gridBagConstraints1.ipadx = 40;
			gridBagConstraints1.insets = new Insets(5, 5, 5, 5);
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.NONE;
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.weightx = 0.0;
			gridBagConstraints.ipadx = 50;
			gridBagConstraints.insets = new Insets(5, 5, 5, 5);
			jPanel3 = new JPanel();
			jPanel3.setLayout(new GridBagLayout());
			jPanel3.add(getInpCheckPointId(), gridBagConstraints);
			jPanel3.add(getInpSEQ(), gridBagConstraints1);
			jPanel3.add(getInpOutputType(), gridBagConstraints2);
			jPanel3.add(getInpDeleteFlg(), gridBagConstraints3);
		}
		return jPanel3;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
