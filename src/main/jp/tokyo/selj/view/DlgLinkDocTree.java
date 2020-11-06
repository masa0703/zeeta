package jp.tokyo.selj.view;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jp.tokyo.selj.common.PreferenceWindowHelper;
import jp.tokyo.selj.dao.Doc;
import jp.tokyo.selj.dao.DocDao;
import jp.tokyo.selj.dao.Output;
import jp.tokyo.selj.dao.SelJDaoContainer;
import jp.tokyo.selj.model.DocModel;
import jp.tokyo.selj.model.DocNode;

import org.apache.log4j.Logger;
import org.seasar.framework.container.S2Container;

public class DlgLinkDocTree extends JDialog implements ListSelectionListener{
	Logger log = Logger.getLogger(this.getClass());
	static final int ROOT_ID = -100;

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JScrollPane jScrollPane = null;

	private JTree jTree = null;

	private PreferenceWindowHelper pref_;
	FrmZeetaMain mainView_ = null;
	S2Container daoCont_ = SelJDaoContainer.SEL_DAO_CONT;
	DocDao docDao_ = null;
	Output lastSelected_ = null;

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
			mainView_.showDocNode(node.getDoc().getDocId(), true);
		}
	}
	ActSelect actSelect_ = new ActSelect();

	/**
	 * @param owner
	 */
	public DlgLinkDocTree(JDialog owner) {
		super(owner);
		initialize();
	}
	/**
	 * @param owner
	 */
	public DlgLinkDocTree(JFrame owner) {
		super(owner);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(300, 400);
		this.setContentPane(getJContentPane());
		this.setTitle("Linked Node Tree");
		jLabel.setFont(getFont());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabel = new JLabel();
			jLabel.setText("double click/enter key/popup menuでMainTreeをポイント");
			jLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getJScrollPane(), BorderLayout.CENTER);
			jContentPane.add(jLabel, BorderLayout.NORTH);
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
			jScrollPane.setViewportView(getJTree());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jTree	
	 * 	
	 * @return javax.swing.JTree	
	 */
	private JTree getJTree() {
		if (jTree == null) {
			jTree = new JTree();
			jTree.setShowsRootHandles(true);
			jTree.setRootVisible(true);
			jTree.setToggleClickCount(0);
			jTree.setToolTipText("EnterキーでMain Treeをポイントします。");
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
	
	DocModel docModel_;

	private JPopupMenu mnuTreePopup = null;  //  @jve:decl-index=0:visual-constraint="348,25"

	private JMenuItem mnuPointTree = null;

	private JLabel jLabel = null;

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
		refreshDocList();
	}
	void refreshDocList(){
		//lastSelected_に関連するDoc一覧を取得
		List<Doc> docs = docDao_.findByOutputId(lastSelected_.getOutputId());
		
		//rootはダミー
		DocNode root = new DocNode(new Doc(ROOT_ID, lastSelected_.getName()));
		docModel_.setRoot(root);
		for(int i=0; i<docs.size(); i++){
			root.add(new DocNode(docs.get(i)));
		}
		getTree().expandRow(0);
		
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
		super.setVisible(b);
		lastSelected_ = output;
		refreshDocList();
	}

	public void setup(FrmZeetaMain mainView){
		mainView_ = mainView;
		docDao_ = (DocDao) daoCont_.getComponent(DocDao.class);

		//Treeモデル
        docModel_ = new DocModel();
//        docModel_.initializeReverseModel(docNode);
        
		//==== jTreeの設定
        getJTree().setModel(docModel_);
        getJTree().setCellRenderer(new DocTreeCellRenderer(docModel_));
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
		pref_ = new PreferenceWindowHelper(this);
		pref_.restoreForm();
	}	
	
	public JTree getTree(){
		return jTree;
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

}
