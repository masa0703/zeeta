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
package jp.tokyo.selj.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.TransactionManager;

import jp.tokyo.selj.dao.SortType;
import jp.tokyo.selj.dao.SortTypeDao;
import jp.tokyo.selj.dao.User;
import jp.tokyo.selj.dao.UserDao;
import jp.tokyo.selj.dao.Doc;
import jp.tokyo.selj.dao.DocDao;
import jp.tokyo.selj.dao.DocStr;
import jp.tokyo.selj.dao.DocStrDao;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.S2ContainerFactory;
import org.seasar.framework.log.Logger;

public class Migrate {

    private static Logger logger = Logger.getLogger(Migrate.class);
    static Map<Long, Long> idMap = new HashMap<Long, Long>();

    public static void main(String[] args) throws Exception{
        S2Container container = S2ContainerFactory.create("jp/tokyo/selj/dao/selDao.dicon");
        container.init();
    	TransactionManager trn = (TransactionManager)container.getComponent(TransactionManager.class);
        try {
        	trn.begin();
        	//要件
        	migrateYouken(container);
        	
        	//要件構造
        	migrateYoukenKouzou(container);
        	
        	//ソート種別
//        	migrateSortType(container);

        	//作業者
        	migrateUser(container);

        	
            trn.commit();
            System.out.println("@@@@ finish @@@@");
        }catch(Exception e){
        	trn.rollback();
        	e.printStackTrace();
        } finally {
            container.destroy();
        }

    }
    
	private static Connection getConnection() throws ClassNotFoundException, SQLException {
		Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
        Connection con = DriverManager.getConnection("jdbc:odbc:selmig");
		return con;
	}

	static void migrateYouken(S2Container container) throws Exception{
    	DocDao dao = (DocDao) container
        .getComponent(DocDao.class);
        Connection con = getConnection();
		try{
			Statement st = con.createStatement();
			dao.deleteAll();
			ResultSet rs = st.executeQuery("select * from 要件");
			while(rs.next()){
				Doc out = new Doc();
				Number n = (Number)rs.getObject("要件ID");
				long oldId = n.longValue();
				out.setDocId( oldId ); 
				n = (Number)rs.getObject("要件種類ID");
				out.setDocTypeId( (n==null)? 0:n.intValue() );
				out.setDocTitle(rs.getString("要件タイトル"));
				out.setDocCont(rs.getString("要件内容"));
				out.setNewDate(rs.getTimestamp("作成日"));
				out.setUserName(rs.getString("作成者名"));
				n = (Number)rs.getObject("子ノード順序");
				out.setSortTypeId( (n==null)? 0:n.intValue() );
				dao.insert(out);
				logger.info(out);
				idMap.put(oldId, out.getDocId());
			}
		}finally{
			con.close();
		}
    }
    
	
    static void migrateYoukenKouzou(S2Container container) throws Exception{
    	DocStrDao dao = (DocStrDao) container
    		.getComponent(DocStrDao.class);
        Connection con = getConnection();
		try{
			Statement st = con.createStatement();
			dao.deleteAll();
			ResultSet rs = st.executeQuery("select * from 要件構造");
			while(rs.next()){
				DocStr out = new DocStr();
				Number n = (Number)rs.getObject("親要件ID");
				logger.debug("oldId="+n.longValue());
				out.setOyaDocId( idMap.get(n.longValue()) ); 

				n = (Number)rs.getObject("子要件ID");
				out.setKoDocId( idMap.get(n.longValue()) ); 
				
				n = (Number)rs.getObject("SEQ");
				out.setSEQ( n.intValue() ); 

				out.setNewDate(rs.getTimestamp("作成日"));
				out.setUserName(rs.getString("作成者"));

				logger.info(out);
				dao.insert(out);
			}
		}finally{
			con.close();
		}
    }

    static void migrateSortType(S2Container container) throws Exception{
    	SortTypeDao dao = (SortTypeDao) container
    		.getComponent(SortTypeDao.class);
        Connection con = getConnection();
		try{
			Statement st = con.createStatement();
			dao.deleteAll();
			ResultSet rs = st.executeQuery("select * from ソート種別");
			while(rs.next()){
				SortType out = new SortType();
				Number n = (Number)rs.getObject("ソート種別ID");
				out.setSortTypeID( n.intValue() ); 
				out.setSortTypeName(rs.getString("ソート種別名称"));
				out.setOrderSent(rs.getString("Order句"));
				logger.info(out);
				dao.insert(out);
			}
		}finally{
			con.close();
		}
    }

    static void migrateUser(S2Container container) throws Exception{
    	UserDao dao = (UserDao) container
    		.getComponent(UserDao.class);
        Connection con = getConnection();
		try{
			Statement st = con.createStatement();
			dao.deleteAll();
			ResultSet rs = st.executeQuery("select * from 作業者");
			while(rs.next()){
				User out = new User();
				out.setUserName(rs.getString("作業者名"));
				out.setOrg(rs.getString("所属"));
				logger.info(out);
				dao.insert(out);
			}
		}finally{
			con.close();
		}
    }
    
}