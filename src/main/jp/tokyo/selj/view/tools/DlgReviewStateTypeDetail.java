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

import jp.tokyo.selj.common.AppException;
import jp.tokyo.selj.common.NumericTextFormatter;
import jp.tokyo.selj.dao.ReviewStateType;
import jp.tokyo.selj.dao.ReviewStateTypeDao;
import jp.tokyo.selj.dao.SelJDaoContainer;
import jp.tokyo.selj.model.MasterComboModel;
import jp.tokyo.selj.view.ActTransactionBase;

import org.apache.log4j.Logger;
import org.seasar.framework.container.S2Container;

public class DlgReviewStateTypeDetail extends DlgTableMaintUpdateBase {
	Logger log = Logger.getLogger(this.getClass());

	private JPanel jPanel = null;
	private JFormattedTextField inpId = null;
	private JPanel jPanel1 = null;
	private JPanel jPanel2 = null;
	private JButton cmdCommit = null;
	
	S2Container daoCont_ = SelJDaoContainer.SEL_DAO_CONT;  //  @jve:decl-index=0:
	boolean isUpdate_ = false;
	ReviewStateTypeDao dao_ = null;  //  @jve:decl-index=0:
	ReviewStateType reviewStateType_ = null;  //  @jve:decl-index=0:
	//new, update兼用
	private class ActNewOrUpdate extends ActTransactionBase {
		protected Component getOwnerComponent(){
			return DlgReviewStateTypeDetail.this;
		}
		public ActNewOrUpdate(){
			super();
			putValue(Action.NAME, "commit");
			putValue(Action.SHORT_DESCRIPTION, "新規登録または更新");
		}
		public void actionPerformed2(ActionEvent e) {
			reviewStateType_.setReviewStateTypeId(((Number)getInpId().getValue()).intValue());
			reviewStateType_.setReviewStateTypeName( getInpName().getText() );
			reviewStateType_.check();
			if(isUpdate_){
				//更新
				dao_.update(reviewStateType_);
			}else{
				//新規追加
				dao_.insert(reviewStateType_);
			}
			getOwnerComponent().setVisible(false);
			//一旦トランザクションを終了しないとmodel_.executeQuery()がタイムアウトになる
			postProc();
			preProc();
			//JTableをリフレッシュする
			((DlgReviewStateTypeMaint)getOwner()).loadReviewStateTypeTable();

			//MasterComboModelを更新する
			MasterComboModel.refreshrRvieStateType();
		}
	}
	//delete用
	ActDelete actDelete_ = new ActDelete();  //  @jve:decl-index=0:

	private JTextField inpName = null;

	private class ActDelete extends ActTransactionBase {
		protected Component getOwnerComponent(){
			return DlgReviewStateTypeDetail.this;
		}
		public ActDelete(){
			super();
			putValue(Action.NAME, "delete");
			putValue(Action.SHORT_DESCRIPTION, "削除します");
		}
		public void actionPerformed2(ActionEvent e) {
			String id = e.getActionCommand();
			//削除
			dao_.deleteById(Integer.parseInt(id)); 
			//一旦トランザクションを終了しないとmodel_.executeQuery()がタイムアウトになる
			postProc();
			preProc();
			//JTableをリフレッシュする
			((DlgReviewStateTypeMaint)getOwner()).loadReviewStateTypeTable();

			//MasterComboModelを更新する
			MasterComboModel.refreshrRvieStateType();
			
			getOwnerComponent().setVisible(false);
		}
	}

	void setReviewStateType(ReviewStateType reviewStateType) {
		getInpId().setValue(reviewStateType.getReviewStateTypeId());
		getInpName().setText(reviewStateType.getReviewStateTypeName());
	}	
	public DlgReviewStateTypeDetail(JDialog owner) {
		super(owner);
		initialize();
	}
	public void setup(){
		super.setup();
		dao_ = (ReviewStateTypeDao) daoCont_.getComponent(ReviewStateTypeDao.class);
		getCmdCommit().setAction(new ActNewOrUpdate());
	}
	public void newReviewStateType(){
		reviewStateType_ = new ReviewStateType();
		setReviewStateType(reviewStateType_);
		isUpdate_ = false;
		getInpId().setEditable(!isUpdate_);
	}
	public void loadReviewStateType(int id){
		reviewStateType_ = dao_.findById(id); 
		if(reviewStateType_ == null){
			throw new AppException("id="+id+" は、既に削除されています");
		}
		setReviewStateType(reviewStateType_);
		isUpdate_ = true;
		getInpId().setEditable(!isUpdate_);
	}
	public void deleteReviewStateType(int id){
		actDelete_.actionPerformed(new ActionEvent(this, 0, ""+id));
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setSize(new Dimension(459, 121));
        this.setContentPane(getJPanel());
        this.setTitle("review state type");
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
	 * This method initializes inpId	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JFormattedTextField getInpId() {
		if (inpId == null) {
			inpId = new JFormattedTextField(new NumericTextFormatter());
			inpId.setBorder(new TitledBorder("id"));
			inpId.setEditable(false);
		}
		return inpId;
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints21.gridy = 0;
			gridBagConstraints21.weightx = 1.0;
			gridBagConstraints21.gridx = 1;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints2.gridy = 0;
			gridBagConstraints2.weightx = 0.0;
			gridBagConstraints2.ipadx = 60;
			gridBagConstraints2.gridx = 0;
			jPanel1 = new JPanel();
			jPanel1.setLayout(new GridBagLayout());
			jPanel1.add(getInpId(), gridBagConstraints2);
			jPanel1.add(getInpName(), gridBagConstraints21);
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
	 * This method initializes inpName	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getInpName() {
		if (inpName == null) {
			inpName = new JTextField();
			inpName.setBorder(new TitledBorder("name"));
		}
		return inpName;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
