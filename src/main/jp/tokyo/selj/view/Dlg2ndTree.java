package jp.tokyo.selj.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.TreePath;

import jp.tokyo.selj.common.PreferenceWindowHelper;
import jp.tokyo.selj.model.DocModel;
import jp.tokyo.selj.model.DocNode;
import jp.tokyo.selj.view.component.ZTree;
import jp.tokyo.selj.view.trans.TreeNodeTransferHandler;
import jp.tokyo.selj.view.trans.TreeNodeTransferHandler2;

import org.apache.log4j.Logger;

public class Dlg2ndTree extends JDialog {
	Logger log = Logger.getLogger(this.getClass());  //  @jve:decl-index=0:
	
	static final String TREE_TYPE_2ND_TREE = "2nd tree";
	
//	static ImageIcon SYNC_ICON = new ImageIcon(Dlg2ndTree.class.getResource("/image/revSync.png"));
//	static ImageIcon SYNC_ICON2 = new ImageIcon(Dlg2ndTree.class.getResource("/image/revSync2.png"));
	
	private PreferenceWindowHelper pref_;

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JScrollPane jScrollPane = null;

	private FrmZeetaMain zeetaMain_ = null;
	private ZTree jTree = null;

	private class ActSelect extends AbstractAction {
		public ActSelect() {
			putValue(Action.NAME, "point main tree");
		}
		public void actionPerformed(ActionEvent e) {
//			JTree tree = (JTree)e.getSource();
			JTree tree = jTree;
			if(jTree.getSelectionPath() == null){	//何も選択されていない
				return;
			}
			DocNode node = (DocNode)tree.getSelectionPath().getLastPathComponent();
			zeetaMain_.showDocNode(node.getDoc().getDocId(), true);
		}
	}
	ActSelect actSelect_ = new ActSelect();

