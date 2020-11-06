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

@Bean(table = "review")
public class Review implements Serializable {
    private static final long serialVersionUID = 1L;

    private long	outputId;
    private Timestamp reviewDate=new Timestamp(System.currentTimeMillis());
    private String   reviewi;
    private String   reviewer1;
    private String   reviewer2;
    private String   reviewer3;
    private String remark;
    private int   versionNo;
    
    public void check(){
    	if(reviewi == null || "".equals(reviewi.trim()) ){
    		throw new ModelCheckException("authorを入力してください");
    	}
    	if(reviewer1 == null || "".equals(reviewer1.trim()) ){
    		throw new ModelCheckException("reviewer1を入力してください");
    	}
    }
    
	public Timestamp getReviewDate() {
		return reviewDate;
	}
	public void setReviewDate(Timestamp reviewDate) {
		this.reviewDate = reviewDate;
	}
	public long getOutputId() {
		return outputId;
	}
	public void setOutputId(long outputId) {
		this.outputId = outputId;
	}
	public int getVersionNo() {
		return versionNo;
	}
	public void setVersionNo(int versionNo) {
		this.versionNo = versionNo;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getReviewer1() {
		return reviewer1;
	}
	public void setReviewer1(String reviewer1) {
		this.reviewer1 = reviewer1;
	}
	public String getReviewer2() {
		return reviewer2;
	}
	public void setReviewer2(String reviewer2) {
		this.reviewer2 = reviewer2;
	}
	public String getReviewer3() {
		return reviewer3;
	}
	public void setReviewer3(String reviewer3) {
		this.reviewer3 = reviewer3;
	}
	public String getReviewi() {
		return reviewi;
	}
	public void setReviewi(String reviewi) {
		this.reviewi = reviewi;
	}

    
}