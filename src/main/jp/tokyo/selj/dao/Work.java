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

import jp.tokyo.selj.common.SysPreference;

import org.seasar.dao.annotation.tiger.Bean;
import org.seasar.dao.annotation.tiger.Relation;

@Bean(table = "work", noPersistentProperty = "output")
public class Work implements Serializable {
    private static final long serialVersionUID = 1L;

    private long docId;

    private long outputId;

    private int workTypeId;
    
    private WorkType workType;

    private Timestamp newDate = new Timestamp(System.currentTimeMillis());

    private Timestamp compDate = new Timestamp(System.currentTimeMillis());

    private boolean compFlg = true;

    private String pointer;

    private String userName;

    private int versionNo=0;

    private Output output;		//これはs2Daoではセットしない

	public Work() {
		userName = SysPreference.getDefaultUserName();
    }
	public void check(){
		//特になし
	}

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("docId="+docId).append(", ");
        buf.append("outputId="+outputId).append(", ");
        buf.append("workType="+workType).append(", ");
        buf.append("pointer="+pointer).append(", ");
        buf.append(newDate).append(", ");
        buf.append(userName).append(", ");
        buf.append("versionNo="+versionNo);
        return buf.toString();
    }

    public int hashCode() {
        return (int) this.getWorkTypeId();
    }

	public long getDocId() {
		return docId;
	}

	public void setDocId(long docId) {
		this.docId = docId;
	}

	public Timestamp getNewDate() {
		return newDate;
	}

	public void setNewDate(Timestamp newDate) {
		this.newDate = newDate;
	}

	public long getOutputId() {
		return outputId;
	}

	public void setOutputId(long outputId) {
		this.outputId = outputId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
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

	public WorkType getWorkType() {
		return workType;
	}

    @Relation(relationNo = 0, relationKey="workTypeId:workTypeId")
	public void setWorkType(WorkType workType) {
		this.workType = workType;
		this.workTypeId = workType.getWorkTypeId();
	}
	public String getPointer() {
		return pointer;
	}

	public void setPointer(String pointer) {
		this.pointer = pointer;
	}

    //これをやっても、output.outputTypeが読み込まれないので自前でセットする
	public Output getOutput() {
		return output;
	}

	public void setOutput(Output output) {
		this.output = output;
		this.outputId = output.getOutputId();
	}

	public Timestamp getCompDate() {
		return compDate;
	}

	public void setCompDate(Timestamp compDate) {
		this.compDate = compDate;
	}

	public boolean getCompFlg() {
		return compFlg;
	}

	public void setCompFlg(boolean compFlg) {
		this.compFlg = compFlg;
	}

}