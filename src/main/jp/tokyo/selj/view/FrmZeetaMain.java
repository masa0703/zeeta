package jp.tokyo.selj.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.Writer;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.BackingStoreException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import jp.tokyo.selj.SysZeetaManager;
import jp.tokyo.selj.ZeetaDBManager;
import jp.tokyo.selj.common.AppException;
import jp.tokyo.selj.common.DateTextFormatter;
import jp.tokyo.selj.common.FontChooser;
import jp.tokyo.selj.common.TextUndoHandler;
import jp.tokyo.selj.dao.Doc;
import jp.tokyo.selj.dao.DocStr;
import jp.tokyo.selj.dao.SortType;
import jp.tokyo.selj.dao.Work;
import jp.tokyo.selj.dao.WorkType;
import jp.tokyo.selj.model.DocModel;
import jp.tokyo.selj.model.DocNode;
import jp.tokyo.selj.model.MasterComboModel;
import jp.tokyo.selj.model.OutputOfWorkListModel;
import jp.tokyo.selj.model.DocModel.NodeProcessor;
import jp.tokyo.selj.util.ClipboardStringWriter;
import jp.tokyo.selj.view.component.ZTree;
import jp.tokyo.selj.view.tools.DlgTools;
import jp.tokyo.selj.view.trans.TreeNodeTransferHandler;
import jp.tokyo.selj.view.trans.TreeNodeTransferHandler2;

import org.apache.log4j.Logger;

public class FrmZeetaMain extends BaseFrame {
	//2nd tree view との識別をする
	static final String TREE_TYPE_MAIN = "main tree";
	
	
	//actionMap_のキー
	static final String ACTKEY_NODE_COPY = "nodeCopy";
	static final String ACTKEY_NODE_CUT = "nodeCut";
	static final String ACTKEY_NODE_PASTE = "nodePaste";
	
	Logger log = Logger.getLogger(this.getClass());  //  @jve:decl-index=0:
	DocModel docModel_ = null;
	static final String DEVIDER_LOC_KEY = Util.getClassName(FrmZeetaMain.class) + "/devider_loc1";

    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	String dbUrl_ = ZeetaDBManager.getDBUrl();
	static final String VER_ = "Zeeta ver1.3.02";
    public String getTitle(){ 
    	return getInpDocTitle().getText()
    			+ " (" +dbUrl_+")"
    			+ " - "
    			+ VER_;
    }
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    protected TextUndoHandler titleUndoHandler_ = new TextUndoHandler();
    protected TextUndoHandler textUndoHandler_ = new TextUndoHandler();
    Runnable requestFocusToInpDocTitle_ = new Runnable() {
		   public void run() {
			   inpDocTitle.requestFocus();
		   }
    };
    Runnable requestFocusToInpDocCont_ = new Runnable() {
		   public void run() {
			   inpDocCont.requestFocus();
		   }
    };

    protected final String LAST_SELECTED_NODE_ID = getName()+"_selected_id";
	protected final String LINE_WRAP = getName()+"_lineWrap";
	
    private DlgParentDocList frmParentList_ = null;
    
	private JPanel jContentPane = null;

	private JSplitPane jSplitPane = null;

	private ZTree jTree = null;

	private JScrollPane cntScrTree = null;

	DocTreeCellRenderer cellRenderer_ = null;  //  @jve:decl-index=0:
	DlgReverseTree revTree_;
	Dlg2ndTree secndTree_;

	private JTabbedPane jTabbedPane = null;
	private JPanel cntDetail = null;
	private JTextField inpDocTitle = null;
	private JScrollPane cntScrDocCont = null;
	private JTextArea inpDocCont = null;

	private JPanel jPanel1 = null;

	private JToolBar jToolBar = null;

	private JPopupMenu mnuNaiyou = null;  //  @jve:decl-index=0:visual-constraint="676,179"

	private JPopupMenu mnuTreePopup = null;  //  @jve:decl-index=0:visual-constraint="742,26"

	private JPopupMenu mnuBookmark = null;  //  @jve:decl-index=0:visual-constraint="842,26"

	private JPanel jPanel2 = null;

	private JFormattedTextField inpDate = null;

	private JComboBox inpSortType = null;

	private JCheckBoxMenuItem cmdToggleLineWrap = null;

	protected JComboBox inpUser = null;

	private JPanel jPanel3 = null;

	private JTextField dspYoukenId = null;

	//MainViewの状態を管理する
	enum ViewStateType {NEUTRAL, CREATING, UPDATING, DELETING}; 
	class ViewState {
		DetailUpdateListener detailState_ = new DetailUpdateListener();
		int detail_docTypeId_;
		int detail_versionNo_;
		
		ViewStateType state_ = ViewStateType.NEUTRAL;
		boolean settingDetail_;
		TreeNode creatingParent_;
		DocNode currentNode_;
		DocNode clipNode_;
		boolean isCopy_;
		
		public boolean isNeutral(){
			return state_ == ViewStateType.NEUTRAL;
		}
		public void setNeutral(){
			state_ = ViewStateType.NEUTRAL;
		}
		
		public void setUpdating(boolean val){
			if(settingDetail_){	//Detailのセット中は無視
				return;
			}
			if(state_== ViewStateType.CREATING){	//creating中も無視
				return;
			}
			actionMap_.get(ActCancelNewYouken.class).setEnabled(val);
			actionMap_.get(ActCommitDoc.class).setEnabled(val);
			actionMap_.get(ActPrepareCreateDocAsSibling.class).setEnabled(!val);
			actionMap_.get(ActPrepareCreateDocAsChild.class).setEnabled(!val);
			actionMap_.get(ActDuplicateDoc.class).setEnabled(!val);
//			actionMap_.get(ActRemoveYouken.class).setEnabled(!val);
//			actionMap_.get(ActRefreshCurrent.class).setEnabled(!val);
			actionMap_.get(ActMoveUp.class).setEnabled(!val);
			actionMap_.get(ActMoveDown.class).setEnabled(!val);
			actionMap_.get(ActExpandAll.class).setEnabled(!val);
			//tree
			actionMap_.get(ACTKEY_NODE_COPY).setEnabled(!val);
			actionMap_.get(ACTKEY_NODE_CUT).setEnabled(!val);
			actionMap_.get(ACTKEY_NODE_PASTE).setEnabled(!val);

			if(val){
				state_ = ViewStateType.UPDATING;
			}else{
				setNeutral();
			}
		}
		public void setCreating(boolean val){
			getJTree().setEnabled(!val);
			
			actionMap_.get(ActCancelNewYouken.class).setEnabled(val);
			actionMap_.get(ActCommitDoc.class).setEnabled(val);
			actionMap_.get(ActPrepareCreateDocAsSibling.class).setEnabled(!val);
			actionMap_.get(ActPrepareCreateDocAsChild.class).setEnabled(!val);
			actionMap_.get(ActRemoveDoc.class).setEnabled(!val);
			actionMap_.get(ActDuplicateDoc.class).setEnabled(!val);
			actionMap_.get(ActRefreshCurrent.class).setEnabled(!val);
			actionMap_.get(ActMoveUp.class).setEnabled(!val);
			actionMap_.get(ActMoveDown.class).setEnabled(!val);
			actionMap_.get(ActExpandAll.class).setEnabled(!val);
			//tree
			actionMap_.get(ACTKEY_NODE_COPY).setEnabled(!val);
			actionMap_.get(ACTKEY_NODE_CUT).setEnabled(!val);
			actionMap_.get(ACTKEY_NODE_PASTE).setEnabled(!val);

			if(val){
				setDetail(new Doc());
			}
			if(val){
				state_ = ViewStateType.CREATING;
			}else{
				setNeutral();
			}
		}
		public boolean isCreating(){
			return state_== ViewStateType.CREATING;
		}
		public void setCreatingParent(TreeNode parent){
			creatingParent_ = parent;
			log.debug(parent);
		}
		public TreeNode getCreatingParent(){
			return creatingParent_;
		}
		public void setDeleting(boolean val) {
			if(val){
				state_ = ViewStateType.DELETING;
			}else{
				setNeutral();
			}
		}
		public void setCurrentNode(DocNode node) {
			currentNode_ = node;
			Doc doc = node.getDoc();
			setDetail(doc);
			//ルートの場合は、「兄弟ノード作成」ボタンをdisableにしたり
			if(doc.getDocTypeId() == Doc.ROOT_TYPE){
				actionMap_.get(ActPrepareCreateDocAsSibling.class).setEnabled(false);
				actionMap_.get(ActRemoveDoc.class).setEnabled(false);
				actionMap_.get(ActDuplicateDoc.class).setEnabled(false);
				actionMap_.get(ActMoveUp.class).setEnabled(false);
				actionMap_.get(ActMoveDown.class).setEnabled(false);
				//tree(setEnabled(xxx)にしてもキー操作ができてしまうので諦めた)
//				actionMap_.get(ACTKEY_NODE_COPY).setEnabled(false);
//				actionMap_.get(ACTKEY_NODE_CUT).setEnabled(false);
//				actionMap_.get(ACTKEY_NODE_PASTE).setEnabled(!val);

			}else{
				actionMap_.get(ActPrepareCreateDocAsSibling.class).setEnabled(true);
				actionMap_.get(ActRemoveDoc.class).setEnabled(true);
				actionMap_.get(ActDuplicateDoc.class).setEnabled(true);
				actionMap_.get(ActMoveUp.class).setEnabled(true);
				actionMap_.get(ActMoveDown.class).setEnabled(true);
				//tree(setEnabled(xxx)にしてもキー操作ができてしまうので諦めた)
//				actionMap_.get(ACTKEY_NODE_COPY).setEnabled(true);
//				actionMap_.get(ACTKEY_NODE_CUT).setEnabled(true);
//				actionMap_.get(ACTKEY_NODE_PASTE).setEnabled(!val);
			}
		}
		void setDetail(Doc doc) {
			if( doc == null){
				throw new IllegalArgumentException("doc is null."); 
			}
			settingDetail_ = true;	//DocumentListenerが無視するように
			
			dspYoukenId.setText(""+doc.getDocId());
			inpDocTitle.setText(doc.getDocTitle());
			inpDocTitle.setCaretPosition(0);
			inpDocCont.setText(doc.getDocCont());
			inpDocCont.setCaretPosition(0);
			inpDate.setValue(doc.getNewDate());
			inpUser.getModel().setSelectedItem(doc.getUserName());
			
			inpChkLinkNode.setSelected(doc.getDocTypeId() == Doc.LINK_TYPE);
			if(doc.getDocTypeId() == Doc.ROOT_TYPE){
				inpChkLinkNode.setEnabled(false);
			}else{
				String pref = SysZeetaManager.getSysZeeta().getLinkNodePref();
				if( pref != null && pref.trim().length() > 0){
					inpChkLinkNode.setEnabled(false);
				}else{
					inpChkLinkNode.setEnabled(true);
				}
			}
			
			if(doc.getSortType()==null){
				doc.setSortType((SortType)inpSortType.getModel().getElementAt(0));
				doc.setSortTypeId(doc.getSortType().getSortTypeID());
			}
			inpSortType.getModel().setSelectedItem(doc.getSortType());
			detailState_.reset();	//dartyFlagのクリア
			detail_docTypeId_ = doc.getDocTypeId();
			detail_versionNo_ = doc.getVersionNo();
			
			settingDetail_ = false;
			
			//undo , redo
//			Document inpTextDoc = inpDocCont.getDocument();
//		    if(inpTextDoc != null){
//		    	inpTextDoc.addUndoableEditListener(undoHandler);
//		    }
	    	titleUndoHandler_.cleanup();
	    	textUndoHandler_.cleanup();

		}
		public Doc getDetail(){
			return getDetail(new Doc());
		}
		public Doc getDetail(Doc ret){
			ret.setDocId(Long.parseLong(dspYoukenId.getText()));
			ret.setDocTitle(inpDocTitle.getText());
			ret.setDocCont(inpDocCont.getText());
			//JFormattedTextFieldは、実際にロストフォーカスしないと値が取り込まれない
			//もしくは、以下のようにcommitEditを呼び出す必要がある。
			try {
				inpDate.commitEdit();
			} catch (ParseException e1) {
				//無視
			}
			ret.setNewDate((Timestamp)inpDate.getValue());
			ret.setUserName(
					(inpUser.getModel().getSelectedItem()==null)? null:
//						((User)inpUser.getModel().getSelectedItem()).getUserName()
						""+inpUser.getModel().getSelectedItem()
			);
			ret.setDocTypeId(detail_docTypeId_);

			//Titleのプレフィクスでlink nodeを決定する
			String pref = SysZeetaManager.getSysZeeta().getLinkNodePref();
			if( pref != null && pref.trim().length() > 0){
				if(ret.getDocTitle().startsWith(pref)){
					inpChkLinkNode.setSelected(true);
				}else{
					inpChkLinkNode.setSelected(false);
				}
			}
			if(detail_docTypeId_ != Doc.ROOT_TYPE){
				if(inpChkLinkNode.isSelected()){
					ret.setDocTypeId(Doc.LINK_TYPE);
				}else{
					ret.setDocTypeId(Doc.NORMAl_TYPE);
				}
			}
			ret.setVersionNo(detail_versionNo_);

			SortType sortType;
			Object selectObj = inpSortType.getModel().getSelectedItem();
			if( selectObj instanceof SortType ){
				sortType = (SortType)selectObj;
			}else{
				//未設定の場合は、nullではなくStringで来るらしい
				sortType = MasterComboModel.DEFAULT_SORT_TYPE;
			}
			
			ret.setSortType(sortType);
			ret.setSortTypeId(sortType.getSortTypeID());
			
			
			return ret;
		}
		
