package jp.tokyo.selj.view;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import jp.tokyo.selj.common.PreferenceWindowHelper;
import jp.tokyo.selj.dao.Doc;
import jp.tokyo.selj.model.DocModel;
import jp.tokyo.selj.model.DocNode;
import jp.tokyo.selj.view.PnlNodeList.SelectionLintener;

import org.apache.log4j.Logger;

public class DlgParentDocList extends JDialog implements TreeSelectionListener{
	Logger log = Logger.getLogger(this.getClass());
	private PreferenceWindowHelper pref_;

	private JPanel jContentPane = null;
	Doc selectedDoc_;  //  @jve:decl-index=0:
	DocModel docModel_ = null;

	private PnlNodeList dspDocs = null;

	private SelectionLintener docSelectionListener_ = new SelectionLintener(){
		public void process(Doc doc){
			FrmZeetaMain main = (FrmZeetaMain)getOwner();  //  @jve:decl-index=0:
			main.showOyaDocNode(
					doc.getDocId(), 
					selectedDoc_.getDocId(), true);
		}
		public void setRootNode(DocNode rootNode){}
	};

	/**
	 * This is the default constructor
	 */
	public DlgParentDocList(Frame owner) {
		super(owner);
		initialize();
	}

	public void setup(DocModel docModel){
		docModel_ = docModel;

		pref_ = new PreferenceWindowHelper(this);
		pref_.restoreForm();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(400, 100);
		this.setContentPane(getJContentPane());
		this.setTitle("parents List");
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
			jContentPane.add(getDspYoukens(), BorderLayout.CENTER);
		}
		return jContentPane;
	}


	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private PnlNodeList getDspYoukens() {
		if (dspDocs == null) {
			dspDocs = new PnlNodeList();
		}
		return dspDocs;
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
		refreshParentList(docNode);
	}

	public void valueChanged(TreeSelectionEvent e) {
		//選択されたitem
		if( !isShowing() || e.getPath().getLastPathComponent() == null){
			return;
		}
		log.debug("refresh parent list");
		//選択されたノードをdetailに表示
		refreshParentList( (DocNode)e.getPath().getLastPathComponent() );
	}
	void refreshParentList(DocNode node){
		//親一覧画面を更新
		long id = node.getDoc().getDocId();
		List parents = docModel_.getParents(id);
		selectedDoc_ = node.getDoc();
		getDspYoukens().setup(parents, docSelectionListener_);
	}


}  //  @jve:decl-index=0:visual-constraint="14,7"
