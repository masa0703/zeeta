package jp.tokyo.selj.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;

import jp.tokyo.selj.common.AppException;
import jp.tokyo.selj.common.PreferenceWindowHelper;
import jp.tokyo.selj.dao.Output;
import jp.tokyo.selj.dao.SelJDaoContainer;
import jp.tokyo.selj.dao.WorkType;
import jp.tokyo.selj.model.MasterComboModel;
import jp.tokyo.selj.model.OutputListModel;
import jp.tokyo.selj.model.OutputListModel.OutputComparator;

import org.apache.log4j.Logger;
import org.seasar.framework.container.S2Container;

public class DlgOutputList extends JDialog implements DlgOutputDetail.CommitListener{
	Logger log = Logger.getLogger(this.getClass());
	protected Preferences prefs_ = Preferences.userNodeForPackage(this.getClass());

	private static final long serialVersionUID = 1L;

	private static final String SORT_KEY = Util.getClassName(DlgOutputList.class) +"/sort_key";
	private static final String ASC_DESC_KEY = Util.getClassName(DlgOutputList.class) +"/asc_desc_key";

	boolean enableActionLinkedDocTree_ = true;
	
	private JPanel jContentPane = null;

	private JScrollPane jScrollPane = null;

	private LstOutputs dspOutputList = null;

	private JPanel cntFooter = null;

	private JButton cmdSelect = null;
	
	PnlOutputDetail pnlOutputDetail = null;
	S2Container daoCont_ = SelJDaoContainer.SEL_DAO_CONT;  //  @jve:decl-index=0:
	OutputListModel model_ = null;
	FrmZeetaMain mainView_ = null;

	private JButton cmdNew = null;

	private JButton cmdDelete = null;

	private JButton cmdUpdate = null;

	private JPanel cntHeader = null;

	DlgOutputDetail outputDetail_ = null;
	
	PreferenceWindowHelper pref_;
	
	public static final int EVENT_SELECT = 10;
	
	private ActionMap actionMap_ = new ActionMap();  //  @jve:decl-index=0:
	class ActSelectOutput extends ActBase {
		protected Component getOwnerComponent(){
			return DlgOutputList.this;
		}
		public ActSelectOutput(ActionMap map){
			super(map);
			putValue(Action.NAME, "select");
			putValue(Action.SHORT_DESCRIPTION, "作業の結果生成される成果物を選択します");
		}
		public void actionPerformed2(ActionEvent e) {
			if(dspOutputList.getSelectedValue() == null){
				throw new AppException("成果物を選択してください");
			}
			ActionEvent ev = new SelectOutputEvent(DlgOutputList.this,
					EVENT_SELECT, 
					(Output)dspOutputList.getSelectedValue(),
					""
					);
			fireActionEvent(ev);
			setVisible(false);
		}
	}
	class ActSearchNameOrPathOrCreator extends ActBase {
		protected Component getOwnerComponent(){
			return DlgOutputList.this;
		}
		public ActSearchNameOrPathOrCreator(ActionMap map){
			super(map);
			putValue(Action.NAME, "search");
			putValue(Action.SHORT_DESCRIPTION, "nameまたはpathに含まれる文字で検索");
		}
		public void actionPerformed2(ActionEvent e) {
			String word = inpSearchText.getText();
			WorkType workType = (WorkType)inpSelectWorkType.getSelectedItem();
			if(word == null || "".equals(word.trim())){
				word = null;
				if(workType == null){
					throw new AppException("workTypeを選択するか、検索する文字を入れてください");
				}
			}
			if(word == null){
				model_.reloadOutputListByWorkType(workType.getWorkTypeId());
			}else{
				if(!canSelectBtnEnabled_){	//作業用の成果物を検索する場合ではないとき
					inpSelectWorkType.setSelectedItem(null);
					model_.reloadOutputListByNameOrPathOrCreator(word);
				}else{		//作業用の成果物を検索する場合
					WorkType wt = (WorkType)inpSelectWorkType.getSelectedItem();
					model_.reloadOutputListByWorkTypeAndNameOrPathOrCreator(wt, word);
				}
			}
			actionMap_.get(ActNewOutput.class).setEnabled(true);
		}
	}

