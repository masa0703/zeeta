 package jp.tokyo.selj.dao;

import java.io.Serializable;

import org.seasar.dao.annotation.tiger.Bean;

@Bean(table = "reviewStateType")
public class ReviewStateType implements Serializable {

    private int reviewStateTypeId;
    private String reviewStateTypeName;

    public ReviewStateType() {
    }
	public void check(){
    	if(reviewStateTypeName == null || "".equals(reviewStateTypeName.trim()) ){
    		throw new ModelCheckException("レビュー状態名称を入力してください");
    	}
	}

    public String toString() {
    	return reviewStateTypeName;
    }
//    public String toString() {
//        StringBuffer buf = new StringBuffer();
//        buf.append(作業者名).append(", ");
//        buf.append(所属);
//        return buf.toString();
//    }

    public int hashCode() {
        return (int) this.reviewStateTypeName.hashCode();
    }
	public int getReviewStateTypeId() {
		return reviewStateTypeId;
	}
	public void setReviewStateTypeId(int reviewStateTypeId) {
		this.reviewStateTypeId = reviewStateTypeId;
	}
	public String getReviewStateTypeName() {
		return reviewStateTypeName;
	}
	public void setReviewStateTypeName(String reviewStateTypeName) {
		this.reviewStateTypeName = reviewStateTypeName;
	}



}