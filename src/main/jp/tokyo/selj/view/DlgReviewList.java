package jp.tokyo.selj.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.Keymap;

import jp.tokyo.selj.common.AppException;
import jp.tokyo.selj.common.DateTextFormatter;
import jp.tokyo.selj.common.DocumentUpdateListener;
import jp.tokyo.selj.common.PreferenceWindowHelper;
import jp.tokyo.selj.common.TextUndoHandler;
import jp.tokyo.selj.dao.Output;
import jp.tokyo.selj.dao.Review;
import jp.tokyo.selj.dao.ReviewDao;
import jp.tokyo.selj.dao.ReviewDetail;
import jp.tokyo.selj.dao.ReviewDetailDao;
import jp.tokyo.selj.dao.ReviewStateType;
import jp.tokyo.selj.dao.SelJDaoContainer;
import jp.tokyo.selj.model.MasterComboModel;

import org.apache.log4j.Logger;
import org.seasar.framework.container.S2Container;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;

public class DlgReviewList extends JDialog implements ListSelectionListener{

	Logger log = Logger.getLogger(this.getClass());
	private static final long serialVersionUID = 1L;
	PreferenceWindowHelper pref_;
	S2Container daoCont_ = SelJDaoContainer.SEL_DAO_CONT;
	static final String DEVIDER_LOC_KEY1 = Util.getClassName(DlgReviewList.class) + "/devider_loc1";
	static final String DEVIDER_LOC_KEY2 = Util.getClassName(DlgReviewList.class) + "/devider_loc2";
	
    protected TextUndoHandler memoUndoHandler_ = new TextUndoHandler();

	//Viewの状態を管理する
	enum DetailViewStateType {NEUTRAL, CREATING, UPDATING, DELETING}; 
	class DetailViewState implements ListSelectionListener{
		ReviewDetail curDetail_ = null;
		DetailViewStateType state_ = DetailViewStateType.NEUTRAL;
		boolean setting_ = false;
		boolean ignoreChangeValue_ = false;
		
		DocumentUpdateListener docListener_ = new DocumentUpdateListener(){
			protected void setDarty(){
				if(setting_){
					return;
				}
log.debug("setDarty() "+setting_);

				//このIF文は、以下のような不可解な現象に対応するための苦肉の策である。
				//ctrl+s, ctrl+enterでcommitを動作させることにした際、commitアクション
				//動作後にJFormattedTextFieldから何故か更新イベントが発生し、
				//DartyフラグがOnになってしまう。
				if(!isCreating()){
					if(getDspDetailList().getSelectedRow() < 0){
						//更新時に指摘事項リストが何も選択されていない状態は有り得ない
						return;
					}
				}

				super.setDarty();
				if(!isCreating()){
					getCmdCommitDetail().getAction().setEnabled(true);
					getCmdCancelNewDetail().setEnabled(true);
					getCmdNewDetail().setEnabled(false);
				}
			}
		};
		public void setUpdating(){
			ignoreChangeValue_ = false;
			state_ = DetailViewStateType.UPDATING;
			getCmdRemoveDetail().getAction().setEnabled(true);

//			getDspHeaderList().setEnabled(false);
//			getDspDetailList().setEnabled(false);

//			getCmdCommitDetail().getAction().setEnabled(true);
//			getCmdNewDetail().setEnabled(false);
//			getCmdCancelNewDetail().setEnabled(true);
			
			setEnabledInputs(true);
		}
		
		public ReviewDetail getCurrentReviewDetail(){
			return curDetail_;
		}
		
