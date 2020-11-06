package jp.tokyo.selj.view;

/*
 * 
 * 注意！：このオブジェクトは、複数のJTreeで共有しないこと。
 * 　　　というより、他の画面のJTreeにTransferHandlerをセットしないように設計すること。
 */

import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Enumeration;

import javax.swing.Action;
import javax.swing.ActionMap;
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

import jp.tokyo.selj.common.AppException;
import jp.tokyo.selj.common.CatString;
import jp.tokyo.selj.common.MessageView;
import jp.tokyo.selj.dao.Doc;
import jp.tokyo.selj.model.DocModel;
import jp.tokyo.selj.model.DocNode;
import jp.tokyo.selj.view.FrmZeetaMain.ActRefreshSpecific;

import org.apache.log4j.Logger;

public class TreeNodeTransferHandler extends TransferHandler {
	Logger log = Logger.getLogger(this.getClass());
    DataFlavor localFlavor, serialFlavor;
    String localType = DataFlavor.javaJVMLocalObjectMimeType +
                                ";class=jp.tokyo.selj.model.DocModel";
    FrmZeetaMain mainView_;	//ダイアログの表示に使用する
    ActionMap actionMap_;
    enum PassedMethod {NONE, CreTrns, ExpDone_X, ExpDone_C, ImpData, ImpData_ignore}
    PassedMethod passedMethod_ = PassedMethod.NONE;
    Object lastCreatedData_ = null;

