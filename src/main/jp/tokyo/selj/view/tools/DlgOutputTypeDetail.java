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
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import jp.tokyo.selj.common.AppException;
import jp.tokyo.selj.common.NumericTextFormatter;
import jp.tokyo.selj.dao.OutputType;
import jp.tokyo.selj.dao.OutputTypeDao;
import jp.tokyo.selj.dao.SelJDaoContainer;
import jp.tokyo.selj.dao.WorkType;
import jp.tokyo.selj.model.MasterComboModel;
import jp.tokyo.selj.view.ActTransactionBase;

import org.apache.log4j.Logger;
import org.seasar.framework.container.S2Container;

public class DlgOutputTypeDetail extends DlgTableMaintUpdateBase {
	Logger log = Logger.getLogger(this.getClass());

	private JPanel jPanel = null;
	private JTextField inpOutputTypeId = null;
	private JFormattedTextField inpSEQ = null;
	private JTextField inpOutputTypeName = null;
	private JTextArea inpMemo = null;
	private JPanel jPanel1 = null;
	private JPanel jPanel2 = null;
	private JButton cmdCommit = null;
	
	S2Container daoCont_ = SelJDaoContainer.SEL_DAO_CONT;  //  @jve:decl-index=0:
	boolean isUpdate_ = false;
	OutputTypeDao outputTypeDao_ = null;  //  @jve:decl-index=0:
	OutputType outputType_ = null;  //  @jve:decl-index=0:
	private JComboBox inpWorkType = null;

	//new, update兼用
	class ActNewOrUpdateOutputType extends ActTransactionBase {
		protected Component getOwnerComponent(){
			return DlgOutputTypeDetail.this;
		}
		public ActNewOrUpdateOutputType(){
			super();
			putValue(Action.NAME, "commit");
			putValue(Action.SHORT_DESCRIPTION, "新規登録または更新");
		}
		public void actionPerformed2(ActionEvent e) {
			outputType_.setSeq( ((Number)getInpSEQ().getValue()).intValue() );
			outputType_.setOutputTypeName( getInpOutputTypeName().getText() );
			outputType_.setWorkType((WorkType)getInpWorkType().getSelectedItem());
			outputType_.setMemo( getInpMemo().getText() );
			outputType_.check();
			if(isUpdate_){
				//更新
				outputTypeDao_.update(outputType_);
			}else{
				//新規追加
				outputTypeDao_.insert(outputType_);
			}
			getOwnerComponent().setVisible(false);
			//一旦トランザクションを終了しないとmodel_.executeQuery()がタイムアウトになる
			postProc();
			preProc();
			//JTableをリフレッシュする
			if(getOwner() instanceof DlgWorkTypeOutputType){
				((DlgWorkTypeOutputType)getOwner()).loadWorkTypeTable();
			}else{
				((DlgCheckPointAndPropTypeMaint)getOwner()).loadOutputTypeTable();
			}
			//comboBoxModelをリフレッシュ
			MasterComboModel.refreshOutputType();
		}
	}
	//delete用
	ActDeleteOutputType actDeleteOutputType_ = new ActDeleteOutputType();
	class ActDeleteOutputType extends ActTransactionBase {
		protected Component getOwnerComponent(){
			return DlgOutputTypeDetail.this;
		}
		public ActDeleteOutputType(){
			super();
			putValue(Action.NAME, "delete");
			putValue(Action.SHORT_DESCRIPTION, "削除します");
		}
		public void actionPerformed2(ActionEvent e) {
			int id = Integer.parseInt(e.getActionCommand());
			//outputType削除
			outputTypeDao_.deleteByOutputTypeId(id); 
			//一旦トランザクションを終了しないとmodel_.executeQuery()がタイムアウトになる
			postProc();
			preProc();
			//MasterComboModelを更新する
			MasterComboModel.refreshOutputType();
			
			//JTableをリフレッシュする
			((DlgWorkTypeOutputType)getOwner()).refreshOutputTypeTable();

			getOwnerComponent().setVisible(false);
		}
	}

