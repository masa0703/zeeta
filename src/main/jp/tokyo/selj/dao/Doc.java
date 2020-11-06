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
import jp.tokyo.selj.common.SysPreference;

import org.seasar.dao.annotation.tiger.Bean;
import org.seasar.dao.annotation.tiger.Id;
import org.seasar.dao.annotation.tiger.IdType;
import org.seasar.dao.annotation.tiger.Relation;

@Bean(table = "doc")
public class Doc implements Serializable , Cloneable{
    private static final long serialVersionUID = 1L;
    public static final int NORMAl_TYPE=0;
    public static final int ROOT_TYPE=1;
    public static final int LINK_TYPE=2;

    private long docId;

    private int docTypeId=NORMAl_TYPE;

    private String docTitle;

    private String docCont;

    private Timestamp newDate = new Timestamp(System.currentTimeMillis());

    private String userName;
    
    private int sortTypeId;
    
    private int workCount;

    private SortType sortType;
    
    private int versionNo=0;
    

    public void check() throws AppException{
    	if(docTitle == null || "".equals(docTitle.trim()) ){
    		throw new ModelCheckException("タイトルを入力してください");
    	}
    	if(docTitle.length() > 100 ){
    		throw new ModelCheckException("タイトルは100文字以内で指定してください");
    	}
    }
    
    public void markAsRoot(){
    	docTypeId = ROOT_TYPE;
    }
    public boolean isRoot(){
    	return docTypeId == ROOT_TYPE;
    }
    public SortType getSortType() {
		return sortType;
	}
    public void clearAll(){
    	docId = -1;
    	docTitle = null;
    	docCont = null;
    	newDate = null;
    	userName = null;
    }

    @Relation(relationNo = 0, relationKey="sortTypeId:sortTypeId")
	public void setSortType(SortType sortType) {
		this.sortType = sortType;
	}

	public Doc() {
		userName = SysPreference.getDefaultUserName();
    }
	public Doc(String title){
		this();
		setDocTitle(title);
	}
	public Doc(long id, String title){
		this();
		setDocId(id);
		setDocTitle(title);
	}
	public Object clone() throws CloneNotSupportedException{
		return super.clone();
	}
	
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("docId="+docId).append(", ");
        buf.append("docTypeId="+docTypeId).append(", ");
        buf.append("docTitle="+docTitle).append(", ");
        buf.append("docCont="+docCont).append(", ");
        buf.append(newDate).append(", ");
        buf.append(userName).append(", ");
        buf.append("workCount="+workCount).append(", ");
        buf.append("sortTypeId="+sortTypeId).append(", ");
        buf.append("versionNo="+versionNo);
        return buf.toString();
    }

    public int hashCode() {
        return (int) this.getDocId();
    }

	@Override
	public boolean equals(Object arg0) {
		if(arg0 instanceof Doc){
			return ((Doc)arg0).getDocId() == getDocId();
		}
		return false;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Timestamp getNewDate() {
		return newDate;
	}

	public void setNewDate(Timestamp newDate) {
		this.newDate = newDate;
	}

	public int getSortTypeId() {
		return sortTypeId;
	}

	public void setSortTypeId(int sortTypeId) {
		this.sortTypeId = sortTypeId;
	}

//	@Id(value = IdType.IDENTITY)
	@Id(value = IdType.SEQUENCE, sequenceName = "DocId")
	public long getDocId() {
		return docId;
	}

	public void setDocId(long docId) {
		this.docId = docId;
	}

	public String getDocTitle() {
		return docTitle;
	}

	public void setDocTitle(String docTitle) {
		this.docTitle = docTitle;
	}

	public int getDocTypeId() {
		return docTypeId;
	}

	public void setDocTypeId(int docTypeId) {
		this.docTypeId = docTypeId;
	}

	public String getDocCont() {
		return docCont;
	}

	public void setDocCont(String docCont) {
		this.docCont = docCont;
	}

	public int getVersionNo() {
		return versionNo;
	}

	public void setVersionNo(int versionNo) {
		this.versionNo = versionNo;
	}
	public void copyDoc(Doc src) {
		setDocId(src.getDocId());
		setDocCont(src.getDocCont());
		setDocTitle(src.getDocTitle());
		setDocTypeId(src.getDocTypeId());
		setNewDate(src.getNewDate());
		setSortType(src.getSortType());
		setUserName(src.getUserName());
		setVersionNo(src.getVersionNo());
	}

	public int getWorkCount() {
		return workCount;
	}

	public void setWorkCount(int workCount) {
		this.workCount = workCount;
	}


}