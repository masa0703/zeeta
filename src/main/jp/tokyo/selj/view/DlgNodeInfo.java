package jp.tokyo.selj.view;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import jp.tokyo.selj.common.PreferenceWindowHelper;
import jp.tokyo.selj.dao.Doc;
import jp.tokyo.selj.model.DocNode;
import jp.tokyo.selj.util.ClipboardStringWriter;

import org.apache.log4j.Logger;

public class DlgNodeInfo extends JDialog implements TreeSelectionListener{
	Logger log = Logger.getLogger(this.getClass());  //  @jve:decl-index=0:
	private PreferenceWindowHelper pref_;

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JScrollPane cntScrList = null;

	private PnlNodeList pnlParents = null;

	
	public void valueChanged(TreeSelectionEvent e) {
		//選択されたitem
		if( !isShowing() || e.getPath().getLastPathComponent() == null){
			return;
		}
		//選択されたノードをdetailに表示
		refreshNodeInfo((DocNode)e.getPath().getLastPathComponent());
	}
	
	/**
	 * @param owner
	 */
	public DlgNodeInfo(Frame owner) {
		super(owner);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(300, 200);
		this.setTitle("node info");
		this.setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			dspChildren = new JLabel();
			dspChildren.setText("JLabel");
			dspChildren.setBorder(new BevelBorder(BevelBorder.LOWERED));
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getCntScrList(), BorderLayout.CENTER);
			jContentPane.add(getCmdCopyToClipBard(), BorderLayout.NORTH);
			jContentPane.add(dspChildren, BorderLayout.SOUTH);
		}
		return jContentPane;
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
			cntScrList.setViewportView(getDspParents());
		}
		return cntScrList;
	}

	/**
	 * This method initializes dspParents	
	 * 	
	 * @return javax.swing.JList	
	 */
	private PnlNodeList getDspParents() {
		if (pnlParents == null) {
			pnlParents = new PnlNodeList();
		}
		return pnlParents;
	}
	
	List<DocNode> parents_ = new ArrayList<DocNode>();  //  @jve:decl-index=0:

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
		refreshNodeInfo(docNode);
	}
	
	public void setup(){
		pref_ = new PreferenceWindowHelper(this);
		pref_.restoreForm();
	}
	public void refreshNodeInfo(DocNode curNode){
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
		
		//children
		dspChildren.setText("children : "+curNode.getChildCount());
		
		
//		log.debug("docs="+docs);
		pnlParents.setup(docs, sl_);
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
							FrmZeetaMain main = (FrmZeetaMain)getOwner();
							main.selectDocNode(dn_);
						}
					}
					SwingUtilities.invokeLater(new selectDocNode(dn));
				}
			}
			
		}
		public void setRootNode(DocNode rootNode){}
			
	};
	private JButton cmdCopyToClipBard = null;
	private JLabel dspChildren = null;


	/**
	 * This method initializes cmdCopyToClipBard	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdCopyToClipBard() {
		if (cmdCopyToClipBard == null) {
			cmdCopyToClipBard = new JButton();
			cmdCopyToClipBard.setText("copy to clipboard");
			cmdCopyToClipBard.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Writer writer = new ClipboardStringWriter();
					try {
						String indent = "";
						for(DocNode dn : parents_){
							Doc doc = dn.getDoc();
							writer.write(indent);
							writer.write(doc.getDocId() + " : "+doc.getDocTitle());
							writer.write(System.getProperty("line.separator"));
							indent+="\t";
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


}
