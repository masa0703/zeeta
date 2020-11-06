package jp.tokyo.selj.view.trans;

/*
 * 
 * ���ӁI�F���̃I�u�W�F�N�g�́A������JTree�ŋ��L���Ȃ����ƁB
 * �@�@�@�Ƃ������A���̉�ʂ�JTree��TransferHandler���Z�b�g���Ȃ��悤�ɐ݌v���邱�ƁB
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
    
    Component ownerCompo_;	//�_�C�A���O�̕\���Ɏg�p����
    ZTree jtree_;
    Action refreshNode_;
    enum PassedMethod {NONE, CreTrns, ExpDone_X, ExpDone_C, ImpData, ImpData_ignore, Paste_Another_Tree}
    PassedMethod passedMethod_ = PassedMethod.NONE;
    Object lastCreatedData_ = null;
    
    static {
        try {
			ZEETA_1202_FLAVOR = new DataFlavor(ZEETA_1202_MIME_TYPE);	//1.2.02�ȑO��zeeta�m�[�h
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
				"<html>���̃`�F�b�N��On�ɂ����ꍇ��<b>���ӓ_</b><br>" +
				"<pre>" +
				"  1.�V�����m�[�h�́A�ǉ������<br>" +
				"  2.�����̃m�[�h�́A�ė��p����(���̃`�F�b�N��t���Ȃ��ꍇ�́A�ǉ������)<br>" +
				"  3.�����m�[�h�̃^�C�g���ύX�́A���f����Ȃ�<br>" +
				"  4.�����m�[�h�̈ړ��A�폜�͔��f����Ȃ�<br>" +
				"  5.CopyData������Ɍ�Zeeta��Ń^�C�g�����ύX���ꂽ�m�[�h�͊����m�[�h�Ƃ݂Ȃ���Ȃ�<br>" +
				"  6.��x���̋@�\�ŃR�s�[������A�ēxCopyData�����ɓ����m�[�h���R�s�[����ƒǉ��m�[�h�́A2�d�ɓo�^�����<br>" +
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
					"�q�m�[�h�z������import����"
			);
			add(inpFromChildren_);
			inpCopyDataCopy_ = new JCheckBox(
					"<html>CopyData����Zeeta��̃f�[�^������Zeeta�f�[�^�ɍēx�R�s�[����<br>");
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
			 "����Tree node(<span style=\"color: #CC0000;\"><b>"+ name +"</b></span>)��import���܂��B<br>" +
			 "���A�R�s�[��node�́A�c���[��Ƀ��[�h����Ă��镪����import���܂��B<br>" +
			 "�I��node�z���S�Ă��R�s�[����ꍇ�́A�R�s�[��node��<br>" +
			 "�u�I���m�[�h��S�ēW�J�v�{�^���œW�J���Ă���R�s�[���Ă��������B<br>" +
			 "<br>��낵���ł����H<br>" +
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
					"�e�L�X�g����m�[�h���쐬���܂��B<br>" +
					"�P�s�łP�m�[�h�ATab�̃C���f���g�ŊK�w���쐬���܂��B<br>" +
					"�{������荞�ޏꍇ�A\"�ň͂ނƉ��s���Ă��P�m�[�h�ƂɂȂ�܂��B<br>" +
					"<br>��낵���ł����H<br>" +
					"<hr>" +
					"</html>");
//			messageLabel_.setFont(planeFont_);
			add(messageLabel_);
			inpImportMemo_ = new JCheckBox(
					"�^�C�g�����̌���Tab�ȍ~��{���Ƃ��Ď�荞��"
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
			//Node�K�w���쐬
//			String[] lines = text.split(System.getProperty("line.separator"));	�Ȃ����������͑ʖڂ�����
			String[] lines = text.split("\n");
			
			//�{���̃_�u���N�I�[�e�[�V�����Ԃ�A������
			lines = CatString.concatLine(lines);
			
			int preTabSize = -1;
			DocNode preDocNode = rootDocNode;
			for(int i=0; i<lines.length; i++){
				if("".equals(lines[i].trim())){
					continue;	//��s�͖���
				}
				int curTabSize = CatString.countTab(lines[i]); 
				isExistNode = true;
				
				Doc newDoc = makeDoc(i, lines[i]);
				DocNode curDocNode = new DocNode(newDoc);
				if( preDocNode != null){
					int dist = preTabSize - curTabSize;
					if(dist == 0){
						((DocNode)preDocNode.getParent()).add(curDocNode);		//�Z��
					}else if(dist < 0){
						curTabSize = preTabSize + 1;	//���ʂɑ���tab���������狸��
						preDocNode.add(curDocNode);		//�q
					}else{
						for(int j=0; j<dist; j++){
							if(preDocNode.getParent() == null){
								//�P�s�ڂ���tab�������Ă��āA��̍s��tab�Ȃ��s�������
								//���̂悤�ɂȂ�B
								//��j
								//<tab>aaa
								//bbb
								break;
							}
							preDocNode = (DocNode)preDocNode.getParent();
						}
						if(preDocNode == rootDocNode){
							preDocNode.add(curDocNode);		//�d�����Ȃ��̂Ŏq�Ƃ���
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
     * ����������ꍇ�́Anull��Ԃ�
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
    	doc = new Doc(no+1, importText);	//Id�̓��j�[�N�ł���΂悢
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
        	log.debug("import �ł��܂���");
        	passedMethod_ = PassedMethod.NONE;	//�ʂȃf�[�^�������Ă���
        	return null;
        }
    	
    	TreeNodeTransferable.Data ret = getTransNode(t);
    	
//    	if(isStringFlavor(c, t.getTransferDataFlavors())){
//    		DocNode fromNode = createDocNodeFromText(t);
//        	ret = new TreeNodeTransferable.Data(MADE_FROM_STRING, "", (DocNode)fromNode);
//    		
//    	}else if (!canImport(c, t.getTransferDataFlavors())) {
//        	log.debug("import �ł��܂���");
//        	passedMethod_ = PassedMethod.NONE;	//�ʂȃf�[�^�������Ă���
//        }else{
//        	ret = getTransNode(t);
//        }
    	return ret;
    }
	
    /* �ȉ��̂Q�̃P�[�X�ŌĂяo�����B
     * �Ectrl+V���^�C�v�����ꍇ<br/>
     * �EDnD�Ńh���b�v�����ꍇ
     * @see javax.swing.TransferHandler#importData(javax.swing.JComponent, java.awt.datatransfer.Transferable)
     */
    public boolean importData(JComponent c, Transferable t) {
    	log.trace("start");
    	
    	//=== Drop�m�[�h�������o��
    	TreeNodeTransferable.Data fromInfo = getFromNodeInfo(c, t);
    	if(fromInfo == null){
        	passedMethod_ = PassedMethod.NONE;	//�N���b�v�{�[�h�ɂ́A�ʂȃf�[�^�������Ă���
        	return false;
        }

    	//=== �]����Node
        ZTree toTree = (ZTree)c;	//c�̌^�́AcanImport�Ń`�F�b�N�ς�
        if(toTree.getSelectionPath() == null){
        	//�����I������Ă��Ȃ��Ƃ����ɗ���
//        	passedMethod_ = PassedMethod.NONE;
        	return false;
        }
        DocNode toNode = (DocNode)toTree.getSelectionPath().getLastPathComponent();

        //=== �]��
        boolean ret = false;

    	try{
    		ownerCompo_.setCursor(Util.WAIT_CURSOR);
    		
    		//�����v���Z�X���ۂ�
    		DataFlavor zeeta1300Flavor = getFlavor(ZEETA_1300_FLAVOR, t.getTransferDataFlavors());
    		if(zeeta1300Flavor != null){
    			if( ZEETA_1300_FLAVOR.getParameter(PROC_ID_KEY).equals(
    					zeeta1300Flavor.getParameter(PROC_ID_KEY))){	//�v���Z�XID����v���邩�H
    				//�������͓����v���Z�X�̃P�[�X
    				ret = pasteFromSameProcess(c, fromInfo, toTree, toNode);
    				
    			}else{
    				//�������͕ʃv���Z�X�̃P�[�X
	    			ret = pasteFromAnotherProcess(toTree, fromInfo.getNode(), toNode); 
    			}
    		}else{
				//�������͕ʃv���Z�X�̃P�[�X
    	        if( fromInfo.getOwnerType().equals(MADE_FROM_STRING) ){	        	//������̏ꍇ
   	    			ret = pasteFromAnotherProcess(toTree, fromInfo.getNode(), toNode, true, false);
    	        }else{
            		DataFlavor zeeta1202Flavor = getFlavor(ZEETA_1202_FLAVOR, t.getTransferDataFlavors());
            		if(zeeta1202Flavor != null){		//�ȑO��zeeta�̃m�[�h
       	    			ret = pasteFromAnotherProcess(toTree, fromInfo.getNode(), toNode); 
            		}else{
            			//�����^�ł��̂�zeeta�m�[�h�ł��Ȃ��ꍇ�E�E�E����
            		}
    	        }    			
    		}
    		
    	}catch(RuntimeException e){
    		log.error(e);
    		//DnD�̏ꍇ�́A��O�𓊂��Ă��\������Ȃ�
            if(passedMethod_ == PassedMethod.CreTrns		//����͓���Tree��DnD�̏ꍇ
            	|| passedMethod_ == PassedMethod.NONE	){	//����͈�Tree����̃y�[�X�g�̏ꍇ
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
	 * �����v���Z�X����̃y�[�X�g
	 * @param c
	 * @param fromInfo
	 * @param toTree
	 * @param toNode
	 * @return
	 */
	private boolean pasteFromSameProcess(JComponent c,
			TreeNodeTransferable.Data fromInfo, ZTree toTree, DocNode toNode) {
    	if (fromInfo.getOwnerType().equals(jtree_.getTreeType())) {	//�����c���[�^�C�v��Node
			pasteFromSameTree(fromInfo, toTree, toNode);
		}else{		//�قȂ�c���[�^�C�v�̏ꍇ�imainTree<->2ndTree�j
			pasteFromAnotherTree(fromInfo, toTree, toNode);
		}
    	return true;
	}

	/**
	 * �قȂ�tree��̃R�s�y
	 * @param c
	 * @param fromInfo
	 * @param toTree
	 * @param toNode
	 */
	private void pasteFromAnotherTree(
			TreeNodeTransferable.Data fromInfo, ZTree toTree, DocNode toNode) {
		try{
			paste(toTree, fromInfo.getNode(), toNode );
			
			//�y�[�X�g�������Ƃ�m�点��
			for(TransferListener listener : listeners_){
				listener.pastedNode(fromInfo);
			}

		}catch(DocModel.DoNothingException e){
			//�����e�Ɉړ������ꍇ�Ȃ̂ŁAdeleteDocTrns���Ȃ�
			//�������ADnD�̏ꍇ�́A���̌�exportDone���Ăяo����Ă��܂��̂ŁA
			//������deleteDocTrns����Ă��܂�
			//������~�߂邽�߂Ɉȉ��̃R�[�h���L�q
			passedMethod_ = PassedMethod.ImpData_ignore;
		}
		
	}
	protected void removeNode(DocNode node){
		if(node.isRoot()){
        	passedMethod_ = PassedMethod.NONE;
        	lastCreatedData_ = null;
			throw new AppException("Root�m�[�h�͈ړ��ł��܂���");
		}

		DocModel youkenModel = (DocModel)jtree_.getModel();
 		youkenModel.deleteDocTrns(node);
	}
	/**
	 * ����tree��̃R�s�y
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
			//�����e�Ɉړ������ꍇ�Ȃ̂ŁAdeleteDocTrns���Ȃ�
			//�������ADnD�̏ꍇ�́A���̌�exportDone���Ăяo����Ă��܂��̂ŁA
			//������deleteDocTrns����Ă��܂�
			//������~�߂邽�߂Ɉȉ��̃R�[�h���L�q
			passedMethod_ = PassedMethod.ImpData_ignore;
		}
		
		//ActionType�̏�����
		if(passedMethod_ == PassedMethod.CreTrns){	//�����DnD�̂͂�
			passedMethod_ = PassedMethod.ImpData;
		}else if(passedMethod_ == PassedMethod.ExpDone_C){
  	//        	passedMethod_ = PassedMethod.NONE;	//�����ŏI���E�E�ɂ���ƘA�����ăy�[�X�g�ł��Ȃ�
		}else if(passedMethod_ == PassedMethod.ExpDone_X){
			passedMethod_ = PassedMethod.ExpDone_C;		//���Ƀy�[�X�g���ꂽ�ꍇ�́A���̓���Ƃ���
		}else if(passedMethod_ == PassedMethod.ImpData_ignore){
			//DnD�𖳌��ɂ��邽�߂̃}�[�N�Ȃ̂ŉ������Ȃ�
		}else{
			RuntimeException e 
				= new RuntimeException("���肦�Ȃ���Ԃ�importData���Ăяo����Ă���");
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
            	throw new RuntimeException("canImpot()��OK�������̂ɁE�E�E");
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
		//�v���\�����쐬
		DocNode newNode = docModel.insertDoc(
					toNode,
					fromNode.getDoc());
		//refresh
		refreshNode_.putValue(ActRefreshNode.REFRESH_NODE, newNode);
		refreshNode_.actionPerformed(null);
		
		jTree.expandPath(new TreePath(toNode.getPath()));
		
		//�y�[�X�g����Node���A�N�e�B�u�ɂ���
		setSelection(jTree, toNode, newNode.getDoc().getDocId());
		
    	log.trace("end");
    }
    /**
     * node������docId��id�̎q�m�[�h�Ƀt�H�[�J�X���Z�b�g����
     * @param jTree
     * @param toNode
     * @param id
     */
    protected void setSelection(JTree jTree, DocNode node, long id){
		//�y�[�X�g����Node���A�N�e�B�u�ɂ���
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
			JOptionPane.showMessageDialog(ownerCompo_, "���̃c���[�ւ́A�y�[�X�g�܂��̓h���b�v�ł��܂���B");
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
						,"<html>import���������܂����B<br>" +
						 "CopyData����Zeeta�����import�̏ꍇ�A�ɗ͑����i�K�ōēxCopyData���Ă��������B<br>" +
						 "import�����f�[�^�́Aimport���Ƃ́A�ႤID�ɂȂ��Ă���\�������邽�߁A<br>" +
						 "����ȍ~��import�́A�������K�w���쐬����Ȃ��\��������܂��B"
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
    		//�y�[�X�g���L�����Z�������ꍇ�ɂ���ɂȂ�
    		return false;
    	}
    	
    	DocNode newNode = toNode;	//�G���[�����������ꍇ�̂��߂ɂ�������Ă���
    	try{
			DocModel youkenModel = (DocModel)jTree.getModel();
			
			//�y�[�X�g��m�[�h�ƃy�[�X�g�m�[�h�̗v���\�����쐬
			if(ignoreRoot){	//fromNode��root�͒ǉ����Ȃ�
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
    		//�z�Q�Ɠ��̃G���[����������ƁADB�̓��[���o�b�N����邪
    		//�c���[��Ƀf�[�^���ǉ����ꂽ�܂܂ɂȂ��Ă��܂��̂ŁArefresh����B
			refreshNode_.putValue(ActRefreshNode.REFRESH_NODE, newNode);
			refreshNode_.actionPerformed(null);
			
			jTree.expandPath(new TreePath(toNode.getPath()));
			//�y�[�X�g����Node���A�N�e�B�u�ɂ���
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
    /* �ȉ��̂Q�̃P�[�X�ŌĂяo�����B
     * �Ectrl+X, ctrl+C���^�C�v�����ꍇ<br/>
     * �EDnD�Ńh���b�v�����ꍇimport�̌�ɌĂяo�����
     * @see javax.swing.TransferHandler#exportDone(javax.swing.JComponent, java.awt.datatransfer.Transferable, int)
     */
    protected void exportDone(JComponent c, Transferable t, int action) {
    	log.trace("start");
		DocNode fromNode = null;
		if( t!=null){
			fromNode = getTransNode(t).getNode();
		}
        if(fromNode == null){
        	passedMethod_ = PassedMethod.NONE;	//�N���b�v�{�[�h�ɂ́A�ʂȃf�[�^�������Ă���
        	lastCreatedData_ = null;
        	return ;
        }
        if(passedMethod_ == PassedMethod.CreTrns){
			if(action == MOVE){
				passedMethod_ = PassedMethod.ExpDone_X;
			}else if(action == COPY){
				passedMethod_ = PassedMethod.ExpDone_C;
			}else{
				//DnD�Ńy�[�X�g�Ɏ��s�����ꍇ�ɂ����ɗ���悤��
			}
        }else if(passedMethod_ == PassedMethod.ImpData){	//�����DnD�̃P�[�X
			passedMethod_ = PassedMethod.NONE;
        }else if(passedMethod_ == PassedMethod.ImpData_ignore){	//�����DnD�𖳌��ɂ���P�[�X
			passedMethod_ = PassedMethod.NONE;
        }else if(passedMethod_ == PassedMethod.Paste_Another_Tree){	//��tree�ֈړ������ꍇ
			passedMethod_ = PassedMethod.NONE;
        }else{
        	//���̏�Ԃ́A���肦�Ȃ�
        	RuntimeException e 
        		= new RuntimeException("���肦�Ȃ���Ԃ�importData���Ăяo����Ă���");
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
        if ( !(c instanceof ZTree) ) {return false;}	//ZTree�ł������̃N���X���g�p���Ȃ��̂�false�͂��蓾�Ȃ�
        ZTree toTree = (ZTree)c;
        boolean ret = false;

        //�܂��A��v����t���[�o�[���`�F�b�N
        for(DataFlavor flavor: flavors){
        	for(DataFlavor myFlavor: MY_FLAVORS){
        		if (flavor.equals(myFlavor)) {
        			ret = true;
        		}
        	}
        }
        if( !ret ){		//���݂��Ȃ������炻��܂ł�
        	return ret;
        }
        
        //�X�ɍׂ����`�F�b�N������̂�
        ret = false;
		DataFlavor zeeta1300Flavor = getFlavor(ZEETA_1300_FLAVOR, flavors);
		if(zeeta1300Flavor != null){
			if( ZEETA_1300_FLAVOR.getParameter(PROC_ID_KEY).equals(
					zeeta1300Flavor.getParameter(PROC_ID_KEY))){	//�v���Z�XID����v���邩�H
				//�������͓����v���Z�X�̃P�[�X
				ret = true;
				
			}else{
				//�������͕ʃv���Z�X�̃P�[�X
	    		if(toTree.canPasteNodeFromAnotherProcess()){
	    			ret = true; 
	    		}
			}
		}else{
			//�������͕ʃv���Z�X�̃P�[�X
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
    	//�����́A���ꂵ���Ԃ��Ȃ��炵���B
    	return COPY_OR_MOVE;
    }
}