		public void setNeutral(){
			ignoreChangeValue_ = false;
			state_ = DetailViewStateType.NEUTRAL;

//			getDspHeaderList().setEnabled(true);
//			getDspDetailList().setEnabled(true);

			getCmdNewDetail().setEnabled(true);
			getCmdCommitDetail().getAction().setEnabled(false);
			getCmdCancelNewDetail().setEnabled(false);
			getCmdRemoveDetail().getAction().setEnabled(false);
			setEnabledInputs(false);
			
			docListener_.reset();
			curDetail_ = null;
//			valueChanged(null);
		}
		void setEnabledInputs(boolean val){
			getInpMemo().setEnabled(val);
			getInpReviewer().setEnabled(val);
			getInpState().setEnabled(val);
			getInpUpdDate().setEnabled(val);
		}
		public void setCreating(boolean val){
			ignoreChangeValue_ = false;
//			getDspHeaderList().setEnabled(!val);
//			getDspDetailList().setEnabled(!val);
			
			getCmdCommitDetail().getAction().setEnabled(val);
			getCmdCancelNewDetail().setEnabled(val);
			getCmdRemoveDetail().getAction().setEnabled(!val);
			getCmdNewDetail().setEnabled(!val);

			if(val){
				ReviewDetail detail = new ReviewDetail(reviewDetailListModel_.lastSelectedReview_);
				detail.setUpdUserName(	//コメンテーターの初期値
					reviewDetailListModel_.lastSelectedReview_.getReviewer1()
				);
				detail.setSeq(reviewDetailListModel_.getNextSEQ());
				detail.setReviewStateType(MasterComboModel.DEFAULT_REVIEW_STATE);
				setReviewDetail(detail);
			}
			if(val){
				state_ = DetailViewStateType.CREATING;
			}else{
				setNeutral();
			}
			setEnabledInputs(true);
		}
		public void setReviewDetail(ReviewDetail detail){
			setting_ = true;
			curDetail_ = detail;
			getInpMemo().setText(detail.getMemo());
			getInpReviewer().getModel().setSelectedItem(detail.getUpdUserName());
			getInpState().getModel().setSelectedItem(detail.getReviewStateType());
			getInpUpdDate().setValue(detail.getUpdDate());
			
	    	memoUndoHandler_.cleanup();
			docListener_.reset();
			setting_ = false;
		}
		public ReviewDetail getReviewDetail(){
			curDetail_.setMemo(inpMemo.getText());
			//JFormattedTextFieldは、実際にロストフォーカスしないと値が取り込まれない
			//もしくは、以下のようにcommitEditを呼び出す必要がある。
			try {
				inpUpdDate.commitEdit();
			} catch (ParseException e1) {
				//無視
			}
			curDetail_.setUpdDate((Timestamp)inpUpdDate.getValue());
			curDetail_.setUpdUserName(
					(inpReviewer.getModel().getSelectedItem()==null)? null:
//						((User)inpUser.getModel().getSelectedItem()).getUserName()
						""+inpReviewer.getModel().getSelectedItem()
			);
			curDetail_.setReviewStateType((ReviewStateType)inpState.getModel().getSelectedItem());
			
			return curDetail_;
		}

		public boolean isCreating() {
			return state_ == DetailViewStateType.CREATING;
		}
		public void valueChanged(ListSelectionEvent e) {
			if(ignoreChangeValue_){
				return;
			}
			JTable table = getDspDetailList();
			ReviewDetail detail = null;
			if(table.getSelectedRow() >= 0){
				detail = reviewDetailListModel_.details_.get(table.getSelectedRow());
			}
			if(curDetail_ != null && curDetail_.equals(detail)){
				return;
			}
			updateIfDarty();
			
			if(detail == null){
				setNeutral();
				return;
			}
			
			setReviewDetail(detail);
			setUpdating();
			log.debug("valueChanged() curDetail_="+curDetail_);
		}
		public void updateIfDarty(){
			if(docListener_.isDarty()){
				log.debug("汚れているわ. ");
				// 更新せなあかん
				if( JOptionPane.showConfirmDialog(
						DlgReviewList.this
						,"内容が変更されています。\n" +
						"更新を反映しますか？",""
						,JOptionPane.YES_NO_OPTION
						,JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
					getCmdCommitDetail().getAction().actionPerformed(null);
				}else{
					setNeutral();
				}
			}
		}

		public void setIgnoreChangeValue() {
			ignoreChangeValue_ = true;
		}

	}
	DetailViewState viewState_ = new DetailViewState();  //  @jve:decl-index=0:
	