	/**
	 * @param owner
	 */
	public Dlg2ndTree(Frame owner) {
		super(owner);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(339, 400);
		this.setContentPane(getJContentPane());
		this.setTitle("2nd viewer");
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
			jLabel.setText("enter key / popup menu でMainTreeをポイント");
			jLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getJScrollPane(), BorderLayout.CENTER);
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
			jScrollPane.setViewportView(getJTree());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jTree	
	 * 	
	 * @return javax.swing.JTree	
	 */
	private ZTree getJTree() {
		if (jTree == null) {
			jTree = new ZTree();
			jTree.setShowsRootHandles(true);
			jTree.setToggleClickCount(2);
			jTree.setToolTipText("EnterキーでMain Treeをポイントします。");
			jTree.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if(e.getButton() == MouseEvent.BUTTON3){	//右ボタン
						if(jTree.getSelectionPath() != null){
							getMnuTreePopup().show(jTree, e.getX(), e.getY());
						}
					}
				}
			});
			jTree.setTreeType(TREE_TYPE_2ND_TREE);
			//Enterキーでjump
			jTree.getInputMap().
				put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0), "select");
			jTree.getActionMap().put("select", actSelect_);
			//リスナ登録
			jTree.addTreeExpansionListener(new javax.swing.event.TreeExpansionListener() {
				public void treeExpanded(javax.swing.event.TreeExpansionEvent e) {
					Object node = e.getPath().getLastPathComponent();
					docModel_.addMagoDocFromDb((DocNode)node);
				}
				public void treeCollapsed(javax.swing.event.TreeExpansionEvent e) {
				}
			});

		}
		return jTree;
	}
	
	DocModel docModel_ = new DocModel();  //  @jve:decl-index=0:
	DocTreeCellRenderer renderer_ = new DocTreeCellRenderer(docModel_);

	private JPopupMenu mnuTreePopup = null;  //  @jve:decl-index=0:visual-constraint="348,25"

	private JMenuItem mnuPointTree = null;

	private JLabel jLabel = null;
	private JPanel cntHeader = null;
	private JSlider inpDepth = null;
	static Font bFont_ =null;
	Font cnvBoldFont(Font treeFont){
		if(bFont_==null
		   ||
		   !(treeFont.getName().equals(bFont_.getName())
			  && treeFont.getSize() == bFont_.getSize() )
		){
			bFont_ = new Font(
						treeFont.getName()
						,treeFont.getStyle() | Font.BOLD
						,treeFont.getSize()
					);
		}
		return bFont_;
	}

	DocNode curNode_;
	boolean ignoreChange_ = false;
	protected Preferences prefs_ = Preferences.userNodeForPackage(this.getClass());  //  @jve:decl-index=0:

	//指定ノードのリフレッシュ
	class ActRefreshNode extends ActBase {
		static final String REFRESH_NODE = "REFRESH_NODE";
		public ActRefreshNode(ActionMap map) {
			super(map);
			putValue(Action.NAME, "refreshSpecific");
			putValue(Action.SHORT_DESCRIPTION, "指定ノードのリフレッシュ");
		}
		public void actionPerformed2(ActionEvent e) {
//			if( ! viewState_.checkExistSelectingNode()){
//				return;
//			}
			DocNode node = (DocNode)getValue(REFRESH_NODE);
			if( node == null){
				throw new RuntimeException("node is null.");
			}
			putValue(REFRESH_NODE, null);
			//**** 自身をリロード 
//			viewState_.setDetail( docModel_.reloadDoc(node) );
			
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
	
	public void setup(FrmZeetaMain zeetaMain, ZTree mainTree){
		zeetaMain_ = zeetaMain;
		//==== jTreeの設定
        getJTree().setModel(docModel_);
        getJTree().setCellRenderer(renderer_);
		
		//DnD
		TreeNodeTransferHandler2 transferHandler = 
			new TreeNodeTransferHandler2(this, jTree, new ActRefreshNode(null)){
				@Override
			    protected void setSelection(JTree jTree, DocNode node, long id){
			    	//何もしない
			    }
			};
		jTree.setTransferHandler(transferHandler);
		jTree.setDragEnabled(true);

		TreeNodeTransferHandler2 mainTrans = (TreeNodeTransferHandler2)mainTree.getTransferHandler();
		mainTrans.addListener(transferHandler);
		transferHandler.addListener(mainTrans);
        
        
        pref_ = new PreferenceWindowHelper(this);
		pref_.restoreForm();
		
		//ノードsyncする？
//		ignoreChange_ = prefs_.getBoolean("revTree.sync", false);
//		refreshSyncIcon();
	}
//	public void valueChanged(TreeSelectionEvent e) {
//		curNode_ = null;
//		//選択されたitem
//		if( !isShowing() || e.getPath().getLastPathComponent() == null){
//			return;
//		}
//		//選択されたノードをdetailに表示
////		log.debug("refresh reverseTree");
//		curNode_ = (DocNode)e.getPath().getLastPathComponent();
//		if(ignoreChange_){
//			return;
//		}
//		refreshDocTree( curNode_ );
//	}
	class  DocTreeRefresher implements Runnable{
		DocNode node_;
		public DocTreeRefresher(DocNode node){
			node_ = node;
		}
		public void run() {
	        docModel_.initialize(node_.getDoc().getDocId());
	        inpDepth.setValue(1);
//	        renderer_.setPath(node_.getPath());
			getTree().setVisible(true);
		}
	}
	void refreshDocTree(DocNode node){
		getTree().setVisible(false);
		SwingUtilities.invokeLater(new DocTreeRefresher(node));
	}
	
	@Override
	public void setVisible(boolean b) {
		if(b){
			throw new RuntimeException("setVisible(boolean b, DocNode docNode)をつかわなあかん");
		}else{
			super.setVisible(b);
		}
	}
	public void setVisible(boolean b, DocNode docNode) {
		super.setVisible(b);
		if(b){
//			setExtendedState(JFrame.NORMAL);
			refreshDocTree(docNode);
		}
	}

//	void collapseChildren(DocNode oyaNode){
//        Enumeration<DocNode> children = oyaNode.children();
//        while(children.hasMoreElements()){
//        	DocNode docNode = children.nextElement();
//        	collapseChildren(docNode);
//            getJTree().collapsePath(new TreePath(docNode.getPath()));
//            log.debug("collapse->"+docNode);
//        }
//	}
	
	
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

	/**
	 * This method initializes cntHeader	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntHeader() {
		if (cntHeader == null) {
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints1.gridy = 1;
			gridBagConstraints1.ipadx = 0;
			gridBagConstraints1.weightx = 5.0;
			gridBagConstraints1.gridx = 0;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.ipadx = 0;
			gridBagConstraints.gridwidth = 2;
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.gridy = 0;
			cntHeader = new JPanel();
			cntHeader.setLayout(new GridBagLayout());
			cntHeader.add(jLabel, gridBagConstraints);
			cntHeader.add(getInpDepth(), gridBagConstraints1);
		}
		return cntHeader;
	}

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
			inpDepth.setToolTipText("ノードを展開するルートからの深さを指定します。");
			inpDepth.setSnapToTicks(true);
			inpDepth.setValue(0);
			inpDepth.setBorder( 
					new TitledBorder(null, "expand depth", 
							TitledBorder.LEADING, TitledBorder.TOP, 
							new Font("Dialog", Font.PLAIN, 12), new Color(51, 51, 51))
				);

			inpDepth.addChangeListener(new javax.swing.event.ChangeListener() {
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					expandNode((DocNode)docModel_.getRoot(), inpDepth.getValue());
				}
				void expandNode(DocNode node, int depth){
					if(depth <= 0){
						jTree.collapsePath(new TreePath(node.getPath()));
						return;
					}
					depth--;
					Enumeration children = node.children();
					while(children.hasMoreElements()){
						DocNode child = (DocNode)children.nextElement();
						jTree.expandPath(new TreePath(child.getPath()));
						expandNode(child, depth);
					}
				}
			});
		}
		return inpDepth;
	}
	

}  //  @jve:decl-index=0:visual-constraint="10,10"