	abstract class ActUseOutputDetail extends ActBase {
		protected Component getOwnerComponent(){
			return DlgOutputList.this;
		}
		public ActUseOutputDetail(ActionMap map){
			super(map);
			outputDetail_.setup((OutputListModel)getDspOutputList().getModel());
			outputDetail_.setLocationRelativeTo(DlgOutputList.this.getRootPane());	//一発目は、画面の中央に表示
		}
	}
	class ActNewOutput extends ActUseOutputDetail {
		public ActNewOutput(ActionMap map){
			super(map);
			putValue(Action.NAME, "new");
			putValue(Action.SHORT_DESCRIPTION, "成果物を追加します");
		}
		public void actionPerformed2(ActionEvent e) {
			outputDetail_.newOutput();
			if(getInpSelectWorkType().getSelectedItem() != null){
				WorkType workType = (WorkType)getInpSelectWorkType().getSelectedItem();
				outputDetail_.resetOutputTypeList(workType.getWorkTypeId());
			}else{
				throw new AppException("作業種類(workType)を選択してください");
			}
			outputDetail_.setVisible(true);
		}
	}
	class ActShowOutputUpdater extends ActUseOutputDetail {
		public ActShowOutputUpdater(ActionMap map){
			super(map);
			putValue(Action.NAME, "update");
			putValue(Action.SHORT_DESCRIPTION, "成果物を編集します");
		}
		public void actionPerformed2(ActionEvent e) {
			if(getDspOutputList().getSelectedValue() == null){
				throw new AppException("いずれかの成果物を選択してください");
			}
			Output output = (Output)getDspOutputList().getSelectedValue();
			outputDetail_.resetOutputTypeList();
			outputDetail_.loadOutput( output.getOutputId());
			outputDetail_.setVisible(true);
		}
	}
	class ActRemoveOutput extends ActTransactionBase {
		protected Component getOwnerComponent(){
			return DlgOutputList.this;
		}
		public ActRemoveOutput(ActionMap map){
			super(map);
			putValue(Action.NAME, "remove");
			putValue(Action.SHORT_DESCRIPTION, "成果物を削除します");
		}
		public void actionPerformed2(ActionEvent e) {
			if(dspOutputList.getSelectedValue() == null){
				throw new AppException("成果物を選択してください");
			}
			Output output = (Output)dspOutputList.getSelectedValue();
			if( JOptionPane.showConfirmDialog(
					getOwnerComponent()
					,output.getName() + " を削除します。\n" +
					"よろしいですか？",""
					,JOptionPane.YES_NO_OPTION
					,JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION){
				return;
			}
			model_.deleteOutput(output);
		}
	}
	//ActShowLinkedDocTreeを使うことにしたが、もったいないので残している
	class ActShowLinkedDocList extends ActBase {
		DlgLinkDocList dlgLinkDocList_ = null;
		protected Component getOwnerComponent(){
			return DlgOutputList.this;
		}
		public ActShowLinkedDocList(ActionMap map){
			super(map);
			putValue(Action.NAME, "Linked Node");
			putValue(Action.SHORT_DESCRIPTION, "成果物に関連するノード一覧を表示");
		}
		public void actionPerformed2(ActionEvent e) {
			if(dspOutputList.getSelectedValue() == null){
				return;
			}
			if(dlgLinkDocList_ == null){
				dlgLinkDocList_ = new DlgLinkDocList(DlgOutputList.this);
				dlgLinkDocList_.setup(mainView_);
				getDspOutputList().addListSelectionListener(dlgLinkDocList_);
				dlgLinkDocList_.setLocationRelativeTo(DlgOutputList.this);
			}
			dlgLinkDocList_.setVisible(true, (Output)dspOutputList.getSelectedValue());
		}
	}
	class ActShowLinkedDocTree extends ActBase {
		DlgLinkDocTree dlgLinkDocTree_ = null;
		protected Component getOwnerComponent(){
			return DlgOutputList.this;
		}
		public ActShowLinkedDocTree(ActionMap map){
			super(map);
			putValue(Action.NAME, "Linked Node");
			putValue(Action.SHORT_DESCRIPTION, "成果物に関連するノードツリーを表示");
		}
		public void actionPerformed2(ActionEvent e) {
			if(dspOutputList.getSelectedValue() == null){
				return;
			}
			if(dlgLinkDocTree_ == null){
//				dlgLinkDocTree_ = new DlgLinkDocTree(DlgOutputList.this);
				dlgLinkDocTree_ = new DlgLinkDocTree(mainView_);
				dlgLinkDocTree_.setup(mainView_);
				getDspOutputList().addListSelectionListener(dlgLinkDocTree_);
				dlgLinkDocTree_.setLocationRelativeTo(DlgOutputList.this);
			}
			dlgLinkDocTree_.setVisible(true, (Output)dspOutputList.getSelectedValue());
		}
	}
	class ActShowCheckList extends ActBase {
		DlgCheckList dlgCheckList_ = null;
		protected Component getOwnerComponent(){
			return DlgOutputList.this;
		}
		public ActShowCheckList(ActionMap map){
			super(map);
			putValue(Action.NAME, "check");
			putValue(Action.SHORT_DESCRIPTION, "成果物チェックリストを表示");
		}
		public void actionPerformed2(ActionEvent e) {
			if(dspOutputList.getSelectedValue() == null){
				throw new AppException("成果物を選択してください");
			}
			
			if(dlgCheckList_ == null){
				dlgCheckList_ = new DlgCheckList(DlgOutputList.this);
				dlgCheckList_.setup();
//				dlgCheckList_.setLocationRelativeTo(DlgOutputList.this);
				getDspOutputList().addListSelectionListener(dlgCheckList_);
			}
			Output output = (Output)dspOutputList.getSelectedValue();
			dlgCheckList_.setVisible(true, output);
		}
	}

