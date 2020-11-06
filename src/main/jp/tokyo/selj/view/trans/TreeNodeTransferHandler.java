package jp.tokyo.selj.view.trans;

/*
 * 
 * 注意！：このオブジェクトは、複数のJTreeで共有しないこと。
 * 　　　というより、他の画面のJTreeにTransferHandlerをセットしないように設計すること。
 */

import java.awt.Component;
import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import jp.tokyo.selj.ZeetaMain;
import jp.tokyo.selj.common.AppException;
import jp.tokyo.selj.common.CatString;
import jp.tokyo.selj.common.MessageView;
import jp.tokyo.selj.dao.Doc;
import jp.tokyo.selj.model.DocModel;
import jp.tokyo.selj.model.DocNode;
import jp.tokyo.selj.view.Util;
import jp.tokyo.selj.view.FrmZeetaMain.ActRefreshNode;
import jp.tokyo.selj.view.component.ZTree;
import jp.tokyo.selj.view.trans.TreeNodeTransferable.Data;

import org.apache.log4j.Logger;

public class TreeNodeTransferHandler extends TransferHandler {
	Logger log = Logger.getLogger(this.getClass());
	static String PROC_ID_KEY = "processId";
	
    static String ZEETA_1202_MIME_TYPE = DataFlavor.javaJVMLocalObjectMimeType +
                                ";class=jp.tokyo.selj.model.DocModel";
    static DataFlavor ZEETA_1202_FLAVOR;

    static String ZEETA_1300_MIME_TYPE = DataFlavor.javaJVMLocalObjectMimeType +
    							";class="+Data.class.getName()+";"+PROC_ID_KEY+"="+ZeetaMain.getProcessId();
    static DataFlavor ZEETA_1300_FLAVOR;
    
    static DataFlavor[] MY_FLAVORS;
    
    Component ownerCompo_;	//ダイアログの表示に使用する
    ZTree jtree_;
    Action refreshNode_;
    enum PassedMethod {NONE, CreTrns, ExpDone_X, ExpDone_C, ImpData, ImpData_ignore, Paste_Another_Tree}
    PassedMethod passedMethod_ = PassedMethod.NONE;
    Object lastCreatedData_ = null;
    