		public DocNode getCurrentNode() {
			return currentNode_;
		}
		public boolean checkExistSelectingNode(){
			boolean ret = true;
			if(currentNode_ == null){
				ret = false;
			}
			if( !ret){
				showMsg_selectTree();
			}
			return ret;
		}
		void showMsg_selectTree(){
			JOptionPane.showMessageDialog(
					FrmZeetaMain.this
					,"ツリー上の要件を選択してください。",""
					,JOptionPane.INFORMATION_MESSAGE);
		}
		public void clipCurrentNode() {
			clipNode_ = currentNode_;
		}
		public DocNode getClipNode() {
			if(clipNode_ == null || 
				clipNode_.getRoot() != docModel_.getRoot()){	//削除されたノードはrootに紐づかない
				throw new AppException("クリップノードが無効です");
			}
			return clipNode_;
		}
		public boolean isUpdating() {
			return state_ == ViewStateType.UPDATING;
		}
		public DetailUpdateListener getDetailState() {
			return detailState_;
		}
//		public boolean isCopy() {
//			return isCopy_;
//		}
//		public void setCopy(boolean val) {
//			isCopy_ = val;
//		}
	};
	ViewState viewState_ = new ViewState();  //  @jve:decl-index=0:
	
	
	void warnningIfNotNeutral(){
		if( ! viewState_.isNeutral()){
			throw new AppException("編集中です。commitかcancelボタンを押してください。");
		}
	}
	
	
	//Actionの親クラス
	private abstract class ActBase2 extends ActBase {
		ActBase2(ActionMap map){
			super(map);
		}
		protected Component getOwnerComponent(){
			return FrmZeetaMain.this;
		}
	}
	//titleにフォーカス
	class ActFocusOnTitle extends ActBase2 {
		public ActFocusOnTitle(ActionMap map){
			super(map);
			putValue(Action.NAME, "FocusOnTitle");
			putValue(Action.SHORT_DESCRIPTION, "Titleにフォーカス");
		}
		public void actionPerformed2(ActionEvent e) {
			getInpDocTitle().requestFocus();
		}
	}
	private abstract class ActTransactionBase2 extends ActTransactionBase {
		ActTransactionBase2(ActionMap map){
			super(map);
		}
		protected Component getOwnerComponent(){
			return FrmZeetaMain.this;
		}
	}
	//ノードクリップ
	class ActClipNode extends ActBase2 {
		public ActClipNode(ActionMap map){
			super(map);
			putValue(Action.NAME, "clip");
			putValue(Action.SHORT_DESCRIPTION, "ノードをクリップボードへ取り込む");
		}
		public void actionPerformed2(ActionEvent e) {
			warnningIfNotNeutral();
//			if( ! viewState_.isNeutral()){
//				return;
//			}
			log.debug("clipNode()");
			if( ! viewState_.checkExistSelectingNode()){
				return;
			}
			viewState_.clipCurrentNode();
		}
	}
	//ペーストの親
	abstract class ActPasteNode extends ActTransactionBase2 {
		static final int MOVE=0;
		static final int COPY=1;
		ActPasteNode(ActionMap map){
			super(map);
		}
		public void pasteNode(ActionEvent e,int type){
			warnningIfNotNeutral();
//			if( ! viewState_.isNeutral()){
//				return;
//			}
			log.debug("pasteNode():"+type);
			if( ! viewState_.checkExistSelectingNode()){
				return;
			}
			
			//要件構造を作成
			DocNode newNode = docModel_.insertDoc(
					viewState_.getCurrentNode(),
					viewState_.getClipNode().getDoc());
			//refresh
			Action act = actionMap_.get(ActRefreshNode.class);
			act.putValue(ActRefreshNode.REFRESH_NODE, newNode);
			act.actionPerformed(e);
			
			if(type == MOVE){
				docModel_.deleteDoc(viewState_.getClipNode());
			}
			jTree.expandPath(new TreePath(viewState_.getCurrentNode().getPath()));
		}
	}
	//移動ペースト
	class ActPasteNodeMove extends ActPasteNode {
		public ActPasteNodeMove(ActionMap map){
			super(map);
			putValue(Action.NAME, "move paste");
			putValue(Action.SHORT_DESCRIPTION, "ノードを貼り付ける（移動）");
		}
		public void actionPerformed2(ActionEvent e) {
			pasteNode(e,MOVE);
		}
	}
	//コピーペースト
	class ActPasteNodeCopy extends ActPasteNode {
		public ActPasteNodeCopy(ActionMap map){
			super(map);
			putValue(Action.NAME, "copy paste");
			putValue(Action.SHORT_DESCRIPTION, "ノードを貼り付ける（コピー）");
		}
		public void actionPerformed2(ActionEvent e) {
			pasteNode(e,COPY);
		}
	}
	//削除
	class ActRemoveDoc extends ActTransactionBase2 {
		boolean isExecute_ = false;
		public ActRemoveDoc(ActionMap map) {
			super(map);
			putValue(Action.NAME, "remove");
			putValue(Action.SHORT_DESCRIPTION, "ノードを削除(ctrl+D, Del)");
			putValue(Action.SMALL_ICON, 
					new ImageIcon(getClass().getResource("/image/remove.gif")));
		}
		public void actionPerformed2(ActionEvent e) {
			if( ! viewState_.isNeutral()){
				if( viewState_.isUpdating() ){
					//更新中のデータを削除してもかまわない
					viewState_.getDetailState().reset();	//これをやらないと次のステップで「更新されていますが・・・」が出てしまう
				}else{
					return;
				}
			}
			if( ! viewState_.checkExistSelectingNode()){
				return;
			}
			viewState_.setDeleting(true);

			setCursor(Util.WAIT_CURSOR);
			//確認
			try{
				isExecute_ = false;
				if( JOptionPane.showConfirmDialog(
						FrmZeetaMain.this
						,"選択中のノードと全ての子ノードを削除します。\n" +
						"ただし、他の親からの関連がある場合は、ノードの削除は行わず" +
						"関連のみ削除します。\n" +
						"よろしいですか？",""
						,JOptionPane.YES_NO_OPTION
						,JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
					
					DocNode removeNode = viewState_.getCurrentNode();
					//次ノードを探しておく
					DocNode next = (DocNode)removeNode.getNextSibling();
					if(next == null){
						next = (DocNode)removeNode.getPreviousSibling();
						if(next == null){
							next = (DocNode)removeNode.getNextNode();
							if(next == null){
								next = (DocNode)removeNode.getParent();
							}
						}
						isExecute_ = true;
					}
					docModel_.deleteDoc(removeNode);
					jTree.setSelectionPath(new TreePath(next.getPath()));
				}
			}finally{
				//状態を戻す
				viewState_.setDeleting(false);
				setCursor(Cursor.getDefaultCursor());
			}
		}
		@Override
		protected void postProc() {
			super.postProc();
			if(isExecute_){
				//トランザクションを完了させてからメッセージを表示する
				JOptionPane.showMessageDialog(
						FrmZeetaMain.this
						,"削除完了。",""
						,JOptionPane.INFORMATION_MESSAGE);
			}
		}
		
	}

	//ノード追加の親
	abstract class ActPrepareCreateDoc extends ActBase2 {
		static final int AS_CHILD=0;
		static final int AS_SIBLING=1;
		
