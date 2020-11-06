package jp.tokyo.selj.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;

import jp.tokyo.selj.ZeetaMain;
import jp.tokyo.selj.common.AppException;
import jp.tokyo.selj.common.BigDecimalTextFormatter;
import jp.tokyo.selj.common.DocumentUpdateListener;
import jp.tokyo.selj.common.MessageView;
import jp.tokyo.selj.common.PreferenceWindowHelper;
import jp.tokyo.selj.dao.Doc;
import jp.tokyo.selj.dao.DocDao;
import jp.tokyo.selj.dao.ModelCheckException;
import jp.tokyo.selj.dao.Output;
import jp.tokyo.selj.dao.OutputDao;
import jp.tokyo.selj.dao.OutputPropType;
import jp.tokyo.selj.dao.OutputTypePropType;
import jp.tokyo.selj.dao.OutputTypePropTypeDao;
import jp.tokyo.selj.dao.SelJDaoContainer;
import jp.tokyo.selj.dao.Work;
import jp.tokyo.selj.dao.WorkDao;
import jp.tokyo.selj.dao.WorkProp;
import jp.tokyo.selj.dao.WorkPropDao;
import jp.tokyo.selj.model.DocModel;
import jp.tokyo.selj.model.DocNode;
import jp.tokyo.selj.model.OutputListModel;

import org.apache.log4j.Logger;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.exception.SQLRuntimeException;

public class DlgOutputDetail extends JDialog {
	static final int VALUE_SCALE = 2;	//valueの小数以下桁数
	static final String DEVIDER_LOC_KEY1 = Util.getClassName(DlgOutputDetail.class) + "/devider_loc1";

	Output newOutput_ = null;
	Set<CommitListener> listeners_ = new HashSet<CommitListener>();
	
	public interface CommitListener {
		void commitOutput(Output output);
	}
	
	
	private static final String TITLE = "output";
	private static final long serialVersionUID = 1L;
	Logger log = Logger.getLogger(this.getClass());
	static final int ROOT_ID = -100;

	private JPanel jContentPane = null;
	PnlOutputDetail pnlOutputDetail = null;
	
	S2Container daoCont_ = SelJDaoContainer.SEL_DAO_CONT;
	OutputDao outputDao_ = null;  //  @jve:decl-index=0:
	DocDao docDao_ = null;
	WorkDao workDao_ = null;
	WorkPropDao workPropDao_ = null;
	OutputTypePropTypeDao outputTypePropTypeDao_ = null;  //  @jve:decl-index=0:
	
	OutputListModel model_;
	DocModel docModel_;

	private JPanel jPanel = null;

	private JButton cmdOk = null;
	
	boolean isUpdate_ = false;
	
	
	//Viewの状態を管理する
	enum PropEditViewStateType {NEUTRAL, CREATING, UPDATING, DELETING}; 
	class PropEditViewState implements ListSelectionListener{
		WorkProp curWorkProp_ = null;
		PropEditViewStateType state_ = PropEditViewStateType.NEUTRAL;
		boolean setting_ = false;
		boolean ignoreChangeValue_ = false;
		
		DocumentUpdateListener propdocListener_ = new DocumentUpdateListener(){
			protected void setDarty(){
				if(setting_){
					return;
				}
				super.setDarty();
				if(!isCreating()){
					getCmdCommitProp().getAction().setEnabled(true);
					getCmdCancelProp().setEnabled(true);
					getCmdNewProp().setEnabled(false);
				}
			}
		};
		public void setUpdating(){
			ignoreChangeValue_ = false;
			state_ = PropEditViewStateType.UPDATING;
			getCmdNewProp().setEnabled(true);
			getCmdRemoveProp().getAction().setEnabled(true);

			setEnabledInputs(true);
		}
		
		public WorkProp getCurrentWorkProp(){
			return curWorkProp_;
		}
		
		public void setNeutral(){
			ignoreChangeValue_ = false;
			state_ = PropEditViewStateType.NEUTRAL;

			getCmdNewProp().setEnabled(false);
			getCmdCommitProp().getAction().setEnabled(false);
			getCmdCancelProp().setEnabled(false);
			getCmdRemoveProp().getAction().setEnabled(false);
			
			propdocListener_.reset();
			setEnabledInputs(false);
			curWorkProp_ = null;
//			valueChanged(null);
			if(canCurNodeHaveWorkProp()){
				getCmdNewProp().setEnabled(true);
			}
		}
		public boolean canCurNodeHaveWorkProp(){
			TreePath path = jTree.getSelectionPath();
			//選択しているノードなし
			if( path == null || path.getLastPathComponent() == null){
				return false;
			}
			DocNode docNode = (DocNode)path.getLastPathComponent();
			//ルートと直下のノード以外は、属性を設定できない
			if(docNode.getParent() == null || docNode.getLevel() != 1){
				return false;
			}
			return true;
		}

