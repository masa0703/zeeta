package jp.tokyo.selj.view.trans;

import java.awt.Component;

import javax.swing.Action;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;

import jp.tokyo.selj.common.AppException;
import jp.tokyo.selj.model.DocModel;
import jp.tokyo.selj.model.DocNode;
import jp.tokyo.selj.view.component.ZTree;
import jp.tokyo.selj.view.component.ZTree.DnDStatus;
import jp.tokyo.selj.view.trans.TreeNodeTransferHandler.PassedMethod;

public class TreeNodeTransferHandler2 extends TreeNodeTransferHandler 
	implements TreeNodeTransferHandler.TransferListener{

	public TreeNodeTransferHandler2(Component mainView, ZTree jtree,
			Action refreshNode) {
		super(mainView, jtree, refreshNode);
		
	}

	public void pastedNode(TreeNodeTransferable.Data fromInfo) {
		//ActionType‚ÌXV
		if(passedMethod_ == PassedMethod.CreTrns){	//‚±‚ê‚ÍDnD‚Ì‚Í‚¸
			if( jtree_.getDnDStatus() == DnDStatus.Move){
				removeNode(fromInfo.getNode());
				passedMethod_ = PassedMethod.Paste_Another_Tree;
			}
		}else if(passedMethod_ == PassedMethod.ExpDone_X){	//ctrl+X -> ctrl+V
			removeNode(fromInfo.getNode());
			passedMethod_ = PassedMethod.Paste_Another_Tree;
		}
	}
}
