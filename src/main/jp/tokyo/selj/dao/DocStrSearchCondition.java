package jp.tokyo.selj.dao;

public class DocStrSearchCondition {
	private String orderByString;
	private long docId;
	
	public DocStrSearchCondition(long parentId, String orderByString){
		this.docId = parentId;
		this.orderByString = orderByString;
	}
	public String getOrderByString() {
		return orderByString;
	}

	public void setOrderByString(String orderByString) {
		this.orderByString = orderByString;
	}
	public long getDocId() {
		return docId;
	}
	public void setDocId(long parentId) {
		docId = parentId;
	}
}