		void setEnabledInputs(boolean val){
			getInpJisseki().setEnabled(val);
			getInpPropType().setEnabled(val);
			getInpPropValue().setEnabled(val);
		}
		public void setCreating(boolean val){
			ignoreChangeValue_ = false;
			
			getCmdCommitProp().getAction().setEnabled(val);
			getCmdCancelProp().setEnabled(val);
			getCmdRemoveProp().getAction().setEnabled(!val);
			getCmdNewProp().setEnabled(!val);

			if(val){
				Doc doc = ((DocNode)getJTree().getSelectionPath().getLastPathComponent()).getDoc();
				Work work = workDao_.findWorkByOutputId(
						doc.getDocId(), getPnlOutput().getOutput().getOutputId());
				
				WorkProp wp = new WorkProp();
				wp.setDocId(doc.getDocId());
				wp.setWorkTypeId(work.getWorkTypeId());
				setWorkProp(wp);
			}
			if(val){
				state_ = PropEditViewStateType.CREATING;
			}else{
				setNeutral();
			}
			setEnabledInputs(true);
		}
		public void setWorkProp(WorkProp wp){
			setting_ = true;
			curWorkProp_ = wp;
			getInpJisseki().getModel().setSelectedItem(wp.isJissekiFlg());
			getInpPropType().getModel().setSelectedItem(wp.getOutputPropType());
			getInpPropValue().setValue(wp.getValue());
			if(wp.getOutputPropType()!=null){
				dspPropTypeUnit.setText(wp.getOutputPropType().getUnitName());
				dspPropTypeDesc.setText(wp.getOutputPropType().getDescr());
			}else{
				dspPropTypeUnit.setText("***");
				dspPropTypeDesc.setText("***");
			}
			
			propdocListener_.reset();
			setting_ = false;
		}
		public WorkProp getWorkProp(){
			if(curWorkProp_ == null){
				return null;
			}
			curWorkProp_.setJissekiFlg((Boolean)inpJisseki.getModel().getSelectedItem());
			
			//JFormattedTextFieldは、実際にロストフォーカスしないと値が取り込まれない
			//もしくは、以下のようにcommitEditを呼び出す必要がある。
			try {
				inpPropValue.commitEdit();
			} catch (ParseException e1) {
				//無視
			}
			curWorkProp_.setValue((BigDecimal)inpPropValue.getValue());
			curWorkProp_.setOutputPropType(
					(OutputPropType)inpPropType.getModel().getSelectedItem()
			);
			
			return curWorkProp_;
		}

		public boolean isCreating() {
			return state_ == PropEditViewStateType.CREATING;
		}
		public void valueChanged(ListSelectionEvent e) {
			if(ignoreChangeValue_){
				return;
			}
			
			//成果物を選択している場合は、どのボタンも無効とする
			TreePath path = jTree.getSelectionPath();
			if( path == null || path.getLastPathComponent() == null){
				return;
			}
			DocNode docNode = (DocNode)path.getLastPathComponent();
			if(docNode.getParent() == null || docNode.getLevel() != 1){
				return ;
			}

			
			JTable table = getDspPropList();
			WorkProp wp = null;
			if(table.getSelectedRow() >= 0){
				wp = propListModel_.workProps_.get(table.getSelectedRow());
			}
			if(curWorkProp_ != null && curWorkProp_.equals(wp)){
				return;
			}
			updateIfDarty();
			
			if(wp == null){
				setNeutral();
				return;
			}
			
			setWorkProp(wp);
			setUpdating();
			log.debug("valueChanged() curDetail_="+curWorkProp_);
		}
		public void updateIfDarty(){
			if(propdocListener_.isDarty()){
				log.debug("汚れているわ. ");
				// 更新せなあかん
				if( JOptionPane.showConfirmDialog(
						DlgOutputDetail.this
						,"作業属性が変更されています。\n" +
						"更新を反映しますか？",""
						,JOptionPane.YES_NO_OPTION
						,JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
					getCmdCommitProp().getAction().actionPerformed(null);
				}else{
					setNeutral();
				}
			}
		}

		public void setIgnoreChangeValue() {
			ignoreChangeValue_ = true;
		}

	}
	PropEditViewState viewState_ = new PropEditViewState();  //  @jve:decl-index=0:
	DocumentUpdateListener docListener_ = new DocumentUpdateListener();

	private void updateIfDarty(){
		if(docListener_.isDarty()){
			log.debug("汚れているわ. ");
			// 更新せなあかん
			if( JOptionPane.showConfirmDialog(
					DlgOutputDetail.this
					,"成果物の内容が変更されています。\n" +
					"更新を反映しますか？",""
					,JOptionPane.YES_NO_OPTION
					,JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
				getCmdOk().getAction().actionPerformed(null);
			}else{
				docListener_.reset();
			}
		}
	}

	private class ActSelect extends AbstractAction {
		public ActSelect() {
			putValue(Action.NAME, "point main tree");
		}
		public void actionPerformed(ActionEvent e) {
			JTree tree = jTree;
			if(jTree.getSelectionPath() == null ){	//何も選択されていない
				return;
			}
			DocNode node = (DocNode)tree.getSelectionPath().getLastPathComponent();
			if(node.getDoc().getDocId() == ROOT_ID){
				return;		//rootは成果物なのでスキップ
			}
			ZeetaMain.getMainView().showDocNode(node.getDoc().getDocId(), true);
		}
	}
	ActSelect actSelect_ = new ActSelect();
	
    class FileDropListener implements DropTargetListener {
        /** ドラッグされたものが入ってきた。*/
        public void dragEnter(DropTargetDragEvent e) {
            // ファイルリストと文字列なら受け入れる
            if (e.isDataFlavorSupported(DataFlavor.javaFileListFlavor)){
                e.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE); // 受け入れる
            }
        }
        public void dragExit(DropTargetEvent e) {}
        public void dragOver(DropTargetDragEvent e) {}
        public void dropActionChanged(DropTargetDragEvent e) {}

        /** ドロップされた。Transferableからデータを取得する。*/
        public void drop(DropTargetDropEvent e) {
            try {
                // ファイルリストのとき
                if (e.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    Transferable t = e.getTransferable();
                    List files = (List)t.getTransferData(DataFlavor.javaFileListFlavor);
                    if(files.size() <= 0){
                    	return;
                    }
                    if(files.size() > 1){
                    	throw new AppException("１つのファイルしかドロップできません");
                    }
                    File file = (File) files.get(0);
                    getPnlOutput().inpName.setText(file.getName());
                    getPnlOutput().inpPath.setText(""+file.getPath());
                    e.dropComplete(true);
                }
            } catch (Exception ex) {
                e.dropComplete(false);
        		MessageView.show(DlgOutputDetail.this, ex);
            }
        }
    }

	
	
