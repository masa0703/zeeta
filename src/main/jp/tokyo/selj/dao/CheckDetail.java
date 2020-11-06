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

@Bean(table = "checkDetail")
public class CheckDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private long	outputId;
    private Timestamp checkDate;
    private long	checkPointId;
    private CheckPoint	checkPoint;
    private Timestamp updDate=new Timestamp(System.currentTimeMillis());
    private int checkStateId = 0;
    private CheckState checkState;
    private int versionNo;
    
    public CheckDetail(){
    }
    public CheckDetail(Check check){
    	outputId = check.getOutputId();
    	checkDate = check.getCheckDate();
    }
    
	public Timestamp getCheckDate() {
		return checkDate;
	}
	public void setCheckDate(Timestamp checkDate) {
		this.checkDate = checkDate;
	}
	public long getCheckPointId() {
		return checkPointId;
	}
	public void setCheckPointId(long checkPointId) {
		this.checkPointId = checkPointId;
	}
	public int getCheckStateId() {
		return checkStateId;
	}
	public void setCheckStateId(int checkStateId) {
		this.checkStateId = checkStateId;
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
	public CheckPoint getCheckPoint() {
		return checkPoint;
	}
    @Relation(relationNo = 0, relationKey="checkPointId:checkPointId")
	public void setCheckPoint(CheckPoint checkPoint) {
		this.checkPoint = checkPoint;
		this.checkPointId = checkPoint.getCheckPointId();
	}
	public CheckState getCheckState() {
		return checkState;
	}
    @Relation(relationNo = 1, relationKey="checkStateId:checkStateId")
	public void setCheckState(CheckState checkState) {
		this.checkState = checkState;
		this.checkStateId = checkState.getCheckStateId();
	}

   
}