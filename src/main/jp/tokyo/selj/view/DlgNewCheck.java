package jp.tokyo.selj.view;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import jp.tokyo.selj.common.AppException;
import jp.tokyo.selj.common.SysPreference;
import jp.tokyo.selj.dao.Check;
import jp.tokyo.selj.dao.CheckDao;
import jp.tokyo.selj.dao.CheckDetail;
import jp.tokyo.selj.dao.CheckDetailDao;
import jp.tokyo.selj.dao.CheckPoint;
import jp.tokyo.selj.dao.CheckPointDao;
import jp.tokyo.selj.dao.Output;
import jp.tokyo.selj.dao.SelJDaoContainer;
import jp.tokyo.selj.model.MasterComboModel;

import org.seasar.framework.container.S2Container;

public class DlgNewCheck extends JDialog {

	private static final long serialVersionUID = 1L;
	S2Container daoCont_ = SelJDaoContainer.SEL_DAO_CONT;

	private JPanel jContentPane = null;

	private JComboBox inpUserName = null;

	private JButton cmdNewCheck = null;
	
	Output output_;
	Check check_;  //  @jve:decl-index=0:
	CheckPointDao checkPointDao_= null;  //  @jve:decl-index=0:
	CheckDao checkDao_= null;
	CheckDetailDao checkDetailDao_= null;
	DlgCheckList dlgCheckList_  = null;
	private JTextField inpRemark = null;
	
	class ActCreateOrUpdateCheckList extends ActTransactionBase {
		protected Component getOwnerComponent(){
			return DlgNewCheck.this;
		}
		public ActCreateOrUpdateCheckList(){
			super();
			putValue(Action.NAME, "commit");
			putValue(Action.SHORT_DESCRIPTION, "新規のチェックリストを作成する");
		}
		public void actionPerformed2(ActionEvent e) {
			if(check_ == null){
				create(e);
			}else{
				update(e);
			}
			dlgCheckList_.refreshCheckList();
			setVisible(false);
		}
		void update(ActionEvent e){
			check_.setUserName(""+getInpUserName().getSelectedItem());
			check_.setRemark(getInpRemark().getText());
			checkDao_.update(check_);
		}
		void create(ActionEvent e) {
			//checkPointを読み込む
			List<CheckPoint> checkPoints = checkPointDao_.findByOutputTypeId(output_.getOutputTypeId());
			if(checkPoints.size() <= 0){
				throw new AppException("この成果物種類にチェック項目が登録されていません");
			}

			Check check = new Check();
			check.setOutputId(output_.getOutputId());
			check.setUserName(
					(getInpUserName().getSelectedItem()==null)? null:
						""+getInpUserName().getModel().getSelectedItem()
					);
			check.setRemark(getInpRemark().getText());
			checkDao_.insert(check);

			for(int i=0; i<checkPoints.size(); i++){
				CheckDetail cd = new CheckDetail(check);
				cd.setCheckPoint(checkPoints.get(i));
				
				checkDetailDao_.insert(cd);
			}
		}
	}

	/**
	 * @param owner
	 */
	public DlgNewCheck(Dialog owner) {
		super(owner, true);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(521, 171);
		this.setResizable(false);
		this.setTitle("new/update check");
		this.setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints2.gridy = 1;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.gridx = 0;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints1.gridy = 2;
			gridBagConstraints1.ipadx = 34;
			gridBagConstraints1.ipady = 2;
			gridBagConstraints1.gridx = 0;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.NONE;
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.ipady = 27;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.ipadx = 40;
			gridBagConstraints.insets = new Insets(0, 0, 0, 0);
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.add(getInpUserName(), gridBagConstraints);
			jContentPane.add(getCmdNewCheck(), gridBagConstraints1);
			jContentPane.add(getInpRemark(), gridBagConstraints2);
		}
		return jContentPane;
	}

	/**
	 * This method initializes inpUserName	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getInpUserName() {
		if (inpUserName == null) {
			inpUserName = new JComboBox();
			inpUserName.setPreferredSize(new Dimension(200, 25));
			inpUserName.setEditable(true);
			inpUserName.setBorder(new TitledBorder("checker"));
		}
		return inpUserName;
	}
	public void setup(DlgCheckList dlgCheckList){
		dlgCheckList_ = dlgCheckList;
		getInpUserName().setModel(MasterComboModel.newUserComboBoxModel());
		inpUserName.setSelectedItem(SysPreference.getDefaultUserName());
		
		checkPointDao_ = (CheckPointDao)daoCont_.getComponent(CheckPointDao.class);
		checkDao_ = (CheckDao)daoCont_.getComponent(CheckDao.class);
		checkDetailDao_ = (CheckDetailDao)daoCont_.getComponent(CheckDetailDao.class);
		getCmdNewCheck().setAction(new ActCreateOrUpdateCheckList());
	}
	@Override
	public void setVisible(boolean b) {
		if(b){
			throw new RuntimeException("setVisible(boolean b, Output output)をつかわなあかん");
		}else{
			super.setVisible(b);
		}
	}
	public void setVisible(boolean b, Output output, Check check) {
		output_ = output;
		check_ = check;
		if(check_ != null){
			getInpUserName().setSelectedItem(check_.getUserName());
			getInpRemark().setText(check_.getRemark());
		}
		super.setVisible(b);
	}

	
	/**
	 * This method initializes cmdNewCheck	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdNewCheck() {
		if (cmdNewCheck == null) {
			cmdNewCheck = new JButton();
			cmdNewCheck.setText("commit");
		}
		return cmdNewCheck;
	}

	/**
	 * This method initializes inpRemark	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getInpRemark() {
		if (inpRemark == null) {
			inpRemark = new JTextField();
			inpRemark.setBorder(new TitledBorder("remark"));
		}
		return inpRemark;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