	class ActNewOrUpdateOutput extends ActTransactionBase {
		protected Component getOwnerComponent(){
			return DlgOutputDetail.this;
		}
		public ActNewOrUpdateOutput(){
			super();
			putValue(Action.NAME, "commit");
			putValue(Action.SHORT_DESCRIPTION, "成果物の更新または新規登録を行います");
		}
		public void actionPerformed2(ActionEvent e) {
			commitOutput(e);
		}
	}
	class ActShowReviewList extends ActBase {
		DlgReviewList dlgList_ = null;
		protected Component getOwnerComponent(){
			return DlgOutputDetail.this;
		}
		public ActShowReviewList(){
			super();
			putValue(Action.NAME, "review");
			putValue(Action.SHORT_DESCRIPTION, "成果物レビューリストを表示");
		}
		public void actionPerformed2(ActionEvent e) {
			if(dlgList_ == null){
				dlgList_ = new DlgReviewList(DlgOutputDetail.this, true);
				dlgList_.setup();
			}
			Output output = getPnlOutput().getOutput();
			dlgList_.setVisible(true, output);
		}
	}
	class ActShowCheckList extends ActBase {
		DlgCheckList dlgCheckList_ = null;
		protected Component getOwnerComponent(){
			return DlgOutputDetail.this;
		}
		public ActShowCheckList(){
			super();
			putValue(Action.NAME, "check");
			putValue(Action.SHORT_DESCRIPTION, "成果物チェックリストを表示");
		}
		public void actionPerformed2(ActionEvent e) {
			if(dlgCheckList_ == null){
				dlgCheckList_ = new DlgCheckList(DlgOutputDetail.this, true);
				dlgCheckList_.setup();
			}
			Output output = getPnlOutput().getOutput();
			dlgCheckList_.setVisible(true, output);
		}
	}
//	class ActNewProp extends ActBase {
//		protected Component getOwnerComponent(){
//			return DlgOutputDetail.this;
//		}
//		public ActNewProp(){
//			super();
//			putValue(Action.NAME, "new");
//			putValue(Action.SHORT_DESCRIPTION, "作業属性を追加します");
//		}
//		public void actionPerformed2(ActionEvent e) {
//			if(getJTree().getSelectionPath() == null){
//				throw new AppException("Tree上のノードを選択してください");
//			}
//			
//			Doc doc = ((DocNode)getJTree().getSelectionPath().getLastPathComponent()).getDoc();
//			Work work = workDao_.findWorkByOutputId(
//					doc.getDocId(), getPnlOutput().getOutput().getOutputId());
//			
//			WorkProp wp = new WorkProp();
//			wp.setDocId(doc.getDocId());
//			wp.setWorkTypeId(work.getWorkTypeId());
//			
//			propListModel_.addWorkProp(wp);
//		}
//	}
	class ActCommitProp extends ActTransactionBase {
		protected Component getOwnerComponent(){
			return DlgOutputDetail.this;
		}
		public ActCommitProp(){
			super();
			setEnabled(false);
			putValue(Action.NAME, "commit");
			putValue(Action.SHORT_DESCRIPTION, "チェック結果を書き込みます");
		}
		public void actionPerformed2(ActionEvent e) {
			if(inpPropType.getModel().getSelectedItem() == null){
				throw new ModelCheckException("作業属性種類を選択してください");
			}

			WorkProp wp = viewState_.getWorkProp();
			if(wp == null){
				return;
			}
			wp.check();
			
			if(viewState_.isCreating()){
				//新規登録の場合
				workPropDao_.insert(wp);
			}else{
				//updateの場合
				//予実やpropTypeが変更されている場合がある
				WorkProp wp2 = workPropDao_.findWithoutVersion(wp);
				if(wp2 == null){
					wp.setVersionNo(0);
					workPropDao_.insert(wp);
				}else{
					wp.setVersionNo(wp2.getVersionNo());
					workPropDao_.update(wp);
				}
			}
			viewState_.setIgnoreChangeValue();
			//detailListをリフレッシュ
			propListModel_.refresh();

			viewState_.setNeutral();
		}
		@Override
		protected void handleException(Exception ex) {
			if( ex instanceof SQLRuntimeException){
				if(((SQLRuntimeException)ex).getMessageCode().equals("ESSR0071")){
					ex = new AppException("作業属性の種類が重複しています");
				}
			}
			super.handleException(ex);
		}
	}
	class ActRemoveProp extends ActTransactionBase {
		protected Component getOwnerComponent(){
			return DlgOutputDetail.this;
		}
		public ActRemoveProp(){
			super();
			setEnabled(false);
			putValue(Action.NAME, "remove");
			putValue(Action.SHORT_DESCRIPTION, "作業属性を削除します");
		}
		public void actionPerformed2(ActionEvent e) {
			if(getDspPropList().getSelectedRow() < 0){
				throw new AppException("いずれかの作業属性を選択してください");
			}
			if( JOptionPane.showConfirmDialog(
					DlgOutputDetail.this
					,"選択した作業属性を削除します。\n" +
					"よろしいですか？",""
					,JOptionPane.YES_NO_OPTION
					,JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){

				workPropDao_.delete(viewState_.curWorkProp_);
				//detailListをリフレッシュ
				propListModel_.refresh();

				viewState_.setNeutral();
			}
			
		}
	}
	class ActOpenFile extends ActBase {
		DlgCheckList dlgCheckList_ = null;
		protected Component getOwnerComponent(){
			return DlgOutputDetail.this;
		}
		public ActOpenFile(){
			super();
			putValue(Action.NAME, "open");
			putValue(Action.SHORT_DESCRIPTION, "ファイルをオープンします（Windows only）");
		}
		public void actionPerformed2(ActionEvent e) {
			String path = getPnlOutput().getInpPath().getText();
			if(path == null || path.trim().length() <= 0){
				throw new AppException("pathを指定してください");
			}
			//実験
			Runtime rt = Runtime.getRuntime();
			try{
				//pathに空白が含まれているとうまく起動しない。pathの前に "" を入れて、pathを"で
				//囲むといいらしい。へんなの・・・
				Process pr = rt.exec(new String[] {"cmd", "/c", "start", "\"\" \""+path+"\""});
				log.debug("cmd /c start "+ path);
				pr.waitFor();
				log.debug("pr.exitValue() = "+ pr.exitValue());
			}catch(Exception ex){
				throw new RuntimeException(ex);
			}
			
		}
	}

	
	
