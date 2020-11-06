package jp.tokyo.selj.view.tools;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import jp.tokyo.selj.common.AppException;
import jp.tokyo.selj.dao.OutputPropType;
import jp.tokyo.selj.dao.OutputPropTypeDao;
import jp.tokyo.selj.dao.OutputTypePropType;
import jp.tokyo.selj.dao.OutputTypePropTypeDao;
import jp.tokyo.selj.dao.SelJDaoContainer;
import jp.tokyo.selj.model.DBTableModel;
import jp.tokyo.selj.view.ActBase;
import jp.tokyo.selj.view.ActTransactionBase;

import org.apache.log4j.Logger;
import org.seasar.framework.container.S2Container;


public class DlgCheckPointAndPropTypeMaint extends JDialog {
	Logger log = Logger.getLogger(this.getClass()); 

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private PnlTableMaint cntCheckPoint = null;

	private PnlTableMaint cntOutputType = null;

	CheckPointModel checkPointModel_ = null;
	OutputTypeModel outputTypeModel_ = null;

	ActionMap actionMap_ = new ActionMap();

	S2Container daoCont_ = SelJDaoContainer.SEL_DAO_CONT;  //  @jve:decl-index=0:
	OutputPropTypeDao outputPropTypeDao_ = null;
	OutputTypePropTypeDao outputTypePropTypeDao_ = null;

