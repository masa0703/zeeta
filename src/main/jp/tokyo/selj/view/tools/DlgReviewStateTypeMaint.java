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


public class DlgReviewStateTypeMaint extends JDialog {
	Logger log = Logger.getLogger(this.getClass()); 

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private PnlTableMaint cntTable = null;

	ReviewModel tableModel_ = null;

	ActionMap actionMap_ = new ActionMap();
	//Actionの親クラス
	private abstract class ActBase2 extends ActBase {
		ActBase2(ActionMap map){
			super(map);
		}
		protected Component getOwnerComponent(){
			return DlgReviewStateTypeMaint.this;
		}
	}
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	class ActReviewStateTypeShowNewDlg extends ActBase2 {
		DlgReviewStateTypeDetail dialog_ = null;
		public ActReviewStateTypeShowNewDlg(ActionMap map){
			super(map);
			putValue(Action.NAME, "new");
			putValue(Action.SHORT_DESCRIPTION, "レビュー状態種類を追加します(Ins)");
		}
		public void actionPerformed2(ActionEvent e) {
			getDlg().newReviewStateType();
			getDlg().setVisible(true);
		}
		protected DlgReviewStateTypeDetail getDlg(){
			if(dialog_ == null){
				dialog_ = new  DlgReviewStateTypeDetail(DlgReviewStateTypeMaint.this);
				dialog_.setup();
				dialog_.setLocationRelativeTo(getOwnerComponent());
			}
			return dialog_;
		}
	}
	class ActReviewStateTypeShowUpdateDlg extends ActReviewStateTypeShowNewDlg {
		public ActReviewStateTypeShowUpdateDlg(ActionMap map){
			super(map);
			putValue(Action.NAME, "update");
			putValue(Action.SHORT_DESCRIPTION, "レビュー状態種類を更新します");
		}
		public void actionPerformed2(ActionEvent e) {
			JTable table = getCntUsertbl().getJTable();
			if(table.getSelectedRow() < 0){
				throw new AppException("レビュー状態を選択してください"); 
			}
			int nameIndex = tableModel_.getColumnIndex("reviewStateTypeId");
			Number id = (Number)table.getValueAt(table.getSelectedRow(), nameIndex);
			getDlg().loadReviewStateType(id.intValue());
			getDlg().setVisible(true);
		}
	}
	class ActReviewStateTypeRemove extends ActReviewStateTypeShowNewDlg {
		public ActReviewStateTypeRemove(ActionMap map){
			super(map);
			putValue(Action.NAME, "remove");
			putValue(Action.SHORT_DESCRIPTION, "レビュー状態種類を削除します(Del)");
		}
		public void actionPerformed2(ActionEvent e) {
			//ユーザの削除を行う
			JTable table = getCntUsertbl().getJTable();
			if(table.getSelectedRow() < 0){
				throw new AppException("レビュー状態種類を選択してください"); 
			}
			int nameIndex = tableModel_.getColumnIndex("reviewStateTypeId");
			Number id = (Number)table.getValueAt(table.getSelectedRow(), nameIndex);

			if( JOptionPane.showConfirmDialog(
					DlgReviewStateTypeMaint.this
					,"選択中のレビュー状態種類を削除します。\n" +
					"よろしいですか？",""
					,JOptionPane.YES_NO_OPTION
					,JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
						
				getDlg().deleteReviewStateType(id.intValue());
			}
//			getDlg().setVisible(true);
		}
	}

	//テーブルモデル
	class ReviewModel extends DBTableModel{
		@Override
		public String getTableName() {
			return "usertbl";
		}
		
		@Override
		public String getQuerySql() {
			return "SELECT " +
					"reviewStateTypeId" +
					",reviewStateTypeName" +
					" FROM reviewStateType" +
					" ORDER BY reviewStateTypeId";
		}
	}
	
	public void setup(){
		tableModel_ = new ReviewModel();
		getCntUsertbl().getJTable().setModel(tableModel_);
		
		//Actionセット
		getCntUsertbl().getActionMap().put(PnlTableMaint.ACTION_KEY_NEW, 
				new ActReviewStateTypeShowNewDlg(actionMap_));
		getCntUsertbl().getActionMap().put(PnlTableMaint.ACTION_KEY_UPDATE, 
				new ActReviewStateTypeShowUpdateDlg(actionMap_));
		getCntUsertbl().getActionMap().put(PnlTableMaint.ACTION_KEY_DELETE, 
				new ActReviewStateTypeRemove(actionMap_));
		getCntUsertbl().setup();
		
	}
	
	/**
	 * @param owner
	 */
	public DlgReviewStateTypeMaint(JDialog owner) {
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
		this.setTitle("review state type maintenance.");
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
		if (cntTable == null) {
			cntTable = new PnlTableMaint();
			cntTable.setTitle("review state type");
		}
		return cntTable;
	}

	public void loadReviewStateTypeTable() {
		JTable table = getCntUsertbl().getJTable();
		int selectedIndex = table.getSelectedRow();
		
		tableModel_.executeQuery();
			
		//選択状態を復元
		if((selectedIndex >= 0) && ( tableModel_.getRowCount() > selectedIndex)){
			table.getSelectionModel().addSelectionInterval(selectedIndex, selectedIndex);
		}
	}


}  //  @jve:decl-index=0:visual-constraint="10,10"