	class PropTypeComboBoxModel extends DefaultComboBoxModel{
		public void setOutputTypePropTypes(List<OutputTypePropType> otpts){
			this.removeAllElements();
			for(OutputTypePropType otpt: otpts){
				addElement(otpt.getOutputPropType());
			}
		}
	}
	PropTypeComboBoxModel propTypeComboModel_ = new PropTypeComboBoxModel();
	
	//サブクラスで使用するために分離
	protected void commitOutput(ActionEvent e){
		Output output = getPnlOutput().getOutput();
		output.check();
		if(isUpdate_){
			//更新
			model_.updateOutput(output);
			DlgOutputDetail.this.setVisible(false);
		}else{
			//新規追加
			model_.insertOutput(output);
			DlgOutputDetail.this.setVisible(false);
			newOutput_ = output;
		}
		//MasterComboModelを更新する
//		MasterComboModel.refreshOutputType();
		
		//Listenerに通知
		for(Iterator<CommitListener> it = listeners_.iterator(); it.hasNext();){
			it.next().commitOutput(newOutput_);
		}
		docListener_.reset();
	}
	/**
	 * 新規作成した成果物を返却する。
	 * 新規作成していない場合は、null
	 * @return
	 */
	public Output getNewOutput(){
		return newOutput_;
	}
	
	public DlgOutputDetail() {	//for VE
		super();
		initialize();
	}
	/**
	 * @param owner
	 */
	public DlgOutputDetail(Frame owner, boolean isModal) {
		super(owner, isModal);
		initialize();
	}
	public DlgOutputDetail(JDialog owner, boolean isModal) {
		super(owner, isModal);
		initialize();
	}
	PreferenceWindowHelper pref_;
	private JButton cmdShowReview = null;
	private JButton cmdShowCheckList = null;
	private JTabbedPane cntTab = null;
	private JPanel cntDetail = null;
	private JPanel cntNodeTree = null;  //  @jve:decl-index=0:visual-constraint="624,118"
	private JScrollPane jScrollPane = null;
	private JTree jTree = null;
	private JPopupMenu mnuTreePopup = null;  //  @jve:decl-index=0:visual-constraint="614,56"
	private JMenuItem mnuPointTree = null;
	private JSplitPane cntSplitNodeTree = null;
	private JScrollPane cntPropList = null;
	private JTable dspPropList = null;
	
	
	class PropListModel extends AbstractTableModel implements TreeSelectionListener{
		Doc curDoc_;
		List<WorkProp> workProps_ = Collections.EMPTY_LIST;
		final String[] colNames_ = 
			new String[] {"予実", "propType", "value", "unit", "desc."};
		
