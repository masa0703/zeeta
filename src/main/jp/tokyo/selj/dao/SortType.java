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

@Bean(table = "sortType")
public class SortType implements Serializable {

    private int sortTypeId;
    private String sortTypeName;
    private String orderSent;
    
    public static int SEQ=0;

    public SortType() {
    }

    public String toString() {
    	return sortTypeName;
    }
//    public String toString() {
//        StringBuffer buf = new StringBuffer();
//        buf.append(ソート種別ID).append(", ");
//        buf.append(ソート種別名称).append(", ");
//        buf.append(Order句);
//        return buf.toString();
//    }

    public int hashCode() {
        return (int) this.getSortTypeID();
    }

	public String getOrderSent() {
		return orderSent;
	}

	public void setOrderSent(String orderSent) {
		this.orderSent = orderSent;
	}

	public int getSortTypeID() {
		return sortTypeId;
	}

	public void setSortTypeID(int sortTypeId) {
		this.sortTypeId = sortTypeId;
	}

	public String getSortTypeName() {
		return sortTypeName;
	}

	public void setSortTypeName(String sortTypeName) {
		this.sortTypeName = sortTypeName;
	}


}