	private JPanel jContentPane = null;

	private JPanel cntHeader = null;

	PnlOutput pnlOutput_ = null;

	private JList dspHeaderList = null;

	private JScrollPane dspDetailListScr = null;

	private JTable dspDetailList = null;

	private JPanel cntHeaderList = null;

	private JButton cmdNewReview = null;

	private JButton cmdRemoveReview = null;

	private JScrollPane cntHeaderListScr = null;
	
	Output lastSelectedOutput_ = null;
	ReviewDao headerDao_ = null;
	ReviewDetailDao detailDao_ = null;

	class ActShowNewReview extends ActBase {
		DlgNewReview dlgNewReview_ = null;
		protected Component getOwnerComponent(){
			return DlgReviewList.this;
		}
		public ActShowNewReview(){
			super();
			putValue(Action.NAME, "C");
			putValue(Action.SHORT_DESCRIPTION, "新規のレビューを作成する");
		}
		public void actionPerformed2(ActionEvent e) {
			if(dlgNewReview_ == null){
				dlgNewReview_ = new DlgNewReview(DlgReviewList.this);
				dlgNewReview_.setLocationRelativeTo(DlgReviewList.this);
				dlgNewReview_.setup(DlgReviewList.this);
			}
			dlgNewReview_.setVisible(true, lastSelectedOutput_);
		}
	}
	class ActShowUpdateReview extends ActBase {
		DlgNewReview dlgNewReview_ = null;
		protected Component getOwnerComponent(){
			return DlgReviewList.this;
		}
		public ActShowUpdateReview(){
			super();
			putValue(Action.NAME, "U");
			putValue(Action.SHORT_DESCRIPTION, "レビューを更新する");
		}
		public void actionPerformed2(ActionEvent e) {
			Review header = (Review)getDspHeaderList().getSelectedValue();
			if(header == null){
				throw new AppException("いずれかのレビューを選択してください");
			}
			if(dlgNewReview_ == null){
				dlgNewReview_ = new DlgNewReview(DlgReviewList.this);
				dlgNewReview_.setLocationRelativeTo(DlgReviewList.this);
				dlgNewReview_.setup(DlgReviewList.this);
			}
			dlgNewReview_.setVisible(true, lastSelectedOutput_, header);
		}
	}
	class ActRemoveReview extends ActTransactionBase {
		protected Component getOwnerComponent(){
			return DlgReviewList.this;
		}
		public ActRemoveReview(){
			super();
			putValue(Action.NAME, "D");
			putValue(Action.SHORT_DESCRIPTION, "選択したレビューを削除します");
		}
		public void actionPerformed2(ActionEvent e) {
			Review header = (Review)getDspHeaderList().getSelectedValue();
			if(header == null){
				throw new AppException("いずれかのレビューを選択してください");
			}
			if( JOptionPane.showConfirmDialog(
					DlgReviewList.this
					,"選択したレビューを削除します。\n" +
					"よろしいですか？",""
					,JOptionPane.YES_NO_OPTION
					,JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
				headerDao_.remove(header);
				refreshReviewList();
			}
			
		}
	}
	class ActCommitDetail extends ActTransactionBase {
		protected Component getOwnerComponent(){
			return DlgReviewList.this;
		}
		public ActCommitDetail(){
			super();
			setEnabled(false);
			putValue(Action.NAME, "commit");
			putValue(Action.SHORT_DESCRIPTION, "チェック結果を書き込みます");
		}
		public void actionPerformed2(ActionEvent e) {
log.debug("actionPerformed2() start");
			ReviewDetail detail = viewState_.getReviewDetail();
			detail.check();
			
			if(viewState_.isCreating()){
				//新規登録の場合
				detailDao_.insert(detail);
			}else{
				//updateの場合
				detailDao_.update(detail);
			}
			viewState_.setIgnoreChangeValue();
			//detailListをリフレッシュ
			reviewDetailListModel_.refreshDetailList();

			viewState_.setNeutral();
		}
	}
	class ActRemoveDetail extends ActTransactionBase {
		protected Component getOwnerComponent(){
			return DlgReviewList.this;
		}
		public ActRemoveDetail(){
			super();
			setEnabled(false);
			putValue(Action.NAME, "remove");
			putValue(Action.SHORT_DESCRIPTION, "指摘事項を削除します");
		}
		public void actionPerformed2(ActionEvent e) {
			if(getDspDetailList().getSelectedRow() < 0){
				throw new AppException("いずれかの指摘事項を選択してください");
			}
			if( JOptionPane.showConfirmDialog(
					DlgReviewList.this
					,"選択した指摘事項を削除します。\n" +
					"よろしいですか？",""
					,JOptionPane.YES_NO_OPTION
					,JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){

				detailDao_.remove(viewState_.curDetail_);
				//detailListをリフレッシュ
				reviewDetailListModel_.refreshDetailList();

				viewState_.setNeutral();
			}
			
		}
	}

