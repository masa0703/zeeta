package jp.tokyo.selj.view.tools;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;

import jp.tokyo.selj.common.AppException;
import jp.tokyo.selj.model.DBTableModel;
import jp.tokyo.selj.view.ActBase;

import org.apache.log4j.Logger;


public class DlgUserMaint extends JDialog {
	Logger log = Logger.getLogger(this.getClass()); 

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private PnlTableMaint cntUsertbl = null;

	UsertblModel userModel_ = null;

	ActionMap actionMap_ = new ActionMap();
	//Actionの親クラス
	private abstract class ActBase2 extends ActBase {
		ActBase2(ActionMap map){
			super(map);
		}
		protected Component getOwnerComponent(){
			return DlgUserMaint.this;
		}
	}
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	// usertbl
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	class ActUsertblShowNewDlg extends ActBase2 {
		DlgUsertblDetail dialog_ = null;
		public ActUsertblShowNewDlg(ActionMap map){
			super(map);
			putValue(Action.NAME, "new");
			putValue(Action.SHORT_DESCRIPTION, "ユーザを追加します(Ins)");
		}
		public void actionPerformed2(ActionEvent e) {
			getDlg().newUsertbl();
			getDlg().setVisible(true);
		}
		protected DlgUsertblDetail getDlg(){
			if(dialog_ == null){
				dialog_ = new  DlgUsertblDetail(DlgUserMaint.this);
				dialog_.setup();
				dialog_.setLocationRelativeTo(getOwnerComponent());
			}
			return dialog_;
		}
	}
	class ActUsertblShowUpdateDlg extends ActUsertblShowNewDlg {
		public ActUsertblShowUpdateDlg(ActionMap map){
			super(map);
			putValue(Action.NAME, "update");
			putValue(Action.SHORT_DESCRIPTION, "ユーザを更新します");
		}
		public void actionPerformed2(ActionEvent e) {
			JTable table = getCntUsertbl().getJTable();
			if(table.getSelectedRow() < 0){
				throw new AppException("ユーザを選択してください"); 
			}
			int nameIndex = userModel_.getColumnIndex("userName");
			String name = (String)table.getValueAt(table.getSelectedRow(), nameIndex);
			getDlg().loadUsertbl(name);
			getDlg().setVisible(true);
		}
	}
	class ActUsertblRemove extends ActUsertblShowNewDlg {
		public ActUsertblRemove(ActionMap map){
			super(map);
			putValue(Action.NAME, "remove");
			putValue(Action.SHORT_DESCRIPTION, "ユーザを削除します(Del)");
		}
		public void actionPerformed2(ActionEvent e) {
			//ユーザの削除を行う
			JTable table = getCntUsertbl().getJTable();
			if(table.getSelectedRow() < 0){
				throw new AppException("work typeを選択してください"); 
			}
			int nameIndex = userModel_.getColumnIndex("userName");
			String name = (String)table.getValueAt(
								table.getSelectedRow(), nameIndex);

			if( JOptionPane.showConfirmDialog(
					DlgUserMaint.this
					,"選択中のユーザを削除します。\n" +
					"よろしいですか？",""
					,JOptionPane.YES_NO_OPTION
					,JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
						
				getDlg().deleteUsertbl(name);
			}
//			getDlg().setVisible(true);
		}
	}

	//ユーザテーブルモデル
	class UsertblModel extends DBTableModel{
		@Override
		public String getTableName() {
			return "usertbl";
		}
		
		@Override
		public String getQuerySql() {
			return "SELECT " +
					"userName" +
					",org" +
					" FROM usertbl" +
					" ORDER BY userName";
		}
	}
	
	public void setup(){
		userModel_ = new UsertblModel();
		getCntUsertbl().getJTable().setModel(userModel_);
		
		//workTypeのActionセット
		getCntUsertbl().getActionMap().put(PnlTableMaint.ACTION_KEY_NEW, 
				new ActUsertblShowNewDlg(actionMap_));
		getCntUsertbl().getActionMap().put(PnlTableMaint.ACTION_KEY_UPDATE, 
				new ActUsertblShowUpdateDlg(actionMap_));
		getCntUsertbl().getActionMap().put(PnlTableMaint.ACTION_KEY_DELETE, 
				new ActUsertblRemove(actionMap_));
		getCntUsertbl().setup();
		
	}
	
	/**
	 * @param owner
	 */
	public DlgUserMaint(JDialog owner) {
		super(owner, true);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(488, 267);
		this.setContentPane(getJContentPane());
		this.setTitle("usertbl maintenance.");
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			GridLayout gridLayout = new GridLayout();
			gridLayout.setRows(1);
			jContentPane = new JPanel();
			jContentPane.setLayout(gridLayout);
			jContentPane.add(getCntUsertbl(), null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes cntUsertbl	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private PnlTableMaint getCntUsertbl() {
		if (cntUsertbl == null) {
			cntUsertbl = new PnlTableMaint();
			cntUsertbl.setTitle("user");
		}
		return cntUsertbl;
	}

	public void loadUsertblTable() {
		JTable table = getCntUsertbl().getJTable();
		int selectedIndex = table.getSelectedRow();
		
		userModel_.executeQuery();
			
		//選択状態を復元
		if((selectedIndex >= 0) && ( userModel_.getRowCount() > selectedIndex)){
			table.getSelectionModel().addSelectionInterval(selectedIndex, selectedIndex);
		}
	}


}  //  @jve:decl-index=0:visual-constraint="10,10"