	class ActShowReviewList extends ActBase {
		DlgReviewList dlgList_ = null;
		protected Component getOwnerComponent(){
			return DlgOutputList.this;
		}
		public ActShowReviewList(ActionMap map){
			super(map);
			putValue(Action.NAME, "review");
			putValue(Action.SHORT_DESCRIPTION, "成果物レビューリストを表示");
		}
		public void actionPerformed2(ActionEvent e) {
			if(dspOutputList.getSelectedValue() == null){
				throw new AppException("成果物を選択してください");
			}
			
			if(dlgList_ == null){
				dlgList_ = new DlgReviewList(DlgOutputList.this);
				dlgList_.setup();
//				dlgCheckList_.setLocationRelativeTo(DlgOutputList.this);
				getDspOutputList().addListSelectionListener(dlgList_);
			}
			Output output = (Output)dspOutputList.getSelectedValue();
			dlgList_.setVisible(true, output);
		}
	}
	

	public void setup(FrmZeetaMain mainView){
		mainView_ = mainView;
		model_ = new OutputListModel();
		getDspOutputList().setModel(model_);
		getDspOutputList().setup();

		outputDetail_ = new DlgOutputDetail(DlgOutputList.this, isModal());
		outputDetail_.addCommitOutputListener(this);
		
		//Action生成
		new ActSelectOutput(actionMap_).setEnabled(false);
		new ActNewOutput(actionMap_).setEnabled(false);
		new ActShowOutputUpdater(actionMap_).setEnabled(false);
		new ActRemoveOutput(actionMap_).setEnabled(false);
//		new ActShowLinkedDocList(actionMap_).setEnabled(false);
		new ActShowCheckList(actionMap_).setEnabled(false);
		new ActShowReviewList(actionMap_).setEnabled(false);
		new ActShowLinkedDocTree(actionMap_).setEnabled(false);
		new ActSearchNameOrPathOrCreator(actionMap_).setEnabled(true);
		
		//Actionをボタンへマップ
		getCmdSelect().setAction(actionMap_.get(ActSelectOutput.class));
		getCmdNew().setAction(actionMap_.get(ActNewOutput.class));
		getCmdDelete().setAction(actionMap_.get(ActRemoveOutput.class));
		getCmdUpdate().setAction(actionMap_.get(ActShowOutputUpdater.class));
		getCmdLinkedDocTree().setAction(actionMap_.get(ActShowLinkedDocTree.class));
		getCmdCheckList().setAction(actionMap_.get(ActShowCheckList.class));
		getCmdReviewList().setAction(actionMap_.get(ActShowReviewList.class));
		getCmdSearch().setAction(actionMap_.get(ActSearchNameOrPathOrCreator.class));
		
		//Actionポップアップメニューへマップ
		JPopupMenu menu = getDspOutputList().getPopupMenu();
		menu.add(actionMap_.get(ActSelectOutput.class));
		menu.add(actionMap_.get(ActNewOutput.class));
		menu.add(actionMap_.get(ActShowOutputUpdater.class));
		menu.add(actionMap_.get(ActRemoveOutput.class));
		menu.add(actionMap_.get(ActShowLinkedDocTree.class));
		menu.add(actionMap_.get(ActShowCheckList.class));
		menu.add(actionMap_.get(ActShowReviewList.class));
		
		getInpSelectWorkType().setModel(MasterComboModel.newWorkTypeComboBoxModel());
		getInpSelectWorkType().setSelectedItem(null);
	
		inpSearchText.getInputMap().
			put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0), "search");
		inpSearchText.getActionMap().put("search", actionMap_.get(ActSearchNameOrPathOrCreator.class));

