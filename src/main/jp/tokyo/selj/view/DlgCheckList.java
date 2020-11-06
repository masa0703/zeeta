package jp.tokyo.selj.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import jp.tokyo.selj.common.AppException;
import jp.tokyo.selj.common.DateTextFormatter;
import jp.tokyo.selj.common.PreferenceWindowHelper;
import jp.tokyo.selj.dao.Check;
import jp.tokyo.selj.dao.CheckDao;
import jp.tokyo.selj.dao.CheckDetail;
import jp.tokyo.selj.dao.CheckDetailDao;
import jp.tokyo.selj.dao.CheckState;
import jp.tokyo.selj.dao.Output;
import jp.tokyo.selj.dao.SelJDaoContainer;
import jp.tokyo.selj.model.MasterComboModel;

import org.apache.log4j.Logger;
import org.seasar.framework.container.S2Container;

public class DlgCheckList extends JDialog implements ListSelectionListener{

	Logger log = Logger.getLogger(this.getClass());
	private static final long serialVersionUID = 1L;
	PreferenceWindowHelper pref_;
	S2Container daoCont_ = SelJDaoContainer.SEL_DAO_CONT;

	private JPanel jContentPane = null;

	private JPanel cntHeader = null;

	PnlOutput pnlOutput_ = null;

	private JList dspCheckList = null;

	private JScrollPane dspCheckDetailListScr = null;

	private JTable dspCheckDetailList = null;

	private JPanel cntCheckList = null;

	private JButton cmdNew = null;

	private JButton cmdRemove = null;

	private JScrollPane cntCheckListScr = null;
	
	Output lastSelected_ = null;
	CheckDao checkDao_ = null;
	CheckDetailDao checkDetailDao_ = null;

	class ActShowNewCheck extends ActBase {
		DlgNewCheck dlgNewCheck_ = null;
		protected Component getOwnerComponent(){
			return DlgCheckList.this;
		}
		public ActShowNewCheck(){
			super();
			putValue(Action.NAME, "C");
			putValue(Action.SHORT_DESCRIPTION, "新規のチェックリストを作成する");
		}
		public void actionPerformed2(ActionEvent e) {
			getDlg().setVisible(true, lastSelected_, null);
		}
		DlgNewCheck getDlg(){
			if(dlgNewCheck_ == null){
				dlgNewCheck_ = new DlgNewCheck(DlgCheckList.this);
				dlgNewCheck_.setLocationRelativeTo(DlgCheckList.this);
				dlgNewCheck_.setup(DlgCheckList.this);
			}
			return dlgNewCheck_;
		}
	}
	class ActShowUpdateCheck extends ActShowNewCheck {
		public ActShowUpdateCheck(){
			super();
			putValue(Action.NAME, "U");
			putValue(Action.SHORT_DESCRIPTION, "チェックリストを更新する");
		}
		public void actionPerformed2(ActionEvent e) {
			Check check = (Check)getDspCheckList().getSelectedValue();
			if(check == null){
				throw new AppException("いずれかのチェックを選択してください");
			}
			getDlg().setVisible(true, lastSelected_, check);
		}
	}
	class ActRemoveCheck extends ActTransactionBase {
		protected Component getOwnerComponent(){
			return DlgCheckList.this;
		}
		public ActRemoveCheck(){
			super();
			putValue(Action.NAME, "D");
			putValue(Action.SHORT_DESCRIPTION, "選択したチェックを削除します");
		}
		public void actionPerformed2(ActionEvent e) {
			Check check = (Check)getDspCheckList().getSelectedValue();
			if(check == null){
				throw new AppException("いずれかのチェックを選択してください");
			}
			if( JOptionPane.showConfirmDialog(
					DlgCheckList.this
					,"選択したチェックを削除します。\n" +
					"よろしいですか？",""
					,JOptionPane.YES_NO_OPTION
					,JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
				checkDao_.remove(check);
				refreshCheckList();
			}
			
		}
	}
	class ActApplyAll extends ActTransactionBase {
		protected Component getOwnerComponent(){
			return DlgCheckList.this;
		}
		public ActApplyAll(){
			super();
			setEnabled(false);
			putValue(Action.NAME, "apply all");
			putValue(Action.SHORT_DESCRIPTION, "チェック結果を書き込みます");
		}
		public void actionPerformed2(ActionEvent e) {
			List<CheckDetail> details = checkDetailListModel_.checkDetails_;
			for(int i=0; i<details.size(); i++){
				checkDetailDao_.update(details.get(i));
			}
			checkDetailListModel_.setDarty(false);
		}
	}

