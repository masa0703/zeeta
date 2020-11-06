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
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import jp.tokyo.selj.common.AppException;
import jp.tokyo.selj.dao.SelJDaoContainer;
import jp.tokyo.selj.dao.User;
import jp.tokyo.selj.dao.UserDao;
import jp.tokyo.selj.model.MasterComboModel;
import jp.tokyo.selj.view.ActTransactionBase;

import org.apache.log4j.Logger;
import org.seasar.framework.container.S2Container;

public class DlgUsertblDetail extends DlgTableMaintUpdateBase {
	Logger log = Logger.getLogger(this.getClass());

	private JPanel jPanel = null;
	private JTextField inpUsertblName = null;
	private JPanel jPanel1 = null;
	private JPanel jPanel2 = null;
	private JButton cmdCommit = null;
	
	S2Container daoCont_ = SelJDaoContainer.SEL_DAO_CONT;  //  @jve:decl-index=0:
	boolean isUpdate_ = false;
	UserDao userDao_ = null;  //  @jve:decl-index=0:
	User user_ = null;  //  @jve:decl-index=0:
	//new, update兼用
	private class ActNewOrUpdate extends ActTransactionBase {
		protected Component getOwnerComponent(){
			return DlgUsertblDetail.this;
		}
		public ActNewOrUpdate(){
			super();
			putValue(Action.NAME, "commit");
			putValue(Action.SHORT_DESCRIPTION, "新規登録または更新");
		}
		public void actionPerformed2(ActionEvent e) {
			if(isUpdate_ &&
					!user_.getUserName().equals(getInpUsertblName().getText())){
				//PKが変更されていた場合
				userDao_.remove(user_);		//一旦削除
				isUpdate_ = false;
			}
			user_.setUserName( getInpUsertblName().getText() );
			user_.setOrg( getInpOrg().getText() );
			user_.check();
			if(isUpdate_){
				//更新
				userDao_.update(user_);
			}else{
				//新規追加
				userDao_.insert(user_);
			}
			getOwnerComponent().setVisible(false);
			//一旦トランザクションを終了しないとmodel_.executeQuery()がタイムアウトになる
			postProc();
			preProc();
			//JTableをリフレッシュする
			((DlgUserMaint)getOwner()).loadUsertblTable();

			//MasterComboModelを更新する
			MasterComboModel.refreshUser();
		}
	}
	//delete用
	ActDelete actDelete_ = new ActDelete();  //  @jve:decl-index=0:

	private JTextField inpOrg = null;

	private class ActDelete extends ActTransactionBase {
		protected Component getOwnerComponent(){
			return DlgUsertblDetail.this;
		}
		public ActDelete(){
			super();
			putValue(Action.NAME, "delete");
			putValue(Action.SHORT_DESCRIPTION, "削除します");
		}
		public void actionPerformed2(ActionEvent e) {
			String name = e.getActionCommand();
			//削除
			userDao_.deleteByName(name); 
			//一旦トランザクションを終了しないとmodel_.executeQuery()がタイムアウトになる
			postProc();
			preProc();
			//JTableをリフレッシュする
			((DlgUserMaint)getOwner()).loadUsertblTable();

			//MasterComboModelを更新する
			MasterComboModel.refreshUser();
			
			getOwnerComponent().setVisible(false);
		}
	}

	void setUsertbl(User user) {
		getInpUsertblName().setText(user.getUserName());
		getInpOrg().setText(user.getOrg());
	}	
	public DlgUsertblDetail(JDialog owner) {
		super(owner);
		initialize();
	}
	public void setup(){
		super.setup();
		userDao_ = (UserDao) daoCont_.getComponent(UserDao.class);
		getCmdCommit().setAction(new ActNewOrUpdate());
	}
	public void newUsertbl(){
		user_ = new User();
		setUsertbl(user_);
		isUpdate_ = false;
	}
	public void loadUsertbl(String name){
		user_ = userDao_.findByName(name); 
		if(user_ == null){
			throw new AppException("ユーザ="+name+" は、既に削除されています");
		}
		setUsertbl(user_);
		isUpdate_ = true;
	}
	public void deleteUsertbl(String name){
		actDelete_.actionPerformed(new ActionEvent(this, 0, name));
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setSize(new Dimension(556, 121));
        this.setContentPane(getJPanel());
        this.setTitle("user");
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
	 * This method initializes inpUsertblName	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getInpUsertblName() {
		if (inpUsertblName == null) {
			inpUsertblName = new JTextField();
			inpUsertblName.setBorder(new TitledBorder("name"));
		}
		return inpUsertblName;
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
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.gridx = 0;
			jPanel1 = new JPanel();
			jPanel1.setLayout(new GridBagLayout());
			jPanel1.add(getInpUsertblName(), gridBagConstraints2);
			jPanel1.add(getInpOrg(), gridBagConstraints21);
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
	private JTextField getInpOrg() {
		if (inpOrg == null) {
			inpOrg = new JTextField();
			inpOrg.setBorder(new TitledBorder("org"));
		}
		return inpOrg;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