		void setCurDoc(Doc doc){
			curDoc_ = doc;
			if(doc == null){
				workProps_ = Collections.EMPTY_LIST;
			}else{
				Work work = workDao_.findWorkByOutputId(
						curDoc_.getDocId(), 
						pnlOutputDetail.getOutput().getOutputId());
				if(work == null){
					workProps_ = Collections.EMPTY_LIST;
				}else{
					workProps_ = workPropDao_.findProps(
							work.getDocId(), work.getWorkTypeId());
				}
			}
			fireTableDataChanged();
		}
		void calcSummary(DocNode docNode){
			workProps_ = new ArrayList<WorkProp>();
			for(int i=0; i < docNode.getChildCount(); i++){
				Doc child = ((DocNode)docNode.getChildAt(i)).getDoc();
				Work work = workDao_.findWorkByOutputId(
						child.getDocId(), 
						pnlOutputDetail.getOutput().getOutputId());
				if(work != null){
					List<WorkProp> wps = workPropDao_.findProps(
							work.getDocId(), work.getWorkTypeId());
					workProps_ = marge(workProps_, wps);
				}
			}
			fireTableDataChanged();
		}
		List<WorkProp> marge(List<WorkProp> dists, List<WorkProp> srcs){
			for(WorkProp src: srcs){
				boolean match = false;
				for(WorkProp dist: dists){
					if(dist.isJissekiFlg() == src.isJissekiFlg() &&
							dist.getOutputPropTypeId() == src.getOutputPropTypeId()){
						dist.setValue(dist.getValue().add(src.getValue()));
						match = true;
						break;
					}
				}
				if(!match){
					dists.add(src);
				}
			}
			return dists;
		}
		
		public void refresh() {
			setCurDoc(curDoc_);
		}

		public void addWorkProp(WorkProp wp) {
			if(workProps_ == Collections.EMPTY_LIST){
				workProps_ = new ArrayList<WorkProp>();
			}
			workProps_.add(wp);
			fireTableDataChanged();
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}

		@Override
		public String getColumnName(int arg0) {
			return colNames_[arg0];
		}

		public int getColumnCount() {
			return colNames_.length;
		}

		public int getRowCount() {
			return workProps_.size();
		}

		public Object getValueAt(int arg0, int arg1) {
			Object ret = null;
			WorkProp wp = workProps_.get(arg0);
			switch(arg1){
			case 0:
				if(wp.isJissekiFlg()){
					ret = "実績";
				}else{
					ret = "予定";
				}
				break;
			case 1:		//propType
				if(wp.getOutputPropType() == null){
					ret = null;
				}else{
					ret = wp.getOutputPropType().getOutputPropTypeName();
				}
				break;
			case 2:		//value
				ret = wp.getValue();
				break;
			case 3:		//unit
				if(wp.getOutputPropType() == null){
					ret = null;
				}else{
					ret = wp.getOutputPropType().getUnitName();
				}
				break;
			case 4:		//desc.
				if(wp.getOutputPropType() == null){
					ret = null;
				}else{
					ret = wp.getOutputPropType().getDescr();
				}
				break;
			}
			return ret;
		}
		

		public void valueChanged(TreeSelectionEvent e) {
			viewState_.setNeutral();
			
			DocNode docNode = null;
			
			if(e.getPath() != null ){
				docNode = (DocNode)e.getPath().getLastPathComponent();
				//成果物が選択された（rootノード）
				if(docNode.isRoot()){
					calcSummary(docNode);
				}else{
					if( !viewState_.canCurNodeHaveWorkProp()){
						setCurDoc(null);
					}else{
						setCurDoc( docNode.getDoc() );
					}
				}
			}else{
				setCurDoc(null);
			}
		}
	}
	