	//チェックリストテーブルモデル
	class CheckDetailListModel extends AbstractTableModel implements ListSelectionListener{
		String[] columnNames_ = {"check point", "result", "date"};
		List<CheckDetail> checkDetails_ = Collections.EMPTY_LIST;
		Check lastSelected_ = null;
		boolean isDarty_ = false;
		
		void setDarty(boolean b){
			isDarty_ = b;
			getCmdApplyAll().setEnabled(b);
		}
		
		@Override
		public String getColumnName(int column) {
			return this.columnNames_[column];
		}
		public int getColumnCount() {
			return columnNames_.length;
		}
		public int getRowCount() {
			return checkDetails_.size();
		}
		public Object getValueAt(int rowIndex, int columnIndex) {
			Object ret = null;
			CheckDetail checkDetail = checkDetails_.get(rowIndex);
			switch(columnIndex){
			case 0:
				ret = checkDetail.getCheckPoint().getCheckCont();
				break;
			case 1:
				ret = checkDetail.getCheckState().getCheckStateName();
				break;
			case 2:
				ret = DateTextFormatter.dateToString(checkDetail.getCheckDate());
				break;
			}
			return ret;
		}
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			CheckDetail checkDetail = checkDetails_.get(rowIndex);
			switch(columnIndex){
			case 0:
				break;
			case 1:
				if(!checkDetail.getCheckState().equals(aValue)){
					log.debug("aValue="+aValue);
					checkDetail.setCheckState((CheckState)aValue);
					checkDetail.setUpdDate(new Timestamp(System.currentTimeMillis()));
					fireTableCellUpdated(rowIndex, 2);
					setDarty(true);
				}
				break;
			case 2:
				break;
			}
		}
		public void valueChanged(ListSelectionEvent e) {
			Check selected = (Check)getDspCheckList().getSelectedValue();
			if(lastSelected_ == selected ){
				//2回呼び出される無駄を排除
				return;
			}
			lastSelected_ = selected;
			//もし変更されていたら更新する
			updateIfDarty();
			setDarty(false);

			if(lastSelected_ == null){
				checkDetails_ = Collections.EMPTY_LIST;
			}else{
				checkDetails_ = checkDetailDao_.findByOutputIdDate(
							lastSelected_.getOutputId(), 
							lastSelected_.getCheckDate());
			}
			fireTableDataChanged();
		}
		public final boolean isCellEditable(int row,int column){
			return column == 1;
		}
		public void updateIfDarty(){
			if(!isDarty_){
				return;
			}
			// 更新せなあかん
			if( JOptionPane.showConfirmDialog(
					DlgCheckList.this
					,"内容が変更されています。\n" +
					"更新を反映しますか？",""
					,JOptionPane.YES_NO_OPTION
					,JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
				new ActApplyAll().actionPerformed(null);
			}
		}
	}
	CheckDetailListModel checkDetailListModel_ = null;
	private JPanel cntCheckDetail = null;
	private JPanel cntCheckDetailFooter = null;
	private JButton cmdApplyAll = null;
	private JSplitPane cntSplitBody = null;
	private JButton cmdUpdate = null;
	private JPanel cntButtons = null;
	
	
	class CheckListModel extends AbstractListModel{
		List<Check> checkList_=null;
		CheckListModel(List<Check> checkList){
			checkList_ = checkList;
		}
		
		public Object getElementAt(int index) {
			return checkList_.get(index);
		}
		public int getSize() {
			return checkList_.size();
		}
	}
	
	public void valueChanged(ListSelectionEvent e) {
		//選択されたitem
		JList list = (JList)e.getSource();
		if( !isShowing() || list.getSelectedValue() == null){
			return;
		}
		if(list.getSelectedValue() == lastSelected_){
			//なぜか同じイベントが２つ連続するため
			return;
		}
		log.debug("selected="+list.getSelectedValue());
		lastSelected_ = (Output)list.getSelectedValue();
		refreshCheckList();
	}

