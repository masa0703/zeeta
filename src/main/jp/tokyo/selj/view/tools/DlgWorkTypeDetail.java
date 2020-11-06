package jp.tokyo.selj.view.tools;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import jp.tokyo.selj.common.AppException;
import jp.tokyo.selj.common.NumericTextFormatter;
import jp.tokyo.selj.dao.OutputTypeDao;
import jp.tokyo.selj.dao.SelJDaoContainer;
import jp.tokyo.selj.dao.WorkType;
import jp.tokyo.selj.dao.WorkTypeDao;
import jp.tokyo.selj.model.MasterComboModel;
import jp.tokyo.selj.view.ActTransactionBase;

import org.seasar.framework.container.S2Container;
import java.awt.FlowLayout;

public class DlgWorkTypeDetail extends DlgTableMaintUpdateBase {

	private JPanel jPanel = null;
	private JTextField inpWorkTypeId = null;
	private JFormattedTextField inpSEQ = null;
	private JTextField inpWorkTypeName = null;
	private JTextArea inpMemo = null;
	private JPanel jPanel1 = null;
	private JPanel jPanel2 = null;
	private JButton cmdCommit = null;
	
	S2Container daoCont_ = SelJDaoContainer.SEL_DAO_CONT;  //  @jve:decl-index=0:
	boolean isUpdate_ = false;
	WorkTypeDao workTypeDao_ = null;  //  @jve:decl-index=0:
	WorkType workType_ = null;  //  @jve:decl-index=0:

	//new, update兼用
	class ActNewOrUpdateWorkType extends ActTransactionBase {
		protected Component getOwnerComponent(){
			return DlgWorkTypeDetail.this;
		}
		public ActNewOrUpdateWorkType(){
			super();
			putValue(Action.NAME, "commit");
			putValue(Action.SHORT_DESCRIPTION, "新規登録または更新");
		}
		public void actionPerformed2(ActionEvent e) {
			workType_.setSeq( ((Number)getInpSEQ().getValue()).intValue() );
			workType_.setWorkTypeName( getInpWorkTypeName().getText() );
			workType_.setMemo( getInpMemo().getText() );
			workType_.check();
			if(isUpdate_){
				//更新
				workTypeDao_.update(workType_);
			}else{
				//新規追加
				workTypeDao_.insert(workType_);
			}
			getOwnerComponent().setVisible(false);
			//一旦トランザクションを終了しないとmodel_.executeQuery()がタイムアウトになる
			postProc();
			preProc();
			//MasterComboModelを更新する
			MasterComboModel.refreshWorkType();
			//JTableをリフレッシュする
			((DlgWorkTypeOutputType)getOwner()).loadWorkTypeTable();
//			model_.executeQuery();	
		}
	}
	//delete用
	ActDeleteWorkType actDeleteWorkType_ = new ActDeleteWorkType();
	class ActDeleteWorkType extends ActTransactionBase {
		protected Component getOwnerComponent(){
			return DlgWorkTypeDetail.this;
		}
		public ActDeleteWorkType(){
			super();
			putValue(Action.NAME, "delete");
			putValue(Action.SHORT_DESCRIPTION, "削除");
		}
		public void actionPerformed2(ActionEvent e) {
			int id = Integer.parseInt(e.getActionCommand());
			//成果物種類が紐づいていたらだめ
			OutputTypeDao outputTypeDao = (OutputTypeDao)daoCont_.getComponent(OutputTypeDao.class);
			if(outputTypeDao.findByWorkTypeId(id).size() > 0){
				throw new AppException("関連する成果物種類(output type)を削除するか別の作業種類に割り当ててください。");
			}
			//workType削除
			workTypeDao_.deleteByWorkTypeId(id); 
			//一旦トランザクションを終了しないとmodel_.executeQuery()がタイムアウトになる
			postProc();
			preProc();
			//MasterComboModelを更新する
			MasterComboModel.refreshWorkType();
			
			//JTableをリフレッシュする
			((DlgWorkTypeOutputType)getOwner()).loadWorkTypeTable();

			getOwnerComponent().setVisible(false);
		}
	}

	void setWorkType(WorkType workType) {
		getInpSEQ().setValue(workType.getSeq());
		getInpWorkTypeName().setText(workType.getWorkTypeName());
		getInpWorkTypeName().setText(workType_.getWorkTypeName());
		getInpMemo().setText(workType_.getMemo());
	}	
	public DlgWorkTypeDetail(JDialog owner) {
		super(owner);
		initialize();
	}
	public void setup(){
		super.setup();
		workTypeDao_ = (WorkTypeDao) daoCont_.getComponent(WorkTypeDao.class);
		getCmdCommit().setAction(new ActNewOrUpdateWorkType());
	}
	public void newWorkType(){
		workType_ = new WorkType();
		setWorkType(workType_);
		isUpdate_ = false;
	}
	public void loadWorkType(long id){
		workType_ = workTypeDao_.findByWorkTypeId(id); 
		if(workType_ == null){
			throw new AppException("作業種類ID="+id+" は、既に削除されています");
		}
		setWorkType(workType_);
		isUpdate_ = true;
	}
	public void deleteWorkType(long id){
//		workTypeDao_.deleteByWorkTypeId(id); 
		actDeleteWorkType_.actionPerformed(new ActionEvent(this, 0, ""+id));
	}
	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setSize(new Dimension(479, 176));
        this.setContentPane(getJPanel());
        this.setTitle("work type");
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
	 * This method initializes inpWorkTypeId	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getInpWorkTypeId() {
		if (inpWorkTypeId == null) {
			inpWorkTypeId = new JTextField();
			inpWorkTypeId.setBorder(new TitledBorder("id"));
			inpWorkTypeId.setEditable(false);
		}
		return inpWorkTypeId;
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
	 * This method initializes inpWorkTypeName	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getInpWorkTypeName() {
		if (inpWorkTypeName == null) {
			inpWorkTypeName = new JTextField();
			inpWorkTypeName.setBorder(new TitledBorder("name"));
		}
		return inpWorkTypeName;
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
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.fill = GridBagConstraints.BOTH;
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.gridy = 1;
			gridBagConstraints3.weightx = 1.0;
			gridBagConstraints3.weighty = 10.0;
			gridBagConstraints3.gridwidth = 3;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints2.gridy = -1;
			gridBagConstraints2.weightx = 8.0;
			gridBagConstraints2.gridx = -1;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints1.gridy = -1;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.gridx = -1;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.gridy = -1;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.gridx = -1;
			jPanel1 = new JPanel();
			jPanel1.setLayout(new GridBagLayout());
			jPanel1.add(getInpWorkTypeId(), gridBagConstraints);
			jPanel1.add(getInpSEQ(), gridBagConstraints1);
			jPanel1.add(getInpWorkTypeName(), gridBagConstraints2);
			jPanel1.add(getInpMemo(), gridBagConstraints3);
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

}  //  @jve:decl-index=0:visual-constraint="10,10"