		ActPrepareCreateDoc(ActionMap map){
			super(map);
		}
		void prepareCreateYouken(int type) {
			warnningIfNotNeutral();
//			if( ! viewState_.isNeutral()){
//				return;
//			}
			if( ! viewState_.checkExistSelectingNode()){
				return;
			}
			if( type == AS_CHILD){
				viewState_.setCreatingParent(viewState_.getCurrentNode());
			}else{
				//rootの兄弟は追加できない
				if(viewState_.currentNode_.isRoot()){
					throw new AppException("rootノードに兄弟ノードは作成できません");
				}
				viewState_.setCreatingParent(viewState_.getCurrentNode().getParent());
			}
			viewState_.setCreating(true);
			
			getJTabbedPane().setSelectedIndex(0);
			inpDocTitle.requestFocus();
		}
	}
	//子ノード追加
	class ActPrepareCreateDocAsChild extends ActPrepareCreateDoc {
		public ActPrepareCreateDocAsChild(ActionMap map) {
			super(map);
			putValue(Action.NAME, "New as child");
			putValue(Action.SHORT_DESCRIPTION, "子ノード追加(Ins、ctrl+N)");
			putValue(Action.SMALL_ICON, 
					new ImageIcon(getClass().getResource("/image/newAsChild.gif")));
		}
		public void actionPerformed2(ActionEvent e) {
			prepareCreateYouken(AS_CHILD);
		}
	}
	//兄弟ノード追加
	class ActPrepareCreateDocAsSibling extends ActPrepareCreateDoc {
		public ActPrepareCreateDocAsSibling(ActionMap map) {
			super(map);
			putValue(Action.NAME, "New as sibling");
			putValue(Action.SHORT_DESCRIPTION, "兄弟ノード追加(Shift+Ins, ctrl+M)");
			putValue(Action.SMALL_ICON, 
					new ImageIcon(getClass().getResource("/image/newAsSibling.gif")));
		}
		public void actionPerformed2(ActionEvent e) {
			prepareCreateYouken(AS_SIBLING);
		}
	}
	//コミット
	class ActCommitDoc extends ActTransactionBase2 {
		public ActCommitDoc(ActionMap map) {
			super(map);
			putValue(Action.NAME, "commit");
			putValue(Action.SHORT_DESCRIPTION, "反映(ctrl+S, ctrl+Enter)");
			putValue(Action.ACCELERATOR_KEY, 
					KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.ALT_MASK));
		}
		public void actionPerformed2(ActionEvent e) {
			if(viewState_.isCreating()){
				createDoc(e);
				jTree.requestFocus();
			}else if(viewState_.isUpdating()){
				actionMap_.get(ActUpdateDoc.class).actionPerformed(e);
				jTree.requestFocus();
			}else{
				//無視や。（ショートカットキーの場合ここに来る）
			}
		}
		public void createDoc(ActionEvent e) {
			if(! viewState_.isCreating()){
				return;
			}
			DocNode oyaNode = 
				(DocNode)viewState_.getCreatingParent();
			Doc newYouken = viewState_.getDetail();
			//要件と構造を追加
			log.debug("oyaNode="+oyaNode);
			DocNode newNode
				= docModel_.insertDoc(oyaNode, newYouken);

			//追加したノードを見えるようにする
			jTree.expandPath(new TreePath(oyaNode.getPath()));
			jTree.setSelectionPath(new TreePath(newNode.getPath()));
			jTree.scrollPathToVisible(new TreePath(newNode.getPath()));
			
			viewState_.setCreating(false);
		}
	}
	//2重化
	class ActDuplicateDoc extends ActTransactionBase2 {
		public ActDuplicateDoc(ActionMap map) {
			super(map);
			putValue(Action.NAME, "duplicate");
			putValue(Action.SHORT_DESCRIPTION, "ノードを２重化(ctrl+W)");
			putValue(Action.SMALL_ICON, 
					new ImageIcon(getClass().getResource("/image/dup.gif")));
//			putValue(Action.ACCELERATOR_KEY, 
//					KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.ALT_MASK));
		}
		public void actionPerformed2(ActionEvent e) {	
			if(viewState_.isCreating()){
				return;
			}
			if(viewState_.isUpdating()){
				return;
			}
			duplicateDoc(e);
		}
		public void duplicateDoc(ActionEvent e) {

			//カレントノードを複製
			DocNode curNode = (DocNode)viewState_.getCurrentNode();
			Doc newDoc = new Doc();
			Timestamp saveDate = newDoc.getNewDate();	//日付を退避
			String saveUser = newDoc.getUserName();			//作成者を退避
			newDoc.copyDoc(curNode.getDoc());	//全属性をコピー
			newDoc.setNewDate(saveDate);	//日付を入れ替え
			newDoc.setUserName(saveUser);	//作成者を入れ替え
			newDoc.setDocId(-1);			//このようにしないと新規ノードにならない
			
			//要件と構造を追加
			DocNode oyaNode = (DocNode)curNode.getParent();
			log.debug("oyaNode="+oyaNode);
			DocNode newNode	= docModel_.insertDoc(oyaNode, newDoc);

			//追加したノードを見えるようにする
			jTree.expandPath(new TreePath(oyaNode.getPath()));
			jTree.setSelectionPath(new TreePath(newNode.getPath()));
			jTree.scrollPathToVisible(new TreePath(newNode.getPath()));
		}
	}
	class ActUpdateDoc extends ActBase2 {
		public ActUpdateDoc(ActionMap map) {
			super(map);
			putValue(Action.NAME, "update");
			putValue(Action.SHORT_DESCRIPTION, "更新");
		}
		public void actionPerformed2(ActionEvent e) {
			if( ! viewState_.isUpdating()){
				return;
			}
			
			boolean isSortTypeDarty = viewState_.getDetailState().isSortTypeDarty();
			
			cmdOkNew.requestFocus();
			
			//以下の１行のようにいきなりカレントDocを更新すると、入力エラーで
			//DBの更新がキャンセルされてもtreeノードが、更新されてしまう。
//			newDoc = viewState_.getDetail(viewState_.getCurrentNode().getDoc());
			Doc newDoc;
			try {
				newDoc = (Doc)viewState_.getCurrentNode().getDoc().clone();
			} catch (CloneNotSupportedException e1) {
				throw new RuntimeException(e1);
			}
			newDoc = viewState_.getDetail(newDoc);
			docModel_.updateDoc(newDoc);

			viewState_.getDetailState().reset();
			//更新したノードに新しいdocをセット
			viewState_.getCurrentNode().setUserObject(newDoc);
			
			//現在表示されている同じノードを更新する
			docModel_.docChanged(newDoc);
			
			//表示状態記録
			int caretP = inpDocCont.getCaretPosition();
			Rectangle rect = inpDocCont.getVisibleRect();

			viewState_.setDetail(newDoc);	//versionNoが更新されているのでリロード
			
			if(e != null && e.getActionCommand().equals(ActUpdateIfDarty.class.getName())){
				//ノードを変更しようとしている場合は、表示状態を復元する必要はない
			}else{
				//表示状態復元
				class SetView implements Runnable {
					Rectangle _rect;
					int _caretP;
					SetView(Rectangle rect, int caretP){
						_rect = rect;
						_caretP = caretP;
					}
					public void run() {
						inpDocCont.scrollRectToVisible(_rect);
						inpDocCont.setCaretPosition(_caretP);
					}			
				}
				SwingUtilities.invokeLater(new SetView(rect, caretP));
			}			
			if(isSortTypeDarty){	//子ノード並びが更新されている場合
				actionMap_.get(ActRefreshCurrent.class).actionPerformed(e);
			}else{
//				docModel_.reload(viewState_.getCurrentNode());　
				//↑これをやると、子ノードが閉じてしまうので削除してみた ver0.4.34
			}
			//ルートの場合は、ウィンドウタイトルに反映
			if(newDoc.getDocTypeId() == Doc.ROOT_TYPE){
				setTitle(getTitle());
			}
			
		}
	}
	//キャンセル
	class ActCancelNewYouken extends ActBase2 {
		public ActCancelNewYouken(ActionMap map) {
			super(map);
			putValue(Action.NAME, "cancel");
			putValue(Action.SHORT_DESCRIPTION, "キャンセル(Esc)");
			map.put(this.getClass(), this);
		}
		public void actionPerformed2(ActionEvent e) {
			if(viewState_.isCreating() || viewState_.isUpdating()){
				if( JOptionPane.showConfirmDialog(
						FrmZeetaMain.this
						,"編集中の内容を破棄します。\n" +
						"よろしいですか？",""
						,JOptionPane.YES_NO_OPTION
						,JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION){
					return;
				}
			}else{
				return;
			}
			TreePath path = new TreePath(viewState_.getCurrentNode().getPath());
			viewState_.getDetailState().reset();	//これをやらないと次のステップで「更新されていますが・・・」が出てしまう
			jTree.setSelectionPath(null);	//一旦選択を外さないと以下で選択イベントが発生しない
			jTree.setSelectionPath(path);
			viewState_.setCreating(false);	//更新処理が動作しないように、この位置でセットする必要がある
			viewState_.setUpdating(false);
		}
	}
	//更新されていたらDB更新
	class ActUpdateIfDarty extends ActBase2 {
		public ActUpdateIfDarty(ActionMap map) {
			super(map);
			putValue(Action.NAME, "updateIfDarty");
			putValue(Action.SHORT_DESCRIPTION, "更新されていたら更新");
		}
		public void actionPerformed2(ActionEvent e) {
			if( viewState_.isCreating()){
				return;
			}
			//更新されているかチェック
			if( viewState_.getDetailState().isDarty() ){
				log.debug("汚れているわ. ");
				// 更新せなあかん
				if( JOptionPane.showConfirmDialog(
						FrmZeetaMain.this
						,"内容が変更されています。\n" +
						"更新を反映しますか？",""
						,JOptionPane.YES_NO_OPTION
						,JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
					;
					actionMap_.get(ActUpdateDoc.class).actionPerformed(e);
				}else{
					//Detailを元に戻す・・・必要はない。
					//ここは、TreeNodeを変更する際に呼び出されるので、再度キャンセル
					//されたNodeを選択した場合は、読み込み直されている。
				}
				viewState_.setUpdating(false);
			}
		}
	}
	//カレントノードのリフレッシュ
	class ActRefreshCurrent extends ActBase2 {
		public ActRefreshCurrent(ActionMap map) {
			super(map);
			putValue(Action.NAME, "refresh");
			putValue(Action.SHORT_DESCRIPTION, "カレントノードのリフレッシュ(F5)");
			putValue(Action.SMALL_ICON, 
					new ImageIcon(getClass().getResource("/image/refresh.gif")));
		}
		public void actionPerformed2(ActionEvent e) {
//			if( ! viewState_.isNeutral()){	編集中の場合もあり得る
//				return;
//			}
			if( ! viewState_.checkExistSelectingNode()){
				return;
			}
			if(viewState_.isCreating() || viewState_.isUpdating()){
				Action cancelAction = actionMap_.get(ActCancelNewYouken.class);
				cancelAction.actionPerformed(null);
				if(viewState_.isCreating() || viewState_.isUpdating()){
					//状態が変化していない場合は、「破棄しますか？No」を選択した場合
					return;
				}
			}
			
			Action act = actionMap_.get(ActRefreshNode.class);
			act.putValue(ActRefreshNode.REFRESH_NODE,  viewState_.getCurrentNode());
			act.actionPerformed(e);
		}
	}
	//指定ノードのリフレッシュ
	public class ActRefreshNode extends ActBase {		//TreeNodeTransferHandlerから参照されるためpublicにしている
		public static final String REFRESH_NODE = "REFRESH_NODE";
		public ActRefreshNode(ActionMap map) {
			super(map);
			putValue(Action.NAME, "refreshSpecific");
			putValue(Action.SHORT_DESCRIPTION, "指定ノードのリフレッシュ");
		}
		public void actionPerformed2(ActionEvent e) {
			if( ! viewState_.checkExistSelectingNode()){
				return;
			}
			DocNode node = (DocNode)getValue(REFRESH_NODE);
			if( node == null){
				throw new RuntimeException("node is null.");
			}
			putValue(REFRESH_NODE, null);
			warnningIfNotNeutral();
//			if( ! viewState_.isNeutral()){
//				return;
//			}
			//**** 自身をリロード 
			viewState_.setDetail( docModel_.reloadDoc(node) );
			
			//**** 孫nodeまでリロード 
			node.removeAllChildren();
			log.debug("node.removeAllChildren();やったよ");
			docModel_.reload(node);
			
			//子要件を追加
			docModel_.addChildDocFromDb(node);
			//孫要件を追加
			docModel_.addMagoDocFromDb(node);
			
			jTree.expandPath(new TreePath(node.getPath()));
		}
	}
	//成果物一覧画面表示(基底)
	abstract class ActShowOutputListBase extends ActBase2 {
		DlgOutputList outputList_ = null;
		public ActShowOutputListBase(ActionMap map) {
			super(map);
		}
	}
	class ActShowOutputList extends ActShowOutputListBase {
		public ActShowOutputList(ActionMap map) {
			super(map);
			putValue(Action.NAME, "output list");
			putValue(Action.SHORT_DESCRIPTION, "成果物一覧画面表示");
			putValue(Action.SMALL_ICON, 
					new ImageIcon(getClass().getResource("/image/outputs.gif")));
		}
		public void actionPerformed2(ActionEvent e) {
			getDlgOutputList().setModal(false);
			getDlgOutputList().makeList();
			getDlgOutputList().setVisible(true);

		}
		DlgOutputList getDlgOutputList(){
			if(outputList_ == null){
				outputList_ = new DlgOutputList(null,false);
				outputList_.setName(this.getClass().getName());
				outputList_.setup(FrmZeetaMain.this);
//				outputList_.setLocationRelativeTo(FrmZeetaMain.this.getRootPane());
			}
			return outputList_;
		}
	}
	//成果物一覧画面表示(作業登録用)
	class ActShowOutputListForWork extends ActShowOutputListBase {
		public ActShowOutputListForWork(ActionMap map) {
			super(map);
			putValue(Action.NAME, "select output");
			putValue(Action.SHORT_DESCRIPTION, "成果物一覧画面表示");
//			putValue(Action.SMALL_ICON, 
//					new ImageIcon(getClass().getResource("/image/common.gif")));
			getDlgOutputList().addActionListener(new OutputSelectEventListener());
		}
		public void actionPerformed2(ActionEvent e) {
			Object selectedItem = getInpSelectWorkType().getModel().getSelectedItem();
			if(selectedItem == null || !(selectedItem instanceof WorkType)){
				return;
			}
			//既に登録済みの作業の場合は、エラーとする
			OutputOfWorkListModel workModel = 
				(OutputOfWorkListModel)getDspWorkList().getModel();
			if(workModel.isWorkExist(
					viewState_.currentNode_.getDoc().getDocId(), 
					((WorkType)selectedItem).getWorkTypeId()) ){
				throw new AppException("すでに登録済みの作業です");
			}
			getDlgOutputList().makeList((WorkType)selectedItem);
			getDlgOutputList().setVisible(true);
		}
		DlgOutputList getDlgOutputList(){
			if(outputList_ == null){
				outputList_ = new DlgOutputList(FrmZeetaMain.this, true);
				outputList_.setName(this.getClass().getName());
				outputList_.setup(FrmZeetaMain.this);
				outputList_.setDisableActionLinkedDocTree();
//				outputList_.setLocationRelativeTo(FrmZeetaMain.this.getRootPane());
			}
			return outputList_;
		}
	}
	//親一覧画面表示
	class ActShowParents extends ActBase2 {
		public ActShowParents(ActionMap map) {
			super(map);
			putValue(Action.NAME, "showParents");
			putValue(Action.SHORT_DESCRIPTION, "親一覧画面表示");
			putValue(Action.SMALL_ICON, 
					new ImageIcon(getClass().getResource("/image/common.gif")));
		}
		public void actionPerformed2(ActionEvent e) {
			getFrmParentList().setVisible(true, viewState_.getCurrentNode());
		}
	}
	abstract class ActMove extends ActTransactionBase2 {
		public ActMove(ActionMap map) {
			super(map);
		}
		protected boolean check(){
			warnningIfNotNeutral();
//			if( ! viewState_.isNeutral()){
//				return false;
//			}
			if( ! viewState_.checkExistSelectingNode()){
				return false;
			}
			DocNode oyaNode = 
				(DocNode)viewState_.getCurrentNode().getParent();
			
			//親が「SEQ順」になっていること
			if(oyaNode.getDoc().getSortType().getSortTypeID()
					!= SortType.SEQ){
				throw new AppException("親ノードを「指定順」に変更してください");
			}
			return true;
		}
		protected void moveAndUpdate(int moveSize){
			setCursor(Util.WAIT_CURSOR);
			try{
				DocNode oyaNode = 
					(DocNode)viewState_.getCurrentNode().getParent();
	
				//Tree上で移動
				DocNode curNode = viewState_.getCurrentNode();
	//			curNode.removeFromParent(); これをやらなくてもいいらしい
				oyaNode.insert(curNode, oyaNode.getIndex(curNode) + moveSize);
				
				//SEQのリナンバリング
				docModel_.renumberSequence(oyaNode);
				
				//Viewの更新
				docModel_.reload(oyaNode);
				TreePath path = new TreePath(curNode.getPath());
				jTree.setSelectionPath(path);
				jTree.scrollPathToVisible(path);
			}finally{
				setCursor(Cursor.getDefaultCursor());
			}
		}
		
	}	
	//上に移動
	class ActMoveUp extends ActMove {
		public ActMoveUp(ActionMap map) {
			super(map);
			putValue(Action.NAME, "moveUp");
			putValue(Action.SHORT_DESCRIPTION, "一つ上に移動(ctrl+↑)");
			putValue(Action.SMALL_ICON, 
					new ImageIcon(getClass().getResource("/image/moveUp.gif")));
		}
		public void actionPerformed2(ActionEvent e) {
			log.trace("start");
			if( !check() ){
				return;
			}
			
			//手前ノードの存在チェック
			DocNode prevNode = 
				(DocNode)viewState_.getCurrentNode().getPreviousSibling();
			if(prevNode == null){
				log.debug("上がない");
				return;
			}
			//Tree上でノードの移動とDB更新
			moveAndUpdate(-1);
			
			log.trace("end");
		}
	}
	//下に移動
	class ActMoveDown extends ActMove {
		public ActMoveDown(ActionMap map) {
			super(map);
			putValue(Action.NAME, "moveDown");
			putValue(Action.SHORT_DESCRIPTION, "一つ下に移動(ctrl+↓)");
			putValue(Action.SMALL_ICON, 
					new ImageIcon(getClass().getResource("/image/moveDown.gif")));
		}
		public void actionPerformed2(ActionEvent e) {
			log.trace("start");
			if( !check() ){
				return;
			}
			//手前ノードの存在チェック
			DocNode nextNode = 
				(DocNode)viewState_.getCurrentNode().getNextSibling();
			if(nextNode == null){
				log.debug("下がない");
				return;
			}
			//Tree上でノードの移動とDB更新
			moveAndUpdate(1);
			
			log.trace("end");
		}
	}
	//検索画面表示
	class ActShowSearchWindow extends ActBase2 {
		DlgNewSearch searchForm_;
		public ActShowSearchWindow(ActionMap map) {
			super(map);
			putValue(Action.NAME, "OpenSearchWindow");
			putValue(Action.SHORT_DESCRIPTION, "検索画面を開く(ctrl+F)");
			putValue(Action.SMALL_ICON, 
					new ImageIcon(getClass().getResource("/image/search.gif")));
		}
		public void actionPerformed2(ActionEvent e) {
			
			if(searchForm_ == null){
				searchForm_ = new DlgNewSearch(FrmZeetaMain.this);
				searchForm_.setLocationRelativeTo(FrmZeetaMain.this);	//一発目は、画面の中央に表示
				searchForm_.setup();
			}
			searchForm_.setVisible(true);
			searchForm_.setRequestFocusToInput();
		}
	}
	//Tools画面表示
	class ActShowToolsWindow extends ActBase2 {
		DlgTools tools = null;
		public ActShowToolsWindow(ActionMap map) {
			super(map);
			putValue(Action.NAME, "tools");
			putValue(Action.SHORT_DESCRIPTION, "ツールウィンドウを開く");
			putValue(Action.SMALL_ICON, 
					new ImageIcon(getClass().getResource("/image/tools.gif")));
		}
		public void actionPerformed2(ActionEvent e) {
			if(tools == null){
				tools = new DlgTools(FrmZeetaMain.this);
				tools.setLocationRelativeTo(FrmZeetaMain.this);	//一発目は、画面の中央に表示
				tools.setup();
			}
			tools.setVisible(true);
		}
	}
	//全て展開
	class ActExpandAll extends ActBase2 {
		public ActExpandAll(ActionMap map) {
			super(map);
			putValue(Action.NAME, "ExpandAll");
			putValue(Action.SHORT_DESCRIPTION, "選択ノードを全て展開");
			putValue(Action.SMALL_ICON, 
					new ImageIcon(getClass().getResource("/image/expandAll.gif")));
		}
		public void actionPerformed2(ActionEvent e) {
			setCursor(Util.WAIT_CURSOR);
			try{
				docModel_.processAllNode2( 
					viewState_.getCurrentNode(),
					new NodeProcessor(){
						public boolean process(DocNode node) {
							jTree.expandPath(
									new TreePath( node.getPath() ) );
							return true;	//続行
						}
					}
				);
			}finally{
				setCursor(Cursor.getDefaultCursor());
			}
		}
	}
	class ActShowDebugWindow extends ActBase2 {
		public ActShowDebugWindow(ActionMap map) {
			super(map);
			putValue(Action.NAME, "debug");
			putValue(Action.SHORT_DESCRIPTION, "show debug window");
		}
		public void actionPerformed2(ActionEvent e) {
			DlgDebug debugView = new DlgDebug(FrmZeetaMain.this);
			debugView.setLocationRelativeTo(FrmZeetaMain.this);
			debugView.setVisible(true);
	    }
	}
	//逆ツリー画面表示
	class ActShowReverseTreeView extends ActBase2 {
		public ActShowReverseTreeView(ActionMap map) {
			super(map);
			putValue(Action.NAME, "show reverse tree");
			putValue(Action.SHORT_DESCRIPTION, "逆ツリーを表示");
			putValue(Action.SMALL_ICON, 
					new ImageIcon(getClass().getResource("/image/reverseTree.gif")));
		}
		public void actionPerformed2(ActionEvent e) {
			setCursor(Util.WAIT_CURSOR);
			try{
				getDlgReverseTree().setVisible(true, viewState_.getCurrentNode());
			}finally{
				setCursor(Cursor.getDefaultCursor());
			}

	    }
	}
	//2ndツリー画面表示
	class ActShow2ndTreeView extends ActBase2 {
		public ActShow2ndTreeView(ActionMap map) {
			super(map);
			putValue(Action.NAME, "show 2nd view");
			putValue(Action.SHORT_DESCRIPTION, "2nd view. 離れたノードのコピペに便利です");
			putValue(Action.SMALL_ICON, 
					new ImageIcon(getClass().getResource("/image/2ndView.gif")));
		}
		public void actionPerformed2(ActionEvent e) {
			setCursor(Util.WAIT_CURSOR);
			try{
				getDlg2ndTree().setVisible(true, viewState_.getCurrentNode());
			}finally{
				setCursor(Cursor.getDefaultCursor());
			}

	    }
	}
	//Export画面表示
	class ActShowExportView extends ActBase2 {
		DlgExport exportMenu_;
		public ActShowExportView(ActionMap map) {
			super(map);
			putValue(Action.NAME, "export");
			putValue(Action.SHORT_DESCRIPTION, "選択中のノード配下をexport");
			putValue(Action.SMALL_ICON, 
					new ImageIcon(getClass().getResource("/image/export.gif")));
		}
		public void actionPerformed2(ActionEvent e) {
			if(exportMenu_ == null){
				exportMenu_ = new DlgExport(FrmZeetaMain.this);
				exportMenu_.setLocationRelativeTo(FrmZeetaMain.this);
			}
			exportMenu_.setup(viewState_.getCurrentNode().getDoc(), docModel_);
			exportMenu_.setVisible(true);

	    }
	}
	//Summary画面表示
	class ActShowSummaryView extends ActBase2 {
		DlgSummary dlg_;
		public ActShowSummaryView(ActionMap map) {
			super(map);
			putValue(Action.NAME, "summary");
			putValue(Action.SHORT_DESCRIPTION, "選択中のノード配下の作業属性を集計");
			putValue(Action.SMALL_ICON, 
					new ImageIcon(getClass().getResource("/image/sum.png")));
		}
		public void actionPerformed2(ActionEvent e) {
			if(dlg_ == null){
				dlg_ = new DlgSummary(FrmZeetaMain.this);
				dlg_.setLocationRelativeTo(FrmZeetaMain.this);
			}
			dlg_.setup(viewState_.getCurrentNode().getDoc(), docModel_);
			dlg_.setVisible(true);
	    }
	}
	class ActShowWorkUpdater extends ActBase2 {
		DlgWorkDetail workDetail_ = null;
		public ActShowWorkUpdater(ActionMap map){
			super(map);
			putValue(Action.NAME, "update");
			putValue(Action.SHORT_DESCRIPTION, "作業成果物の編集");
		}
		public void actionPerformed2(ActionEvent e) {
			if(getDspWorkList().getSelectedValue() == null){
				throw new AppException("いずれかの作業を選択してください");
			}
			Work work = (Work)getDspWorkList().getSelectedValue();
			if(workDetail_ == null){
				workDetail_ = new DlgWorkDetail(FrmZeetaMain.this);
				workDetail_.setup((OutputOfWorkListModel)getDspWorkList().getModel());
				workDetail_.setLocationRelativeTo(getDspWorkList().getRootPane());	//一発目は、画面の中央に表示
			}
			workDetail_.setWork( work );
			workDetail_.setVisible(true);
		}
	}
	class ActRemoveWork extends ActTransactionBase2 {
		public ActRemoveWork(ActionMap map){
			super(map);
			putValue(Action.NAME, "remove");
			putValue(Action.SHORT_DESCRIPTION, "作業を削除する");
		}
		public void actionPerformed2(ActionEvent e) {
			if(getDspWorkList().getSelectedValue() == null){
				throw new AppException("いずれかの作業を選択してください");
			}
			Work work = (Work)getDspWorkList().getSelectedValue();
			if( JOptionPane.showConfirmDialog(
					FrmZeetaMain.this
					,work.getWorkType().getWorkTypeName() + " 作業を削除します。\n" +
					"よろしいですか？",""
					,JOptionPane.YES_NO_OPTION
					,JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION){
				return;
			}

			OutputOfWorkListModel model = (OutputOfWorkListModel)getDspWorkList().getModel();
			model.removeWork(work);
			//カレントノードのアイコンを変更
			Doc curDoc = viewState_.currentNode_.getDoc();
			if(curDoc.getWorkCount() > 0){
				curDoc.setWorkCount(curDoc.getWorkCount() - 1);
				docModel_.refreshWorkCount(curDoc.getDocId(), curDoc.getWorkCount());
				jTree.repaint();	//これをやらないと変化しない
			}
		}
	}
	class ActChooseTreeFont extends ActBase2 {
		public ActChooseTreeFont(ActionMap map){
			super(map);
			putValue(Action.NAME, "choose font");
			putValue(Action.SHORT_DESCRIPTION, "Treeフォントを変更します");
			putValue(Action.SMALL_ICON, 
					new ImageIcon(getClass().getResource("/image/font.png")));
		}
		public void actionPerformed2(ActionEvent e) {
			Font font = FontChooser.showDialog(FrmZeetaMain.this, 
					"choose font", 
					jTree.getFont());
			if( font != null){
				updateFont(font);
			}
		}
		protected void updateFont(Font font){
			cellRenderer_.setFont(font);	//PropertyChangeListenerでは1テンポ遅れる
			jTree.setFont(font);
			
			//保存
			prefs_.putBoolean("tree.font.bold", font.isBold());
			prefs_.putBoolean("tree.font.italic", font.isItalic());
			prefs_.put("tree.font.name", font.getName());
			prefs_.putInt("tree.font.size", font.getSize());
			try {
				prefs_.flush();
			} catch (BackingStoreException e1) {
				throw new RuntimeException(e1);
			}
		}
	}
	class ActChooseContentFont extends ActBase2 {
		public ActChooseContentFont(ActionMap map){
			super(map);
			putValue(Action.NAME, "choose font");
			putValue(Action.SHORT_DESCRIPTION, "タイトルとエディタのフォントを変更します");
			putValue(Action.SMALL_ICON, 
					new ImageIcon(getClass().getResource("/image/font.png")));
		}
		public void actionPerformed2(ActionEvent e) {
			Font font = FontChooser.showDialog(FrmZeetaMain.this, 
					"choose font", 
					inpDocCont.getFont());
			if( font != null){
				updateFont(font);
			}
		}
		protected void updateFont(Font font){
			inpDocTitle.setFont(font);
			inpDocCont.setFont(font);
			
			//保存
			prefs_.putBoolean("content.font.bold", font.isBold());
			prefs_.putBoolean("content.font.italic", font.isItalic());
			prefs_.put("content.font.name", font.getName());
			prefs_.putInt("content.font.size", font.getSize());
			try {
				prefs_.flush();
			} catch (BackingStoreException e1) {
				throw new RuntimeException(e1);
			}
		}
	}
	BookMarkManager bookMarkMan_ = new BookMarkManager(this);
	class ActBookMark extends ActBase2 {
		public ActBookMark(ActionMap map){
			super(map);
			putValue(Action.NAME, "book mark");
			putValue(Action.SHORT_DESCRIPTION, "カレントノードをbookmark");
			putValue(Action.SMALL_ICON, 
					new ImageIcon(getClass().getResource("/image/addbkmrk_co.gif")));

		}
		public void actionPerformed2(ActionEvent e) {
			if( ! viewState_.checkExistSelectingNode()){
				return;
			}
			DocNode node = viewState_.getCurrentNode();
			
			//親DocNodeリスト作成
			bookMarkMan_.addBookMark(node);

		}
	}
	class ActBookMarkNavi extends ActBase2 {
		class  ActNavi extends AbstractAction {
			BookMarkManager.BookMark bkm_;
			public ActNavi(BookMarkManager.BookMark bkm){
				bkm_ = bkm;
				putValue(Action.NAME, bkm_.title_);
			}
			public final void actionPerformed(ActionEvent e) {
				try{
					showDocNode(bkm_.getPath(), true);
				}catch(NotFoundDocOnDB ex){
					bookMarkMan_.removeAndStore(bkm_);
					throw new AppException("[" + bkm_.title_+"] へのパスが見つかりません");
				}
			}
		}
		public ActBookMarkNavi(ActionMap map){
			super(map);
			putValue(Action.NAME, "show book mark");
			putValue(Action.SHORT_DESCRIPTION, "bookmarkリストを表示");
			putValue(Action.SMALL_ICON, 
					new ImageIcon(getClass().getResource("/image/bkmrk_nav.gif")));

		}
		public void actionPerformed2(ActionEvent e) {
			if(bookMarkMan_.bookMarks.size() <= 0){
				throw new AppException("まだ１つもbookmarkが登録されていません");
			}
			
			JPopupMenu menu = getMnuBookmark();
			menu.removeAll();
			for(int i=  bookMarkMan_.bookMarks.size() -1; i >= 0;i--){
				menu.add(new ActNavi(bookMarkMan_.bookMarks.get(i)));
			}
			menu.show(FrmZeetaMain.this, 
					FrmZeetaMain.this.getMousePosition().x, 
					FrmZeetaMain.this.getMousePosition().y);
			
		}
	}
	
