package jp.tokyo.selj.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
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
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import jp.tokyo.selj.common.PreferenceWindowHelper;
import jp.tokyo.selj.model.DocModel;
import jp.tokyo.selj.model.DocNode;

import org.apache.log4j.Logger;

public class DlgReverseTree extends JDialog implements TreeSelectionListener{
	Logger log = Logger.getLogger(this.getClass());  //  @jve:decl-index=0:
	
	static ImageIcon SYNC_ICON = new ImageIcon(DlgReverseTree.class.getResource("/image/revSync.png"));
	static ImageIcon SYNC_ICON2 = new ImageIcon(DlgReverseTree.class.getResource("/image/revSync2.png"));
	
	private PreferenceWindowHelper pref_;

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JScrollPane jScrollPane = null;

	private JTree jTree = null;

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
			FrmZeetaMain main = (FrmZeetaMain)getOwner();
			main.showDocNode(node.getDoc().getDocId(), true);
		}
	}
	ActSelect actSelect_ = new ActSelect();

	/**
	 * @param owner
	 */
	public DlgReverseTree(Frame owner) {
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
		this.setTitle("reverse Tree");
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
	private JTree getJTree() {
		if (jTree == null) {
			jTree = new JTree();
			jTree.setShowsRootHandles(true);
			jTree.setToggleClickCount(0);
			jTree.setToolTipText("Enterキーで正ツリーをポイントします。");
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
			//リスナ登録
			jTree.addTreeExpansionListener(new javax.swing.event.TreeExpansionListener() {
				public void treeExpanded(javax.swing.event.TreeExpansionEvent e) {
					Object node = e.getPath().getLastPathComponent();
					docModel_.addJijiDocFromDb((DocNode)node);
				}
				public void treeCollapsed(javax.swing.event.TreeExpansionEvent e) {
				}
			});

		}
		return jTree;
	}
	
	DocModel docModel_ = new DocModel();  //  @jve:decl-index=0:
	DocTreeCellRenderer2 renderer_ = new DocTreeCellRenderer2(docModel_);

	private JPopupMenu mnuTreePopup = null;  //  @jve:decl-index=0:visual-constraint="348,25"

	private JMenuItem mnuPointTree = null;

	private JLabel jLabel = null;
	private JPanel cntHeader = null;
	private JSlider inpDepth = null;
	static Font bFont_ =null;
	private JLabel cmdSyncNode = null;
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

	class DocTreeCellRenderer2 extends DocTreeCellRenderer{
		TreeNode[] path_;
		public DocTreeCellRenderer2(DocModel docModel) {
			super(docModel);
		}
		public void setPath(TreeNode[] path){
			path_ = path;
		}

		@Override
		protected void decorateFont(JTree tree, DocNode node) {
			super.decorateFont(tree, node);
			if(path_ == null || node.getLevel() < 1){	//node.getLevel()==0は、ルートなのでそのままにしておく
				return;
			}
			int index = path_.length - node.getLevel() -1;
			if( index >= 0 && path_.length > index &&
				node.getUserObject().equals(
					((DocNode)path_[index]).getUserObject()
				) 
			){
//				setFont(cnvBoldFont(tree.getFont()));
				setForeground(Color.GRAY);
			}
		}
	}
	DocNode curNode_;
	boolean ignoreChange_ = false;
	protected Preferences prefs_ = Preferences.userNodeForPackage(this.getClass());  //  @jve:decl-index=0:
	public void setup(){
		
		//==== jTreeの設定
        getJTree().setModel(docModel_);
        getJTree().setCellRenderer(renderer_);
		
		pref_ = new PreferenceWindowHelper(this);
		pref_.restoreForm();
		
		//ノードsyncする？
		ignoreChange_ = prefs_.getBoolean("revTree.sync", false);
		refreshSyncIcon();
	}
	public void valueChanged(TreeSelectionEvent e) {
		curNode_ = null;
		//選択されたitem
		if( !isShowing() || e.getPath().getLastPathComponent() == null){
			return;
		}
		//選択されたノードをdetailに表示
//		log.debug("refresh reverseTree");
		curNode_ = (DocNode)e.getPath().getLastPathComponent();
		if(ignoreChange_){
			return;
		}
		refreshDocTree( curNode_ );
	}
	class  DocTreeRefresher implements Runnable{
		DocNode node_;
		public DocTreeRefresher(DocNode node){
			node_ = node;
		}
		public void run() {
	        docModel_.initializeReverseModel(node_);
	        inpDepth.setValue(1);
	        renderer_.setPath(node_.getPath());
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
		refreshDocTree(docNode);
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
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 1;
			gridBagConstraints2.insets = new Insets(0, 5, 0, 5);
			gridBagConstraints2.fill = GridBagConstraints.NONE;
			gridBagConstraints2.gridy = 1;
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
			cntHeader.add(getCmdSyncNode(), gridBagConstraints2);
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
			});
		}
		return inpDepth;
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

	/**
	 * This method initializes cmdSyncNode	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JLabel getCmdSyncNode() {
		if (cmdSyncNode == null) {
			cmdSyncNode = new JLabel();
//			cmdSyncNode.setText("node sync.");
			cmdSyncNode.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					ignoreChange_ = !ignoreChange_;
					refreshSyncIcon();
					//記録
					try {
						prefs_.putBoolean("revTree.sync", ignoreChange_);
						prefs_.flush();
					} catch (BackingStoreException e1) {
						throw new RuntimeException(e1);
					}
				}
			});
			cmdSyncNode.setIcon(SYNC_ICON);	//後でリセットされる
			cmdSyncNode.setToolTipText("メインツリーのノード選択と同期");
		}
		return cmdSyncNode;
	}
	void refreshSyncIcon(){
		if(ignoreChange_){
			cmdSyncNode.setIcon(SYNC_ICON2);
		}else{
			cmdSyncNode.setIcon(SYNC_ICON);
			if(curNode_ != null){
				refreshDocTree(curNode_);
			}
		}
	}
	

}  //  @jve:decl-index=0:visual-constraint="10,10"
