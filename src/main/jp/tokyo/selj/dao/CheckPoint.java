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

import org.seasar.dao.annotation.tiger.Bean;
import org.seasar.dao.annotation.tiger.Id;
import org.seasar.dao.annotation.tiger.IdType;
import org.seasar.dao.annotation.tiger.Relation;

@Bean(table = "checkPoint")
public class CheckPoint implements Serializable {
    private static final long serialVersionUID = 1L;

    private long checkPointId;
    private long   outputTypeId;
    private OutputType   outputType;
    private String checkCont;
    private int SEQ;
    private boolean deleteFlg=false;
    private int versionNo;
    
    public void check(){
    	
    }
    
	public String getCheckCont() {
		return checkCont;
	}
	public void setCheckCont(String checkCont) {
		this.checkCont = checkCont;
	}
	@Id(value = IdType.SEQUENCE, sequenceName = "checkPointId")
	public long getCheckPointId() {
		return checkPointId;
	}
	public void setCheckPointId(long checkPointId) {
		this.checkPointId = checkPointId;
	}
	public boolean isDeleteFlg() {
		return deleteFlg;
	}
	public void setDeleteFlg(boolean deleteFlg) {
		this.deleteFlg = deleteFlg;
	}
	public long getOutputTypeId() {
		return outputTypeId;
	}
	public void setOutputTypeId(long outputTypeId) {
		this.outputTypeId = outputTypeId;
	}
	public int getSEQ() {
		return SEQ;
	}
	public void setSEQ(int seq) {
		SEQ = seq;
	}
	public int getVersionNo() {
		return versionNo;
	}
	public void setVersionNo(int versionNo) {
		this.versionNo = versionNo;
	}

	public OutputType getOutputType() {
		return outputType;
	}

    @Relation(relationNo = 0, relationKey="outputTypeId:outputTypeId")
	public void setOutputType(OutputType outputType) {
		this.outputType = outputType;
		if(outputType != null){
			this.outputTypeId = outputType.getOutputTypeId();
		}else{
			this.outputTypeId = -1;
		}
	}
    
   
}