	PropListModel propListModel_;
	private JPanel cntRight = null;
	private JPanel cntPropFooter = null;
	private JButton cmdNewProp = null;
	private JButton cmdCommitProp = null;
	private JButton cmdRemoveProp = null;
	private JPanel cntPropButtons = null;
	private JPanel cntPropEdit = null;
	private JComboBox inpJisseki = null;
	private JComboBox inpPropType = null;
	private JFormattedTextField inpPropValue = null;
	private JLabel dspPropTypeUnit = null;
	private JLabel dspPropTypeDesc = null;
	private JButton cmdCancelProp = null;
	private JButton cmdOpenFile = null;
	public void setup(OutputListModel model){
		model_ = model;
		outputDao_ = (OutputDao) daoCont_.getComponent(OutputDao.class);
		getPnlOutput().setup();

		docDao_ = (DocDao) daoCont_.getComponent(DocDao.class);
		workDao_ = (WorkDao)daoCont_.getComponent(WorkDao.class);
		workPropDao_ = (WorkPropDao)daoCont_.getComponent(WorkPropDao.class);
		outputTypePropTypeDao_ = (OutputTypePropTypeDao)daoCont_.getComponent(OutputTypePropTypeDao.class);

		//WorkProp操作ボタン
//		getCmdNewProp().setAction(new ActNewProp());
		getCmdCommitProp().setAction(new ActCommitProp());
		getCmdRemoveProp().setAction(new ActRemoveProp());
		
		//Treeモデル
        docModel_ = new DocModel();
        
		//==== jTreeの設定
        getJTree().setModel(docModel_);
        getJTree().setCellRenderer(new DocTreeCellRenderer4Work(docModel_));
		//リスナ登録
        getJTree().addTreeExpansionListener(new javax.swing.event.TreeExpansionListener() {
			public void treeExpanded(javax.swing.event.TreeExpansionEvent e) {
				log.debug("treeExpanded");
				Object node = e.getPath().getLastPathComponent();
				docModel_.addMagoDocFromDb((DocNode)node);
			}
			public void treeCollapsed(javax.swing.event.TreeExpansionEvent e) {
			}
		});
        
        //propListModel
    	propListModel_ = new PropListModel();
    	getDspPropList().setModel(propListModel_);
    	getJTree().addTreeSelectionListener(propListModel_);
        
        //tableの初期化
    	setupTable();
    	
		//WindowListener登録
		this.addWindowListener(
			new WindowAdapter(){
				public void windowClosing(WindowEvent e) {
					viewState_.updateIfDarty();
					updateIfDarty();
					pref_.getPriference().put(DEVIDER_LOC_KEY1, ""+getCntSplitNodeTree().getDividerLocation());
				}
			}
		);

    	//
    	viewState_.setNeutral();
		docListener_.reset();
		viewState_.propdocListener_.reset();

		//prop edit部の更新状態を監視する
		getInpJisseki().getModel().addListDataListener(viewState_.propdocListener_);
		getInpPropType().getModel().addListDataListener(viewState_.propdocListener_);
		getInpPropValue().getDocument().addDocumentListener(viewState_.propdocListener_);
		
		getInputPanel().getInpCreator().addActionListener(docListener_);
		getInputPanel().getInpDate().getDocument().addDocumentListener(docListener_);
		getInputPanel().getInpMemo().getDocument().addDocumentListener(docListener_);
		getInputPanel().getInpName().getDocument().addDocumentListener(docListener_);
		getInputPanel().getInpPath().getDocument().addDocumentListener(docListener_);
		getInputPanel().getInpOutputType().addActionListener(docListener_);

		pref_ = new PreferenceWindowHelper(this);
		pref_.restoreForm();
		
		//スプリットパネルの状態を復元
		getCntSplitNodeTree().setDividerLocation(
			Integer.parseInt(
				pref_.getPriference().get(DEVIDER_LOC_KEY1, 
						""+getCntSplitNodeTree().getDividerLocation())
			)
		);

	}
	void setupTable(){
		
//		dspPropList.getColumn("date").setMaxWidth(100);
		
		//作業属性一覧にリスナー登録
		dspPropList.getSelectionModel().addListSelectionListener(viewState_);

		dspPropList.getColumn("予実").setMaxWidth(40);
		dspPropList.getColumn("propType").setMinWidth(120);
		dspPropList.getColumn("value").setMaxWidth(100);
		dspPropList.getColumn("unit").setMaxWidth(100);
	}
	public void addCommitOutputListener(CommitListener l){
		log.debug("start");
		listeners_.add(l);
	}
	public void removeCommitOutputListener(CommitListener l){
		listeners_.remove(l);
	}
	
	public void newOutput(){
		getPnlOutput().setOutput(new Output());
		getPnlOutput().resetOutputTypeList();
		this.setTitle(TITLE+"[new]");
		isUpdate_ = false;
		newOutput_ = null;

		propTypeComboModel_.removeAllElements();
		docModel_ = new DocModel();
        getJTree().setModel(docModel_);
        docListener_.reset();
	}
	//workTypeIdに関連する成果物種類しか選択できないようにする
	public void resetOutputTypeList(long workTypeId){
		getPnlOutput().resetOutputTypeList(workTypeId);
	}
	public void resetOutputTypeList(){
		getPnlOutput().resetOutputTypeList();
	}
	