	private JButton cmdOkNew = null;
	private JButton cmdCancelNew = null;
	class DetailUpdateListener implements DocumentListener, ActionListener, ListDataListener{
		boolean isDarty_ = false;
		boolean isSortTypeDarty_ = false;
		public void reset(){
			isDarty_ = false;
			isSortTypeDarty_ = false;
			viewState_.setUpdating(false);
		}
		public boolean isDarty(){
			return isDarty_;
		}
		public boolean isSortTypeDarty(){
			return isSortTypeDarty_;
		}
		public void changedUpdate(DocumentEvent e) {
			isDarty_ = true;
			viewState_.setUpdating(true);
		}

		public void insertUpdate(DocumentEvent e) {
			isDarty_ = true;
			viewState_.setUpdating(true);
		}

		public void removeUpdate(DocumentEvent e) {
			isDarty_ = true;
			viewState_.setUpdating(true);
		}

		public void actionPerformed(ActionEvent e) {
			isDarty_ = true;
			viewState_.setUpdating(true);
			if(e.getSource() == getInpSortType()){
				isSortTypeDarty_ = true;
			}
		}
		public void contentsChanged(ListDataEvent e) {
			//Userが手入力されて、且つ、他のフィールドにカーソルが移動した瞬間に発生する
			isDarty_ = true;
			viewState_.setUpdating(true);
		}
		public void intervalAdded(ListDataEvent e) {
			isDarty_ = true;
			viewState_.setUpdating(true);
		}
		public void intervalRemoved(ListDataEvent e) {
			isDarty_ = true;
			viewState_.setUpdating(true);
		}
	};
//	private DetailUpdateListener detailUpdateListener_ = new DetailUpdateListener();  //  @jve:decl-index=0:
	private ActionMap actionMap_ = new ActionMap();
	private JPanel cntWorks = null;
	private JScrollPane contOutputList = null;
	private LstWorks dspWorkList = null;
	private JPanel contButtons = null;
	private JComboBox inpSelectWorkType = null;
	private JButton cmdShowOutputList = null;
	/**
	 * This method initializes jSplitPane	
	 * 	
	 * @return javax.swing.JSplitPane	
	 */
	private JSplitPane getJSplitPane() {
		if (jSplitPane == null) {
			jSplitPane = new JSplitPane();
			jSplitPane.setPreferredSize(new Dimension(50, 50));
			jSplitPane.setDividerSize(8);
			jSplitPane.setResizeWeight(0.8D);
			jSplitPane.setContinuousLayout(false);
			jSplitPane.setLeftComponent(getLeft());
			jSplitPane.setRightComponent(getJTabbedPane());
			jSplitPane.setOneTouchExpandable(true);
		}
		return jSplitPane;
	}
	JPanel left=null;
	private JPanel getLeft(){
		if (left == null) {
			left = new JPanel();
			left.setLayout(new BorderLayout());
			left.add(getCntScrTree(), BorderLayout.CENTER);
		}
		return left;
	}