	private JTabbedPane cntSlaveTab = null;
	//Actionの親クラス
	private abstract class ActBase2 extends ActBase {
		ActBase2(ActionMap map){
			super(map);
		}
		protected Component getOwnerComponent(){
			return DlgCheckPointAndPropTypeMaint.this;
		}
	}
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	// checkpoint
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	class ActCheckPointShowNewDlg extends ActBase2 {
		DlgCheckPointDetail dialog_ = null;
		public ActCheckPointShowNewDlg(ActionMap map){
			super(map);
			putValue(Action.NAME, "new");
			putValue(Action.SHORT_DESCRIPTION, "チェック項目を追加します(Ins)");
		}
		public void actionPerformed2(ActionEvent e) {
			getDlg().newCheckPoint(checkPointModel_.curOutputTypeId_);
			getDlg().setVisible(true);
		}
		protected DlgCheckPointDetail getDlg(){
			if(dialog_ == null){
				dialog_ = new  DlgCheckPointDetail(DlgCheckPointAndPropTypeMaint.this);
				dialog_.setup();
				dialog_.setLocationRelativeTo(getOwnerComponent());
			}
			return dialog_;
		}
	}
	class ActCheckPointShowUpdateDlg extends ActCheckPointShowNewDlg {
		public ActCheckPointShowUpdateDlg(ActionMap map){
			super(map);
			putValue(Action.NAME, "update");
			putValue(Action.SHORT_DESCRIPTION, "チェック項目を更新します");
		}
		public void actionPerformed2(ActionEvent e) {
			JTable table = getCntCheckPoint().getJTable();
			if(table.getSelectedRow() < 0){
				throw new AppException("check pointを選択してください"); 
			}
			int idIndex = checkPointModel_.getColumnIndex("id");
			int id = ((Number)table.getValueAt(
								table.getSelectedRow(), idIndex)).intValue();
			getDlg().loadCheckPoint(id);
			getDlg().setVisible(true);
		}
	}
	class ActCheckPointRemove extends ActCheckPointShowNewDlg {
		public ActCheckPointRemove(ActionMap map){
			super(map);
			putValue(Action.NAME, "remove");
			putValue(Action.SHORT_DESCRIPTION, "チェック項目を削除します(Del)");
		}
		public void actionPerformed2(ActionEvent e) {
			//作業種類の削除を行う
			JTable table = getCntCheckPoint().getJTable();
			if(table.getSelectedRow() < 0){
				throw new AppException("check pointを選択してください"); 
			}
			int idIndex = checkPointModel_.getColumnIndex("id");
			int id = ((Number)table.getValueAt(
								table.getSelectedRow(), idIndex)).intValue();

			if( JOptionPane.showConfirmDialog(
					DlgCheckPointAndPropTypeMaint.this
					,"選択中のチェック項目を削除します。\n" +
					"チェック項目がすでに使用されている場合は、delete flagをOnにしてください。\n" +
					"よろしいですか？",""
					,JOptionPane.YES_NO_OPTION
					,JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
						
				getDlg().deleteCheckPoint(id);
			}
//			getDlg().setVisible(true);
		}
	}
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	// outputPropType
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	class ActOutputPropTypeShowNewDlg extends ActBase2 {
		DlgOutputPropTypeDetail dialog_ = null;
		public ActOutputPropTypeShowNewDlg(ActionMap map){
			super(map);
			putValue(Action.NAME, "new");
			putValue(Action.SHORT_DESCRIPTION, "成果物属性種類を追加します(Ins)");
		}
		public void actionPerformed2(ActionEvent e) {
			getDlg().newWorkPropType();
			getDlg().setVisible(true);
		}
		protected DlgOutputPropTypeDetail getDlg(){
			if(dialog_ == null){
				dialog_ = new  DlgOutputPropTypeDetail(DlgCheckPointAndPropTypeMaint.this);
				dialog_.setup();
				dialog_.setLocationRelativeTo(getOwnerComponent());
			}
			return dialog_;
		}
	}
	class ActOutputPropTypeShowUpdateDlg extends ActOutputPropTypeShowNewDlg {
		public ActOutputPropTypeShowUpdateDlg(ActionMap map){
			super(map);
			putValue(Action.NAME, "update");
			putValue(Action.SHORT_DESCRIPTION, "成果物属性種類を更新します");
		}
		public void actionPerformed2(ActionEvent e) {
			JList workPeopTypes = getInpSelectOutputPropTypeList();
			if(workPeopTypes.getSelectedIndex() < 0){
				throw new AppException("workPropTypeを選択してください"); 
			}
			getDlg().setWorkPropType((OutputPropType)workPeopTypes.getSelectedValue());
			getDlg().setVisible(true);
		}
	}
	class ActOutputPropTypeRemove extends ActOutputPropTypeShowNewDlg {
		public ActOutputPropTypeRemove(ActionMap map){
			super(map);
			putValue(Action.NAME, "remove");
			putValue(Action.SHORT_DESCRIPTION, "成果物属性種類を削除します(Del)");
		}
		public void actionPerformed2(ActionEvent e) {
			JList outputPropTypes = getInpSelectOutputPropTypeList();
			if(outputPropTypes.getSelectedIndex() < 0){
				throw new AppException("成果物属性種類を選択してください"); 
			}

			if( JOptionPane.showConfirmDialog(
					DlgCheckPointAndPropTypeMaint.this
					,"選択中の成果物属性種類を削除します。\n" +
					"よろしいですか？",""
					,JOptionPane.YES_NO_OPTION
					,JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
						
				getDlg().deleteWorkPropType(
					((OutputPropType)outputPropTypes.getSelectedValue()).getOutputPropTypeId()
				);
			}
		}
	}
	class ActApplySetting extends ActTransactionBase {
		public ActApplySetting(ActionMap map){
			super(map);
			putValue(Action.NAME, "apply setting");
			putValue(Action.SHORT_DESCRIPTION, "選択中の成果物属性種類を成果物種類に関連付けます");
		}
		public void actionPerformed2(ActionEvent e) {
			long outputTypeId = getSelectedOutputTypeId();
			if(outputTypeId < 0){
				throw new AppException("成果物種類を選択してください");
			}
			
			int[] selectIndices = getInpSelectOutputPropTypeList().getSelectedIndices();

			outputTypePropTypeDao_.deleteByOutputTyeId(outputTypeId);	//一旦全削除
			for(int i=0;i < selectIndices.length; i++){
				OutputTypePropType otpt = new OutputTypePropType();
				otpt.setOutputTypeId(outputTypeId);
				OutputPropType opt = (OutputPropType)
					getInpSelectOutputPropTypeList().getModel().getElementAt(selectIndices[i]);		
				otpt.setOutputPropTypeId(opt.getOutputPropTypeId());
				
				outputTypePropTypeDao_.insert(otpt);
			}
		}
	}

	//成果物種類テーブルモデル
	class OutputTypeModel extends DBTableModel{
		@Override
		public String getTableName() {
			return "outputtype";
		}
		
		@Override
		public String getQuerySql() {
			return "SELECT " +
					"outputTypeId as id" +
					",workTypeId" +
					",SEQ" +
					",outputTypeName" +
					",memo" +
					",newDate" +
					",versionNo" +
					" FROM outputtype" +
					" ORDER BY workTypeId,SEQ"
					;
		}

		@Override
		public String getWhereOfUpdateSql(int row) {
			int id = ((Number)getValueAt(row, "id")).intValue();
			return "outputTypeId=" + id;
		}
	}
	//チェックポイントテーブルモデル
	class CheckPointModel extends DBTableModel{
		long curOutputTypeId_ = -1;
		@Override
		public String getTableName() {
			return "checkPoint";
		}
		