	@Override
	public void setVisible(boolean b) {
		if(b){
			throw new RuntimeException("setVisible(boolean b, Output output)をつかわなあかん");
		}else{
			super.setVisible(b);
		}
	}
	public void setVisible(boolean b, Output output) {
		lastSelected_ = output;
		refreshCheckList();
		super.setVisible(b);
	}
	void refreshCheckList(){
		pnlOutput_.setOutput(lastSelected_);
		
		getCmdRemove().setEnabled(false);
		//lastSelected_に関連するCheck一覧をCheckListにセット
		List<Check> checks = checkDao_.findByOutputId(lastSelected_.getOutputId());
		
		getDspCheckList().setModel(new CheckListModel(checks));
		if(checks.size() > 0){
			getDspCheckList().setSelectedIndex(0);
		}
	}

	public void setup(){
		//ボタンにアクション
		getCmdNew().setAction(new ActShowNewCheck());
		getCmdUpdate().setAction(new ActShowUpdateCheck());
		getCmdRemove().setAction(new ActRemoveCheck());
		getCmdApplyAll().setAction(new ActApplyAll());
		
		pnlOutput_ = new PnlOutput();
		pnlOutput_.setOpaque(false);
		cntHeader.add(pnlOutput_, BorderLayout.CENTER);
		getDspCheckDetailList().setModel(new CheckDetailListModel());
		
		checkDao_ = (CheckDao) daoCont_.getComponent(CheckDao.class);
		checkDetailDao_ = (CheckDetailDao) daoCont_.getComponent(CheckDetailDao.class);

		checkDetailListModel_ = new CheckDetailListModel();
		dspCheckList.addListSelectionListener(checkDetailListModel_);
		dspCheckList.addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e) {
				getCmdRemove().setEnabled(getDspCheckList().getSelectedValue() != null);
			}
		});
		dspCheckDetailList.setModel(checkDetailListModel_);
		
		TableColumn resultCol = dspCheckDetailList.getColumn("result");
		//tableの列幅設定
		resultCol.setMaxWidth(100);
		dspCheckDetailList.getColumn("date").setMaxWidth(100);
		
		//tableのresult列
		JComboBox comboBox = new JComboBox(MasterComboModel.newCheckStateComboBoxModel());
		comboBox.setRenderer(
			new DefaultListCellRenderer(){
				public Component getListCellRendererComponent(JList list,Object value, 
						int index, boolean isSelected, boolean cellHasFocus){
					super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
					CheckState val = (CheckState)value;
					setText(val.getCheckStateName());
					return this;
				}
			}
		);
		resultCol.setCellEditor(
			new DefaultCellEditor(comboBox)
		);
		
		//WindowListener登録
		this.addWindowListener(
			new WindowAdapter(){
				public void windowClosing(WindowEvent e) {
					checkDetailListModel_.updateIfDarty();
				}
			}
		);
		
		pref_ = new PreferenceWindowHelper(this);
		pref_.restoreForm();

	}
	/**
	 * @param owner
	 */
	public DlgCheckList(Dialog owner) {
		this(owner, false);
	}
	/**
	 * @param owner
	 */
	public DlgCheckList(Dialog owner, boolean modal) {
		super(owner, modal);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(800, 400);
		this.setTitle("check list");
		this.setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getCntHeader(), BorderLayout.NORTH);
			jContentPane.add(getCntSplitBody(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

	/**
	 * This method initializes cntHeader	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntHeader() {
		if (cntHeader == null) {
			cntHeader = new JPanel();
			cntHeader.setLayout(new BorderLayout());
		}
		return cntHeader;
	}

	/**
	 * This method initializes dspCheckList	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getDspCheckList() {
		if (dspCheckList == null) {
			dspCheckList = new JList();
			dspCheckList.setPreferredSize(new Dimension(250, 0));
			dspCheckList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			dspCheckList.setFont(new Font("Dialog", Font.PLAIN, 12));
			dspCheckList.setCellRenderer(
				new DefaultListCellRenderer(){
					public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
						JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
						Check check = (Check)value;
						String line = "";
						SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
						line += formatter.format(check.getCheckDate());
						line += " " + ((check.getRemark() == null)? "":check.getRemark());
						line += " "+check.getUserName();
						label.setText(line);
						return label;
					}
					
				}
			);
			dspCheckList.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					System.out.println("mouseClicked()"); //     		
					if(e.getClickCount() == 2){
		    			Action action = cmdUpdate.getAction();
		    			if(action != null){
		    				action.actionPerformed(null);
		    			}
					}
				}
			});
		}
		return dspCheckList;
	}
	/**
	 * This method initializes dspCheckDetailListScr	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getDspCheckDetailListScr() {
		if (dspCheckDetailListScr == null) {
			dspCheckDetailListScr = new JScrollPane();
			dspCheckDetailListScr.setViewportView(getDspCheckDetailList());
		}
		return dspCheckDetailListScr;
	}
	/**
	 * This method initializes dspCheckDetailList	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getDspCheckDetailList() {
		if (dspCheckDetailList == null) {
			dspCheckDetailList = new JTable();
			dspCheckDetailList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		return dspCheckDetailList;
	}
	/**
	 * This method initializes cntCheckList	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntCheckList() {
		if (cntCheckList == null) {
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.gridx = 0;
			gridBagConstraints12.gridy = 0;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.fill = GridBagConstraints.BOTH;
			gridBagConstraints11.weighty = 1.0;
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.gridy = 1;
			gridBagConstraints11.gridwidth = 1;
			gridBagConstraints11.weightx = 1.0;
			cntCheckList = new JPanel();
			cntCheckList.setLayout(new GridBagLayout());
//			cntCheckList.setPreferredSize(new Dimension(150, 0));
			cntCheckList.add(getCntCheckListScr(), gridBagConstraints11);
			cntCheckList.add(getCntButtons(), gridBagConstraints12);
		}
		return cntCheckList;
	}
	/**
	 * This method initializes cmdNew	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdNew() {
		if (cmdNew == null) {
			cmdNew = new JButton();
			cmdNew.setText("C");
		}
		return cmdNew;
	}
	/**
	 * This method initializes cmdRemove	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdRemove() {
		if (cmdRemove == null) {
			cmdRemove = new JButton();
			cmdRemove.setText("D");
		}
		return cmdRemove;
	}
	/**
	 * This method initializes cntCheckListScr	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getCntCheckListScr() {
		if (cntCheckListScr == null) {
			cntCheckListScr = new JScrollPane();
			cntCheckListScr.setViewportView(getDspCheckList());
		}
		return cntCheckListScr;
	}

	/**
	 * This method initializes cntCheckDetail	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntCheckDetail() {
		if (cntCheckDetail == null) {
			cntCheckDetail = new JPanel();
			cntCheckDetail.setLayout(new BorderLayout());
			cntCheckDetail.add(getDspCheckDetailListScr(), BorderLayout.CENTER);
			cntCheckDetail.add(getCntCheckDetailFooter(), BorderLayout.SOUTH);
		}
		return cntCheckDetail;
	}

	/**
	 * This method initializes cntCheckDetailFooter	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntCheckDetailFooter() {
		if (cntCheckDetailFooter == null) {
			cntCheckDetailFooter = new JPanel();
			cntCheckDetailFooter.setLayout(new FlowLayout());
			cntCheckDetailFooter.add(getCmdApplyAll(), null);
		}
		return cntCheckDetailFooter;
	}

	/**
	 * This method initializes cmdApplyAll	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdApplyAll() {
		if (cmdApplyAll == null) {
			cmdApplyAll = new JButton();
		}
		return cmdApplyAll;
	}

	/**
	 * This method initializes cntSplitBody	
	 * 	
	 * @return javax.swing.JSplitPane	
	 */
	private JSplitPane getCntSplitBody() {
		if (cntSplitBody == null) {
			cntSplitBody = new JSplitPane();
//			cntSplitBody.setDividerSize(3);
			cntSplitBody.setOneTouchExpandable(true);
			cntSplitBody.setDividerLocation(150);
			cntSplitBody.setLeftComponent(getCntCheckList());
			cntSplitBody.setRightComponent(getCntCheckDetail());
		}
		return cntSplitBody;
	}

	/**
	 * This method initializes cmdUpdate	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdUpdate() {
		if (cmdUpdate == null) {
			cmdUpdate = new JButton();
			cmdUpdate.setText("U");
		}
		return cmdUpdate;
	}

	/**
	 * This method initializes cntButtons	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntButtons() {
		if (cntButtons == null) {
			cntButtons = new JPanel();
			cntButtons.setLayout(new FlowLayout(FlowLayout.CENTER, 0,0));
			cntButtons.add(getCmdNew(), null);
			cntButtons.add(getCmdUpdate(), null);
			cntButtons.add(getCmdRemove(), null);
		}
		return cntButtons;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