	/**
	 * This method initializes jTree	
	 * 	
	 * @return javax.swing.JTree	
	 */
	private ZTree getJTree() {
		if (jTree == null) {
			jTree = new ZTree();
			jTree.setTreeType(TREE_TYPE_MAIN);
			jTree.setCanPasteNodeFromAnotherProcess(true);
			jTree.setShowsRootHandles(true);
//			jTree.setToggleClickCount(0);
			jTree.getSelectionModel().
				setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			
			//リスナ登録
			jTree.addTreeExpansionListener(new javax.swing.event.TreeExpansionListener() {
				public void treeExpanded(javax.swing.event.TreeExpansionEvent e) {
					Object node = e.getPath().getLastPathComponent();
					docModel_.addMagoDocFromDb((DocNode)node);
				}
				public void treeCollapsed(javax.swing.event.TreeExpansionEvent e) {
				}
			});
			jTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
				boolean isSelfSelecting_ = false;
				public void valueChanged(javax.swing.event.TreeSelectionEvent e) {
					if(isSelfSelecting_){	//このメソッド内で選択した場合
						return;
					}
					try{
						actionMap_.get(ActUpdateIfDarty.class).actionPerformed(
								new ActionEvent(e.getSource(), 0, ActUpdateIfDarty.class.getName())
						);
					}catch(AppException e1){
						log.debug("AppException ");
						isSelfSelecting_ = true;
						jTree.setSelectionPath(new TreePath(viewState_.getCurrentNode().getPath()));
						isSelfSelecting_ = false;
						throw e1;
					}
					
					//選択されたノードをdetailに表示
					if( !(e.getPath().getLastPathComponent() instanceof DocNode)){
						return;
					}
					DocNode node = (DocNode)e.getPath().getLastPathComponent();
					
					nodeHistory_.add(node);
					
//					setDetail((Youken)node.getYouken());  これじゃあかんのや。
					//よそで更新されとるかもしれんのんで読み込みなおしや
//					youkenModel_.reloadYouken(node);
					//★上を行うとツリーの展開状態が保存されない
					//が、共有要件がリロードされないという問題もあるので止めた。
					//結局、リロードは、何かアクションがあったときにModel側で行っている。

					viewState_.setCurrentNode(node);

					//★ノードチェンジは、DlgNodeListもリスナとして登録している
					//ことを忘れるな
					
					jTree.repaint();	//共有ノードの表示を変えたりするため
				}
			});
			jTree.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if(e.getButton() == MouseEvent.BUTTON3){	//右ボタン
						if(viewState_.getCurrentNode() == null){
							throw new AppException("どれかのノードを選択してください");
						}
						getMnuTreePopup().show(jTree, e.getX(), e.getY());
					}
				}
			});
//			jTree.addMouseMotionListener(new java.awt.event.MouseMotionListener() {
//				public void mouseMoved(java.awt.event.MouseEvent e) {
//					TreePath path = jTree.getClosestPathForLocation(e.getX(), e.getY());
//					System.out.println(path.getLastPathComponent());
//				}
//				public void mouseDragged(java.awt.event.MouseEvent e) {
//				}
//			});
		}
		return jTree;
	}
	class NodeHistory {
		int MAX_SIZE = 100;
		int curIndex_ = -1;
		List<DocNode> history_ = new LinkedList<DocNode>();		//初めて使うLinkedList
		
		public void add(DocNode docNode){
			if(getCurrentNode() != null && !getCurrentNode().equals(docNode)){
				if( curIndex_ < (history_.size() -1)){
					//histry backしている最中
					
					//0.9.06までは、以下のようにしていたが止めた
					//　curIndex_以降を削除する
//					while(true){
//						history_.remove(history_.size()-1);
//						if( curIndex_ >= (history_.size() -1)){
//							break;
//						}
//					}
				}

				history_.add(docNode);
				if(history_.size() > MAX_SIZE){
					history_.remove(0);
				}
				curIndex_ = history_.size() - 1;
			}else if(curIndex_ == -1){
				history_.add(docNode);
				curIndex_ = history_.size() - 1;
			}
		}
		public DocNode getCurrentNode(){
			//TODO 削除されていることを考慮すること
			if(curIndex_ >= 0 ){
				return history_.get(curIndex_);
			}else{
				return null;
			}
		}
		public DocNode back(){
			DocNode ret = null;
			if( curIndex_ > 0){
				curIndex_--;
				ret = history_.get(curIndex_);
				//TODO 削除されていることを考慮すること
			}
			return ret;
		}
		public DocNode forward(){
			if( curIndex_ < (history_.size() -1)){
				curIndex_++;
			}
			return history_.get(curIndex_);
		}
	}
	NodeHistory nodeHistory_ = new NodeHistory();
	
	//NodeInfoタブ用
	class NodeInfo implements TreeSelectionListener, ChangeListener{
		List<DocNode> parents_ = new ArrayList<DocNode>();
		
		NodeInfo(){
			getJTabbedPane().addChangeListener(this);
		}
		
		
		PnlNodeList.SelectionLintener sl_ =	new PnlNodeList.SelectionLintener(){
			public void process(Doc doc) {
				for(DocNode dn: parents_){
					if(dn.getDoc().equals(doc)){
						class selectDocNode implements Runnable{
							DocNode dn_;  //  @jve:decl-index=0:
							public selectDocNode(DocNode dn){
								dn_ = dn;
							}
							public void run() {
//								FrmZeetaMain main = (FrmZeetaMain)getOwner();
								FrmZeetaMain.this.selectDocNode(dn_);
							}
						}
						SwingUtilities.invokeLater(new selectDocNode(dn));
					}
				}
			}
			public void setRootNode(DocNode rootNode){}
		};

		
		
		public void valueChanged(TreeSelectionEvent e) {
			//選択されたitem
			if( !(getJTabbedPane().getSelectedComponent().getName().equals(
					getCntNodeInfo().getName())
				)
			){
				return;
			}
			//選択されたノードをdetailに表示
			DocNode dn = (DocNode)e.getPath().getLastPathComponent();
			refreshNodeInfo(dn);
			
			//スライダーの設定
			viewState_.setCurrentNode(dn);
			setDepthFlag_ = true;
			if( jTree.isExpanded(new TreePath(dn.getPath())) ){
				getInpDepth().setValue(1);
			}else{
				getInpDepth().setValue(0);
			}
			setDepthFlag_ = false;
			
			//Link Info はクリア
			dspLinkedUser.setText("");
			dspLinkedDate.setText("");

		}
		public void refreshNodeInfo(DocNode curNode){
			if( curNode == null){
				return;
			}
			//親DocNodeリスト作成
			parents_.clear();
			DocNode node = (DocNode)curNode.getParent();
			while(node != null){
				parents_.add(0, node);
				node = (DocNode)node.getParent();
			}
			//自身も追加
			parents_.add(curNode);

			//親Docリスト作成
			List<Doc> docs = new ArrayList<Doc>();
			for(DocNode dn: parents_){
				docs.add(dn.getDoc());
			}
			getPnlParents().setup(docs, sl_);
			getPnlParents().getDspNodes2().getSelectionModel()
				.setSelectionInterval(docs.size()-1, docs.size()-1);
			
			//children
			dspChildren.setText("children : "+curNode.getChildCount());
		}
		public void stateChanged(ChangeEvent arg0) {
			if( !(getJTabbedPane().getSelectedComponent().getName().equals(
					getCntNodeInfo().getName())
				)
			){
				return;
			}
			//選択されたノードをdetailに表示
			refreshNodeInfo(viewState_.getCurrentNode());
		}

	}