    public TreeNodeTransferHandler(FrmZeetaMain mainView, ActionMap actionMap) {
        try {
            localFlavor = new DataFlavor(localType);
        } catch (ClassNotFoundException e) {
       		log.debug("TreeNodeTransferHandler: unable to create data flavor");
        }
        serialFlavor = new DataFlavor(DocNode.class,"DocNode");
        mainView_ = mainView;
        actionMap_ = actionMap;
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
				mainView_
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
    		if(pos > 1){
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
    
    
	
    /* 以下の２つのケースで呼び出される。
     * ・ctrl+Vをタイプした場合<br/>
     * ・DnDでドロップした場合
     * @see javax.swing.TransferHandler#importData(javax.swing.JComponent, java.awt.datatransfer.Transferable)
     */
    public boolean importData(JComponent c, Transferable t) {
    	log.trace("start");
    	
    	boolean isStringData = false;
    	DocNode fromNode = null;
    	if(isStringFlavor(c, t.getTransferDataFlavors())){
    		fromNode = createDocNodeFromText(t);
    		isStringData = true;
    	}else if (!canImport(c, t.getTransferDataFlavors())) {
        	log.debug("import できません");
        	passedMethod_ = PassedMethod.NONE;	//別なデータが入っている
            return false;
        }else{
            fromNode = getTransNode(t);
        }

    	if(fromNode == null){
        	passedMethod_ = PassedMethod.NONE;	//クリップボードには、別なデータが入っている
        	return false;
        }

        JTree tree = (JTree)c;	//cの型は、canImportでチェック済み
        if(tree.getSelectionPath() == null){
        	//何も選択されていないとここに来る
//        	passedMethod_ = PassedMethod.NONE;
        	return false;
        }
        //転送先Node
        DocNode toNode = (DocNode)tree.getSelectionPath().getLastPathComponent();

        boolean isSameTree = false;
        boolean copyFromChildren = false;
        boolean copyCopyData = false;
        if( isStringData ){
        	copyFromChildren = isStringData;
        }else if (lastCreatedData_ == fromNode) {	//同じツリー上のNode
			isSameTree = true;
		}else{
			confirmImportNodePanel_.setImportDocName(fromNode.getDoc().getDocTitle());
			if( JOptionPane.showConfirmDialog(
					mainView_
					,confirmImportNodePanel_
					,""
					,JOptionPane.YES_NO_OPTION
					,JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
			}else{
				return false;
			}
			copyFromChildren = confirmImportNodePanel_.inpFromChildren_.isSelected();
			copyCopyData = confirmImportNodePanel_.inpCopyDataCopy_.isSelected();
		}
        log.debug((isSameTree)? "同じツリー上のNode":"異なるツリー上のNode");
        log.debug("転送元は "+fromNode);
        log.debug("転送先は "+toNode);

        boolean isDnD = false;
        if(passedMethod_ == PassedMethod.CreTrns){	//これはDnDのはず
        	isDnD = true;
        }
        boolean ret;
    	try{
    		mainView_.setCursor(Util.WAIT_CURSOR);
    		if (isSameTree) {
    			try{
	        		paste(tree, fromNode, toNode );
	                if(passedMethod_ == PassedMethod.ExpDone_X){
	        			DocModel youkenModel = (DocModel)((JTree)c).getModel();
	        	 		youkenModel.deleteDocTrns(fromNode);
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
                ret = true;
	        }else{
	            pasteFromAnotherProcess(tree, fromNode, toNode, copyFromChildren, copyCopyData);
	        	ret = true;
	            if( !isStringData && copyCopyData){
	            	SwingUtilities.invokeLater(new Runnable(){
	            		public void run(){
	            			JOptionPane.showMessageDialog(
								mainView_
								,"<html>importが完了しました。<br>" +
								 "CopyDataしたZeetaからのimportの場合、極力早い段階で再度CopyDataしてください。<br>" +
								 "importしたデータは、import元とは、違うIDになっている可能性があるため、<br>" +
								 "次回以降のimportは、正しい階層が作成されない可能性があります。"
								,""
								,JOptionPane.INFORMATION_MESSAGE);
	            		}
	            	});
	            }
	        }
    	}catch(RuntimeException e){
    		log.error(e);
    		if(isDnD){
    			//仕方がないので自分で出す。
    			MessageView.show(mainView_, e);
    		}
    		throw e;
    	}finally{
    		mainView_.setCursor(Cursor.getDefaultCursor());
    	}
    	
       	
       	log.trace("end. ret="+ret);
        return ret;
    }

	private DocNode getTransNode(Transferable t) {
		DocNode ret = null;
		if(t == null){
			return ret;
		}
		try {
            if (hasLocalArrayListFlavor(t.getTransferDataFlavors())) {
            	ret = (DocNode)t.getTransferData(localFlavor);
            } else if (hasSerialArrayListFlavor(t.getTransferDataFlavors())) {
            	ret = (DocNode)t.getTransferData(serialFlavor);
            }
        } catch (UnsupportedFlavorException ufe) {
        	log.error("importData: unsupported data flavor");
        } catch (IOException ioe) {
        	log.error("importData: I/O exception");
        }
		return ret;
	}
    void paste(JTree jTree, DocNode fromNode, DocNode toNode){
    	log.trace("start");
		
		DocModel docModel = (DocModel)jTree.getModel();
		//要件構造を作成
		DocNode newNode = docModel.insertDoc(
					toNode,
					fromNode.getDoc());
		//refresh
		Action act = actionMap_.get(ActRefreshSpecific.class);
		act.putValue(ActRefreshSpecific.REFRESH_NODE, newNode);
		act.actionPerformed(null);
		
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
    void setSelection(JTree jTree, DocNode node, long id){
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
    
    void pasteFromAnotherProcess(JTree jTree, DocNode fromNode, DocNode toNode, 
    		boolean ignoreRoot, boolean copyCopyData){
    	log.trace("start");
    	
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
			Action act = actionMap_.get(ActRefreshSpecific.class);
			act.putValue(ActRefreshSpecific.REFRESH_NODE, newNode);
			act.actionPerformed(null);
			
			jTree.expandPath(new TreePath(toNode.getPath()));
			//ペーストしたNodeをアクティブにする
			if(newNode != null){
				setSelection(jTree, toNode, newNode.getDoc().getDocId());
			}
    	}
    	
    	log.trace("end");
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
		DocNode fromNode = getTransNode(t);
        if(fromNode == null){
        	passedMethod_ = PassedMethod.NONE;	//クリップボードには、別なデータが入っている
        	lastCreatedData_ = null;
        	return ;
        }
		if(action == MOVE){
			if(fromNode.isRoot()){
	        	passedMethod_ = PassedMethod.NONE;
	        	lastCreatedData_ = null;
				throw new AppException("Rootノードは移動できません");
			}
		}
        if(passedMethod_ == PassedMethod.CreTrns){
			if(action == MOVE){
				passedMethod_ = PassedMethod.ExpDone_X;
			}else{
				passedMethod_ = PassedMethod.ExpDone_C;
			}
        }else if(passedMethod_ == PassedMethod.ImpData){	//これはDnDのケース
			passedMethod_ = PassedMethod.NONE;
			if(action == MOVE){
				DocModel youkenModel = (DocModel)((JTree)c).getModel();
		 		youkenModel.deleteDocTrns(fromNode);
			}
        }else if(passedMethod_ == PassedMethod.ImpData_ignore){	//これはDnDを無効にするケース
			passedMethod_ = PassedMethod.NONE;
        }else{
        	//この状態は、ありえない
        	RuntimeException e 
        		= new RuntimeException("ありえない状態でimportDataが呼び出されている");
        	log.error(e);
			MessageView.show(mainView_, e);
        	throw e;
        }

    	log.trace("end");
    }

    private boolean hasLocalArrayListFlavor(DataFlavor[] flavors) {
        if (localFlavor == null) {
            return false;
        }

        for (int i = 0; i < flavors.length; i++) {
            if (flavors[i].equals(localFlavor)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasSerialArrayListFlavor(DataFlavor[] flavors) {
        if (serialFlavor == null) {
            return false;
        }

        for (int i = 0; i < flavors.length; i++) {
            if (flavors[i].equals(serialFlavor)) {
                return true;
            }
        }
        return false;
    }
    public boolean isStringFlavor(JComponent c, DataFlavor[] flavors) {
        if ( !(c instanceof JTree) ) {return false;}
        for (int i = 0; i < flavors.length; i++) {
            if (DataFlavor.stringFlavor.equals(flavors[i])) {
                return true;
            }
        }
        return false;
    }

    public boolean canImport(JComponent c, DataFlavor[] flavors) {
    	log.debug("flavors="+flavors);
        if ( !(c instanceof JTree) ) {return false;}
        
        //以下はサンプルにあったもの
        if (hasLocalArrayListFlavor(flavors))  { return true; }
        if (hasSerialArrayListFlavor(flavors)) { return true; }
        return false;
    }

    protected Transferable createTransferable(JComponent c) {
    	log.trace("start");
    	Transferable ret = null;
    	JTree tree;
        if (c instanceof JTree) {
            tree = (JTree)c;
            if(tree.getSelectionPath() == null){
            	return null;
            }
            DocNode selectedNode = 
            	(DocNode)tree.getSelectionPath().getLastPathComponent();
            if (selectedNode == null) {
                return null;
            }
            ret = new TreeNodeTransferable(selectedNode);
            lastCreatedData_ = selectedNode;
			passedMethod_ = PassedMethod.CreTrns;
        }
    	log.trace("end");
        return ret;
    }

    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }

    /**
     * @author mikami
     *
     */
    public class TreeNodeTransferable implements Transferable {
    	DocNode node_;
        public TreeNodeTransferable(DocNode node) {
        	log.trace("start");
        	node_ = node;
        	log.trace("end");
        }

		public Object getTransferData(DataFlavor flavor)
                                 throws UnsupportedFlavorException {
        	log.trace("start");
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
        	log.trace("end");
            return node_;
        }

        public DataFlavor[] getTransferDataFlavors() {
        	log.trace("start/end");
            return new DataFlavor[] { localFlavor,
                                      serialFlavor };
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
        	log.trace("start");
        	log.debug("flavor="+flavor);
            if (localFlavor.equals(flavor)) {
                return true;
            }
            if (serialFlavor.equals(flavor)) {
                return true;
            }
        	log.trace("end");
            return false;
        }
    }
    
}