	void setOutputType(OutputType outputType) {
		getInpSEQ().setValue(outputType.getSeq());
		getInpOutputTypeName().setText(outputType.getOutputTypeName());
		getInpMemo().setText(outputType.getMemo());
		
		if( outputType.getWorkType() == null){
			//作業種類のポジショニング
			DefaultComboBoxModel cmodel = (DefaultComboBoxModel)getInpWorkType().getModel();
			for(int i=0; i < cmodel.getSize(); i++){
				WorkType workType = (WorkType)cmodel.getElementAt(i);
				if(workType.getWorkTypeId() == outputType.getWorkTypeId()){
					outputType.setWorkType(workType);
					break;
				}
			}
		}
		getInpWorkType().getModel().setSelectedItem(outputType.getWorkType());
	}	
	public DlgOutputTypeDetail(JDialog owner) {
		super(owner);
		initialize();
	}
	public void setup(){
		super.setup();
		outputTypeDao_ = (OutputTypeDao) daoCont_.getComponent(OutputTypeDao.class);
		getCmdCommit().setAction(new ActNewOrUpdateOutputType());
		getInpWorkType().setModel(MasterComboModel.newWorkTypeComboBoxModel());
	}
	public void newOutputType(int workTypeId){
		outputType_ = new OutputType();
		outputType_.setWorkTypeId(workTypeId);
		setOutputType(outputType_);
		isUpdate_ = false;
	}
	public void loadOutputType(long id){
		outputType_ = outputTypeDao_.findByOutputTypeId(id); 
		if(outputType_ == null){
			throw new AppException("成果物種類ID="+id+" は、既に削除されています");
		}
		setOutputType(outputType_);
		isUpdate_ = true;
	}
	public void deleteOutputType(int outputTypeId){
		actDeleteOutputType_.actionPerformed(new ActionEvent(this, 0, ""+outputTypeId));
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setSize(new Dimension(622, 176));
        this.setContentPane(getJPanel());
        this.setTitle("output type");
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
	 * This method initializes inpOutputTypeId	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getInpOutputTypeId() {
		if (inpOutputTypeId == null) {
			inpOutputTypeId = new JTextField();
			inpOutputTypeId.setBorder(new TitledBorder("id"));
			inpOutputTypeId.setEditable(false);
		}
		return inpOutputTypeId;
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
		}
		return inpSEQ;
	}

	/**
	 * This method initializes inpOutputTypeName	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getInpOutputTypeName() {
		if (inpOutputTypeName == null) {
			inpOutputTypeName = new JTextField();
			inpOutputTypeName.setBorder(new TitledBorder("name"));
		}
		return inpOutputTypeName;
	}

	/**
	 * This method initializes inpMemo	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextArea getInpMemo() {
		if (inpMemo == null) {
			inpMemo = new JTextArea();
			inpMemo.setBorder(new TitledBorder("memo"));
		}
		return inpMemo;
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
			gridBagConstraints31.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints31.gridy = 0;
			gridBagConstraints31.weightx = 3.0;
			gridBagConstraints31.gridx = 3;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.fill = GridBagConstraints.BOTH;
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.gridy = 1;
			gridBagConstraints3.weightx = 1.0;
			gridBagConstraints3.weighty = 10.0;
			gridBagConstraints3.gridwidth = 4;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints2.gridy = 0;
			gridBagConstraints2.weightx = 8.0;
			gridBagConstraints2.gridx = 2;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.gridx = 1;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.gridx = 0;
			jPanel1 = new JPanel();
			jPanel1.setLayout(new GridBagLayout());
			jPanel1.add(getInpOutputTypeId(), gridBagConstraints);
			jPanel1.add(getInpSEQ(), gridBagConstraints1);
			jPanel1.add(getInpOutputTypeName(), gridBagConstraints2);
			jPanel1.add(getInpMemo(), gridBagConstraints3);
			jPanel1.add(getInpWorkType(), gridBagConstraints31);
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
	 * This method initializes inpWorkType	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getInpWorkType() {
		if (inpWorkType == null) {
			inpWorkType = new JComboBox();
		}
		return inpWorkType;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
