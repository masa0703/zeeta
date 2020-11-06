package jp.tokyo.selj.model;

import javax.swing.tree.DefaultMutableTreeNode;

import jp.tokyo.selj.dao.Doc;
import jp.tokyo.selj.dao.DocStrDao;

public class DocNode extends DefaultMutableTreeNode {
    private int parentCount=0;

	public DocNode() {
	}

	public DocNode(Object userObject) {
		super(userObject);
	}

	public DocNode(Object userObject, boolean allowsChildren) {
		super(userObject, allowsChildren);
	}
	public Doc getDoc(){
		return (Doc)getUserObject();
	}
	public int getParentCount() {
		return parentCount;
	}

	public void setParentCount(int parentCount) {
		this.parentCount = parentCount;
	}
	public void resetParentCount(DocStrDao docStrDao) {
		parentCount = docStrDao.getParentCount(getDoc().getDocId());
	}

}