		@Override
		public String getQuerySql() {
			return "SELECT " +
					"checkPointId as id" +
					",SEQ" +
					",outputTypeId" +
					",checkCont" +
					",deleteFlg" +
					",versionNo" +
					" FROM checkPoint" +
					" WHERE outputTypeId="+curOutputTypeId_ +
					" ORDER BY SEQ";
		}

		public void setOutputTypeId(long id) {
			if(curOutputTypeId_ == id){
				return;
			}
			curOutputTypeId_ = id;
			executeQuery();
		}
	}
	class Synchronizer implements ListSelectionListener{
		public void valueChanged(ListSelectionEvent e) {
			long outputTypeId = getSelectedOutputTypeId();
			if(outputTypeId < 0){
				checkPointModel_.clear();
				getInpSelectOutputPropTypeList().setSelectedIndices(new int[]{});
				return;
			}
			//check point
			checkPointModel_.setOutputTypeId(outputTypeId);
			setCheckPointTableColumnWidth();
			
			//output prop type
			refreshOutputPropTypeTable();
		}
	}
	long getSelectedOutputTypeId(){
		JTable outputTypeTable = getCntOutputType().getJTable();
		if(outputTypeTable.getSelectedRow() < 0){
			return -1;
		}
		return ((Number)outputTypeTable.getValueAt(
					outputTypeTable.getSelectedRow(), 0)).intValue();
	}
	
	void setCheckPointTableColumnWidth(){
		//outputTypesの列幅を決定
		JTable table = getCntCheckPoint().getJTable();
		int verIndex = checkPointModel_.getVersionNoColumnIndex();
		if(verIndex >= 0){
			TableColumn col = table.getColumnModel().getColumn(verIndex);
			col.setMinWidth(0);
			col.setMaxWidth(0);
		}
		table.getColumnModel().getColumn(
				checkPointModel_.getColumnIndex("id")).setMaxWidth(40);	//id
		table.getColumnModel().getColumn(
				checkPointModel_.getColumnIndex("seq")).setMaxWidth(60);	//seq
		table.getColumnModel().getColumn(
				checkPointModel_.getColumnIndex("outputTypeId")).setMaxWidth(100);
		table.getColumnModel().getColumn(
				checkPointModel_.getColumnIndex("deleteFlg")).setMaxWidth(60);
		
	}
	
	public void setup(){
		checkPointModel_ = new CheckPointModel();
		outputTypeModel_ = new OutputTypeModel();
		getCntCheckPoint().getJTable().setModel(checkPointModel_);
		getCntOutputType().getJTable().setModel(outputTypeModel_);
		outputPropTypeDao_ = (OutputPropTypeDao)daoCont_.getComponent(OutputPropTypeDao.class);
		outputTypePropTypeDao_ = (OutputTypePropTypeDao)daoCont_.getComponent(OutputTypePropTypeDao.class);

		//outputTypeのActionセット
//		getCntOutputType().getActionMap().put(PnlTableMaint.ACTION_KEY_NEW, 
//				new ActOutputTypeShowNewDlg(actionMap_));
//		getCntOutputType().getActionMap().put(PnlTableMaint.ACTION_KEY_UPDATE, 
//				new ActOutputTypeUpdate(actionMap_));
//		getCntOutputType().getActionMap().put(PnlTableMaint.ACTION_KEY_DELETE, 
//				new ActOutputTypeDelete(actionMap_));
//		getCntOutputType().setup();
		
		//workTypeのActionセット
		getCntCheckPoint().getActionMap().put(PnlTableMaint.ACTION_KEY_NEW, 
				new ActCheckPointShowNewDlg(actionMap_));
		getCntCheckPoint().getActionMap().put(PnlTableMaint.ACTION_KEY_UPDATE, 
				new ActCheckPointShowUpdateDlg(actionMap_));
		getCntCheckPoint().getActionMap().put(PnlTableMaint.ACTION_KEY_DELETE, 
				new ActCheckPointRemove(actionMap_));
		getCntCheckPoint().setup();

		//workPropType
		getCmdNewOutputPropType().setAction(new ActOutputPropTypeShowNewDlg(actionMap_));
		getCmdUpdateOutputPropType().setAction(new ActOutputPropTypeShowUpdateDlg(actionMap_));
		getCmdRemoveOutputPropType().setAction(new ActOutputPropTypeRemove(actionMap_));
		getCmdApplySetting().setAction(new ActApplySetting(actionMap_));
		
		
		getInpSelectOutputPropTypeList().setCellRenderer(
				new DefaultListCellRenderer(){
					@Override
					public Component getListCellRendererComponent(JList arg0, Object arg1, int arg2, boolean arg3, boolean arg4) {
						Component ret = super.getListCellRendererComponent(arg0, arg1, arg2, arg3, arg4);
						JLabel lbl = (JLabel)ret;
						OutputPropType wpt = (OutputPropType)arg1;
						String line = "<html>";
						line += "id="+wpt.getOutputPropTypeId();
						line += ", SEQ="+wpt.getSeq();
						line += ", <b>"+wpt.getOutputPropTypeName()+"</b>"
								+" : "+wpt.getUnitName();
								
						if(wpt.getDescr()!=null && !"".equals(wpt.getDescr())){
							line += "("+wpt.getDescr()+")";
						}
						line +="</html>";
						lbl.setText(line);
						return ret;
					}
				}
		);

		refreshOutputPropTypeTable();

		//作業種類の行選択イベントを検知
		getCntOutputType().setTableSelectionListener(new Synchronizer());
	}
	
