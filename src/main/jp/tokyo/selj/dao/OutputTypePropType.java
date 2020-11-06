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
import org.seasar.dao.annotation.tiger.Relation;

@Bean(table = "outputTypePropType")
public class OutputTypePropType implements Serializable {
    private static final long serialVersionUID = 1L;

    private long outputTypeId;
    private long outputPropTypeId;
    private OutputPropType outputPropType;

    public void check() throws AppException{
    }
    
	public OutputTypePropType() {
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("outputTypeId"+outputTypeId).append(", ");
        buf.append("outputPropTypeId="+outputPropTypeId);
        return buf.toString();
    }

    public int hashCode() {
        return (int) ((outputTypeId*1000)+outputPropTypeId);
    }

	public long getOutputPropTypeId() {
		return outputPropTypeId;
	}

	public void setOutputPropTypeId(long outputPropTypeId) {
		this.outputPropTypeId = outputPropTypeId;
	}

	public long getOutputTypeId() {
		return outputTypeId;
	}

	public void setOutputTypeId(long outputTypeId) {
		this.outputTypeId = outputTypeId;
	}

	public OutputPropType getOutputPropType() {
		return outputPropType;
	}

    @Relation(relationNo = 0, relationKey="outputPropTypeId")
	public void setOutputPropType(OutputPropType outputPropType) {
		this.outputPropType = outputPropType;
		this.outputPropTypeId = outputPropType.getOutputPropTypeId();
	}


}