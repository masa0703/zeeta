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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import jp.tokyo.selj.common.AppException;
import jp.tokyo.selj.dao.OutputPropTypeDao;
import jp.tokyo.selj.dao.SelJDaoContainer;
import jp.tokyo.selj.model.DBTableModel;
import jp.tokyo.selj.view.ActBase;

import org.apache.log4j.Logger;
import org.seasar.framework.container.S2Container;


public class DlgWorkTypeOutputType extends JDialog {
	Logger log = Logger.getLogger(this.getClass()); 

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private PnlTableMaint cntWorkType = null;

	private PnlTableMaint cntOutputType = null;

	WorkTypeModel workTypeModel_ = null;
	OutputTypeModel outputTypeModel_ = null;

	S2Container daoCont_ = SelJDaoContainer.SEL_DAO_CONT;  //  @jve:decl-index=0:
	OutputPropTypeDao workPropTypeDao_ = null;

	ActionMap actionMap_ = new ActionMap();

	//Actionの親クラス
	private abstract class ActBase2 extends ActBase {
		ActBase2(ActionMap map){
			super(map);
		}
		protected Component getOwnerComponent(){
			return DlgWorkTypeOutputType.this;
		}
	}
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	// workType
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	class ActWorkTypeShowNewDlg extends ActBase2 {
		DlgWorkTypeDetail dialog_ = null;
		public ActWorkTypeShowNewDlg(ActionMap map){
			super(map);
			putValue(Action.NAME, "new");
			putValue(Action.SHORT_DESCRIPTION, "作業種類を追加します(Ins)");
		}
		public void actionPerformed2(ActionEvent e) {
			getDlg().newWorkType();
			getDlg().setVisible(true);
		}
		protected DlgWorkTypeDetail getDlg(){
			if(dialog_ == null){
				dialog_ = new  DlgWorkTypeDetail(DlgWorkTypeOutputType.this);
				dialog_.setup();
				dialog_.setLocationRelativeTo(getOwnerComponent());
			}
			return dialog_;
		}
	}
	class ActWorkTypeShowUpdateDlg extends ActWorkTypeShowNewDlg {
		public ActWorkTypeShowUpdateDlg(ActionMap map){
			super(map);
			putValue(Action.NAME, "update");
			putValue(Action.SHORT_DESCRIPTION, "作業種類を更新します");
		}
		public void actionPerformed2(ActionEvent e) {
			JTable table = getCntWorkType().getJTable();
			if(table.getSelectedRow() < 0){
				throw new AppException("work typeを選択してください"); 
			}
			int idIndex = workTypeModel_.getColumnIndex("id");
			int id = ((Number)table.getValueAt(
								table.getSelectedRow(), idIndex)).intValue();
			getDlg().loadWorkType(id);
			getDlg().setVisible(true);
		}
	}
	class ActWorkTypeRemove extends ActWorkTypeShowNewDlg {
		public ActWorkTypeRemove(ActionMap map){
			super(map);
			putValue(Action.NAME, "remove");
			putValue(Action.SHORT_DESCRIPTION, "作業種類を削除します(Del)");
		}
		public void actionPerformed2(ActionEvent e) {
			//作業種類の削除を行う
			JTable table = getCntWorkType().getJTable();
			if(table.getSelectedRow() < 0){
				throw new AppException("work typeを選択してください"); 
			}
			int idIndex = workTypeModel_.getColumnIndex("id");
			int id = ((Number)table.getValueAt(
								table.getSelectedRow(), idIndex)).intValue();

			if( JOptionPane.showConfirmDialog(
					DlgWorkTypeOutputType.this
					,"選択中の作業種類を削除します。\n" +
					"よろしいですか？",""
					,JOptionPane.YES_NO_OPTION
					,JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
						
				getDlg().deleteWorkType(id);
			}
//			getDlg().setVisible(true);
		}
	}

	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	// outputType
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	class ActOutputTypeShowNewDlg extends ActBase2 {
		DlgOutputTypeDetail dialog_ = null;
		public ActOutputTypeShowNewDlg(ActionMap map){
			super(map);
			putValue(Action.NAME, "new");
			putValue(Action.SHORT_DESCRIPTION, "成果物種類を追加します(Ins)");
		}
		public void actionPerformed2(ActionEvent e) {
			//選択中の作業タイプを取得
			JTable workTypeTable = getCntWorkType().getJTable();
			if(workTypeTable.getSelectedRow() < 0){
				throw new AppException("work typeを選択してください"); 
			}
			int idIndex = workTypeModel_.getColumnIndex("id");
			int workTypeId = ((Number)workTypeTable.getValueAt(
								workTypeTable.getSelectedRow(), idIndex)).intValue();

			//成果物種類の新規登録画面表示
			getDlg().newOutputType(workTypeId);
			getDlg().setVisible(true);
		}
		protected DlgOutputTypeDetail getDlg(){
			if(dialog_ == null){
				dialog_ = new  DlgOutputTypeDetail(DlgWorkTypeOutputType.this);
				dialog_.setup();
				dialog_.setLocationRelativeTo(getOwnerComponent());
			}
			return dialog_;
		}
	}
	class ActOutputTypeUpdate extends ActOutputTypeShowNewDlg {
		public ActOutputTypeUpdate(ActionMap map){
			super(map);
			putValue(Action.NAME, "update");
			putValue(Action.SHORT_DESCRIPTION, "成果物種類を更新します");
		}
		public void actionPerformed2(ActionEvent e) {
			JTable table = getCntOutputType().getJTable();
			if(table.getSelectedRow() < 0){
				throw new AppException("output typeを選択してください"); 
			}
			int idIndex = outputTypeModel_.getColumnIndex("id");
			int id = ((Number)table.getValueAt(
								table.getSelectedRow(), idIndex)).intValue();
			getDlg().loadOutputType(id);
			getDlg().setVisible(true);
		}
	}
	class ActOutputTypeDelete extends ActOutputTypeShowNewDlg {
		public ActOutputTypeDelete(ActionMap map){
			super(map);
			putValue(Action.NAME, "remove");
			putValue(Action.SHORT_DESCRIPTION, "成果物種類を削除します(Del)");
		}
		public void actionPerformed2(ActionEvent e) {
			//成果物種類の削除を行う
			JTable table = getCntOutputType().getJTable();
			if(table.getSelectedRow() < 0){
				throw new AppException("work typeを選択してください"); 
			}
			int idIndex = outputTypeModel_.getColumnIndex("id");
			int id = ((Number)table.getValueAt(
								table.getSelectedRow(), idIndex)).intValue();

			if( JOptionPane.showConfirmDialog(
					DlgWorkTypeOutputType.this
					,"選択中の成果物種類を削除します。\n" +
					"よろしいですか？",""
					,JOptionPane.YES_NO_OPTION
					,JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
						
				getDlg().deleteOutputType(id);
			}
//			getDlg().setVisible(true);
			
		}
	}


