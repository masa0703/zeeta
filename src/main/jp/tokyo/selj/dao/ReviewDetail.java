/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package jp.tokyo.selj.dao;

import java.io.Serializable;
import java.sql.Timestamp;

import org.seasar.dao.annotation.tiger.Bean;
import org.seasar.dao.annotation.tiger.Relation;

@Bean(table = "reviewDetail")
public class ReviewDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private long	outputId;
    private Timestamp reviewDate;
    
    private int	seq;
    private String	memo;
    private Timestamp updDate=new Timestamp(System.currentTimeMillis());
    private String updUserName;
    private int reviewStateTypeId;
    private ReviewStateType reviewStateType;
    private int versionNo;
    
    public ReviewDetail(){
    }
    public ReviewDetail(Review review){
    	outputId = review.getOutputId();
    	reviewDate = review.getReviewDate();
    }
    public void check(){
		if(updUserName == null || "".equals(updUserName.trim()) ){
			throw new ModelCheckException("commentatorを入力してください");
		}
		if(memo == null || "".equals(memo.trim()) ){
			throw new ModelCheckException("commentを入力してください");
		}
    }
	public Timestamp getReviewDate() {
		return reviewDate;
	}
	public void setReviewDate(Timestamp date) {
		this.reviewDate = date;
	}
	public long getOutputId() {
		return outputId;
	}
	public void setOutputId(long outputId) {
		this.outputId = outputId;
	}
	public Timestamp getUpdDate() {
		return updDate;
	}
	public void setUpdDate(Timestamp updDate) {
		this.updDate = updDate;
	}
	public int getVersionNo() {
		return versionNo;
	}
	public void setVersionNo(int versionNo) {
		this.versionNo = versionNo;
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
	public int getReviewStateTypeId() {
		return reviewStateTypeId;
	}
	public void setReviewStateTypeId(int reviewStateTypeId) {
		this.reviewStateTypeId = reviewStateTypeId;
	}
	public int getSeq() {
		return seq;
	}
	public void setSeq(int seq) {
		this.seq = seq;
	}
	public String getUpdUserName() {
		return updUserName;
	}
	public void setUpdUserName(String updUserName) {
		this.updUserName = updUserName;
	}
	public ReviewStateType getReviewStateType() {
		return reviewStateType;
	}
    @Relation(relationNo = 0, relationKey="reviewStateTypeId:reviewStateTypeId")
	public void setReviewStateType(ReviewStateType reviewState) {
		this.reviewStateType = reviewState;
		this.reviewStateTypeId = reviewState.getReviewStateTypeId();
	}

   
}