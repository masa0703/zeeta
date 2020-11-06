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
import java.math.BigDecimal;

import jp.tokyo.selj.common.AppException;

import org.seasar.dao.annotation.tiger.Bean;
import org.seasar.dao.annotation.tiger.Relation;

@Bean(table = "workProp")
public class WorkProp implements Serializable {
    private static final long serialVersionUID = 1L;

    private long docId = -1;
    private int workTypeId = -1;
    private int outputPropTypeId = -1;
    private boolean jissekiFlg = true;
    private BigDecimal value;
    private int versionNo;
    private OutputPropType outputPropType = null;
    
    public void check() throws AppException{
    	if(docId < 0){
    		throw new ModelCheckException("docId‚ª•s³‚Å‚· docId="+docId);
    	}
    	if(workTypeId < 0){
    		throw new ModelCheckException("workTypeId‚ª•s³‚Å‚· workTypeId="+workTypeId);
    	}
    	if(outputPropTypeId < 0){
    		throw new ModelCheckException("outputPropTypeId‚ª•s³‚Å‚· outputPropTypeId="+outputPropTypeId);
    	}
    }
    
	public WorkProp() {
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("docId="+docId).append(", ");
        buf.append("workTypeId="+workTypeId).append(", ");
        buf.append("outputPropTypeId="+outputPropTypeId).append(", ");
        buf.append("jissekiFlg="+jissekiFlg).append(", ");
        buf.append("value="+value).append(", ");
        buf.append("versionNo="+versionNo);
        return buf.toString();
    }

    public int hashCode() {
        return (int) ((docId*10000)+(workTypeId*100)+outputPropTypeId);
    }

	public long getDocId() {
		return docId;
	}

	public void setDocId(long docId) {
		this.docId = docId;
	}

	public boolean isJissekiFlg() {
		return jissekiFlg;
	}

	public void setJissekiFlg(boolean jissekiFlg) {
		this.jissekiFlg = jissekiFlg;
	}

	public int getOutputPropTypeId() {
		return outputPropTypeId;
	}

	public void setOutputPropTypeId(int outputPropTypeId) {
		this.outputPropTypeId = outputPropTypeId;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public int getVersionNo() {
		return versionNo;
	}

	public void setVersionNo(int versionNo) {
		this.versionNo = versionNo;
	}

	public int getWorkTypeId() {
		return workTypeId;
	}

	public void setWorkTypeId(int workTypeId) {
		this.workTypeId = workTypeId;
	}

	public OutputPropType getOutputPropType() {
		return outputPropType;
	}

    @Relation(relationNo = 0, relationKey="outputPropTypeId:outputPropTypeId")
	public void setOutputPropType(OutputPropType outputPropType) {
		this.outputPropType = outputPropType;
		if( outputPropType != null){
			this.outputPropTypeId = (int)outputPropType.getOutputPropTypeId();
		}else{
			this.outputPropTypeId = -1;
		}
	}


}