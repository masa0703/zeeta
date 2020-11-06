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

import jp.tokyo.selj.common.AppException;

import org.seasar.dao.annotation.tiger.Bean;
import org.seasar.dao.annotation.tiger.Id;
import org.seasar.dao.annotation.tiger.IdType;

@Bean(table = "outputPropType")
public class OutputPropType implements Serializable {
    private static final long serialVersionUID = 1L;

    private long outputPropTypeId=-1;

    private String outputPropTypeName;

    private int seq;

    private String unitName;

    private String descr;

    private int versionNo=0;
    

    public void check() throws AppException{
    	if(outputPropTypeName == null || "".equals(outputPropTypeName.trim()) ){
    		throw new ModelCheckException("ñºèÃ(outputPropTypeName)Çì¸óÕÇµÇƒÇ≠ÇæÇ≥Ç¢");
    	}
    	if(unitName == null || "".equals(unitName.trim())){
    		throw new ModelCheckException("íPà ñº(unitName)Çì¸óÕÇµÇƒÇ≠ÇæÇ≥Ç¢");
    	}
    }
    
	public OutputPropType() {
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(outputPropTypeId).append(", ");
        buf.append("outputPropTypeName="+outputPropTypeName).append(", ");
        buf.append("SEQ="+seq).append(", ");
        buf.append("unitName="+unitName).append(", ");
        buf.append("descr="+descr).append(", ");
        buf.append("versionNo="+versionNo);
        return buf.toString();
    }

    public int hashCode() {
        return this.outputPropTypeName.hashCode();
    }

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	public int getVersionNo() {
		return versionNo;
	}

	public void setVersionNo(int versionNo) {
		this.versionNo = versionNo;
	}

	@Id(value = IdType.SEQUENCE, sequenceName = "OutputPropTypeId")
	public long getOutputPropTypeId() {
		return outputPropTypeId;
	}

	public void setOutputPropTypeId(long outputPropTypeId) {
		this.outputPropTypeId = outputPropTypeId;
	}

	public String getOutputPropTypeName() {
		return outputPropTypeName;
	}

	public void setOutputPropTypeName(String outputPropTypeName) {
		this.outputPropTypeName = outputPropTypeName;
	}

	public String getDescr() {
		return descr;
	}

	public void setDescr(String desc) {
		this.descr = desc;
	}

}