package jp.tokyo.selj.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.SystemColor;

import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import javax.swing.tree.TreeNode;

import jp.tokyo.selj.dao.Work;
import jp.tokyo.selj.model.DocModel;
import jp.tokyo.selj.model.DocNode;

public class DocTreeCellRenderer4ReverseTree extends DocTreeCellRenderer{
	TreeNode[] path_;
	public DocTreeCellRenderer4ReverseTree(DocModel docModel) {
		super(docModel);
	}
	public void setPath(TreeNode[] path){
		path_ = path;
	}
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		Component c = super.getTreeCellRendererComponent(
							tree, value,
							sel, expanded, leaf, row, hasFocus);
		DocNode docNode = null;
		if(value instanceof DocNode){	//JTree�Ƀ��f�����Z�b�g����O��String��`�悷��炵��
			docNode = (DocNode)value;
		}else{
			return c;
		}

		setOpaque(true); //��������ƑI����Ԃ̔w�i���`�悳��Ȃ�
		//�Ȃ̂Ŏ����ŕ`��
		if(sel){
			c = setSelectedNode(c, docNode);
		}else{
			c = setNoneSelectedNode(c, docNode);
		}
		return c;
	}
	private Component setSelectedNode(Component c, DocNode docNode){
		c.setBackground(SystemColor.LIGHT_GRAY);
		if(isParentNode(docNode)){	//���݂̐e�m�[�h�͐F��ς���
			c.setForeground(SystemColor.BLUE);
		}else{
			c.setForeground(SystemColor.BLACK);
		}
		return c;
	}
	private Component setNoneSelectedNode(Component c, DocNode docNode){
		if(isParentNode(docNode)){	//���݂̐e�m�[�h�͐F��ς���
			c.setForeground(SystemColor.ORANGE);
		}else{
			c.setForeground(SystemColor.white);
		}
		return c;
	}
	
	private boolean isParentNode(DocNode node){
		if(path_ == null || node.getLevel() < 1){	//node.getLevel()==0�́A���[�g�Ȃ̂ł��̂܂܂ɂ��Ă���
			return false;
		}
		int index = path_.length - node.getLevel() -1;
		return 
			index >= 0 
			&& path_.length > index 
			&& node.getUserObject().equals( ((DocNode)path_[index]).getUserObject() );
	}
}
