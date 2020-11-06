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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.XADataSource;

import jp.tokyo.selj.ZeetaDBManager;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.S2ContainerFactory;
import org.seasar.framework.log.Logger;

public class CopyData {

    private static Logger logger = Logger.getLogger(CopyData.class);

    public static void main(String[] args) throws Exception{
		//version check
    	//万が一、dbSetup.diconを更新し忘れても、これでスキーマが更新される
		ZeetaDBManager.check();

		Connection dstCon = getDstConnection();
        Connection srcCon = getSrcConnection();
        try {
        	//doc
        	copyTable(dstCon, srcCon, "doc");
        	
        	//docStr
        	copyTable(dstCon, srcCon, "docStr");
        	
        	//SortType
//        	migrateSortType(container);

        	//user
        	copyTable(dstCon, srcCon, "usertbl");

        	//workType
        	copyTable(dstCon, srcCon, "workType");

        	//outputType
        	copyTable(dstCon, srcCon, "outputType");

        	//output
        	copyTable(dstCon, srcCon, "output");
        	
        	//work
        	copyTable(dstCon, srcCon, "work");
        	
        	//シーケンスの初期化
        	initSequence(dstCon, srcCon, "DocId");
        	initSequence(dstCon, srcCon, "OutputId");
        	initSequence(dstCon, srcCon, "OutputTypeId");
        	initSequence(dstCon, srcCon, "WorkTypeId");
 
        	//------ ここからver0.6
        	copyTable(dstCon, srcCon, "checkState");
        	copyTable(dstCon, srcCon, "checkPoint");
        	copyTable(dstCon, srcCon, "checktbl");
        	copyTable(dstCon, srcCon, "checkDetail");
//        	copyTable(dstCon, srcCon, "workPropType");
//        	copyTable(dstCon, srcCon, "workProp");
        	
//        	initSequence(dstCon, srcCon, "WorkPropTypeId");
        	initSequence(dstCon, srcCon, "CheckPointId");
        	
        	//------ ここからver0.6.10
        	copyTable(dstCon, srcCon, "reviewStateType");
        	copyTable(dstCon, srcCon, "review");
        	copyTable(dstCon, srcCon, "reviewDetail");

        	//------ ここからver0.7.00
        	copyTable(dstCon, srcCon, "outputPropType");
        	copyTable(dstCon, srcCon, "outputTypePropType");
        	copyTable(dstCon, srcCon, "workProp");

        	initSequence(dstCon, srcCon, "OutputPropTypeId");

            dstCon.commit();
            logger.info("@@@@ finish @@@@");
            
        }catch(Exception e){
        	dstCon.rollback();
        	e.printStackTrace();
        } finally {
            srcCon.close();
            dstCon.close();
        }

    }
    
	static void initSequence(Connection dstCon, Connection srcCon
			,String seqName) throws Exception{

		Statement srcSt = srcCon.createStatement();
		//postgreSqlでは、nextvalが使用されないとcurrvalが使用できない
		ResultSet rs = srcSt.executeQuery("select nextval('" + seqName + "') as nextval");
		rs.next();
		long curVal = ((Number)rs.getObject("nextval")).longValue();
//		curVal--;	これをやると、postgressでは、最初のinsertで一意制約違反になるのでやめた
		
		Statement dstSt = dstCon.createStatement();
		dstSt.executeUpdate("ALTER SEQUENCE " + seqName
				+ " RESTART WITH " + curVal);
		
        logger.info(seqName + " set to " + curVal);
    }
	static void copyTable(Connection dstCon, Connection srcCon
			,String tableName) throws Exception{

		//コピー元データのロード
		Statement srcSt = srcCon.createStatement();
		ResultSet rs = srcSt.executeQuery("select * from " + tableName);

		//カラム一覧作成
		ResultSetMetaData rsm = rs.getMetaData();
		String[] columns = new String[rsm.getColumnCount()];
		for(int i=0; i<columns.length; i++){
			columns[i] = rsm.getColumnName(i+1);
		}
		
		//insert文作成
		String sql = makeInsertStatement(tableName, columns);

		//コピー先データの削除
		Statement dstSt = dstCon.createStatement();
		dstSt.executeUpdate("delete from " + tableName);	//rollbackしたいのでtruncateは使わない
		
		//コピー先のinsert準備
		PreparedStatement dstPrep = dstCon.prepareStatement(sql);
		
		try{
			int recCount = 0;
			while(rs.next()){
				for(int i=0; i<columns.length; i++){
					dstPrep.setObject(i+1, rs.getObject(columns[i]));
				}
				dstPrep.execute();
				recCount++;
			}
	        logger.info(tableName + " (" + recCount + " rec) copy complete!");
		}catch(Exception e){
			logger.error("error ! table="+tableName);
			for(int i=0; i<columns.length; i++){
				logger.error(rs.getObject(columns[i]));
			}
			throw e;
		}
    }

	private static String makeInsertStatement(String tableName, String[] columns) {
		String sql ="insert into " + tableName + " (";
		String sep = "";
		for(int i=0; i<columns.length; i++){
			sql += (sep + columns[i]);
			sep = ",";
		}
		sql += ") values(";
		sep = "";
		for(int i=0; i<columns.length; i++){
			sql += (sep + "?");
			sep = ",";
		}
		sql += ")";
		return sql;
	}
    private static Connection getDstConnection() throws SQLException{
        S2Container container = S2ContainerFactory.create("jp/tokyo/selj/dao/selDao.dicon");
        container.init();
        XADataSource ds = (XADataSource)container.getComponent("xaDataSource");
		return ds.getXAConnection().getConnection();
	}
	private static Connection getSrcConnection() throws ClassNotFoundException, SQLException {
		Class.forName(System.getProperty("srcDb.driver"));
        Connection con = DriverManager.getConnection(
        		System.getProperty("srcDb.url"),
        		System.getProperty("srcDb.user"),
        		System.getProperty("srcDb.password")
        		);
		return con;
	}
    
}