	//レビューリストテーブルモデル
	class ReviewDetailListModel extends AbstractTableModel implements ListSelectionListener{
		String[] columnNames_ = {"comment", "state"};
		List<ReviewDetail> details_ = Collections.EMPTY_LIST;
		Review lastSelectedReview_ = null;
		boolean isDarty_ = false;
		
		void setDarty(boolean b){
			isDarty_ = b;
			getCmdCommitDetail().setEnabled(b);
		}
		
		@Override
		public String getColumnName(int column) {
			return this.columnNames_[column];
		}
		public int getColumnCount() {
			return columnNames_.length;
		}
		public int getRowCount() {
			return details_.size();
		}
		public Object getValueAt(int rowIndex, int columnIndex) {
			Object ret = null;
			ReviewDetail detail = details_.get(rowIndex);
			switch(columnIndex){
			case 0:
				ret = detail.getMemo();
				break;
			case 1:
				ret = detail.getReviewStateType().getReviewStateTypeName();
				break;
			}
			return ret;
		}
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			log.debug("aValue="+aValue);
		}
		public void valueChanged(ListSelectionEvent e) {
			Review selected = (Review)getDspHeaderList().getSelectedValue();
			if(lastSelectedReview_ == selected ){
				//2回呼び出される無駄を排除
				return;
			}
			lastSelectedReview_ = selected;
			setDarty(false);

			refreshDetailList();
		}
		public void refreshDetailList(){
			if(lastSelectedReview_ == null){
				details_ = Collections.EMPTY_LIST;
			}else{
				details_ = detailDao_.findByOutputIdDate(
							lastSelectedReview_.getOutputId(), 
							lastSelectedReview_.getReviewDate());
			}
			fireTableDataChanged();
		}
		public int getNextSEQ(){
			int ret = 0;
			for(int i=0; i<details_.size(); i++){
				if(ret < details_.get(i).getSeq()){
					ret = details_.get(i).getSeq();
				}
			}
			return ret + 1;
		}
		
		public final boolean isCellEditable(int row,int column){
			return false;
		}
	}
	ReviewDetailListModel reviewDetailListModel_ = null;
	private JPanel cntDetail = null;
	private JPanel cntDetailFooter = null;
	private JButton cmdCommitDetail = null;
	private JButton cmdRemoveDetail = null;
	private JSplitPane cntSplitBody = null;
	private JSplitPane jSplitPane = null;
	private JTextArea inpMemo = null;
	private JFormattedTextField inpUpdDate = null;
	private JPanel cntReviewDetail2 = null;
	private JComboBox inpState = null;
	private JButton cmdNewDetail = null;
	private JButton cmdCancelNewDetail = null;
	private JComboBox inpReviewer = null;
	private JPanel cntReviewDetail = null;
	private JButton cmdUpdateReview = null;
	private JPanel cntEdit = null;
	private JPanel cntBtns = null;
	private JPanel cntButtons = null;
	private JScrollPane cntScrMemo = null;
	
	
	class ReviewListModel extends AbstractListModel{
		List<Review> reviewList_=null;
		ReviewListModel(List<Review> reviewList){
			reviewList_ = reviewList;
		}
		