    static {
        try {
			ZEETA_1202_FLAVOR = new DataFlavor(ZEETA_1202_MIME_TYPE);	//1.2.02以前のzeetaノード
			ZEETA_1300_FLAVOR = new DataFlavor(ZEETA_1300_MIME_TYPE);
            MY_FLAVORS = new DataFlavor[] { ZEETA_1300_FLAVOR, ZEETA_1202_FLAVOR, DataFlavor.stringFlavor };
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
    }
    
    private List<TransferListener> listeners_ = new ArrayList<TransferListener>();
    
    public interface TransferListener {
    	public void pastedNode(Data fromInfo);
    }
    public void addListener(TransferListener listener){
    	listeners_.add(listener);
    }

    public TreeNodeTransferHandler(Component mainView, ZTree jtree, Action refreshNode) {
        ownerCompo_ = mainView;
        jtree_ = jtree;
        refreshNode_ = refreshNode;
    }

//	Font planeFont_ = new Font("Dialog", Font.PLAIN, 12);
	private class ImportNodeConfirmationPanel extends JPanel {
		JCheckBox inpFromChildren_;
		JCheckBox inpCopyDataCopy_;
		JLabel messageLabel_;
		JLabel warnig_ = new JLabel(
				"<html>↑のチェックをOnにした場合の<b>注意点</b><br>" +
				"<pre>" +
				"  1.新しいノードは、追加される<br>" +
				"  2.既存のノードは、再利用する(このチェックを付けない場合は、追加される)<br>" +
				"  3.既存ノードのタイトル変更は、反映されない<br>" +
				"  4.既存ノードの移動、削除は反映されない<br>" +
				"  5.CopyDataした後に元Zeeta上でタイトルが変更されたノードは既存ノードとみなされない<br>" +
				"  6.一度この機能でコピーした後、再度CopyDataせずに同じノードをコピーすると追加ノードは、2重に登録される<br>" +
				"");

		ImportNodeConfirmationPanel(){
			super();
			initialize();
		}
		void initialize(){
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			messageLabel_ =new JLabel();
//			messageLabel_.setFont(planeFont_);
			add(messageLabel_);
			inpFromChildren_ = new JCheckBox(
					"子ノード配下からimportする"
			);
			add(inpFromChildren_);
			inpCopyDataCopy_ = new JCheckBox(
					"<html>CopyDataしたZeeta上のデータを元のZeetaデータに再度コピーする<br>");
			add(inpCopyDataCopy_);
			inpCopyDataCopy_.addItemListener(new ItemListener(){
				public void itemStateChanged(ItemEvent e) {
					if(inpCopyDataCopy_.isSelected() ){
						add(warnig_);
					}else{
						remove(warnig_);
					}
					SwingUtilities.getWindowAncestor(
							ImportNodeConfirmationPanel.this).pack();
				}});
		}
		void setImportDocName(String name){
			messageLabel_.setText(
			 "<html>" +
			 "他のTree node(<span style=\"color: #CC0000;\"><b>"+ name +"</b></span>)をimportします。<br>" +
			 "尚、コピー元nodeは、ツリー上にロードされている分だけimportします。<br>" +
			 "選択node配下全てをコピーする場合は、コピー元nodeを<br>" +
			 "「選択ノードを全て展開」ボタンで展開してからコピーしてください。<br>" +
			 "<br>よろしいですか？<br>" +
			 "<hr>" +
			 "</html>");
			
		}
	}
	ImportNodeConfirmationPanel confirmImportNodePanel_ = new ImportNodeConfirmationPanel();

	private class ImportTextConfirmationPanel extends JPanel {
		JCheckBox inpImportMemo_;
		JLabel messageLabel_;
		ImportTextConfirmationPanel(){
			super();
			initialize();
		}
		void initialize(){
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			messageLabel_ =new JLabel(
					"<html>" +
					"テキストからノードを作成します。<br>" +
					"１行で１ノード、Tabのインデントで階層を作成します。<br>" +
					"本文を取り込む場合、\"で囲むと改行しても１ノードとになります。<br>" +
					"<br>よろしいですか？<br>" +
					"<hr>" +
					"</html>");
//			messageLabel_.setFont(planeFont_);
			add(messageLabel_);
			inpImportMemo_ = new JCheckBox(
					"タイトル文の後ろのTab以降を本文として取り込む"
			);
			inpImportMemo_.setSelected(true);
			add(inpImportMemo_);
		}
	}
	ImportTextConfirmationPanel confirmImportTextPanel_ = new ImportTextConfirmationPanel();

    DocNode createDocNodeFromText(Transferable t) {
		if( JOptionPane.showConfirmDialog(
				ownerCompo_
				,confirmImportTextPanel_
				,""
				,JOptionPane.YES_NO_OPTION
				,JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
		}else{
			return null;
		}
		DocNode rootDocNode = new DocNode(new Doc(0, "(imported node)"));
		boolean isExistNode = false;
		try{
			String text = (String)t.getTransferData(DataFlavor.stringFlavor);
			//Node階層を作成
//			String[] lines = text.split(System.getProperty("line.separator"));	なぜかこっちは駄目だった
			String[] lines = text.split("\n");
			
			//本文のダブルクオーテーション間を連結する
			lines = CatString.concatLine(lines);
			
			int preTabSize = -1;
			DocNode preDocNode = rootDocNode;
			for(int i=0; i<lines.length; i++){
				if("".equals(lines[i].trim())){
					continue;	//空行は無視
				}
				int curTabSize = CatString.countTab(lines[i]); 
				isExistNode = true;
				
				Doc newDoc = makeDoc(i, lines[i]);
				DocNode curDocNode = new DocNode(newDoc);
				if( preDocNode != null){
					int dist = preTabSize - curTabSize;
					if(dist == 0){
						((DocNode)preDocNode.getParent()).add(curDocNode);		//兄弟
					}else if(dist < 0){
						curTabSize = preTabSize + 1;	//無駄に多くtabがあったら矯正
						preDocNode.add(curDocNode);		//子
					}else{
						for(int j=0; j<dist; j++){
							if(preDocNode.getParent() == null){
								//１行目からtabが入っていて、後の行にtabなし行があると
								//このようになる。
								//例）
								//<tab>aaa
								//bbb
								break;
							}
							preDocNode = (DocNode)preDocNode.getParent();
						}
						if(preDocNode == rootDocNode){
							preDocNode.add(curDocNode);		//仕方がないので子とする
						}else{
							((DocNode)preDocNode.getParent()).add(curDocNode);
						}
					}
				}
				preTabSize = curTabSize;
				preDocNode = curDocNode;
			}
			
		}catch(RuntimeException e){
			throw e;
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		return (isExistNode)? rootDocNode:null;
    }
    
    
    /*
     * 続きがある場合は、nullを返す
     */
    Doc makeDoc(int no, String importText){
    	importText = importText.trim();
    	Doc doc = null;
    	String memo = null;
    	if(confirmImportTextPanel_.inpImportMemo_.isSelected()){
    		int pos = importText.indexOf('\t');
    		if(pos > 0){
    			memo = importText.substring(pos+1).trim();
    			importText = importText.substring(0, pos).trim();
    		}
    	}
    	doc = new Doc(no+1, importText);	//Idはユニークであればよい
    	if(memo != null){
    		doc.setDocCont(memo);
    	}
    	doc.check();
    	return doc;
    }
    
    static final String OLD_ZEETA_NODE = "old zeeta node"; 
    static final String MADE_FROM_STRING = "made from string";

    private TreeNodeTransferable.Data getFromNodeInfo(JComponent c, Transferable t){
    	
    	if (!canImport(c, t.getTransferDataFlavors())) {
        	log.debug("import できません");
        	passedMethod_ = PassedMethod.NONE;	//別なデータが入っている
        	return null;
        }
    	
    	TreeNodeTransferable.Data ret = getTransNode(t);
    	
//    	if(isStringFlavor(c, t.getTransferDataFlavors())){
//    		DocNode fromNode = createDocNodeFromText(t);
//        	ret = new TreeNodeTransferable.Data(MADE_FROM_STRING, "", (DocNode)fromNode);
//    		
//    	}else if (!canImport(c, t.getTransferDataFlavors())) {
//        	log.debug("import できません");
//        	passedMethod_ = PassedMethod.NONE;	//別なデータが入っている
//        }else{
//        	ret = getTransNode(t);
//        }
    	return ret;
    }
	
    /* 以下の２つのケースで呼び出される。
     * ・ctrl+Vをタイプした場合<br/>
     * ・DnDでドロップした場合
     * @see javax.swing.TransferHandler#importData(javax.swing.JComponent, java.awt.datatransfer.Transferable)
     */
    public boolean importData(JComponent c, Transferable t) {
    	log.trace("start");
    	
    	//=== Dropノード情報を取り出す
    	TreeNodeTransferable.Data fromInfo = getFromNodeInfo(c, t);
    	if(fromInfo == null){
        	passedMethod_ = PassedMethod.NONE;	//クリップボードには、別なデータが入っている
        	return false;
        }

    	//=== 転送先Node
        ZTree toTree = (ZTree)c;	//cの型は、canImportでチェック済み
        if(toTree.getSelectionPath() == null){
        	//何も選択されていないとここに来る
//        	passedMethod_ = PassedMethod.NONE;
        	return false;
        }
        DocNode toNode = (DocNode)toTree.getSelectionPath().getLastPathComponent();

        //=== 転送
        boolean ret = false;

    	try{
    		ownerCompo_.setCursor(Util.WAIT_CURSOR);
    		
    		//同じプロセスか否か
    		DataFlavor zeeta1300Flavor = getFlavor(ZEETA_1300_FLAVOR, t.getTransferDataFlavors());
    		if(zeeta1300Flavor != null){
    			if( ZEETA_1300_FLAVOR.getParameter(PROC_ID_KEY).equals(
    					zeeta1300Flavor.getParameter(PROC_ID_KEY))){	//プロセスIDが一致するか？
    				//★ここは同じプロセスのケース
    				ret = pasteFromSameProcess(c, fromInfo, toTree, toNode);
    				
    			}else{
    				//★ここは別プロセスのケース
	    			ret = pasteFromAnotherProcess(toTree, fromInfo.getNode(), toNode); 
    			}
    		}else{
				//★ここは別プロセスのケース
    	        if( fromInfo.getOwnerType().equals(MADE_FROM_STRING) ){	        	//文字列の場合
   	    			ret = pasteFromAnotherProcess(toTree, fromInfo.getNode(), toNode, true, false);
    	        }else{
            		DataFlavor zeeta1202Flavor = getFlavor(ZEETA_1202_FLAVOR, t.getTransferDataFlavors());
            		if(zeeta1202Flavor != null){		//以前のzeetaのノード
       	    			ret = pasteFromAnotherProcess(toTree, fromInfo.getNode(), toNode); 
            		}else{
            			//文字型でも昔のzeetaノードでもない場合・・・無視
            		}
    	        }    			
    		}
    		
    	}catch(RuntimeException e){
    		log.error(e);
    		//DnDの場合は、例外を投げても表示されない
            if(passedMethod_ == PassedMethod.CreTrns		//これは同一TreeのDnDの場合
            	|| passedMethod_ == PassedMethod.NONE	){	//これは異Treeからのペーストの場合
    			MessageView.show(ownerCompo_, e);
            }else{
            	throw e;
            }
    	}finally{
    		ownerCompo_.setCursor(Cursor.getDefaultCursor());
    	}
    	
       	
       	log.trace("end. ret="+ret);
        return ret;
    }
	/**
	 * 同じプロセスからのペースト
	 * @param c
	 * @param fromInfo
	 * @param toTree
	 * @param toNode
	 * @return
	 */
	private boolean pasteFromSameProcess(JComponent c,
			TreeNodeTransferable.Data fromInfo, ZTree toTree, DocNode toNode) {
    	if (fromInfo.getOwnerType().equals(jtree_.getTreeType())) {	//同じツリータイプのNode
			pasteFromSameTree(fromInfo, toTree, toNode);
		}else{		//異なるツリータイプの場合（mainTree<->2ndTree）
			pasteFromAnotherTree(fromInfo, toTree, toNode);
		}
    	return true;
	}

	/**
	 * 異なるtree上のコピペ
	 * @param c
	 * @param fromInfo
	 * @param toTree
	 * @param toNode
	 */
	private void pasteFromAnotherTree(
			TreeNodeTransferable.Data fromInfo, ZTree toTree, DocNode toNode) {
		try{
			paste(toTree, fromInfo.getNode(), toNode );
			
			//ペーストしたことを知らせる
			for(TransferListener listener : listeners_){
				listener.pastedNode(fromInfo);
			}

		}catch(DocModel.DoNothingException e){
			//同じ親に移動した場合なので、deleteDocTrnsしない
			//ただし、DnDの場合は、この後exportDoneが呼び出されてしまうので、
			//そこでdeleteDocTrnsされてしまう
			//これを止めるために以下のコードを記述
			passedMethod_ = PassedMethod.ImpData_ignore;
		}
		
	}
	protected void removeNode(DocNode node){
		if(node.isRoot()){
        	passedMethod_ = PassedMethod.NONE;
        	lastCreatedData_ = null;
			throw new AppException("Rootノードは移動できません");
		}

		DocModel youkenModel = (DocModel)jtree_.getModel();
 		youkenModel.deleteDocTrns(node);
	}
	/**
	 * 同じtree上のコピペ
	 * @param c
	 * @param fromInfo
	 * @param toTree
	 * @param toNode
	 */
	private void pasteFromSameTree(
			TreeNodeTransferable.Data fromInfo, ZTree toTree, DocNode toNode) {
		try{
			paste(toTree, fromInfo.getNode(), toNode );

			if( passedMethod_ == PassedMethod.ExpDone_X 	//ctrl+X->ctrl+V
					|| passedMethod_ == PassedMethod.CreTrns ){	//DnD
				removeNode(fromInfo.getNode());
		    }
		}catch(DocModel.DoNothingException e){
			//同じ親に移動した場合なので、deleteDocTrnsしない
			//ただし、DnDの場合は、この後exportDoneが呼び出されてしまうので、
			//そこでdeleteDocTrnsされてしまう
			//これを止めるために以下のコードを記述
			passedMethod_ = PassedMethod.ImpData_ignore;
		}
		
		//ActionTypeの初期化
		if(passedMethod_ == PassedMethod.CreTrns){	//これはDnDのはず
			passedMethod_ = PassedMethod.ImpData;
		}else if(passedMethod_ == PassedMethod.ExpDone_C){
  	//        	passedMethod_ = PassedMethod.NONE;	//ここで終了・・にすると連続してペーストできない
		}else if(passedMethod_ == PassedMethod.ExpDone_X){
			passedMethod_ = PassedMethod.ExpDone_C;		//次にペーストされた場合は、この動作とする
		}else if(passedMethod_ == PassedMethod.ImpData_ignore){
			//DnDを無効にするためのマークなので何もしない
		}else{
			RuntimeException e 
				= new RuntimeException("ありえない状態でimportDataが呼び出されている");
			log.error(e);
			throw e;
		}
	}
    
	private TreeNodeTransferable.Data getTransNode(Transferable t) {
		TreeNodeTransferable.Data ret = null;
		
		if(t == null){
			return ret;
		}
		try {
            if (hasFlavor(ZEETA_1300_FLAVOR, t.getTransferDataFlavors())) {
            	ret = (TreeNodeTransferable.Data)t.getTransferData(ZEETA_1300_FLAVOR);
            	
            }else if (hasFlavor(ZEETA_1202_FLAVOR, t.getTransferDataFlavors())) {
            	ret = new TreeNodeTransferable.Data(OLD_ZEETA_NODE, (DocNode)t.getTransferData(ZEETA_1202_FLAVOR));
            	
            }else if (hasFlavor(DataFlavor.stringFlavor, t.getTransferDataFlavors())){
            	ret = new TreeNodeTransferable.Data(MADE_FROM_STRING, createDocNodeFromText(t));
	
            }else{
            	throw new RuntimeException("canImpot()でOK言うたのに・・・");
            }
        } catch (UnsupportedFlavorException ufe) {
        	log.error("importData: unsupported data flavor");
        } catch (IOException ioe) {
        	log.error("importData: I/O exception");
        }
        
		return ret;
	}
	private boolean hasFlavor(DataFlavor target, DataFlavor[] flavors){
		return (getFlavor(target, flavors)==null)? false:true; 
		
	}
	private DataFlavor getFlavor(DataFlavor target, DataFlavor[] flavors){
        for(DataFlavor flavor: flavors){
            if (flavor.equals(target)) {
                return flavor;
            }
        }
        return null;

	}
    void paste(JTree jTree, DocNode fromNode, DocNode toNode){
    	log.trace("start");
		
		DocModel docModel = (DocModel)jTree.getModel();
		//要件構造を作成
		DocNode newNode = docModel.insertDoc(
					toNode,
					fromNode.getDoc());
		//refresh
		refreshNode_.putValue(ActRefreshNode.REFRESH_NODE, newNode);
		refreshNode_.actionPerformed(null);
		
		jTree.expandPath(new TreePath(toNode.getPath()));
		
		//ペーストしたNodeをアクティブにする
		setSelection(jTree, toNode, newNode.getDoc().getDocId());
		
    	log.trace("end");
    }
    /**
     * node直下のdocIdがidの子ノードにフォーカスをセットする
     * @param jTree
     * @param toNode
     * @param id
     */
    protected void setSelection(JTree jTree, DocNode node, long id){
		//ペーストしたNodeをアクティブにする
		Enumeration children = node.children();
		while(children.hasMoreElements()){
			DocNode child = (DocNode)children.nextElement();
			if(child.getDoc().getDocId() == id){
				TreePath path = new TreePath(child.getPath());
				jTree.setSelectionPath(path);
				jTree.scrollPathToVisible(path);
				break;
			}
		}
    	
    }
    
    boolean pasteFromAnotherProcess(ZTree toTree, DocNode fromNode, DocNode toNode){
		if(!toTree.canPasteNodeFromAnotherProcess()){
			JOptionPane.showMessageDialog(ownerCompo_, "このツリーへは、ペーストまたはドロップできません。");
			return false;
		}

		confirmImportNodePanel_.setImportDocName(fromNode.getDoc().getDocTitle());
		if( JOptionPane.showConfirmDialog(
				ownerCompo_
				,confirmImportNodePanel_
				,""
				,JOptionPane.YES_NO_OPTION
				,JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
		}else{
			return false;
		}
		boolean copyCopyData = confirmImportNodePanel_.inpCopyDataCopy_.isSelected();
		
        boolean ret = pasteFromAnotherProcess(toTree, fromNode, toNode, 
        		confirmImportNodePanel_.inpFromChildren_.isSelected(), 
        		copyCopyData);
        
        if(copyCopyData){
        	SwingUtilities.invokeLater(new Runnable(){
        		public void run(){
        			JOptionPane.showMessageDialog(
						ownerCompo_
						,"<html>importが完了しました。<br>" +
						 "CopyDataしたZeetaからのimportの場合、極力早い段階で再度CopyDataしてください。<br>" +
						 "importしたデータは、import元とは、違うIDになっている可能性があるため、<br>" +
						 "次回以降のimportは、正しい階層が作成されない可能性があります。"
						,""
						,JOptionPane.INFORMATION_MESSAGE);
        		}
        	});
        }
        return ret;
	}
    boolean pasteFromAnotherProcess(JTree jTree, DocNode fromNode, DocNode toNode, 
    		boolean ignoreRoot, boolean copyCopyData){
    	log.trace("start");
    	
    	if(fromNode == null){
    		//ペーストをキャンセルした場合にこれになる
    		return false;
    	}
    	
    	DocNode newNode = toNode;	//エラーが発生した場合のためにこれを入れておく
    	try{
			DocModel youkenModel = (DocModel)jTree.getModel();
			
			//ペースト先ノードとペーストノードの要件構造を作成
			if(ignoreRoot){	//fromNodeのrootは追加しない
				Enumeration children = fromNode.children();
				while(children.hasMoreElements()){
					newNode = (DocNode)children.nextElement();
					youkenModel.insertDocFromAnotherProcessTrns(
							toNode,	newNode, copyCopyData);
				}
			}else{
				newNode = youkenModel.insertDocFromAnotherProcessTrns(
						toNode,	fromNode, copyCopyData);
			}

    	}finally{
    		//循環参照等のエラーが発生すると、DBはロールバックされるが
    		//ツリー上にデータが追加されたままになってしまうので、refreshする。
			refreshNode_.putValue(ActRefreshNode.REFRESH_NODE, newNode);
			refreshNode_.actionPerformed(null);
			
			jTree.expandPath(new TreePath(toNode.getPath()));
			//ペーストしたNodeをアクティブにする
			if(newNode != null){
				setSelection(jTree, toNode, newNode.getDoc().getDocId());
			}
    	}
    	
    	log.trace("end");
    	return true;
    }
    void printNode(DocNode fromNode, String indent){
    	log.debug(indent+fromNode.getDoc());
    	indent = indent + "\t";
    	Enumeration children = fromNode.children();
    	while(children.hasMoreElements()){
    		DocNode child = (DocNode)children.nextElement();
    		printNode(child,indent);
    	}
    }
    /* 以下の２つのケースで呼び出される。
     * ・ctrl+X, ctrl+Cをタイプした場合<br/>
     * ・DnDでドロップした場合importの後に呼び出される
     * @see javax.swing.TransferHandler#exportDone(javax.swing.JComponent, java.awt.datatransfer.Transferable, int)
     */
    protected void exportDone(JComponent c, Transferable t, int action) {
    	log.trace("start");
		DocNode fromNode = null;
		if( t!=null){
			fromNode = getTransNode(t).getNode();
		}
        if(fromNode == null){
        	passedMethod_ = PassedMethod.NONE;	//クリップボードには、別なデータが入っている
        	lastCreatedData_ = null;
        	return ;
        }
        if(passedMethod_ == PassedMethod.CreTrns){
			if(action == MOVE){
				passedMethod_ = PassedMethod.ExpDone_X;
			}else if(action == COPY){
				passedMethod_ = PassedMethod.ExpDone_C;
			}else{
				//DnDでペーストに失敗した場合にここに来るようだ
			}
        }else if(passedMethod_ == PassedMethod.ImpData){	//これはDnDのケース
			passedMethod_ = PassedMethod.NONE;
        }else if(passedMethod_ == PassedMethod.ImpData_ignore){	//これはDnDを無効にするケース
			passedMethod_ = PassedMethod.NONE;
        }else if(passedMethod_ == PassedMethod.Paste_Another_Tree){	//他treeへ移動した場合
			passedMethod_ = PassedMethod.NONE;
        }else{
        	//この状態は、ありえない
        	RuntimeException e 
        		= new RuntimeException("ありえない状態でimportDataが呼び出されている");
        	log.error(e);
			MessageView.show(ownerCompo_, e);
        	throw e;
        }

    	log.trace("end");
    }

//    private boolean isStringFlavor(JComponent c, DataFlavor[] flavors) {
//        if ( !(c instanceof JTree) ) {return false;}
//        for (int i = 0; i < flavors.length; i++) {
//            if (DataFlavor.stringFlavor.equals(flavors[i])) {
//                return true;
//            }
//        }
//        return false;
//    }

    public boolean canImport(JComponent c, DataFlavor[] flavors) {
    	log.debug("flavors="+flavors);
        if ( !(c instanceof ZTree) ) {return false;}	//ZTreeでしかこのクラスを使用しないのでfalseはあり得ない
        ZTree toTree = (ZTree)c;
        boolean ret = false;

        //まず、一致するフレーバーをチェック
        for(DataFlavor flavor: flavors){
        	for(DataFlavor myFlavor: MY_FLAVORS){
        		if (flavor.equals(myFlavor)) {
        			ret = true;
        		}
        	}
        }
        if( !ret ){		//存在しなかったらそれまでよ
        	return ret;
        }
        
        //更に細かいチェックをするのだ
        ret = false;
		DataFlavor zeeta1300Flavor = getFlavor(ZEETA_1300_FLAVOR, flavors);
		if(zeeta1300Flavor != null){
			if( ZEETA_1300_FLAVOR.getParameter(PROC_ID_KEY).equals(
					zeeta1300Flavor.getParameter(PROC_ID_KEY))){	//プロセスIDが一致するか？
				//★ここは同じプロセスのケース
				ret = true;
				
			}else{
				//★ここは別プロセスのケース
	    		if(toTree.canPasteNodeFromAnotherProcess()){
	    			ret = true; 
	    		}
			}
		}else{
			//★ここは別プロセスのケース
    		if(toTree.canPasteNodeFromAnotherProcess()){
    			ret = true; 
    		}
		}
        return ret;
    }

    protected Transferable createTransferable(JComponent c) {
    	log.trace("start");
    	Transferable ret = null;
    	ZTree tree;
        if (c instanceof ZTree) {
            tree = (ZTree)c;
            if(tree.getSelectionPath() == null){
            	return null;
            }
            DocNode selectedNode = 
            	(DocNode)tree.getSelectionPath().getLastPathComponent();
            if (selectedNode == null) {
                return null;
            }
            ret = new TreeNodeTransferable(
            		tree.getTreeType(),
            		selectedNode, 
            		MY_FLAVORS );
            lastCreatedData_ = selectedNode;
			passedMethod_ = PassedMethod.CreTrns;
        }
    	log.trace("end");
        return ret;
    }

    public int getSourceActions(JComponent c) {
    	log.trace("ZTree.dndStatus = "+ ((ZTree)c).getDnDStatus());
//    	 return cnvDnDAction( ((ZTree)c).getDnDStatus() );
    	//ここは、これしか返せないらしい。
    	return COPY_OR_MOVE;
    }
}