		//windowsサイズ・位置を復元
		pref_ = new PreferenceWindowHelper(this);
		pref_.restoreForm();
		
		//ソート条件を復元
		String ky = prefs_.get(SORT_KEY, "ID");
		for(int i=0; i<getInpSelSortCol().getItemCount(); i++){
			Object item = getInpSelSortCol().getItemAt(i);
			if(ky.equals(item.toString())){
				getInpSelSortCol().setSelectedItem(item);
				break;
			}
		}
		String asc = prefs_.get(ASC_DESC_KEY, getInpSelDesc().getText());
		if(getInpSelAsc().getText().equals(asc)){
			getInpSelAsc().setSelected(true);
		}else{
			getInpSelDesc().setSelected(true);
		}
		sortList();	//現在のソート条件を記憶させるために一度は呼び出す

		//WindowListener登録
		this.addWindowListener(
			new WindowAdapter(){
				public void windowClosing(WindowEvent e) {
					prefs_.put(SORT_KEY, ""+getInpSelSortCol().getSelectedItem());
					if(getInpSelAsc().isSelected()){
						prefs_.put(ASC_DESC_KEY, getInpSelAsc().getText());
					}else {
						prefs_.put(ASC_DESC_KEY, getInpSelDesc().getText());
					}
				}
			}
		);
		
	}
	
	WorkType workType_ = null;  //  @jve:decl-index=0:
	public void makeList(WorkType workType){
		actionMap_.get(ActSelectOutput.class).setEnabled(true);
		canSelectBtnEnabled_ = true;
		model_.reloadOutputListByWorkType(workType.getWorkTypeId());
		workType_ = workType;
		getInpSelectWorkType().removeAllItems();
		getInpSelectWorkType().addItem(workType_);
		//Listのダブルクリックアクションを定義
		getDspOutputList().getActionMap().put(LstOutputs.DOUBLE_CLICK_ACTION_KEY, 
				actionMap_.get(ActSelectOutput.class));
	}
	boolean canSelectBtnEnabled_ = true;
	public void makeList(){
		workType_ = null;
//		getInpSelectWorkType().setModel(MasterComboModel.newWorkTypeComboBoxModel());
		//リストをリフレッシュ
		Object item = getInpSelectWorkType().getSelectedItem();
		getInpSelectWorkType().setSelectedItem(null);
		if(item != null){
			getInpSelectWorkType().setSelectedItem(item);
		}
		actionMap_.get(ActSelectOutput.class).setEnabled(false);
		canSelectBtnEnabled_ = false;
		//Listのダブルクリックアクションを定義
		getDspOutputList().getActionMap().put(LstOutputs.DOUBLE_CLICK_ACTION_KEY, 
				actionMap_.get(ActShowOutputUpdater.class));
	}
	public void setDisableActionLinkedDocTree(){
		actionMap_.get(ActShowLinkedDocTree.class).setEnabled(false);
		enableActionLinkedDocTree_ = false;
	}
	
	/**
	 * @param owner
	 */
	public DlgOutputList(Frame owner) {
		this(owner, true);
	}
	/**
	 * @param owner
	 */
	public DlgOutputList(Frame owner, boolean modal) {
		super(owner, modal);
//		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(600, 500);
		this.setTitle("output list");
		this.setContentPane(getJContentPane());
		//表示したときにどれかを選択状態にする
		this.addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentShown(java.awt.event.ComponentEvent e) {
				if(getDspOutputList().getModel().getSize() > 0){
					getDspOutputList().setSelectedIndex(0);
				}
			}
		});
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
			jContentPane.add(getJScrollPane(), BorderLayout.CENTER);
			jContentPane.add(getCntFooter(), BorderLayout.SOUTH);
			jContentPane.add(getCntHeader(), BorderLayout.NORTH);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getDspOutputList());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes dspOutputList	
	 * 	
	 * @return javax.swing.JList	
	 */
	private LstOutputs getDspOutputList() {
		if (dspOutputList == null) {
			dspOutputList = new LstOutputs();
			dspOutputList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			dspOutputList
					.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
						public void valueChanged(javax.swing.event.ListSelectionEvent e) {
							Output output = (Output)dspOutputList.getSelectedValue();
							boolean isSelected = output != null;
							
//							getCmdSelect().setEnabled(canSelectEabled_ & isSelected);
//							getCmdDelete().setEnabled(isSelected);
//							getCmdUpdate().setEnabled(isSelected);
							actionMap_.get(ActSelectOutput.class).setEnabled(canSelectBtnEnabled_ & isSelected);
							actionMap_.get(ActShowOutputUpdater.class).setEnabled(isSelected);
							actionMap_.get(ActRemoveOutput.class).setEnabled(isSelected);
							actionMap_.get(ActShowCheckList.class).setEnabled(isSelected);
							actionMap_.get(ActShowReviewList.class).setEnabled(isSelected);
							
							actionMap_.get(ActShowLinkedDocTree.class)
								.setEnabled(enableActionLinkedDocTree_ && isSelected && output.getWorkCount() > 0);
						}
					});
		}
		return dspOutputList;
	}

	/**
	 * This method initializes cntFooter	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntFooter() {
		if (cntFooter == null) {
			cntFooter = new JPanel();
			cntFooter.setLayout(new FlowLayout());
			cntFooter.add(getCmdNew(), null);
			cntFooter.add(getCmdDelete(), null);
			cntFooter.add(getCmdUpdate(), null);
			cntFooter.add(getCmdLinkedDocTree(), null);
			cntFooter.add(getCmdCheckList(), null);
			cntFooter.add(getCmdReviewList(), null);
			cntFooter.add(getCmdSelect(), null);
		}
		return cntFooter;
	}
	public class SelectOutputEvent extends ActionEvent {
		public Output output_;
		public String worker_;
		public SelectOutputEvent(Object source, int id, Output output, String worker) {
			super(source, id, "");
			output_ = output;
			worker_ = worker;
		}
	}

	/**
	 * This method initializes cmdSelect	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdSelect() {
		if (cmdSelect == null) {
			cmdSelect = new JButton();
			cmdSelect.setText("select");
			cmdSelect.setEnabled(false);
		}
		return cmdSelect;
	}
	void fireActionEvent(ActionEvent ev) {
		Iterator<ActionListener> it = listeners.iterator();
		while(it.hasNext()){
			it.next().actionPerformed(ev);
		}
	}
	/**
	 * This method initializes cmdNew	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdNew() {
		if (cmdNew == null) {
			cmdNew = new JButton();
			cmdNew.setText("new");
		}
		return cmdNew;
	}
	/**
	 * This method initializes cmdDelete	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdDelete() {
		if (cmdDelete == null) {
			cmdDelete = new JButton();
			cmdDelete.setText("delete");
			cmdDelete.setEnabled(false);
		}
		return cmdDelete;
	}
	/**
	 * This method initializes cmdUpdate	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdUpdate() {
		if (cmdUpdate == null) {
			cmdUpdate = new JButton();
			cmdUpdate.setText("update");
			cmdUpdate.setEnabled(false);
		}
		return cmdUpdate;
	}
	/**
	 * This method initializes cntHeader	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntHeader() {
		if (cntHeader == null) {
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.gridwidth = 5;
			gridBagConstraints11.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints11.gridy = 1;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints4.gridy = 0;
			gridBagConstraints4.gridx = 4;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints3.gridx = 3;
			gridBagConstraints3.gridy = 0;
			gridBagConstraints3.weightx = 1.0;
			gridBagConstraints3.insets = new Insets(5, 5, 5, 0);
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.insets = new Insets(5, 5, 5, 0);
			gridBagConstraints2.gridy = 0;
			gridBagConstraints2.gridx = 2;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints1.gridx = 1;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.weightx = 0.5;
			gridBagConstraints1.insets = new Insets(5, 5, 5, 0);
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new Insets(5, 5, 5, 0);
			gridBagConstraints.gridy = 0;
			gridBagConstraints.gridx = 0;
			dspSearchWord = new JLabel();
			dspSearchWord.setText("name / path / creator / memo");
			jLabel1 = new JLabel();
			jLabel1.setText("workType");
			cntHeader = new JPanel();
			cntHeader.setLayout(new GridBagLayout());
			cntHeader.add(jLabel1, gridBagConstraints);
			cntHeader.add(getInpSelectWorkType(), gridBagConstraints1);
			cntHeader.add(dspSearchWord, gridBagConstraints2);
			cntHeader.add(getInpSearchText(), gridBagConstraints3);
			cntHeader.add(getCmdSearch(), gridBagConstraints4);
			cntHeader.add(getCntSortType(), gridBagConstraints11);
		}
		return cntHeader;
	}
	Set<ActionListener> listeners = new HashSet<ActionListener>();  //  @jve:decl-index=0:

	private JComboBox inpSelectWorkType = null;

	private JLabel jLabel1 = null;

	private JButton cmdLinkedDocTree = null;

	private JButton cmdCheckList = null;

	private JButton cmdReviewList = null;

	private JTextField inpSearchText = null;

	private JLabel dspSearchWord = null;

	private JButton cmdSearch = null;

	private JPanel cntSortType = null;

	private JRadioButton inpSelAsc = null;

	private JComboBox inpSelSortCol = null;

	private JRadioButton inpSelDesc = null;

	private JLabel dspSort = null;
	public void addActionListener(ActionListener listener){
		listeners.add(listener);
	}
	public void removeActionListener(ActionListener listener){
		listeners.remove(listener);
	}
	/**
	 * This method initializes inpSelectOutputType	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getInpSelectWorkType() {
		if (inpSelectWorkType == null) {
			inpSelectWorkType = new JComboBox();
			inpSelectWorkType.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
//					if(e.getItem() == null ){		//以前の選択Itemが入っている
					if(inpSelectWorkType.getSelectedItem() == null){
						model_.removeAllElements();
						actionMap_.get(ActNewOutput.class).setEnabled(false);
						return;
					}
					log.debug("getSelectedItem="+inpSelectWorkType.getSelectedItem());
					getInpSearchText().setText("");
					WorkType workType = (WorkType)e.getItem();
					model_.reloadOutputListByWorkType(workType.getWorkTypeId());
					actionMap_.get(ActNewOutput.class).setEnabled(true);
				}
			});
		}
		return inpSelectWorkType;
	}

	/**
	 * This method initializes cmdLinkedDocTree	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdLinkedDocTree() {
		if (cmdLinkedDocTree == null) {
			cmdLinkedDocTree = new JButton();
			cmdLinkedDocTree.setEnabled(false);
			cmdLinkedDocTree.setText("LinkedDoc");
		}
		return cmdLinkedDocTree;
	}

	/**
	 * This method initializes cmdCheckList	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdCheckList() {
		if (cmdCheckList == null) {
			cmdCheckList = new JButton();
			cmdCheckList.setText("check");
		}
		return cmdCheckList;
	}
	/**
	 * This method initializes cmdReviewList	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdReviewList() {
		if (cmdReviewList == null) {
			cmdReviewList = new JButton();
			cmdReviewList.setText("review");
		}
		return cmdReviewList;
	}
	/**
	 * This method initializes inpSearchText	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getInpSearchText() {
		if (inpSearchText == null) {
			inpSearchText = new JTextField();
			inpSearchText.setPreferredSize(new Dimension(160, 20));
		}
		return inpSearchText;
	}
	/**
	 * This method initializes cmdSearch	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdSearch() {
		if (cmdSearch == null) {
			cmdSearch = new JButton();
		}
		return cmdSearch;
	}
	public void commitOutput(Output output) {
		if(output != null){
			getDspOutputList().setSelectedValue(output, true);
		}
	}
	/**
	 * This method initializes cntSortType	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntSortType() {
		if (cntSortType == null) {
			FlowLayout flowLayout = new FlowLayout();
			flowLayout.setAlignment(java.awt.FlowLayout.LEFT);
			flowLayout.setVgap(1);
			dspSort = new JLabel();
			dspSort.setText("order by ");
			cntSortType = new JPanel();
			cntSortType.setLayout(flowLayout);
			cntSortType.add(dspSort, null);
			cntSortType.add(getInpSelSortCol());
			cntSortType.add(getInpSelAsc());
			cntSortType.add(getInpSelDesc(), null);
			
			ButtonGroup group = new ButtonGroup();
			group.add(getInpSelAsc());
			group.add(getInpSelDesc());
			
			ActionListener acl = new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					sortList();
//					if(e.getSource() == getInpSelAsc()){
//						model_.sort((OutputComparator)inpSelSortCol.getSelectedItem(), 
//								SortOrder.ASC);
//					}else{
//						model_.sort((OutputComparator)inpSelSortCol.getSelectedItem(), 
//								SortOrder.DESC);
//					}
				}
				
			};
			getInpSelAsc().addActionListener(acl);
			getInpSelDesc().addActionListener(acl);
		}
		return cntSortType;
	}
	/**
	 * This method initializes inpSelAsc	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getInpSelAsc() {
		if (inpSelAsc == null) {
			inpSelAsc = new JRadioButton();
			inpSelAsc.setText("ASC");
			inpSelAsc.setSelected(true);
			inpSelAsc.setToolTipText("昇順");
		}
		return inpSelAsc;
	}
	/**
	 * This method initializes inpSelSortCol	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getInpSelSortCol() {
		if (inpSelSortCol == null) {
			inpSelSortCol = new JComboBox();
			inpSelSortCol.addItem(OutputListModel.ID_COMPARATOR);
			inpSelSortCol.addItem(OutputListModel.TYPE_COMPARATOR);
			inpSelSortCol.addItem(OutputListModel.NAME_COMPARATOR);
			inpSelSortCol.addItem(OutputListModel.CREATOR_COMPARATOR);
			inpSelSortCol.addItem(OutputListModel.PATH_COMPARATOR);
			inpSelSortCol.addItem(OutputListModel.DATE_COMPARATOR);
			inpSelSortCol.setToolTipText("表示順のキーになる項目を選択してください");
			inpSelSortCol.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					sortList();
				}
			});

		}
		return inpSelSortCol;
	}
	void sortList(){
		OutputComparator cmp = (OutputComparator)inpSelSortCol.getSelectedItem();
		if(cmp == null){
			return;
		}
		model_.sort(cmp, 
				inpSelAsc.isSelected()? OutputListModel.SortOrder.ASC: OutputListModel.SortOrder.DESC);
		
	}
	/**
	 * This method initializes inpSelDesc	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getInpSelDesc() {
		if (inpSelDesc == null) {
			inpSelDesc = new JRadioButton();
			inpSelDesc.setText("DESC");
			inpSelDesc.setToolTipText("降順");
		}
		return inpSelDesc;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