		public Object getElementAt(int index) {
			return reviewList_.get(index);
		}
		public int getSize() {
			return reviewList_.size();
		}
	}
	
	public void valueChanged(ListSelectionEvent e) {
		//選択されたitem
		JList list = (JList)e.getSource();
		if( !isShowing() || list.getSelectedValue() == null){
			return;
		}
		if(list.getSelectedValue() == lastSelectedOutput_){
			//なぜか同じイベントが２つ連続するため
			return;
		}
		log.debug("selected="+list.getSelectedValue());
		lastSelectedOutput_ = (Output)list.getSelectedValue();
		refreshReviewList();
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
		lastSelectedOutput_ = output;
		refreshReviewList();
		super.setVisible(b);
	}
	void refreshReviewList(){
		pnlOutput_.setOutput(lastSelectedOutput_);
		
		getCmdRemoveReview().setEnabled(false);
		getCmdUpdateReview().setEnabled(false);
		//lastSelected_に関連するReview一覧をReviewListにセット
		List<Review> reviews = headerDao_.findByOutputId(lastSelectedOutput_.getOutputId());
		
		getDspHeaderList().setModel(new ReviewListModel(reviews));
		if(reviews.size() > 0){
			getDspHeaderList().setSelectedIndex(0);
		}
	}

	public void setup(){
		//ボタンにアクション
		getCmdNewReview().setAction(new ActShowNewReview());
		getCmdUpdateReview().setAction(new ActShowUpdateReview());
		getCmdRemoveReview().setAction(new ActRemoveReview());
		getCmdCommitDetail().setAction(new ActCommitDetail());
		getCmdRemoveDetail().setAction(new ActRemoveDetail());
		
		//Actionポップアップメニューへマップ
		JPopupMenu menu = ((LstReviews)getDspHeaderList()).getPopupMenu();
		menu.add(getCmdNewReview().getAction());
		menu.add(getCmdUpdateReview().getAction());
		menu.add(getCmdRemoveReview().getAction());
		
		((LstReviews)getDspHeaderList()).getActionMap().put(
				LstReviews.DOUBLE_CLICK_ACTION_KEY, 
				getCmdUpdateReview().getAction());
		
		pnlOutput_ = new PnlOutput();
		pnlOutput_.setOpaque(false);
		cntHeader.add(pnlOutput_, BorderLayout.CENTER);
		
		headerDao_ = (ReviewDao) daoCont_.getComponent(ReviewDao.class);
		detailDao_ = (ReviewDetailDao) daoCont_.getComponent(ReviewDetailDao.class);

		reviewDetailListModel_ = new ReviewDetailListModel();
		dspHeaderList.addListSelectionListener(reviewDetailListModel_);
		dspHeaderList.addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e) {
				getCmdRemoveReview().setEnabled(getDspHeaderList().getSelectedValue() != null);
				getCmdUpdateReview().setEnabled(getDspHeaderList().getSelectedValue() != null);
			}
		});
		dspDetailList.setModel(reviewDetailListModel_);
		
		//tableの列幅設定
//		dspDetailList.getColumn("SEQ").setMaxWidth(100);
		dspDetailList.getColumn("state").setMaxWidth(100);
		
		//レビュー状態combo
		getInpState().setModel(MasterComboModel.newRevieStateTypeComboBoxModel());
		getInpState().setRenderer(
				new DefaultListCellRenderer(){
					public Component getListCellRendererComponent(JList list,Object value, 
							int index, boolean isSelected, boolean cellHasFocus){
						super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
						ReviewStateType val = (ReviewStateType)value;
						setText(val.getReviewStateTypeName());
						return this;
					}
				}
			);
		//指摘者combo
		getInpReviewer().setModel(MasterComboModel.newUserComboBoxModel());
		getInpReviewer().setSelectedItem(null);
		
		//detailListにリスナー登録
		ListSelectionModel selModel = getDspDetailList().getSelectionModel();
		selModel.addListSelectionListener(viewState_);
		
