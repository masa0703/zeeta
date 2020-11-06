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
import java.util.List;

import jp.tokyo.selj.common.AppException;
import jp.tokyo.selj.common.SysPreference;

import org.seasar.dao.annotation.tiger.Bean;
import org.seasar.dao.annotation.tiger.Id;
import org.seasar.dao.annotation.tiger.IdType;
import org.seasar.dao.annotation.tiger.Relation;

@Bean(table = "output")
public class Output implements Serializable {
    private static final long serialVersionUID = 1L;

    private long outputId=-1;

    private int outputTypeId=0;


    private String name;

    private String path;


    private String memo;

    private Timestamp newDate = new Timestamp(System.currentTimeMillis());

    private String userName;
    
    private int workCount;

    private OutputType outputType;

    private int versionNo=0;
    

    public void check() throws AppException{
    	if(name == null || "".equals(name.trim()) ){
    		throw new ModelCheckException("名称(name)を入力してください");
    	}
    	if(outputType == null ){
    		throw new ModelCheckException("成果物種類(outputType)を入力してください");
    	}
//    	if(getOutputId() < 0){	//更新時もチェックせにゃぁ
	    	//nameがユニークになっていること
	    	OutputDao outputDao = (OutputDao) SelJDaoContainer.SEL_DAO_CONT.getComponent(OutputDao.class);
	    	List<Output> result = outputDao.findByName(getName());
	    	if(result.size() > 0){
	    		if(result.size() == 1 && result.get(0).outputId == getOutputId()){
	    			//自分自身だったら名称を変更しない更新だっちゃ
	    		}else{
	    			throw new ModelCheckException("同じ名称(name)が既に存在します");
	    		}
	    	}
//    	}    	
    }
    
	public Output() {
		userName = SysPreference.getDefaultUserName();
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(outputId).append(", ");
        buf.append("docTypeId="+outputTypeId).append(", ");
        buf.append("name="+name).append(", ");
        buf.append("path="+path).append(", ");
        buf.append("memo="+memo).append(", ");
        buf.append(newDate).append(", ");
        buf.append(userName).append(", ");
        buf.append("versionNo="+versionNo);
        return buf.toString();
    }

    public int hashCode() {
        return (int) this.getOutputId();
    }

	@Id(value = IdType.SEQUENCE, sequenceName = "OutputId")
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

	public String getName() {
		return name;
	}

	public void setName(String outputName) {
		this.name = outputName;
	}

	public int getOutputTypeId() {
		return outputTypeId;
	}

	public void setOutputTypeId(int outputTypeId) {
		this.outputTypeId = outputTypeId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public OutputType getOutputType() {
		return outputType;
	}

    @Relation(relationNo = 0, relationKey="outputTypeId:outputTypeId")
	public void setOutputType(OutputType outputType) {
		this.outputType = outputType;
		if(outputType != null){
			this.outputTypeId = outputType.getOutputTypeId();
		}
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getWorkCount() {
		return workCount;
	}

	public void setWorkCount(int workCount) {
		this.workCount = workCount;
	}


}