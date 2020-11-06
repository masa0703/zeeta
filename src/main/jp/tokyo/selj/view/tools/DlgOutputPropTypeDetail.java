package jp.tokyo.selj.view.tools;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import jp.tokyo.selj.common.NumericTextFormatter;
import jp.tokyo.selj.dao.SelJDaoContainer;
import jp.tokyo.selj.dao.OutputPropType;
import jp.tokyo.selj.dao.OutputPropTypeDao;
import jp.tokyo.selj.view.ActTransactionBase;

import org.apache.log4j.Logger;
import org.seasar.framework.container.S2Container;

public class DlgOutputPropTypeDetail extends DlgTableMaintUpdateBase {
	Logger log = Logger.getLogger(this.getClass());

	private JPanel jPanel = null;
	private JTextField inpWorkPropTypeName = null;
	private JPanel jPanel1 = null;
	private JPanel jPanel2 = null;
	private JButton cmdCommit = null;
	
	S2Container daoCont_ = SelJDaoContainer.SEL_DAO_CONT;  //  @jve:decl-index=0:
	boolean isUpdate_ = false;
	OutputPropTypeDao workPropTypeDao_ = null;  //  @jve:decl-index=0:
	OutputPropType workPropType_ = null;  //  @jve:decl-index=0:
	//new, update兼用
	private class ActNewOrUpdate extends ActTransactionBase {
		protected Component getOwnerComponent(){
			return DlgOutputPropTypeDetail.this;
		}
		public ActNewOrUpdate(){
			super();
			putValue(Action.NAME, "commit");
			putValue(Action.SHORT_DESCRIPTION, "新規登録または更新");
		}
		public void actionPerformed2(ActionEvent e) {
			workPropType_.setOutputPropTypeName( getInpWorkPropTypeName().getText() );
			workPropType_.setUnitName( getInpUnitName().getText() );
			workPropType_.setDescr( getInpDesc().getText() );
			workPropType_.setSeq( ((Number)getInpSeq().getValue()).intValue() );
			workPropType_.check();
			if(isUpdate_){
				//更新
				workPropTypeDao_.update(workPropType_);
			}else{
				//新規追加
				workPropTypeDao_.insert(workPropType_);
			}
			getOwnerComponent().setVisible(false);
			//一旦トランザクションを終了しないとmodel_.executeQuery()がタイムアウトになる
			postProc();
			preProc();
			//JTableをリフレッシュする
			((DlgCheckPointAndPropTypeMaint)getOwner()).refreshOutputPropTypeTable();

			//MasterComboModelを更新する
//			MasterComboModel.refreshUser();
		}
	}
	//delete用
	ActDelete actDelete_ = new ActDelete();  //  @jve:decl-index=0:

	private JTextField inpDesc = null;

	private JTextField dspWorkPropTypeId = null;

	private JTextField inpUnitName = null;

	private JFormattedTextField inpSeq = null;

	private class ActDelete extends ActTransactionBase {
		protected Component getOwnerComponent(){
			return DlgOutputPropTypeDetail.this;
		}
		public ActDelete(){
			super();
			putValue(Action.NAME, "delete");
			putValue(Action.SHORT_DESCRIPTION, "削除します");
		}
		public void actionPerformed2(ActionEvent e) {
			String id_str = e.getActionCommand();
			//削除
			workPropTypeDao_.deleteById(Long.parseLong(id_str)); 
			//一旦トランザクションを終了しないとmodel_.executeQuery()がタイムアウトになる
			postProc();
			preProc();
			//一覧をリフレッシュする
			((DlgCheckPointAndPropTypeMaint)getOwner()).refreshOutputPropTypeTable();

			//MasterComboModelを更新する
//			MasterComboModel.refreshUser();
//			getOwnerComponent().setVisible(false);
		}
	}

	public void setWorkPropType(OutputPropType wpt) {
		isUpdate_ = true;
		workPropType_ = wpt;
		getDspWorkPropTypeId().setText(""+wpt.getOutputPropTypeId());
		getInpWorkPropTypeName().setText(wpt.getOutputPropTypeName());
		getInpDesc().setText(wpt.getDescr());
		getInpSeq().setValue(wpt.getSeq());
		getInpUnitName().setText(wpt.getUnitName());
	}	
	public DlgOutputPropTypeDetail(JDialog owner) {
		super(owner);
		initialize();
	}
	public void setup(){
		super.setup();
		workPropTypeDao_ = (OutputPropTypeDao)daoCont_.getComponent(OutputPropTypeDao.class);
		getCmdCommit().setAction(new ActNewOrUpdate());
	}
	public void newWorkPropType(){
		workPropType_ = new OutputPropType();
		setWorkPropType(workPropType_);
		isUpdate_ = false;
	}
	public void deleteWorkPropType(long id){
		//トランザクションを使うのでわざわざActionを使用する
		actDelete_.actionPerformed(new ActionEvent(this, 0, ""+id));
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setSize(new Dimension(627, 157));
        this.setContentPane(getJPanel());
        this.setTitle("output property type");
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
	 * This method initializes inpWorkPropTypeName	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getInpWorkPropTypeName() {
		if (inpWorkPropTypeName == null) {
			inpWorkPropTypeName = new JTextField();
			inpWorkPropTypeName.setBorder(new TitledBorder("workPropTypeName"));
		}
		return inpWorkPropTypeName;
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
			gridBagConstraints22.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints22.gridy = 0;
			gridBagConstraints22.weightx = 0.0;
			gridBagConstraints22.ipadx = 40;
			gridBagConstraints22.gridx = 1;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.gridx = 3;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.weightx = 0.0;
			gridBagConstraints.ipadx = 40;
			gridBagConstraints.gridx = 0;
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints21.gridy = 1;
			gridBagConstraints21.weightx = 1.0;
			gridBagConstraints21.gridwidth = 4;
			gridBagConstraints21.gridx = 0;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints2.gridy = 0;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.gridx = 2;
			jPanel1 = new JPanel();
			jPanel1.setLayout(new GridBagLayout());
			jPanel1.add(getInpWorkPropTypeName(), gridBagConstraints2);
			jPanel1.add(getInpDesc(), gridBagConstraints21);
			jPanel1.add(getDspWorkPropTypeId(), gridBagConstraints);
			jPanel1.add(getInpUnitName(), gridBagConstraints1);
			jPanel1.add(getInpSeq(), gridBagConstraints22);
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
	 * This method initializes inpOrg	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getInpDesc() {
		if (inpDesc == null) {
			inpDesc = new JTextField();
			inpDesc.setBorder(new TitledBorder("describe"));
		}
		return inpDesc;
	}
	/**
	 * This method initializes dspWorkPropTypeId	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getDspWorkPropTypeId() {
		if (dspWorkPropTypeId == null) {
			dspWorkPropTypeId = new JTextField();
			dspWorkPropTypeId.setBorder(new TitledBorder("id"));
			dspWorkPropTypeId.setEditable(false);
		}
		return dspWorkPropTypeId;
	}
	/**
	 * This method initializes inpUnitName	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getInpUnitName() {
		if (inpUnitName == null) {
			inpUnitName = new JTextField();
			inpUnitName.setBorder(new TitledBorder("unitName"));
		}
		return inpUnitName;
	}
	/**
	 * This method initializes inpSeq	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JFormattedTextField getInpSeq() {
		if (inpSeq == null) {
			inpSeq = new JFormattedTextField(new NumericTextFormatter());
			inpSeq.setBorder(new TitledBorder("SEQ"));
		}
		return inpSeq;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