		//detail入力部の更新状態を監視する
		getInpMemo().getDocument().addDocumentListener(viewState_.docListener_);
		getInpUpdDate().getDocument().addDocumentListener(viewState_.docListener_);
		getInpState().getModel().addListDataListener(viewState_.docListener_);
		getInpReviewer().getModel().addListDataListener(viewState_.docListener_);
		viewState_.docListener_.reset();
		
		//inpMemo上のctrl+s, ctrl+enterをcommitに紐付ける
		Keymap keyMap = inpMemo.getKeymap();
		keyMap.addActionForKeyStroke(
				KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK)
				, getCmdCommitDetail().getAction());
		keyMap.addActionForKeyStroke(
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_MASK)
				, getCmdCommitDetail().getAction());
		
		
		//WindowListener登録
		this.addWindowListener(
			new WindowAdapter(){
				public void windowClosing(WindowEvent e) {
					viewState_.updateIfDarty();
					pref_.getPriference().put(DEVIDER_LOC_KEY1, ""+getCntSplitBody().getDividerLocation());
					pref_.getPriference().put(DEVIDER_LOC_KEY2, ""+getJSplitPane().getDividerLocation());
				}
				
			}
		);

		viewState_.setNeutral();
		
		pref_ = new PreferenceWindowHelper(this);
		pref_.restoreForm();
		
		//スプリットパネルの状態を復元
		getCntSplitBody().setDividerLocation(
			Integer.parseInt(
				pref_.getPriference().get(DEVIDER_LOC_KEY1, 
						""+getCntSplitBody().getDividerLocation())
			)
		);
		getJSplitPane().setDividerLocation(
			Integer.parseInt(
				pref_.getPriference().get(DEVIDER_LOC_KEY2, 
					""+getJSplitPane().getDividerLocation())
			)
		);

	}
	/**
	 * @param owner
	 */
	public DlgReviewList(Dialog owner) {
		this(owner, false);
	}
	/**
	 * @param owner
	 */
	public DlgReviewList(Dialog owner, boolean modal) {
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
		this.setTitle("review list");
		this.setContentPane(getJContentPane());
		
		memoUndoHandler_.setup(getInpMemo());

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
	 * This method initializes dspHeaderList	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getDspHeaderList() {
		if (dspHeaderList == null) {
			dspHeaderList = new LstReviews();
			dspHeaderList.setPreferredSize(new Dimension(250, 0));
			dspHeaderList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			dspHeaderList.setFont(new Font("Dialog", Font.PLAIN, 12));
		}
		return dspHeaderList;
	}
	/**
	 * This method initializes dspDetailListScr	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getDspDetailListScr() {
		if (dspDetailListScr == null) {
			dspDetailListScr = new JScrollPane();
			dspDetailListScr.setViewportView(getDspDetailList());
		}
		return dspDetailListScr;
	}
	/**
	 * This method initializes dspDetailList	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getDspDetailList() {
		if (dspDetailList == null) {
			dspDetailList = new JTable();
			dspDetailList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		return dspDetailList;
	}
	/**
	 * This method initializes cntHeaderList	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntHeaderList() {
		if (cntHeaderList == null) {
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.gridx = 0;
			gridBagConstraints21.gridy = 0;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.fill = GridBagConstraints.BOTH;
			gridBagConstraints11.weighty = 1.0;
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.gridy = 1;
			gridBagConstraints11.gridwidth = 1;
			gridBagConstraints11.weightx = 1.0;
			cntHeaderList = new JPanel();
			cntHeaderList.setLayout(new GridBagLayout());
			cntHeaderList.setPreferredSize(new Dimension(150, 0));
			cntHeaderList.add(getCntHeaderListScr(), gridBagConstraints11);
			cntHeaderList.add(getCntButtons(), gridBagConstraints21);
		}
		return cntHeaderList;
	}
	/**
	 * This method initializes cmdNew	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdNewReview() {
		if (cmdNewReview == null) {
			cmdNewReview = new JButton();
			cmdNewReview.setText("C");
		}
		return cmdNewReview;
	}
	/**
	 * This method initializes cmdRemove	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdRemoveReview() {
		if (cmdRemoveReview == null) {
			cmdRemoveReview = new JButton();
			cmdRemoveReview.setText("U");
		}
		return cmdRemoveReview;
	}
	/**
	 * This method initializes cntHeaderListScr	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getCntHeaderListScr() {
		if (cntHeaderListScr == null) {
			cntHeaderListScr = new JScrollPane();
			cntHeaderListScr.setViewportView(getDspHeaderList());
		}
		return cntHeaderListScr;
	}

	/**
	 * This method initializes cntDetail	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntDetail() {
		if (cntDetail == null) {
			cntDetail = new JPanel();
			cntDetail.setLayout(new BorderLayout());
			cntDetail.add(getJSplitPane(), BorderLayout.CENTER);
		}
		return cntDetail;
	}

	/**
	 * This method initializes cntDetailFooter	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntDetailFooter() {
		if (cntDetailFooter == null) {
			cntDetailFooter = new JPanel();
			cntDetailFooter.setLayout(new BorderLayout());
			cntDetailFooter.add(getCntReviewDetail(), BorderLayout.CENTER);
		}
		return cntDetailFooter;
	}

	/**
	 * This method initializes cmdApplyAll	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdCommitDetail() {
		if (cmdCommitDetail == null) {
			cmdCommitDetail = new JButton();
			cmdCommitDetail.setText("commit");
		}
		return cmdCommitDetail;
	}
	private JButton getCmdRemoveDetail() {
		if (cmdRemoveDetail == null) {
			cmdRemoveDetail = new JButton();
			cmdRemoveDetail.setText("remove");
		}
		return cmdRemoveDetail;
	}

	/**
	 * This method initializes cntSplitBody	
	 * 	
	 * @return javax.swing.JSplitPane	
	 */
	private JSplitPane getCntSplitBody() {
		if (cntSplitBody == null) {
			cntSplitBody = new JSplitPane();
			cntSplitBody.setDividerSize(10);
			cntSplitBody.setOneTouchExpandable(true);
			cntSplitBody.setLeftComponent(getCntHeaderList());
			cntSplitBody.setRightComponent(getCntDetail());
		}
		return cntSplitBody;
	}

	/**
	 * This method initializes jSplitPane	
	 * 	
	 * @return javax.swing.JSplitPane	
	 */
	private JSplitPane getJSplitPane() {
		if (jSplitPane == null) {
			jSplitPane = new JSplitPane();
			jSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			jSplitPane.setDividerLocation(140);
			jSplitPane.setResizeWeight(0.5D);
			jSplitPane.setOneTouchExpandable(true);
			jSplitPane.setContinuousLayout(true);
			jSplitPane.setTopComponent(getDspDetailListScr());
			jSplitPane.setBottomComponent(getCntDetailFooter());
		}
		return jSplitPane;
	}

	/**
	 * This method initializes inpMemo	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JTextArea getInpMemo() {
		if (inpMemo == null) {
			inpMemo = new JTextArea();
//			inpMemo.setBorder(new TitledBorder("comment"));
			inpMemo.setToolTipText("ctrl+s, ctrl+enterでcommit");
		}
		return inpMemo;
	}

	/**
	 * This method initializes inpUpdDate	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JFormattedTextField getInpUpdDate() {
		if (inpUpdDate == null) {
			inpUpdDate = new JFormattedTextField(new DateTextFormatter());
			inpUpdDate.setMaximumSize(new Dimension(130, 50));
			inpUpdDate.setBorder(new TitledBorder("update"));
		}
		return inpUpdDate;
	}

	/**
	 * This method initializes cntReviewDetail2	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntReviewDetail2() {
		if (cntReviewDetail2 == null) {
			cntReviewDetail2 = new JPanel();
//			cntReviewDetail2.setMinimumSize(new Dimension(400, 50));
			cntReviewDetail2.setLayout(new BoxLayout(getCntReviewDetail2(), BoxLayout.Y_AXIS));
			cntReviewDetail2.add(getCntEdit(), null);
			cntReviewDetail2.add(getCntBtns(), null);
		}
		return cntReviewDetail2;
	}

	/**
	 * This method initializes inpState	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getInpState() {
		if (inpState == null) {
			inpState = new JComboBox();
			inpState.setBorder(new TitledBorder("state"));
		}
		return inpState;
	}

	/**
	 * This method initializes cmdNewDetail	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdNewDetail() {
		if (cmdNewDetail == null) {
			cmdNewDetail = new JButton();
			cmdNewDetail.setText("new");
			cmdNewDetail.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					viewState_.setCreating(true);
				}
			});
		}
		return cmdNewDetail;
	}
	private JButton getCmdCancelNewDetail() {
		if (cmdCancelNewDetail == null) {
			cmdCancelNewDetail = new JButton();
			cmdCancelNewDetail.setText("cancel");
			cmdCancelNewDetail.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					viewState_.setNeutral();
				}
			});
		}
		return cmdCancelNewDetail;
	}

	/**
	 * This method initializes inpReviewer	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getInpReviewer() {
		if (inpReviewer == null) {
			inpReviewer = new JComboBox();
			inpReviewer.setEditable(true);
			inpReviewer.setBorder(new TitledBorder("commentator"));
		}
		return inpReviewer;
	}

	/**
	 * This method initializes cntReviewDetail	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntReviewDetail() {
		if (cntReviewDetail == null) {
			cntReviewDetail = new JPanel();
			cntReviewDetail.setLayout(new BorderLayout());
			cntReviewDetail.add(getCntReviewDetail2(), BorderLayout.SOUTH);
//			cntReviewDetail.addComponentListener(new java.awt.event.ComponentAdapter() {
			cntReviewDetail.add(getCntScrMemo(), BorderLayout.CENTER);
//				public void componentResized(java.awt.event.ComponentEvent e) {
//					System.out.println("---componentResized()--- "
//							+cntReviewDetail.getHeight() + ", "
//							+getInpMemo().getHeight() + ", "
//							+getCntReviewDetail2().getHeight() + "; "
//							+getJSplitPane().getHeight() + ", "
//							+getJSplitPane().getDividerLocation()
//							);
//				}
//			});
		}
		return cntReviewDetail;
	}

	/**
	 * This method initializes cmdUpdateReview	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdUpdateReview() {
		if (cmdUpdateReview == null) {
			cmdUpdateReview = new JButton();
			cmdUpdateReview.setText("D");
		}
		return cmdUpdateReview;
	}

	/**
	 * This method initializes cntEdit	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntEdit() {
		if (cntEdit == null) {
			cntEdit = new JPanel();
			cntEdit.setLayout(new FlowLayout());
			cntEdit.add(getInpUpdDate(), null);
			cntEdit.add(getInpReviewer(), null);
			cntEdit.add(getInpState(), null);
		}
		return cntEdit;
	}

	/**
	 * This method initializes cntBtns	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntBtns() {
		if (cntBtns == null) {
			cntBtns = new JPanel();
			cntBtns.setLayout(new FlowLayout());
			cntBtns.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			cntBtns.add(getCmdNewDetail(), null);
			cntBtns.add(getCmdCancelNewDetail(), null);
			cntBtns.add(getCmdRemoveDetail(), null);
			cntBtns.add(getCmdCommitDetail(), null);
		}
		return cntBtns;
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
			cntButtons.add(getCmdNewReview());
			cntButtons.add(getCmdUpdateReview());
			cntButtons.add(getCmdRemoveReview());
		}
		return cntButtons;
	}

	/**
	 * This method initializes cntScrMemo	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getCntScrMemo() {
		if (cntScrMemo == null) {
			cntScrMemo = new JScrollPane();
			cntScrMemo.setBorder(new TitledBorder("comment"));
			cntScrMemo.setViewportView(getInpMemo());
		}
		return cntScrMemo;
	}
	
	

}  //  @jve:decl-index=0:visual-constraint="10,10"