//	NodeInfo nodeInfo_ = new NodeInfo();  //  ここで生成すると何も表示されないのだ
	NodeInfo nodeInfo_ = null;
	
	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getCntScrTree() {
		if (cntScrTree == null) {
			cntScrTree = new JScrollPane();
			cntScrTree.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			cntScrTree.setViewportView(getJTree());
			cntScrTree.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		}
		return cntScrTree;
	}

	/**
	 * This method initializes jTabbedPane	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */
	private JTabbedPane getJTabbedPane() {
		if (jTabbedPane == null) {
			jTabbedPane = new JTabbedPane();
			jTabbedPane.setName("");
			jTabbedPane.addTab("node detail", null, getCntDetail(), null);
			jTabbedPane.addTab("more info", null, getCntNodeInfo(), null);
			jTabbedPane.addTab("works", null, getCntWorks(), null);
		}
		return jTabbedPane;
	}

	/**
	 * This method initializes cntDetail	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntDetail() {
		if (cntDetail == null) {
			BorderLayout borderLayout = new BorderLayout();
			borderLayout.setHgap(5);
			borderLayout.setVgap(5);
			cntDetail = new JPanel();
			cntDetail.setLayout(borderLayout);
			cntDetail.add(getCntScrDocCont(), BorderLayout.CENTER);
			cntDetail.add(getJPanel2(), BorderLayout.SOUTH);
			cntDetail.add(getJPanel3(), BorderLayout.NORTH);
			cntDetail.setName("cntDetail");
		}
		return cntDetail;
	}

	/**
	 * This method initializes inpYoukenTitle	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getInpDocTitle() {
		if (inpDocTitle == null) {
			inpDocTitle = new JTextField();
		}
		return inpDocTitle;
	}

	/**
	 * This method initializes jScrollPane1	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getCntScrDocCont() {
		if (cntScrDocCont == null) {
			cntScrDocCont = new JScrollPane();
			cntScrDocCont.setViewportView(getInpDocCont());
		}
		return cntScrDocCont;
	}

	/**
	 * This method initializes inpYoukenNaiyou	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JTextArea getInpDocCont() {
		if (inpDocCont == null) {
			inpDocCont = new JTextArea();
			inpDocCont.setTabSize(4);
			inpDocCont.setLineWrap(
					prefs_.getBoolean(LINE_WRAP, true));
//			inpYoukenNaiyou.setLineWrap(true);
			inpDocCont.setTabSize(4);
			inpDocCont.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if(e.getButton() == MouseEvent.BUTTON3){
						getMnuNaiyou().show(inpDocCont, e.getX(), e.getY());
					}
				}
			});
		}
		return inpDocCont;
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jPanel1 = new JPanel();
			jPanel1.setLayout(new BorderLayout());
			jPanel1.add(getJSplitPane(), BorderLayout.CENTER);
			jPanel1.add(getJToolBar(), BorderLayout.NORTH);
		}
		return jPanel1;
	}

	/**
	 * This method initializes jToolBar	
	 * 	
	 * @return javax.swing.JToolBar	
	 */
	private JToolBar getJToolBar() {
		if (jToolBar == null) {
			jToolBar = new JToolBar();
			jToolBar.setLayout(new BoxLayout(getJToolBar(), BoxLayout.X_AXIS));
		}
		return jToolBar;
	}

	/**
	 * This method initializes mnuNaiyou	
	 * 	
	 * @return javax.swing.JPopupMenu	
	 */
	private JPopupMenu getMnuNaiyou() {
		if (mnuNaiyou == null) {
			mnuNaiyou = new JPopupMenu();
			mnuNaiyou.add(getCmdToggleLineWrap());
			mnuNaiyou.add(new ActChooseContentFont(null));
		}
		return mnuNaiyou;
	}

	/**
	 * This method initializes mnuTreePopup	
	 * 	
	 * @return javax.swing.JPopupMenu	
	 */
	private JPopupMenu getMnuTreePopup() {
		if (mnuTreePopup == null) {
			mnuTreePopup = new JPopupMenu();
		}
		return mnuTreePopup;
	}
	/**
	 * This method initializes mnuTreePopup	
	 * 	
	 * @return javax.swing.JPopupMenu	
	 */
	private JPopupMenu getMnuBookmark() {
		if (mnuBookmark == null) {
			mnuBookmark = new JPopupMenu();
		}
		return mnuBookmark;
	}


	/**
	 * This method initializes jPanel2	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 0;
			GridBagConstraints gridBagCancel = new GridBagConstraints();
			gridBagCancel.gridx = 3;
			gridBagCancel.fill = GridBagConstraints.HORIZONTAL;
			gridBagCancel.weightx = 0.1;
//			gridBagCancel.ipadx = 10;
			gridBagCancel.gridy = 1;
			GridBagConstraints gridBagOk = new GridBagConstraints();
			gridBagOk.gridx = 4;
			gridBagOk.anchor = GridBagConstraints.EAST;
			gridBagOk.insets = new Insets(3, 3, 3, 3);
//			gridBagOk.ipadx = 10;
			gridBagOk.fill = GridBagConstraints.HORIZONTAL;
			gridBagOk.weightx = 0.1;
			gridBagOk.gridy = 1;
			GridBagConstraints gridBagLinkNode = new GridBagConstraints();
			gridBagLinkNode.fill = GridBagConstraints.HORIZONTAL;
			gridBagLinkNode.gridx = 0;
			gridBagLinkNode.gridy = 1;
//			gridBagConstraints4.ipadx = 10;
			gridBagLinkNode.weightx = 1.0;
			gridBagLinkNode.anchor = GridBagConstraints.WEST;
			gridBagLinkNode.ipady = 0;
			gridBagLinkNode.gridwidth = 2;
			gridBagLinkNode.insets = new Insets(3, 3, 3, 3);
			GridBagConstraints gridBagConstSortType = new GridBagConstraints();
			gridBagConstSortType.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstSortType.gridx = 3;
			gridBagConstSortType.gridy = 0;
//			gridBagConstSortType.ipadx = 10;
			gridBagConstSortType.weightx = 0.1;
			gridBagConstSortType.anchor = GridBagConstraints.WEST;
			gridBagConstSortType.gridwidth = 2;
			gridBagConstSortType.insets = new Insets(3, 3, 3, 3);
			GridBagConstraints gridBagDate = new GridBagConstraints();
			gridBagDate.fill = GridBagConstraints.HORIZONTAL;
			gridBagDate.gridx = 1;
			gridBagDate.gridy = 0;
			gridBagDate.weightx = 1.0;
			gridBagDate.anchor = GridBagConstraints.WEST;
			gridBagDate.gridwidth = 1;
			gridBagDate.insets = new Insets(3, 3, 3, 3);
			jPanel2 = new JPanel();
			jPanel2.setLayout(new GridBagLayout());
//			jPanel2.setPreferredSize(new Dimension(1, 64));
			jPanel2.add(getInpSortType(), gridBagConstSortType);
			jPanel2.add(getInpDate(), gridBagDate);
			jPanel2.add(getInpUser(), gridBagLinkNode);
			jPanel2.add(getCmdOkNew(), gridBagOk);
			jPanel2.add(getCmdCancelNew(), gridBagCancel);
			jPanel2.add(getInpChkLinkNode(), gridBagConstraints);
		}
		return jPanel2;
	}

	/**
	 * This method initializes inpDate	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JFormattedTextField getInpDate() {
		if (inpDate == null) {
			inpDate = new JFormattedTextField(new DateTextFormatter());
//			inpDate.setBorder(new TitledBorder("date"));
		}
		return inpDate;
	}

	/**
	 * This method initializes inpSortType	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getInpSortType() {
		if (inpSortType == null) {
			inpSortType = new JComboBox();
//			inpSortType.setBorder(new TitledBorder("sort"));
		}
		return inpSortType;
	}

	/**
	 * This method initializes cmdToggleLineWrap	
	 * 	
	 * @return javax.swing.JCheckBoxMenuItem	
	 */
	private JCheckBoxMenuItem getCmdToggleLineWrap() {
		if (cmdToggleLineWrap == null) {
			cmdToggleLineWrap = new JCheckBoxMenuItem();
			cmdToggleLineWrap.setText("line wrap");
			cmdToggleLineWrap.setSelected(getInpDocCont().getLineWrap());
			cmdToggleLineWrap.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					inpDocCont.setLineWrap(
							e.getStateChange() == ItemEvent.SELECTED);
					prefs_.putBoolean(LINE_WRAP, inpDocCont.getLineWrap());
					
					try {
						prefs_.flush();
					} catch (BackingStoreException e1) {
						throw new RuntimeException(e1);
					}
				}
			});
		}
		return cmdToggleLineWrap;
	}

	/**
	 * VE用コンストラクタ
	 * This is the default constructor
	 */
	public FrmZeetaMain() {
		super();
		//アクションマップの生成(initializeの前に行う必要がある)
		new ActClipNode(actionMap_);
		new ActCancelNewYouken(actionMap_).setEnabled(false);
		new ActCommitDoc(actionMap_).setEnabled(false);
		new ActPasteNodeMove(actionMap_);
		new ActPasteNodeCopy(actionMap_);
		new ActPrepareCreateDocAsChild(actionMap_);
		new ActPrepareCreateDocAsSibling(actionMap_);
		new ActRefreshNode(actionMap_);
		new ActRefreshCurrent(actionMap_);
		new ActUpdateIfDarty(actionMap_);
		new ActUpdateDoc(actionMap_);
		new ActRemoveDoc(actionMap_);
		new ActDuplicateDoc(actionMap_);
		new ActShowParents(actionMap_);
		new ActMoveUp(actionMap_);
		new ActMoveDown(actionMap_);
		new ActShowSearchWindow(actionMap_);
		new ActExpandAll(actionMap_);
		new ActShowDebugWindow(actionMap_);
		new ActShowReverseTreeView(actionMap_);
		new ActShow2ndTreeView(actionMap_);
		new ActShowExportView(actionMap_);
		new ActShowSummaryView(actionMap_);
		new ActShowWorkUpdater(actionMap_);
		new ActRemoveWork(actionMap_);

		new ActShowOutputList(actionMap_);
		new ActShowOutputListForWork(actionMap_);
		new ActShowToolsWindow(actionMap_);

		new ActFocusOnTitle(actionMap_);
		new ActChooseTreeFont(actionMap_);
		new ActBookMark(actionMap_);
		new ActBookMarkNavi(actionMap_);
		
		initialize();
	}
	
	/**
	 * 実行時は必ずこれを呼び出すこと（VEでの編集時とDB操作を分離する）
	 */
	public void setup(){
		
		//画面状態の復元
		restoreForm();
		
		String smartDnD_str = System.getProperty("smartDnD");
		if( smartDnD_str != null && "1".equals(smartDnD_str)){
			getJTree().setNeedShiftForMoveNode(false);
		}

        //Treeモデル
        docModel_ = new DocModel();
        docModel_.initialize();
        getJTree().setModel(docModel_);
		
		//==== jTreeの設定
		cellRenderer_ = new DocTreeCellRenderer(docModel_);
		jTree.setCellRenderer(cellRenderer_);
//		jTree.addPropertyChangeListener("font",cellRenderer_);
		//DnD
		TreeNodeTransferHandler transferHandler = 
			new TreeNodeTransferHandler2(this, jTree, actionMap_.get(ActRefreshNode.class));
		jTree.setTransferHandler(transferHandler);
		jTree.setDragEnabled(true);
		//Treeのショートカットとボタン登録
		setupTreeAction();
		
		//作業リスト
		getDspWorkList().setModel(new OutputOfWorkListModel());
		getDspWorkList().setup();
		getDspWorkList().getActionMap().put(LstWorks.DOUBLE_CLICK_ACTION_KEY,
				actionMap_.get(ActShowWorkUpdater.class));
		getDspWorkList().getPopupMenu().add(actionMap_.get(ActShowWorkUpdater.class));
		getDspWorkList().getPopupMenu().add(actionMap_.get(ActRemoveWork.class));

		//Treeのリスナー登録
		nodeInfo_ = new NodeInfo();  //  @jve:decl-index=0:
		getFrmParentList().setup(docModel_);
		getJTree().addTreeSelectionListener(getFrmParentList());
		getJTree().addTreeSelectionListener(getDspWorkList());
		getJTree().addTreeSelectionListener(getDlgReverseTree());
		getJTree().addTreeSelectionListener(nodeInfo_);
//		getJTree().addTreeSelectionListener(getDlg2ndTree());	同期はしない
		
		//ComboBoxの設定
		setupComboBox();
		
		//各モデルにupdateリスナーを追加(汚れたことの判定)
		DetailUpdateListener dl = viewState_.getDetailState();
		getInpDocTitle().getDocument().addDocumentListener(dl);
		getInpDocCont().getDocument().addDocumentListener(dl);
		getInpDate().getDocument().addDocumentListener(dl);
		getInpUser().addActionListener(dl);
		getInpUser().getModel().addListDataListener(dl);
		getInpSortType().addActionListener(dl);
		getInpChkLinkNode().addActionListener(dl);
		
		//toolbarの生成
		getJToolBar().add(actionMap_.get(ActMoveUp.class));
		getJToolBar().add(actionMap_.get(ActMoveDown.class));
		getJToolBar().add(actionMap_.get(ActPrepareCreateDocAsChild.class));
		getJToolBar().add(actionMap_.get(ActPrepareCreateDocAsSibling.class));
		getJToolBar().add(actionMap_.get(ActRemoveDoc.class));
		getJToolBar().add(actionMap_.get(ActDuplicateDoc.class));
		getJToolBar().add(actionMap_.get(ActRefreshCurrent.class));
		getJToolBar().add(actionMap_.get(ActExpandAll.class));
		getJToolBar().addSeparator();
		getJToolBar().add(actionMap_.get(ActBookMark.class));
		getJToolBar().add(actionMap_.get(ActBookMarkNavi.class));
		getJToolBar().addSeparator();
		getJToolBar().add(actionMap_.get(ActShowParents.class));
		getJToolBar().add(actionMap_.get(ActShowSearchWindow.class));
//		getJToolBar().add(actionMap_.get(ActBackup.class));
		getJToolBar().add(actionMap_.get(ActShow2ndTreeView.class));
		getJToolBar().add(actionMap_.get(ActShowReverseTreeView.class));
		getJToolBar().add(actionMap_.get(ActShowExportView.class));
		getJToolBar().add(actionMap_.get(ActShowSummaryView.class));
		getJToolBar().addSeparator();
		getJToolBar().add(actionMap_.get(ActShowOutputList.class));
		getJToolBar().addSeparator();
		getJToolBar().add(actionMap_.get(ActShowToolsWindow.class));
		getJToolBar().addSeparator();
		
		//Treeのポップアップメニューの生成
		getMnuTreePopup().add(actionMap_.get(ActPrepareCreateDocAsChild.class));
		getMnuTreePopup().add(actionMap_.get(ActPrepareCreateDocAsSibling.class));
		getMnuTreePopup().add(actionMap_.get(ActRemoveDoc.class));
		getMnuTreePopup().add(actionMap_.get(ActDuplicateDoc.class));
		getMnuTreePopup().add(actionMap_.get(ActExpandAll.class));
		getMnuTreePopup().add(actionMap_.get(ActRefreshCurrent.class));
		getMnuTreePopup().add(actionMap_.get(ActMoveUp.class));
		getMnuTreePopup().add(actionMap_.get(ActMoveDown.class));
		getMnuTreePopup().addSeparator();
		getMnuTreePopup().add(actionMap_.get(ActBookMark.class));
		getMnuTreePopup().add(actionMap_.get(ActBookMarkNavi.class));
		getMnuTreePopup().addSeparator();
//		getMnuTreePopup().add(actionMap_.get(ActShowParents.class));
		getMnuTreePopup().add(actionMap_.get(ActShow2ndTreeView.class));
		getMnuTreePopup().add(actionMap_.get(ActShowReverseTreeView.class));
		getMnuTreePopup().add(actionMap_.get(ActChooseTreeFont.class));
		
		//上記以外のAction登録
		getCmdShowOutputList().setAction(actionMap_.get(ActShowOutputListForWork.class));
		
		//KeyEventDispatcher登録
		java.awt.KeyboardFocusManager.
			getCurrentKeyboardFocusManager().
				addKeyEventDispatcher(new MainViewKeyDispatcher(actionMap_, this));

		//WindowListener登録
		this.addWindowListener(
			new WindowAdapter(){
				public void windowClosing(WindowEvent e) {
					log.debug(e);
					//更新チェック
					actionMap_.get(ActUpdateIfDarty.class).actionPerformed(null);
					//最後に選択していたノードを保存
					TreeNode[] paths = viewState_.getCurrentNode().getPath();
					String pathById = makePathById(paths);
					log.debug(LAST_SELECTED_NODE_ID+"="+pathById);
//					prefs_.put(LAST_SELECTED_NODE_ID, pathsById);	//選択中のノードを記憶
					prefs_.put(DEVIDER_LOC_KEY, ""+getJSplitPane().getDividerLocation());
				}
			}
			
		);
//		getInpOutputList2().setModel(new DefaultTableModel(
//				new Object[][] {{"aaa"},{"bbb"}, {"ccc"}}, new Object[] {"1"})
//		);

		getJSplitPane().setDividerLocation(
			Integer.parseInt(
					prefs_.get(DEVIDER_LOC_KEY, 
						""+getJSplitPane().getDividerLocation())
			)
		);

		//fontの復元
		Font orgFont = getJTree().getFont();
		Font f = new Font(
				prefs_.get("tree.font.name", orgFont.getName()), 
				(prefs_.getBoolean("tree.font.bold", orgFont.isBold()) ? Font.BOLD : 0)
				+ (prefs_.getBoolean("tree.font.italic", orgFont.isItalic()) ? Font.ITALIC : 0), 
				prefs_.getInt("tree.font.size", orgFont.getSize()));
		getJTree().setFont(f);
		orgFont = getInpDocCont().getFont();
		f = new Font(
				prefs_.get("content.font.name", orgFont.getName()), 
				(prefs_.getBoolean("content.font.bold", orgFont.isBold()) ? Font.BOLD : 0)
				+ (prefs_.getBoolean("content.font.italic", orgFont.isItalic()) ? Font.ITALIC : 0), 
				prefs_.getInt("content.font.size", orgFont.getSize()));
		getInpDocTitle().setFont(f);
		getInpDocCont().setFont(f);
		
		//選択ノードの復元（保存してないので実質未使用）
		long lastId = prefs_.getLong(LAST_SELECTED_NODE_ID, -1);
		if(lastId == -1){
	        jTree.setSelectionRow(0);
		}else{
			DocNode cur = showDocNode(lastId, false);
			jTree.setSelectionPath(new TreePath(cur.getPath()));
		}
		
	}

	private void setupComboBox() {
		//作業者の設定
		inpUser.setModel(MasterComboModel.newUserComboBoxModel());
		inpUser.getModel().setSelectedItem("");

		//ソート順コンボの設定
		inpSortType.setModel(MasterComboModel.newSortTypeComboBoxModel());
		
		//作業種類コンボの設定
		inpSelectWorkType.setModel(MasterComboModel.newWorkTypeComboBoxModel());
	}
	void setupTreeAction(){
		ActionMap treeActMap = jTree.getActionMap();
		InputMap treeInputMap = jTree.getInputMap();

		//DELキー
		treeActMap.put(ActRemoveDoc.class, actionMap_.get(ActRemoveDoc.class));
		treeInputMap.put(
				KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), 
				ActRemoveDoc.class);

		//F2, enter
		treeActMap.put(ActFocusOnTitle.class, actionMap_.get(ActFocusOnTitle.class));
		treeInputMap.put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), 
				ActFocusOnTitle.class);
		treeInputMap.put(
				KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), 
				ActFocusOnTitle.class);
		
		//ctrl+C/X/V
		class DelegateTreeAction extends AbstractAction {
			Action act_;
			public DelegateTreeAction(Action act) {
				putValue(Action.NAME, act.getValue(Action.NAME));
				act_ = act;
			}
			public void actionPerformed(ActionEvent e) {
				e.setSource(jTree);		//これがミソや！！！
				act_.actionPerformed(e);
			}
		}

		try{
			Action act;
			Object key = 
				treeInputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
			act = new DelegateTreeAction(treeActMap.get(key));
			act.putValue(Action.SHORT_DESCRIPTION, "Treeノードのコピー(ctrl+C)"); 
			act.putValue(Action.SMALL_ICON, 
				new ImageIcon(getClass().getResource("/image/copy.gif")));
			getJToolBar().add(act);
			actionMap_.put(ACTKEY_NODE_COPY, act);
			
			key =treeInputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK));
			act = new DelegateTreeAction(treeActMap.get(key));
			act.putValue(Action.SHORT_DESCRIPTION, "Treeノードをペースト時にカット(ctrl+X)"); 
			act.putValue(Action.SMALL_ICON, 
				new ImageIcon(getClass().getResource("/image/cut.gif")));
			getJToolBar().add(act);
			actionMap_.put(ACTKEY_NODE_CUT, act);
			
			key =treeInputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK));
			act = new DelegateTreeAction(treeActMap.get(key));
			act.putValue(Action.SHORT_DESCRIPTION, "Treeノードのペースト(ctrl+V)"); 
			act.putValue(Action.SMALL_ICON, 
				new ImageIcon(getClass().getResource("/image/paste.gif")));
			getJToolBar().add(act);
			actionMap_.put(ACTKEY_NODE_PASTE, act);
		}catch(NullPointerException ex){
			//TODO　MACの場合このエラーが発生する
			//act = new DelegateTreeAction(treeActMap.get(key));
			//暫定対応として、このエラーを無視し、ショートカットが使用できなくする
			Action act =new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					//なにもしない
					setEnabled(false);
				}
			};
			actionMap_.put(ACTKEY_NODE_COPY, act);
			actionMap_.put(ACTKEY_NODE_CUT, act);
			actionMap_.put(ACTKEY_NODE_PASTE, act);
		}

	}
	
	
	
	public void initState(){
		viewState_.setNeutral();
	}
	String makePathById(TreeNode[] paths) {
		String sep= "";
		StringBuffer sb = new StringBuffer();
		for(int i=0; i < paths.length; i++){
			sb.append(sep);
			sb.append(((DocNode)paths[i]).getDoc().getDocId());
			sep = ",";
		}
		String pathsById = new String(sb);
		return pathsById;
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(640, 507);
		this.setContentPane(getJContentPane());
		this.setTitle(getTitle());
		
		//要件内容の行折り返しの設定
		inpDocCont.setLineWrap(getCmdToggleLineWrap().isSelected());

		//Title,Textのundo, redo
		titleUndoHandler_.setup(inpDocTitle);
		textUndoHandler_.setup(inpDocCont);
	}
	private DlgReverseTree getDlgReverseTree(){
		if(revTree_ == null){
			revTree_ = new DlgReverseTree(FrmZeetaMain.this);
			revTree_.setLocationRelativeTo(FrmZeetaMain.this);
			revTree_.setup();
		}
		return revTree_;
	}
	private Dlg2ndTree getDlg2ndTree(){
		if(secndTree_ == null){
			secndTree_ = new Dlg2ndTree(FrmZeetaMain.this);
			secndTree_.setLocationRelativeTo(FrmZeetaMain.this);
			secndTree_.setup(FrmZeetaMain.this, jTree);
			
		}
		return secndTree_;
	}
	private DlgParentDocList getFrmParentList(){
		if(frmParentList_== null){
			//親一覧
			frmParentList_ =new DlgParentDocList(this);
			frmParentList_.setLocationRelativeTo(this);
		}
		return frmParentList_;
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
			jContentPane.add(getJPanel1(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

	/**
	 * This method initializes inpUser	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getInpUser() {
		if (inpUser == null) {
			inpUser = new JComboBox();
//			inpUser.setBorder(new TitledBorder("creator"));
			inpUser.setEditable(true);
		}
		return inpUser;
	}

	/**
	 * This method initializes jPanel3	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel3() {
		if (jPanel3 == null) {
			BorderLayout borderLayout1 = new BorderLayout();
			borderLayout1.setHgap(2);
			jPanel3 = new JPanel();
			jPanel3.setLayout(borderLayout1);
			jPanel3.add(getDspDocId(), java.awt.BorderLayout.WEST);
			jPanel3.add(getInpDocTitle(), BorderLayout.CENTER);
		}
		return jPanel3;
	}

	/**
	 * This method initializes dspYoukenId	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getDspDocId() {
		if (dspYoukenId == null) {
			dspYoukenId = new JTextField();
			dspYoukenId.setEditable(false);
			dspYoukenId.setPreferredSize(new Dimension(50, 20));
			dspYoukenId.setBorder(new BevelBorder(BevelBorder.LOWERED));
			
			dspYoukenId.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					if(e.getClickCount() >= 2){
						Writer writer = new ClipboardStringWriter();
						try {
							writer.write(getDspDocId().getText() +" : " 
									+ inpDocTitle.getText());
							writer.close();
						} catch (IOException ex) {
							throw new RuntimeException(ex);
						}
					}
				}
			});
		}
		return dspYoukenId;
	}

	/**
	 * This method initializes cmdOkNew	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdOkNew() {
		if (cmdOkNew == null) {
			cmdOkNew = new JButton();
			cmdOkNew.setAction(actionMap_.get(ActCommitDoc.class));
		}
		return cmdOkNew;
	}

	/**
	 * This method initializes cmdCancelNew	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdCancelNew() {
		if (cmdCancelNew == null) {
			cmdCancelNew = new JButton();
			cmdCancelNew.setAction(actionMap_.get(ActCancelNewYouken.class));
		}
		return cmdCancelNew;
	}


	public DocNode showOyaDocNode(long oyaId, long koId, boolean mark) {
		DocNode oyaNode = showDocNode(oyaId, false);
		if(mark){
			//子に印を付ける
			Enumeration children = oyaNode.children();
			while(children.hasMoreElements()){
				DocNode child = (DocNode)children.nextElement();
				if( koId == ((Doc)child.getDoc()).getDocId()){
					cellRenderer_.setMarkingNode(child);
					jTree.scrollPathToVisible(new TreePath(child.getPath()));
					jTree.repaint();
					break;
				}
			}
		}
		return oyaNode;
	}
	public void selectDocNode(DocNode node) {
		jTree.scrollPathToVisible(new TreePath(node.getPath()));
		TreePath tp = new TreePath(node.getPath());
		jTree.setSelectionPath(tp);
		jTree.scrollPathToVisible(tp);
	}
	public DocNode showDocNode( List<Long> idPath, boolean select){
		DocNode leaf = expandIdPath(0, (DocNode)docModel_.getRoot(), idPath);
		if(select){
			showDetailAndSelectWord(leaf, (String)null);
		}
		return leaf;
	}
	public DocNode showDocNode(DocNode startNode, long id, List<Doc> parents, boolean mark){
//			boolean mark, boolean select, String search) {
		List<Long> idPath = new ArrayList<Long>();
		for(Doc parent:parents){
			idPath.add(parent.getDocId());
		}
		
		jTree.expandPath(new TreePath( startNode.getPath() ) );
		DocNode leaf = expandIdPath(startNode.getLevel(), startNode, idPath);
		jTree.scrollPathToVisible(new TreePath(leaf.getPath()));
		
		if(mark){
			//Treeに印を付ける
			cellRenderer_.setMarkingNode(leaf);
			jTree.repaint();
		}
//		if(select){
//			showDetailAndSelectWord(leaf, search);
//		}
		return leaf;
	}
	void showDetailAndSelectWord(DocNode leaf, String[] searchTexts){
		warnningIfNotNeutral();
		showDocNode(leaf);
		if(searchTexts==null){
			return;
		}
		
		//一番近くで見つかったキーワード
		int minP = Integer.MAX_VALUE;
		for(String searchText: searchTexts){
			//タイトル
			int p = inpDocTitle.getText().toLowerCase().indexOf(searchText.toLowerCase());
			if(p >= 0){
				if(minP > p){
					minP = p;
					selectText(inpDocTitle, p, searchText.length());
				}
			}
		}
		if( minP == Integer.MAX_VALUE){
			for(String searchText: searchTexts){
				//text
				int p = inpDocCont.getText().toLowerCase().indexOf(searchText.toLowerCase());
				if(p >= 0){
					if(minP > p){
						minP = p;
						scrollDocContTo(p);
						selectText(inpDocCont, p, searchText.length());
					}
				}
			}
			if( minP != Integer.MAX_VALUE){
				SwingUtilities.invokeLater(requestFocusToInpDocCont_);
			}
		}else{
			SwingUtilities.invokeLater(requestFocusToInpDocTitle_);
		}
	}
	void showDetailAndSelectWord(DocNode leaf, String searchText){
		warnningIfNotNeutral();
		showDocNode(leaf);
		if(searchText==null){
			return;
		}
		
		//タイトル
		int p = inpDocTitle.getText().toLowerCase().indexOf(searchText.toLowerCase());
		if(p >= 0){
			selectText(inpDocTitle, p, searchText.length());
			SwingUtilities.invokeLater(requestFocusToInpDocTitle_);
		}else{
			//text
			p = inpDocCont.getText().toLowerCase().indexOf(searchText.toLowerCase());
			if(p >= 0){
				scrollDocContTo(p);
				selectText(inpDocCont, p, searchText.length());
				SwingUtilities.invokeLater(requestFocusToInpDocCont_);
			}
		}
	}
	void showDocNode(DocNode leaf){
		TreePath tp = new TreePath(leaf.getPath());
		jTree.setSelectionPath(tp);
		jTree.scrollPathToVisible(tp);
	}
	// return false-文字列が見つからない, true-みつかった
	public boolean findText(String search){
		boolean ret = false;
		if(search != null){
			int cp = inpDocCont.getCaretPosition();
			int p = inpDocCont.getText().toLowerCase().indexOf(search.toLowerCase(), cp);
			if(p >= 0){
				ret = true;
				scrollDocContTo(p);
				selectText(inpDocCont, p, search.length());
				SwingUtilities.invokeLater(requestFocusToInpDocCont_);
			}
		}
		return ret;
	}
	void scrollDocContTo(int startPoint){
		Rectangle rect;
		try {
			rect = inpDocCont.modelToView(startPoint);
		} catch (BadLocationException e) {
			throw new RuntimeException(e);
		}
		getCntScrDocCont().getViewport().setViewPosition(new Point(0, rect.y));
		inpDocCont.invalidate();	//これを入れないと、下にゴミが残る場合がある
	}
	void selectText(JTextComponent tc, int startPoint, int len){
		tc.setCaretPosition(startPoint);
		tc.select(startPoint, startPoint+len);
	}
	public DocNode showDocNode(long id, boolean mark) {
		List<Long> idPath = docModel_.getPathToDocRoot(id);
		
		//rootを展開
		DocNode root = (DocNode)docModel_.getRoot();
		if(idPath.get(0) != ((Doc)root.getDoc()).getDocId() ){
			throw new RuntimeException("検索したrootとTreeのrootが違う");
		}
		jTree.expandPath(new TreePath( root.getPath() ) );
		DocNode leaf = expandIdPath(root.getLevel(), root, idPath);
		jTree.scrollPathToVisible(new TreePath(leaf.getPath()));
		
		if(mark){
			//Treeに印を付ける
			cellRenderer_.setMarkingNode(leaf);
			jTree.repaint();
		}
//		if(select){
//			showDetailAndSelectWord(leaf, search);
//		}
		return leaf;
	}
	//nodeの子ノードからidPath上のidを持つ要件Nodeを展開する。
	//idPathの最後のNodeを返却する。
	DocNode expandIdPath(int initLevel, DocNode node, List<Long> idPath){
		
		int level = node.getLevel() + 1;	//rootは展開されている前提

		if((idPath.size()+initLevel) <= level){
			return node;
		}
		DocNode ret = null;
		for(int i=0; i<2; i++){	
			Enumeration children = node.children();
			while(children.hasMoreElements()){
				DocNode checkNode = (DocNode)children.nextElement();
				
				if( idPath.get(level-initLevel) == ((Doc)checkNode.getDoc()).getDocId() ){
					log.debug("expand doc="+checkNode.getDoc());
					TreePath treePath = new TreePath( checkNode.getPath() );
					if( !jTree.isExpanded(treePath) ){
						jTree.expandPath(treePath);
					}
					ret = expandIdPath(initLevel, checkNode, idPath);
					break;
				}
			}
			//見つからない場合は、リロードしてリトライしてみる
			if( ret == null ){	//not found
				if(i > 0){
					throw new NotFoundDocOnDB(
							"id="+idPath.get(level-initLevel)+" が見つかりません");
				}else{
					TreePath path = new TreePath( node.getPath() );
					jTree.collapsePath(path);
					//子ノードを読み込みなおしてみる
					Action act = actionMap_.get(ActRefreshNode.class);
					act.putValue(ActRefreshNode.REFRESH_NODE,  node);
					act.actionPerformed(null);
				}
			}else{
				break;
			}
		}
		return ret;
	}
	class NotFoundDocOnDB extends AppException{
		public NotFoundDocOnDB(String msg){
			super(msg);
		}
	}

	/**
	 * This method initializes cntWorks	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntWorks() {
		if (cntWorks == null) {
			cntWorks = new JPanel();
			cntWorks.setLayout(new BorderLayout());
			cntWorks.add(getContOutputList(), BorderLayout.CENTER);
			cntWorks.add(getContButtons(), BorderLayout.NORTH);
			cntWorks.add(getCntFooter(), BorderLayout.SOUTH);
			cntWorks.setName("cntWorks");

		}
		return cntWorks;
	}

	/**
	 * This method initializes contOutputList	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getContOutputList() {
		if (contOutputList == null) {
			contOutputList = new JScrollPane();
			contOutputList.setViewportView(getDspWorkList());
		}
		return contOutputList;
	}

	/**
	 * This method initializes inpOutputList2	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private LstWorks getDspWorkList() {
		if (dspWorkList == null) {
			dspWorkList = new LstWorks();
			dspWorkList
				.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
					public void valueChanged(javax.swing.event.ListSelectionEvent e) {
						boolean isSelected = e.getFirstIndex() >= 0;
						getCmdRemoveWork().setEnabled(isSelected);
						getCmdUpdateOutput().setEnabled(isSelected);
					}
				});
		}
		return dspWorkList;
	}
//	private TblOutputList getInpOutputList2() {
//		if (inpOutputList2 == null) {
//			inpOutputList2 = new TblOutputList();
//		}
//		return inpOutputList2;
//	}

	/**
	 * This method initializes contButtons	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getContButtons() {
		if (contButtons == null) {
			contButtons = new JPanel();
			contButtons.setLayout(new BoxLayout(getContButtons(), BoxLayout.X_AXIS));
			contButtons.add(Box.createHorizontalStrut(10));
			contButtons.add(getInpSelectWorkType(), null);
			contButtons.add(Box.createHorizontalStrut(5));
			contButtons.add(getCmdShowOutputList(), null);
			contButtons.add(Box.createHorizontalStrut(10));
		}
		return contButtons;
	}

	/**
	 * This method initializes inpSelectWorkType	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getInpSelectWorkType() {
		if (inpSelectWorkType == null) {
			inpSelectWorkType = new JComboBox();
			inpSelectWorkType.setBorder(new TitledBorder("work type"));
			inpSelectWorkType.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					if(e.getItem() != null){
						getCmdShowOutputList().setEnabled(true);
					}else{
						getCmdShowOutputList().setEnabled(false);
					}
				}
			});
		}
		return inpSelectWorkType;
	}

	/**
	 * This method initializes cmdShowOutputList	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdShowOutputList() {
		if (cmdShowOutputList == null) {
			cmdShowOutputList = new JButton();
		}
		return cmdShowOutputList;
	}
	private JPanel cntFooter = null;
	private JButton cmdRemoveWork = null;
	private JButton cmdUpdateOutput = null;
	private JSlider inpDepth = null;
	class OutputSelectEventListener implements ActionListener {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			//TODO トランザクションを使用するため、ActTransaction化する
			switch(e.getID()){
			case DlgOutputList.EVENT_SELECT:
				DlgOutputList.SelectOutputEvent ev = 
					(DlgOutputList.SelectOutputEvent)e;
				OutputOfWorkListModel workModel = 
					(OutputOfWorkListModel)getDspWorkList().getModel();
				//作業を追加
				Doc curDoc = viewState_.currentNode_.getDoc();
				workModel.addWork(
						curDoc.getDocId(), 
						ev.output_, 
						ev.worker_, 
						(WorkType)getInpSelectWorkType().getSelectedItem());
				//カレントノードのアイコンを変更
				if(curDoc.getWorkCount() == 0){
					curDoc.setWorkCount(1);
					docModel_.refreshWorkCount(curDoc.getDocId(), curDoc.getWorkCount());
					jTree.repaint();	//これをやらないと変化しない
				}
				break;
			}
		}
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
			cntFooter.add(getCmdRemoveWork(), null);
			cntFooter.add(getCmdUpdateOutput(), null);
		}
		return cntFooter;
	}

	/**
	 * This method initializes cmdRemoveWork	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdRemoveWork() {
		if (cmdRemoveWork == null) {
			cmdRemoveWork = new JButton();
			cmdRemoveWork.setAction(actionMap_.get(ActRemoveWork.class));
			cmdRemoveWork.setEnabled(false);
		}
		return cmdRemoveWork;
	}

	/**
	 * This method initializes cmdUpdateOutput	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdUpdateOutput() {
		if (cmdUpdateOutput == null) {
			cmdUpdateOutput = new JButton();
			cmdUpdateOutput.setAction(actionMap_.get(ActShowWorkUpdater.class));
			cmdUpdateOutput.setEnabled(false);
		}
		return cmdUpdateOutput;
	}

	boolean setDepthFlag_;
	private JPanel cntNodeInfo = null;
	private JScrollPane cntScrList = null;
	private PnlNodeList pnlParents = null;
	private JButton cmdCopyToClipBard = null;
	private JLabel dspChildren = null;
	private JPanel cntLinkCreator = null;
	private JButton cmdRefreshLinkCreator = null;
	private JLabel dspLinkedUserCaption = null;
	private JLabel dspLinkedDateCaption = null;
	private JLabel dspLinkedDate = null;
	private JLabel dspLinkedUser = null;
	private JCheckBox inpChkLinkNode = null;
	/**
	 * This method initializes inpDepth	
	 * 	
	 * @return javax.swing.JSlider	
	 */
	private JSlider getInpDepth() {
		if (inpDepth == null) {
			inpDepth = new JSlider();
			inpDepth.setMaximum(20);
			inpDepth.setMinorTickSpacing(1);
			inpDepth.setMajorTickSpacing(5);
//			inpDepth.setPreferredSize(new Dimension(200, 25));
			inpDepth.setPaintTicks(true);
			inpDepth.setToolTipText("選択中のノード配下を一気に展開します。");
			inpDepth.setSnapToTicks(true);
			inpDepth.setValue(0);
			inpDepth.setBorder( 
					new TitledBorder(null, "expand depth", 
							TitledBorder.LEADING, TitledBorder.TOP, 
							new Font("Dialog", Font.PLAIN, 12), new Color(51, 51, 51))
				);

			inpDepth.addChangeListener(new javax.swing.event.ChangeListener() {
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					if(setDepthFlag_){	//内部でセットしている場合
						return;
					}
					DocNode dn = viewState_.getCurrentNode();
					if( dn == null ){
						return;
					}
					expandNode(jTree, dn, inpDepth.getValue());
				}
				protected void expandNode(JTree jTree, DocNode node, int depth){
					if(node == null){
						return;
					}
					if(depth <= 0){
						jTree.collapsePath(new TreePath(node.getPath()));
						return;
					}
					depth--;
					Enumeration children = node.children();
					while(children.hasMoreElements()){
						DocNode child = (DocNode)children.nextElement();
						jTree.expandPath(new TreePath(child.getPath()));
						expandNode(jTree, child, depth);
					}
				}
			});
// 			inpDepth.setVisible(false);
		}
		return inpDepth;
	}
	

	/**
	 * This method initializes cntNodeInfo	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntNodeInfo() {
		if (cntNodeInfo == null) {
			GridBagConstraints gbcLinkCreator = new GridBagConstraints();
			gbcLinkCreator.gridx = 0;
			gbcLinkCreator.fill = GridBagConstraints.BOTH;
			gbcLinkCreator.gridwidth = 2;
			gbcLinkCreator.gridy = 3;
			GridBagConstraints gbcInpDepth = new GridBagConstraints();
			gbcInpDepth.fill = GridBagConstraints.HORIZONTAL;
			gbcInpDepth.gridy = 0;
			gbcInpDepth.weightx = 1.0;
			gbcInpDepth.gridwidth = 2;
			gbcInpDepth.gridx = 0;
			GridBagConstraints gbcDspChildren = new GridBagConstraints();
			gbcDspChildren.gridx = 1;
			gbcDspChildren.insets = new Insets(0, 0, 0, 10);
			gbcDspChildren.weightx = 3.0;
			gbcDspChildren.gridy = 1;
			dspChildren = new JLabel();
			dspChildren.setText("children : ");
			GridBagConstraints gbcCmdCopyToClipBoard = new GridBagConstraints();
			gbcCmdCopyToClipBoard.fill = GridBagConstraints.NONE;
			gbcCmdCopyToClipBoard.weighty = 0.0;
			gbcCmdCopyToClipBoard.weightx = 1.0;
			gbcCmdCopyToClipBoard.gridx = 0;
			gbcCmdCopyToClipBoard.anchor = GridBagConstraints.WEST;
			gbcCmdCopyToClipBoard.ipadx = 0;
			gbcCmdCopyToClipBoard.insets = new Insets(10, 10, 0, 0);
			gbcCmdCopyToClipBoard.gridy = 1;
			GridBagConstraints gbcScrList = new GridBagConstraints();
			gbcScrList.fill = GridBagConstraints.BOTH;
			gbcScrList.weighty = 10.0;
			gbcScrList.weightx = 1.0;
			gbcScrList.gridx = 0;
			gbcScrList.gridwidth = 2;
			gbcScrList.gridy = 2;
			cntNodeInfo = new JPanel();
			cntNodeInfo.setLayout(new GridBagLayout());
			cntNodeInfo.add(getCntScrList(), gbcScrList);
			cntNodeInfo.setName("cntNodeInfo");
			cntNodeInfo.add(getCmdCopyToClipBard(), gbcCmdCopyToClipBoard);
			cntNodeInfo.add(dspChildren, gbcDspChildren);
			cntNodeInfo.add(getInpDepth(), gbcInpDepth);
			cntNodeInfo.add(getCntLinkCreator(), gbcLinkCreator);
		}
		return cntNodeInfo;
	}

	/**
	 * This method initializes cntScrList	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getCntScrList() {
		if (cntScrList == null) {
			cntScrList = new JScrollPane();
			cntScrList.setBorder(new TitledBorder("node path"));
			cntScrList.setViewportView(getPnlParents());
		}
		return cntScrList;
	}

	/**
	 * This method initializes pnlParents	
	 * 	
	 * @return jp.tokyo.selj.view.PnlNodeList	
	 */
	private PnlNodeList getPnlParents() {
		if (pnlParents == null) {
			pnlParents = new PnlNodeList();
		}
		return pnlParents;
	}

	/**
	 * This method initializes cmdCopyToClipBard	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdCopyToClipBard() {
		if (cmdCopyToClipBard == null) {
			cmdCopyToClipBard = new JButton();
			cmdCopyToClipBard.setText("copy node path");
			cmdCopyToClipBard.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Writer writer = new ClipboardStringWriter();
					try {
						String indent = "";
						for(DocNode dn : nodeInfo_.parents_){
							Doc doc = dn.getDoc();
							writer.write(indent);
							writer.write(doc.getDocId() + " : "+doc.getDocTitle());
							writer.write(System.getProperty("line.separator"));
//							indent+="\t";
						}
					} catch (IOException ex) {
						throw new RuntimeException(ex);
					}finally{
						try {
							writer.close();
						} catch (IOException ex) {
							throw new RuntimeException(ex);
						}
					}
				}
			});
		}
		return cmdCopyToClipBard;
	}

	/**
	 * This method initializes cntLinkCreator	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntLinkCreator() {
		if (cntLinkCreator == null) {
			GridBagConstraints gbcDate = new GridBagConstraints();
			gbcDate.insets = new Insets(0, 0, 0, 0);
			gbcDate.gridy = 0;
			gbcDate.ipadx = 0;
			gbcDate.ipady = 0;
			gbcDate.weightx = 1.0;
			gbcDate.fill = GridBagConstraints.HORIZONTAL;
			gbcDate.gridx = 4;
			GridBagConstraints gbcDateCap = new GridBagConstraints();
			gbcDateCap.gridx = 3;
			gbcDateCap.ipadx = 0;
			gbcDateCap.ipady = 0;
			gbcDateCap.gridy = 0;
			GridBagConstraints gbcUser = new GridBagConstraints();
			gbcUser.gridx = 2;
			gbcUser.ipadx = 0;
			gbcUser.ipady = 0;
			gbcUser.weightx = 2.0;
			gbcUser.fill = GridBagConstraints.HORIZONTAL;
			gbcUser.gridy = 0;
			GridBagConstraints gbcUserCap = new GridBagConstraints();
			gbcUserCap.gridx = 1;
			gbcUserCap.ipadx = 0;
			gbcUserCap.ipady = 10;
			gbcUserCap.gridy = 0;
			GridBagConstraints gbcButton = new GridBagConstraints();
			gbcButton.gridx = 0;
			gbcButton.ipadx = 0;
			gbcButton.gridy = 0;
			dspLinkedUser = new JLabel();
			dspLinkedUser.setBorder(new BevelBorder(BevelBorder.LOWERED));
			dspLinkedUser.setText("");
			dspLinkedDate = new JLabel();
			dspLinkedDate.setBorder(dspLinkedUser.getBorder());
			dspLinkedDate.setText("");
			dspLinkedDateCaption = new JLabel();
			dspLinkedDateCaption.setText("date : ");
			dspLinkedDateCaption.setHorizontalAlignment(SwingConstants.RIGHT);
			dspLinkedUserCaption = new JLabel();
			dspLinkedUserCaption.setText("user : ");
			dspLinkedUserCaption.setHorizontalAlignment(SwingConstants.RIGHT);
			cntLinkCreator = new JPanel();
			cntLinkCreator.setToolTipText("親ノードの関係を作成したユーザと日時");
			cntLinkCreator.setLayout(new GridBagLayout());
			cntLinkCreator.setBorder(new TitledBorder("link info"));
			cntLinkCreator.add(getCmdRefreshLinkCreator(), gbcButton);
			cntLinkCreator.add(dspLinkedUserCaption, gbcUserCap);
			cntLinkCreator.add(dspLinkedUser, gbcUser);
			cntLinkCreator.add(dspLinkedDateCaption, gbcDateCap);
			cntLinkCreator.add(dspLinkedDate, gbcDate);
		}
		return cntLinkCreator;
	}

	/**
	 * This method initializes cmdRefreshLinkCreator	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdRefreshLinkCreator() {
		if (cmdRefreshLinkCreator == null) {
			cmdRefreshLinkCreator = new JButton();
			cmdRefreshLinkCreator.setText("refresh");
			cmdRefreshLinkCreator.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					long id = viewState_.getCurrentNode().getDoc().getDocId();
					if( viewState_.getCurrentNode().getParentCount() > 0 ){
						long pId = ((DocNode)viewState_.getCurrentNode().getParent()).getDoc().getDocId();
						DocStr docStr = docModel_.getDocStr(pId, id);
						dspLinkedUser.setText(docStr.getUserName());
						dspLinkedDate.setText(DateTextFormatter.dateToString(docStr.getNewDate()));
					}
				}
			});
		}
		return cmdRefreshLinkCreator;
	}

	/**
	 * This method initializes inpChkLinkNode	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getInpChkLinkNode() {
		if (inpChkLinkNode == null) {
			inpChkLinkNode = new JCheckBox();
			inpChkLinkNode.setText("link node");
			inpChkLinkNode.setToolTipText("Titleの先頭の文字列でこれのon/offを自動設定 -->" +
					"ツールウィンドウ＞Preference");
		}
		return inpChkLinkNode;
	}

}  //  @jve:decl-index=0:visual-constraint="16,13"