	/**
	 * @param owner
	 */
	public DlgCheckPointAndPropTypeMaint(JDialog owner) {
		super(owner, true);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(800, 640);
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
			jContentPane.add(getCntOutputType(), null);
			jContentPane.add(getCntSlaveTab(), null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes cntCheckPoint	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private PnlTableMaint getCntCheckPoint() {
		if (cntCheckPoint == null) {
			cntCheckPoint = new PnlTableMaint();
			cntCheckPoint.setTitle("check point");
		}
		return cntCheckPoint;
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
			cntOutputType.hideButtons();
		}
		return cntOutputType;
	}

	public void refreshOutputPropTypeTable() {
		List wptList = outputPropTypeDao_.findAll();
		getInpSelectOutputPropTypeList().setListData(wptList.toArray());
		long outputPropTypeId = getSelectedOutputTypeId();
		if(outputPropTypeId < 0){
			return; 
		}
		List<OutputTypePropType> otpts = outputTypePropTypeDao_.findByOutputTypeId(outputPropTypeId);
		for(OutputTypePropType otpt: otpts){
			for(int i=0; i < getInpSelectOutputPropTypeList().getModel().getSize(); i++){
				OutputPropType opt = (OutputPropType)getInpSelectOutputPropTypeList().getModel().getElementAt(i);
				if(otpt.getOutputPropTypeId() == opt.getOutputPropTypeId()){
					getInpSelectOutputPropTypeList().addSelectionInterval(i, i);
				}
			}
		}
	}

	public void refreshCheckPointTable() {
		checkPointModel_.executeQuery();
		//最後に指定されたcheckPointIdは、checkPointModel_内に保存されている
		setCheckPointTableColumnWidth();
	}
	public void loadOutputTypeTable() {
		JTable table = getCntOutputType().getJTable();
		int selectedIndex = table.getSelectedRow();
		
		outputTypeModel_.executeQuery();
		
		//outputTypeの列幅を決定
		int verIndex = outputTypeModel_.getVersionNoColumnIndex();
		if(verIndex >= 0){
			TableColumn col = table.getColumnModel().getColumn(verIndex);
			col.setMinWidth(0);
			col.setMaxWidth(0);
		}
		table.getColumnModel().getColumn(
				outputTypeModel_.getColumnIndex("id")).setMaxWidth(40);	//id
		table.getColumnModel().getColumn(
				outputTypeModel_.getColumnIndex("workTypeId")).setMaxWidth(100);
		table.getColumnModel().getColumn(
				outputTypeModel_.getColumnIndex("seq")).setMaxWidth(60);	//seq
		
		//選択状態を復元
		if((selectedIndex >= 0) && ( outputTypeModel_.getRowCount() > selectedIndex)){
			table.getSelectionModel().addSelectionInterval(selectedIndex, selectedIndex);
		}
	}

	/**
	 * This method initializes cntSlaveTab	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */
	private JTabbedPane getCntSlaveTab() {
		if (cntSlaveTab == null) {
			cntSlaveTab = new JTabbedPane();
			cntSlaveTab.addTab("check point", null, getCntCheckPoint(), null);
			cntSlaveTab.addTab("workPropType", null, getCntOutputPropType(), null);
		}
		return cntSlaveTab;
	}

	private JLabel lblWorkPropType = null;
	private JPanel cntOutputPropType = null;
	private JPanel cntOutputPropTypeBtns = null;
	private JButton cmdUpdateOutputPropType = null;
	private JButton cmdRemoveOutputPropType = null;
	private JScrollPane cntSelectPropTypeList = null;
	private JButton cmdNewOutputPropType = null;
	private JScrollPane cntOutputPropTypeList = null;
	private JList inpSelectOutputPropTypeList = null;

	private JButton cmdApplySetting = null;
	/**
	 * This method initializes cntOutputPropType	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntOutputPropType() {
		if (cntOutputPropType == null) {
			cntOutputPropType = new JPanel();
			cntOutputPropType.setLayout(new BorderLayout());
			cntOutputPropType.add(getCntOutputPropTypeBtns(), BorderLayout.NORTH);
			cntOutputPropType.add(getCntSelectPropTypeList(), BorderLayout.CENTER);
		}
		return cntOutputPropType;
	}
	/**
	 * This method initializes cntOutputPropTypeBtns	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntOutputPropTypeBtns() {
		if (cntOutputPropTypeBtns == null) {
			lblWorkPropType = new JLabel();
			lblWorkPropType.setText("workPropType");
			cntOutputPropTypeBtns = new JPanel();
			FlowLayout flowLayout = new FlowLayout();
			flowLayout.setAlignment(FlowLayout.LEFT);
			cntOutputPropTypeBtns.setLayout(flowLayout);
			cntOutputPropTypeBtns.add(lblWorkPropType, null);
			cntOutputPropTypeBtns.add(getCmdNewOutputPropType(), null);
			cntOutputPropTypeBtns.add(getCmdUpdateOutputPropType(), null);
			cntOutputPropTypeBtns.add(getCmdRemoveOutputPropType(), null);
			cntOutputPropTypeBtns.add(getCmdApplySetting(), null);
		}
		return cntOutputPropTypeBtns;
	}
	/**
	 * This method initializes cmdNewOutputPropType	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdNewOutputPropType() {
		if (cmdNewOutputPropType == null) {
			cmdNewOutputPropType = new JButton();
			cmdNewOutputPropType.setText("new");
		}
		return cmdNewOutputPropType;
	}

	/**
	 * This method initializes cmdUpdateOutputPropType	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdUpdateOutputPropType() {
		if (cmdUpdateOutputPropType == null) {
			cmdUpdateOutputPropType = new JButton();
			cmdUpdateOutputPropType.setText("update");
		}
		return cmdUpdateOutputPropType;
	}

	/**
	 * This method initializes cmdRemoveOutputPropType	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdRemoveOutputPropType() {
		if (cmdRemoveOutputPropType == null) {
			cmdRemoveOutputPropType = new JButton();
			cmdRemoveOutputPropType.setText("remove");
		}
		return cmdRemoveOutputPropType;
	}

	/**
	 * This method initializes cntSelectPropTypeList	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getCntSelectPropTypeList() {
		if (cntSelectPropTypeList == null) {
			cntSelectPropTypeList = new JScrollPane();
			cntSelectPropTypeList.setViewportView(getCntOutputPropTypeList());
		}
		return cntSelectPropTypeList;
	}

	/**
	 * This method initializes cntOutputPropTypeList	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getCntOutputPropTypeList() {
		if (cntOutputPropTypeList == null) {
			cntOutputPropTypeList = new JScrollPane();
			cntOutputPropTypeList.setViewportView(getInpSelectOutputPropTypeList());
		}
		return cntOutputPropTypeList;
	}

	/**
	 * This method initializes inpSelectOutputPropTypeList	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getInpSelectOutputPropTypeList() {
		if (inpSelectOutputPropTypeList == null) {
			inpSelectOutputPropTypeList = new JList();
			inpSelectOutputPropTypeList.setFont(new Font("Dialog", Font.PLAIN, 12));
			inpSelectOutputPropTypeList.setToolTipText("ctrl+clickで複数の属性種類を選択し「apply Setting」ボタンを押してください");
			inpSelectOutputPropTypeList.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					if(e.getClickCount() >= 2){
						actionMap_.get(ActOutputPropTypeShowUpdateDlg.class)
							.actionPerformed(null);
					}
				}
			});
		}
		return inpSelectOutputPropTypeList;
	}

	/**
	 * This method initializes cmdApplySetting	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdApplySetting() {
		if (cmdApplySetting == null) {
			cmdApplySetting = new JButton();
			cmdApplySetting.setText("applySetting");
		}
		return cmdApplySetting;
	}



}  //  @jve:decl-index=0:visual-constraint="10,10"
