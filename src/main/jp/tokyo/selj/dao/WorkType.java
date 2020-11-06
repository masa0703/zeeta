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

import jp.tokyo.selj.common.AppException;

import org.seasar.dao.annotation.tiger.Bean;
import org.seasar.dao.annotation.tiger.Id;
import org.seasar.dao.annotation.tiger.IdType;

@Bean(table = "workType")
public class WorkType implements Serializable {

    private int workTypeId;
    private int seq=0;
    private String workTypeName;
    private String memo;
    private Timestamp newDate = new Timestamp(System.currentTimeMillis());
    private int versionNo=0;

    public WorkType() {
    }
    
    public void check(){
    	if(workTypeName == null || "".equals(workTypeName.trim())){
    		throw new AppException("workTypeNameÇÕè»ó™Ç≈Ç´Ç‹ÇπÇÒ");
    	}
    }

    public String toString() {
    	return workTypeName;
    }
//    public String toString() {
//        StringBuffer buf = new StringBuffer();
//        buf.append("workTypeId="+workTypeId).append(", ");
//        buf.append("seq="+seq).append(", ");
//        buf.append("workTypeName="+workTypeName).append(", ");
//        buf.append("memo="+memo).append(", ");
//        buf.append("newDate="+newDate).append(", ");
//        buf.append("versionNo="+versionNo);
//        return buf.toString();
//    }

    public int hashCode() {
        return (int) this.getWorkTypeId();
    }

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public Timestamp getNewDate() {
		return newDate;
	}

	public void setNewDate(Timestamp newDate) {
		this.newDate = newDate;
	}

	@Id(value = IdType.SEQUENCE, sequenceName = "WorkTypeId")
	public int getWorkTypeId() {
		return workTypeId;
	}

	public void setWorkTypeId(int workTypeId) {
		this.workTypeId = workTypeId;
	}

	public String getWorkTypeName() {
		return workTypeName;
	}

	public void setWorkTypeName(String workTypeName) {
		this.workTypeName = workTypeName;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public int getVersionNo() {
		return versionNo;
	}

	public void setVersionNo(int versionNo) {
		this.versionNo = versionNo;
	}


}