	//作業種類テーブルモデル
	class WorkTypeModel extends DBTableModel{
		@Override
		public String getTableName() {
			return "worktype";
		}
		
		@Override
		public String getQuerySql() {
			return "SELECT " +
					"workTypeId as id" +
					",SEQ" +
					",workTypeName" +
					",memo" +
					",newDate" +
					",versionNo" +
					" FROM worktype" +
					" ORDER BY SEQ";
		}

//		@Override
//		public boolean isCellEditable(int row,int column){
//			if( !super.isCellEditable(row,column)){
//				return false;
//			}
//			if(column == getColumnIndex("workTypeId") 
//			|| column == getColumnIndex("newDate")
//			){
//				return false;
//			}else{
//				return true;
//			}
//		}
	}
	//成果物種類テーブルモデル
	class OutputTypeModel extends DBTableModel{
		int selectedWorkTypeId_ = -1;
		int curWorkTypeId_ = -1;

		@Override
		public String getTableName() {
			return "outputtype";
		}
		
		@Override
		public String getQuerySql() {
			return "SELECT " +
					"outputTypeId as id" +
					",SEQ" +
					",outputTypeName" +
					",workTypeId" +
					",memo" +
					",newDate" +
					",versionNo" +
					" FROM outputtype" +
					" WHERE workTypeId="+selectedWorkTypeId_ +
					" ORDER BY SEQ"
					;
		}

		//		@Override
//		public boolean isCellEditable(int row,int column){
//			if( !super.isCellEditable(row,column)){
//				return false;
//			}
//			if(column == getColumnIndex("outputId") //workTypeIdは編集可にしておく
//			|| column == getColumnIndex("newDate")
//			){
//				return false;
//			}else{
//				return true;
//			}
//		}