	public void loadOutput(long outputId){
		Output output = outputDao_.findById(outputId); 
		if(output == null){
			throw new RuntimeException("成果物ID="+outputId+" は、既に削除されています");
		}
		getPnlOutput().setOutput(output);
		refreshDocTree(output);
		this.setTitle(TITLE+"[update]");
		isUpdate_ = true;

	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(600, 320);
		this.setTitle(TITLE);
		this.setContentPane(getJContentPane());

		getCmdOk().setAction(new ActNewOrUpdateOutput());
		getCmdShowCheckList().setAction(new ActShowCheckList());
		getCmdShowReview().setAction(new ActShowReviewList());
		getCmdOpenFile().setAction(new ActOpenFile());

		// ドロップ可能なターゲットとして登録
		FileDropListener l = new FileDropListener();
        new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, l, true);
        new DropTarget(getPnlOutput().inpName, DnDConstants.ACTION_COPY_OR_MOVE, l, true);
        new DropTarget(getPnlOutput().inpPath, DnDConstants.ACTION_COPY_OR_MOVE, l, true);
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
			jContentPane.add(getCntTab(), BorderLayout.CENTER);
		}
		return jContentPane;
	}
	protected PnlOutputDetail getPnlOutput(){
		if(pnlOutputDetail == null){
			pnlOutputDetail = new PnlOutputDetail();
		}
		return pnlOutputDetail;
	}
	public PnlOutputDetail getInputPanel(){
		return getPnlOutput();
	}
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setLayout(new FlowLayout());
			jPanel.add(getCmdShowCheckList(), null);
			jPanel.add(getCmdShowReview(), null);
			jPanel.add(getCmdOpenFile(), null);
			jPanel.add(getCmdOk(), null);
		}
		return jPanel;
	}
	/**
	 * This method initializes cmdOk	
	 * 	
	 * @return javax.swing.JButton	
	 */
	protected JButton getCmdOk() {
		if (cmdOk == null) {
			cmdOk = new JButton();
		}
		return cmdOk;
	}

	/**
	 * This method initializes cmdShowReview	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdShowReview() {
		if (cmdShowReview == null) {
			cmdShowReview = new JButton();
		}
		return cmdShowReview;
	}

	/**
	 * This method initializes cmdShowCheckList	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdShowCheckList() {
		if (cmdShowCheckList == null) {
			cmdShowCheckList = new JButton();
		}
		return cmdShowCheckList;
	}
	/**
	 * This method initializes mnuTreePopup	
	 * 	
	 * @return javax.swing.JPopupMenu	
	 */
	private JPopupMenu getMnuTreePopup() {
		if (mnuTreePopup == null) {
			mnuTreePopup = new JPopupMenu();
			mnuTreePopup.add(getMnuPointTree());
		}
		return mnuTreePopup;
	}
	/**
	 * This method initializes mnuPointTree	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getMnuPointTree() {
		if (mnuPointTree == null) {
			mnuPointTree = new JMenuItem();
			mnuPointTree.setAction(actSelect_);
		}
		return mnuPointTree;
	}

	/**
	 * This method initializes cntTab	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */
	private JTabbedPane getCntTab() {
		if (cntTab == null) {
			cntTab = new JTabbedPane();
			cntTab.addTab("detail", null, getCntDetail(), null);
			cntTab.addTab("include", null, getCntNodeTree(), null);
		}
		return cntTab;
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
			cntDetail.add(getPnlOutput(), BorderLayout.CENTER);
			cntDetail.add(getJPanel(), BorderLayout.SOUTH);
		}
		return cntDetail;
	}
	/**
	 * This method initializes cntNodeTree	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntNodeTree() {
		if (cntNodeTree == null) {
			cntNodeTree = new JPanel();
			cntNodeTree.setLayout(new BorderLayout());
			cntNodeTree.add(getCntSplitNodeTree(), BorderLayout.CENTER);
		}
		return cntNodeTree;
	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJTree());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jTree	
	 * 	
	 * @return javax.swing.JTree	
	 */
	JTree getJTree() {
		if (jTree == null) {
			jTree = new JTree();
			jTree.setShowsRootHandles(true);
			jTree.setRootVisible(true);
			jTree.setToggleClickCount(0);
			jTree.setToolTipText("Enterキー,ダブルクリック,Popupメニューで正ツリーをポイントします。");
			jTree.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if(e.getButton() == MouseEvent.BUTTON3){	//右ボタン
						if(jTree.getSelectionPath() != null){
							getMnuTreePopup().show(jTree, e.getX(), e.getY());
						}
					}else if(e.getClickCount() >= 2){
						actSelect_.actionPerformed(null);
					}
				}
			});
			//Enterキーでjump
			jTree.getInputMap().
				put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0), "select");
			jTree.getActionMap().put("select", actSelect_);
		}
		return jTree;
	}
	void refreshDocTree(Output output){
		
		//outputに紐付くOutputPropTypeを取得
		List<OutputTypePropType> propTypes = outputTypePropTypeDao_.findByOutputTypeId(output.getOutputTypeId());
		propTypeComboModel_.setOutputTypePropTypes(propTypes);
		
		//outputに関連するDoc一覧を取得
		List<Doc> docs = docDao_.findByOutputId(output.getOutputId());
		
		//rootはダミー
		DocNode root = new DocNode(new Doc(ROOT_ID, output.getName()));
		docModel_.setRoot(root);
		for(int i=0; i<docs.size(); i++){
			Doc doc = docs.get(i);
			root.add(new DocNode(doc));
		}
		getJTree().expandRow(0);
		viewState_.setNeutral();
		docListener_.reset();
		viewState_.propdocListener_.reset();
	}

	/**
	 * This method initializes cntSplitNodeTree	
	 * 	
	 * @return javax.swing.JSplitPane	
	 */
	private JSplitPane getCntSplitNodeTree() {
		if (cntSplitNodeTree == null) {
			cntSplitNodeTree = new JSplitPane();
			cntSplitNodeTree.setResizeWeight(0.5D);
			cntSplitNodeTree.setRightComponent(getCntRight());
			cntSplitNodeTree.setLeftComponent(getJScrollPane());
		}
		return cntSplitNodeTree;
	}

	/**
	 * This method initializes cntPropList	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getCntPropList() {
		if (cntPropList == null) {
			cntPropList = new JScrollPane();
			cntPropList.setViewportView(getDspPropList());
		}
		return cntPropList;
	}

	/**
	 * This method initializes dspPropList	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getDspPropList() {
		if (dspPropList == null) {
			dspPropList = new JTable();

		}
		return dspPropList;
	}

	/**
	 * This method initializes cntRight	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntRight() {
		if (cntRight == null) {
			cntRight = new JPanel();
			cntRight.setLayout(new BorderLayout());
			cntRight.add(getCntPropList(), BorderLayout.CENTER);
			cntRight.add(getCntPropFooter(), BorderLayout.SOUTH);
		}
		return cntRight;
	}

	/**
	 * This method initializes cntPropFooter	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntPropFooter() {
		if (cntPropFooter == null) {
			cntPropFooter = new JPanel();
			cntPropFooter.setLayout(new BorderLayout());
			cntPropFooter.add(getCntPropButtons(), BorderLayout.SOUTH);
			cntPropFooter.add(getCntPropEdit(), BorderLayout.CENTER);
		}
		return cntPropFooter;
	}

	/**
	 * This method initializes cmdNewProp	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdNewProp() {
		if (cmdNewProp == null) {
			cmdNewProp = new JButton();
			cmdNewProp.setText("new");
			cmdNewProp.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(getJTree().getSelectionPath() == null){
						throw new AppException("Tree上のノードを選択してください");
					}
					viewState_.setCreating(true);
				}
			});
		}
		return cmdNewProp;
	}

	/**
	 * This method initializes cmdCommitProp	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdCommitProp() {
		if (cmdCommitProp == null) {
			cmdCommitProp = new JButton();
		}
		return cmdCommitProp;
	}

	/**
	 * This method initializes cmdRemoveProp	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdRemoveProp() {
		if (cmdRemoveProp == null) {
			cmdRemoveProp = new JButton();
		}
		return cmdRemoveProp;
	}

	/**
	 * This method initializes cntPropButtons	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntPropButtons() {
		if (cntPropButtons == null) {
			cntPropButtons = new JPanel();
			cntPropButtons.setLayout(new FlowLayout());
			cntPropButtons.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			cntPropButtons.add(getCmdNewProp(), null);
			cntPropButtons.add(getCmdCancelProp(), null);
			cntPropButtons.add(getCmdRemoveProp(), null);
			cntPropButtons.add(getCmdCommitProp(), null);
		}
		return cntPropButtons;
	}

	/**
	 * This method initializes cntPropEdit	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntPropEdit() {
		if (cntPropEdit == null) {
			dspPropTypeDesc = new JLabel();
			dspPropTypeDesc.setText("***");
			dspPropTypeDesc.setFont(new Font("Dialog", Font.PLAIN, 12));
			dspPropTypeUnit = new JLabel();
			dspPropTypeUnit.setText("***");
			cntPropEdit = new JPanel();
			cntPropEdit.setLayout(new FlowLayout());
			cntPropEdit.add(getInpJisseki());
			cntPropEdit.add(getInpPropType());
			cntPropEdit.add(getInpPropValue());
			cntPropEdit.add(dspPropTypeUnit);
			cntPropEdit.add(dspPropTypeDesc);
		}
		return cntPropEdit;
	}

	/**
	 * This method initializes inpJisseki	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getInpJisseki() {
		if (inpJisseki == null) {
			DefaultComboBoxModel jissekiCbModel = new DefaultComboBoxModel();
			jissekiCbModel.addElement(Boolean.TRUE);
			jissekiCbModel.addElement(Boolean.FALSE);
			inpJisseki = new JComboBox(jissekiCbModel);
			inpJisseki.setRenderer(
				new DefaultListCellRenderer(){
					@Override
					public Component getListCellRendererComponent(JList arg0, Object val, int arg2, boolean arg3, boolean arg4) {
						super.getListCellRendererComponent(arg0, val, arg2, arg3, arg4);
						if(((Boolean)val).booleanValue()){
							setText("実績");
						}else{
							setText("予定");
						}
						return this;
					}
				}
			);
		}
		return inpJisseki;
	}

	/**
	 * This method initializes inpPropType	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getInpPropType() {
		if (inpPropType == null) {
			inpPropType = new JComboBox();
			inpPropType.setPreferredSize(new Dimension(100, 25));
			
			inpPropType = new JComboBox(propTypeComboModel_);
			inpPropType.setRenderer(
				new DefaultListCellRenderer(){
					public Component getListCellRendererComponent(JList list,Object value, 
							int index, boolean isSelected, boolean cellHasFocus){
						super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
						if(value != null && value instanceof OutputPropType){
							OutputPropType opt = (OutputPropType)value;
							this.setText(opt.getOutputPropTypeName());
							
						}
						return this;
					}
				}
			);
			inpPropType.addItemListener(
				new ItemListener(){
					public void itemStateChanged(ItemEvent e) {
						OutputPropType opt = (OutputPropType)inpPropType.getModel().getSelectedItem();
						if(opt == null){
							dspPropTypeUnit.setText("***");
							dspPropTypeDesc.setText("***");
						}else{
							dspPropTypeUnit.setText(opt.getUnitName());
							dspPropTypeDesc.setText(opt.getDescr());
						}
					}
				
				}
			);
			
		}
		return inpPropType;
	}

	/**
	 * This method initializes inpPropValue	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JFormattedTextField getInpPropValue() {
		if (inpPropValue == null) {
			inpPropValue = new JFormattedTextField(new BigDecimalTextFormatter(VALUE_SCALE));
			inpPropValue.setPreferredSize(new Dimension(80, 20));
		}
		return inpPropValue;
	}

	/**
	 * This method initializes cmdCancelProp	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdCancelProp() {
		if (cmdCancelProp == null) {
			cmdCancelProp = new JButton();
			cmdCancelProp.setText("cancel");
			cmdCancelProp.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					viewState_.setNeutral();
					docListener_.reset();
				}
			});
		}
		return cmdCancelProp;
	}
	/**
	 * This method initializes cmdOpenFile	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdOpenFile() {
		if (cmdOpenFile == null) {
			cmdOpenFile = new JButton();
		}
		return cmdOpenFile;
	}

}  //  @jve:decl-index=0:visual-constraint="10,9"
