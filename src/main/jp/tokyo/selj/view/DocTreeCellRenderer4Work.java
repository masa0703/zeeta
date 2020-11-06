package jp.tokyo.selj.view;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;

import jp.tokyo.selj.dao.Work;
import jp.tokyo.selj.model.DocModel;
import jp.tokyo.selj.model.DocNode;

public class DocTreeCellRenderer4Work extends DocTreeCellRenderer {
	Work work_=null;
	public void setWork(Work work){
		work_ = work;
	}
	
	public DocTreeCellRenderer4Work(DocModel docModel) {
		super(docModel);
	}
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		Component ret = super.getTreeCellRendererComponent(
				tree, value,sel, expanded, leaf, row, hasFocus);
		
		DocNode docNode = null;
		if(value instanceof DocNode){	//JTreeにモデルをセットする前はStringを描画するらしい
			docNode = (DocNode)value;
		}
		if(docNode != null && work_ != null){
			if(docNode.getDoc().getDocId() == work_.getDocId()){
				setBorder(getMarkBorder());
			}
		}		
		return ret;
	}
	Border markBorder_=null;
	protected Border getMarkBorder() {
		if( markBorder_ == null){
			markBorder_ = new MatteBorder(0,0,2,0,Color.GREEN);
		}
		return markBorder_;
	}
}