		public void setWorkTypeId(int id) {
			if(curWorkTypeId_ == id){
				return;
			}
			selectedWorkTypeId_ = id;
			executeQuery();
		}
	}
	class Synchronizer implements ListSelectionListener{
		public void valueChanged(ListSelectionEvent e) {
			log.debug("valueChanged() e.getFirstIndex()="+e.getFirstIndex());
			JTable workTypeTable = getCntWorkType().getJTable();
			if(workTypeTable.getSelectedRow() < 0){
				return;
			}
			
			int id = ((Number)workTypeTable.getValueAt(
								workTypeTable.getSelectedRow(), 0)).intValue();
			//outputType
			outputTypeModel_.setWorkTypeId(id);
			setOutputTableColumnWidth();
			
			//workPropType
			
		}
	}
	void setOutputTableColumnWidth(){
		//outputTypesの列幅を決定
		JTable outputTypeTable = getCntOutputType().getJTable();
		int verIndex = outputTypeModel_.getVersionNoColumnIndex();
		if(verIndex >= 0){
			TableColumn col = outputTypeTable.getColumnModel().getColumn(verIndex);
			col.setMinWidth(0);
			col.setMaxWidth(0);
		}
		outputTypeTable.getColumnModel().getColumn(
				outputTypeModel_.getColumnIndex("id")).setMaxWidth(40);	//id
		outputTypeTable.getColumnModel().getColumn(
				outputTypeModel_.getColumnIndex("seq")).setMaxWidth(60);	//seq
		
	}
	
	public void setup(){
		workTypeModel_ = new WorkTypeModel();
		outputTypeModel_ = new OutputTypeModel();
		getCntWorkType().getJTable().setModel(workTypeModel_);
		getCntOutputType().getJTable().setModel(outputTypeModel_);
		workPropTypeDao_ = (OutputPropTypeDao)daoCont_.getComponent(OutputPropTypeDao.class);
		
		//workTypeのActionセット
		getCntWorkType().getActionMap().put(PnlTableMaint.ACTION_KEY_NEW, 
				new ActWorkTypeShowNewDlg(actionMap_));
		getCntWorkType().getActionMap().put(PnlTableMaint.ACTION_KEY_UPDATE, 
				new ActWorkTypeShowUpdateDlg(actionMap_));
		getCntWorkType().getActionMap().put(PnlTableMaint.ACTION_KEY_DELETE, 
				new ActWorkTypeRemove(actionMap_));
		getCntWorkType().setup();
		
		//outputTypeのActionセット
		getCntOutputType().getActionMap().put(PnlTableMaint.ACTION_KEY_NEW, 
				new ActOutputTypeShowNewDlg(actionMap_));
		getCntOutputType().getActionMap().put(PnlTableMaint.ACTION_KEY_UPDATE, 
				new ActOutputTypeUpdate(actionMap_));
		getCntOutputType().getActionMap().put(PnlTableMaint.ACTION_KEY_DELETE, 
				new ActOutputTypeDelete(actionMap_));
		getCntOutputType().setup();
			
		//作業種類の行選択イベントを検知
		getCntWorkType().setTableSelectionListener(new Synchronizer());
	}
	
	/**
	 * @param owner
	 */
	public DlgWorkTypeOutputType(JDialog owner) {
		super(owner, true);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(640, 480);
		this.setContentPane(getJContentPane());
		this.setTitle("work type & output type");
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			GridLayout gridLayout = new GridLayout();
			gridLayout.setRows(2);
			jContentPane = new JPanel();
			jContentPane.setLayout(gridLayout);
			jContentPane.add(getCntWorkType(), null);
			jContentPane.add(getCntOutputType(), null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes cntWorkType	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private PnlTableMaint getCntWorkType() {
		if (cntWorkType == null) {
			cntWorkType = new PnlTableMaint();
			cntWorkType.setTitle("work type");
		}
		return cntWorkType;
	}

	/**
	 * This method initializes cntOutputType	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private PnlTableMaint getCntOutputType() {
		if (cntOutputType == null) {
			cntOutputType = new PnlTableMaint();
			cntOutputType.setTitle("output type");
		}
		return cntOutputType;
	}


	public void refreshOutputTypeTable() {
		outputTypeModel_.executeQuery();
		//最後に指定されたworkTypeIdは、outputTypeModel_内に保存されている
		setOutputTableColumnWidth();
	}
	public void loadWorkTypeTable() {
		JTable table = getCntWorkType().getJTable();
		int selectedIndex = table.getSelectedRow();
		
		workTypeModel_.executeQuery();
		
		//workTypeの列幅を決定
		int verIndex = workTypeModel_.getVersionNoColumnIndex();
		if(verIndex >= 0){
			TableColumn col = table.getColumnModel().getColumn(verIndex);
			col.setMinWidth(0);
			col.setMaxWidth(0);
		}
		table.getColumnModel().getColumn(
				workTypeModel_.getColumnIndex("id")).setMaxWidth(40);	//id
		table.getColumnModel().getColumn(
				workTypeModel_.getColumnIndex("seq")).setMaxWidth(60);	//seq
		
		//選択状態を復元
		if((selectedIndex >= 0) && ( workTypeModel_.getRowCount() > selectedIndex)){
			table.getSelectionModel().addSelectionInterval(selectedIndex, selectedIndex);
		}
	}


}  //  @jve:decl-index=0:visual-constraint="10,10"
