package jp.tokyo.selj;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.XADataSource;

import jp.tokyo.selj.dao.SelJDaoContainer;

import org.apache.log4j.Logger;
import org.seasar.framework.container.S2Container;

public class ZeetaDBManager {
	static Logger log_ = Logger.getLogger(ZeetaDBManager.class);
	
	//ルール：ここを変更する際は、dbSetup.diconも同じように変更する
	//　dbSetup_v05.diconは、一時的なものなので更新しない。
	public static final String DB_VERSION = "616";
	
	static String dbVersion;
	
	static String INSERT_DEFAULT_SYS_RECORD = 
		"insert into sysZeeta(id, version, needUserName) values (" +
		"1," + q(DB_VERSION)+ ", false" + 
		")";
	
	static String sqlList_[][] = new String[][]{
			{"500",
				"ALTER TABLE work ADD compFlg boolean"
			},
			{"501",
				"drop table if exists sysZeeta"
			},
			{"501",
				"create table sysZeeta(" +
				" id INT NOT NULL" +
				" ,version varchar(10) NOT NULL" +
				" ,needUserName boolean" +
				")"
			},
			{"501",
				"insert into sysZeeta(id, version, needUserName) values (" +
					"1,'501', false)"
			},
			{"502",
				"ALTER TABLE work ADD newFlg boolean"
			},
			{"502",
				"update sysZeeta set version='502' where id=1"
			},
			//---------------------------------------
			{"600",
				"CREATE TABLE workPropType ("
				+"   workPropTypeId	INT NOT NULL,"
				+"   workPropTypeName VARCHAR(128),"
				+"   unitName		  VARCHAR(20),"
				+"   memo             VARCHAR(65535),"
				+"   SEQ			  INT,"
				+"   versionNo     	  INT,"
				+"   PRIMARY KEY (workPropTypeId)"
				+")", 
			},
			{"600",
				"CREATE TABLE workProp ("
				+"   docId         INT NOT NULL,"
				+"   workTypeId    INT NOT NULL,"
				+"   workPropTypeId	INT NOT NULL,"
				+"   jissekiFlg		boolean,"
				+"   valuse	      	NUMERIC(20,2),"
				+"   versionNo    	INT,"
				+"   PRIMARY KEY (docId, workTypeId, workPropTypeId, jissekiFlg)"
				+")",
			},
			{"600",
				"ALTER TABLE workProp ADD CONSTRAINT workProp_fk1"
				+" FOREIGN KEY(docId,workTypeId) REFERENCES work(docId,workTypeId) ON DELETE CASCADE",
			},
			{"600",
				"ALTER TABLE workProp ADD CONSTRAINT workProp_fk2"
				+" FOREIGN KEY(workPropTypeId) REFERENCES workPropType(workPropTypeId)",
			},
			{"600",
				"CREATE TABLE checkPoint ("
				+"   checkPointId     INT NOT NULL,"
				+"   outputTypeId     INT NOT NULL,"
				+"   checkCont        VARCHAR(512),"
				+"   SEQ				INT,"
				+"   deleteFlg		boolean,"
				+"   versionNo    	INT,"
				+"   PRIMARY KEY (checkPointId)"
				+")"
			},
			{"600",
				"ALTER TABLE checkPoint ADD CONSTRAINT checkPoint_fk1"
				+" FOREIGN KEY(outputTypeId) REFERENCES outputType(outputTypeId)",
			},
			{"600",
				"CREATE TABLE checktbl ("
				+"   outputId       INT NOT NULL,"
				+"   checkDate      timestamp NOT NULL,"
				+"   userName       varchar(50),"
				+"   versionNo    	INT,"
				+"   PRIMARY KEY (outputId, checkDate)"
				+")",
			},
			{"600",
				"ALTER TABLE checktbl ADD CONSTRAINT checktbl_fk1"
				+" FOREIGN KEY(outputId) REFERENCES output(outputId) ON DELETE CASCADE",
			},
			{"600",
				"CREATE TABLE checkState ("
				+"   checkStateId  INT NOT NULL,"
				+"   checkStateName VARCHAR(100) NOT NULL,"
				+"   PRIMARY KEY (checkStateId)"
				+")",
			},
			{"600",
				"INSERT INTO checkState ( checkStateId, checkStateName ) VALUES (0,'(no check)')",
			},
			{"600",
				"INSERT INTO checkState ( checkStateId, checkStateName ) VALUES (10,'OK')",
			},
			{"600",
				"INSERT INTO checkState ( checkStateId, checkStateName ) VALUES (20,'NG')",
			},
			{"600",
				"INSERT INTO checkState ( checkStateId, checkStateName ) VALUES (30,'(no match)')",
			},
			{"600",
				"CREATE TABLE checkDetail ("
				+"   outputId       INT NOT NULL,"
				+"   checkDate      timestamp NOT NULL,"
				+"   checkPointId   INT NOT NULL,"
				+"   updDate      timestamp,"
				+"   checkStateId		INT NOT NULL,"
				+"   versionNo    	INT,"
				+"   PRIMARY KEY (outputId, checkDate, checkPointId)"
				+")",
			},
			{"600",
				"ALTER TABLE checkDetail ADD CONSTRAINT checkDetail_fk1"
				+" FOREIGN KEY(checkPointId) REFERENCES checkPoint(checkPointId)",
			},
			{"600",
				"ALTER TABLE checkDetail ADD CONSTRAINT checkDetail_fk2"
				+" FOREIGN KEY(checkStateId) REFERENCES checkState(checkStateId)"
			},
			//---------------------------------------
			{"601",
				"CREATE SEQUENCE WorkPropTypeId"
			},
			{"601",
				"CREATE SEQUENCE CheckPointId"
			},
			//---------------------------------------
			{"602",
				"ALTER TABLE checkDetail ADD CONSTRAINT checkDetail_fk3"
				+" FOREIGN KEY(outputId,checkDate) REFERENCES checktbl(outputId,checkDate) ON DELETE CASCADE",
			},
			//---------------------------------------
			{"610",
				"CREATE TABLE review ("
				+"   outputId       INT NOT NULL,"
				+"   reviewDate      timestamp NOT NULL,"
				+"   reviewi         varchar(50),"
				+"   reviewer1       varchar(50),"
				+"   reviewer2       varchar(50),"
				+"   reviewer3       varchar(50),"
				+"   remark          VARCHAR(1024),"
				+"   versionNo        INT,"
				+"   PRIMARY KEY (outputId, reviewDate)"
				+")",
			},
			{"610",
				"ALTER TABLE review ADD CONSTRAINT review_fk1"
				+" FOREIGN KEY(outputId) REFERENCES output(outputId) ON DELETE CASCADE",
			},
			{"610",
				"CREATE TABLE reviewStateType ("
				+"   reviewStateTypeId  INT NOT NULL,"
				+"   reviewStateTypeName VARCHAR(100) NOT NULL,"
				+"   PRIMARY KEY (reviewStateTypeId)"
				+")",
			},
			{"610",
				"INSERT INTO reviewStateType ( reviewStateTypeId, reviewStateTypeName ) VALUES (0,'未対応')",
			},
			{"610",
				"INSERT INTO reviewStateType ( reviewStateTypeId, reviewStateTypeName ) VALUES (20,'対応済')",
			},
			{"610",
				"INSERT INTO reviewStateType ( reviewStateTypeId, reviewStateTypeName ) VALUES (80,'再開')",
			},
			{"610",
				"INSERT INTO reviewStateType ( reviewStateTypeId, reviewStateTypeName ) VALUES (100,'確認済み')",
			},
			{"610",
				"CREATE TABLE reviewDetail ("
				+"   outputId       INT NOT NULL,"
				+"   reviewDate      timestamp NOT NULL,"
				+"   SEQ             INT,"
				+"   memo             VARCHAR(65535),"
				+"   updDate         timestamp,"
				+"   UpdUserName     varchar(50),"
				+"   reviewStateTypeId   INT NOT NULL,"
				+"   versionNo       INT,"
				+"   PRIMARY KEY (outputId, reviewDate, SEQ)"
				+")",
			},
			{"610",
				"ALTER TABLE reviewDetail ADD CONSTRAINT reviewDetail_fk1"
				+" FOREIGN KEY(reviewStateTypeId) REFERENCES reviewStateType(reviewStateTypeId)",
			},
			{"610",
				"ALTER TABLE reviewDetail ADD CONSTRAINT reviewDetail_fk2"
				+" FOREIGN KEY(outputId,reviewDate) REFERENCES review(outputId,reviewDate) ON DELETE CASCADE",
			},
			
			//---------------------------------------
//			{"611",
//				"ALTER TABLE workPropType ADD descr VARCHAR(256)"
//			},
//			{"611",
//				"ALTER TABLE workPropType DROP COLUMN memo" 
//			},
			
			//---------------------------------------
			{"612",
				"DROP SEQUENCE WorkPropTypeId"
			},
			{"612",
				"CREATE SEQUENCE OutputPropTypeId"
			},
			{"612",
				"DROP TABLE workProp",
			},
			{"612",
				"DROP TABLE workPropType",
			},
			{"612",
				"CREATE TABLE outputPropType ("
				+"   outputPropTypeId	INT NOT NULL,"
				+"   outputPropTypeName VARCHAR(128),"
				+"   unitName		  VARCHAR(20),"
				+"   descr            VARCHAR(256),"
				+"   SEQ			  INT,"
				+"   versionNo     	  INT,"
				+"   PRIMARY KEY (outputPropTypeId)"
				+")", 
			},
			{"612",
				"CREATE TABLE workProp ("
				+"   docId         INT NOT NULL,"
				+"   workTypeId    INT NOT NULL,"
				+"   outputPropTypeId	INT NOT NULL,"
				+"   jissekiFlg		boolean,"
				+"   value	      	NUMERIC(10,2),"
				+"   versionNo    	INT,"
				+"   PRIMARY KEY (docId, workTypeId, outputPropTypeId, jissekiFlg)"
				+")",
			},
			{"612",
				"ALTER TABLE workProp ADD CONSTRAINT workProp_fk1"
				+" FOREIGN KEY(docId,workTypeId) REFERENCES work(docId,workTypeId) ON DELETE CASCADE",
			},
			{"612",
				"ALTER TABLE workProp ADD CONSTRAINT workProp_fk2"
				+" FOREIGN KEY(outputPropTypeId) REFERENCES outputPropType(outputPropTypeId)",
			},
			//---------------------------------------
			{"613",
				"CREATE TABLE outputTypePropType ("
				+"   outputTypeId	INT NOT NULL,"
				+"   outputPropTypeId	INT NOT NULL,"
				+"   PRIMARY KEY (outputTypeId, outputPropTypeId)"
				+")", 
			},
			{"613",
				"ALTER TABLE outputTypePropType ADD CONSTRAINT outputTypePropType_fk1"
				+" FOREIGN KEY(outputTypeId) REFERENCES OutputType(outputTypeId) ON DELETE CASCADE"
			},
			{"613",
				"ALTER TABLE outputTypePropType ADD CONSTRAINT outputTypePropType_fk2"
				+" FOREIGN KEY(outputPropTypeId) REFERENCES OutputPropType(outputPropTypeId) ON DELETE CASCADE"
			},
			//---------------------------------------
			{"614",
				"ALTER TABLE checktbl ADD remark VARCHAR(128)"
			},

			//---------------------------------------
			{"615",
				"ALTER TABLE sysZeeta ADD linkNodePref VARCHAR(128)"
			},
			//---------------------------------------
			{"616",
				"ALTER TABLE sysZeeta ADD PRIMARY KEY(id)"
			},
			
			//--------------------------------------
			// sysZeeta
			//--------------------------------------
			{"616",
				"update sysZeeta set version='616' where id=1"
			},
		};
	
	static String q(String val){
		return "'" + val + "'";
	}
	
	public static void check(){
		Connection con = getConnection();
		if(con == null){
			return;
		}
		try{
			loadSysZeeta(con);
			//DBversionを確認
			if(DB_VERSION.equals(dbVersion)){
				log_.info("database version check ...OK!");
				return;
			}
			
			//version に応じたSQLを発行するのだ
			int verNum = Integer.parseInt(dbVersion);
			Statement statement = con.createStatement();
			for(int i=0; i<sqlList_.length; i++){
				if( verNum < Integer.parseInt(sqlList_[i][0])){
					log_.info(sqlList_[i][1]);
					statement.execute(sqlList_[i][1]);
				}
			}
			con.close();
			con = null;
		}catch(SQLException e){
			throw new RuntimeException("DB setup failed.", e);
		}finally{
			if(con != null){
				try{ con.close(); }catch(SQLException ex){}
			}
		}
		
	}

	public static Connection getConnection() {
		//接続
		S2Container daoCont = SelJDaoContainer.SEL_DAO_CONT;
		XADataSource ds = (XADataSource)daoCont.getComponent("xaDataSource");
		Connection con = null;
		try{
			con = ds.getXAConnection().getConnection();
		}catch(SQLException e){
			//コネクションエラー
			return null;	//後でエラーになるのでここでは無視
		}
		return con;
	}
	public static String getDBUrl(){
		Connection con = getConnection();
		if(con != null){
			try{
				return con.getMetaData().getURL();
			}catch(SQLException e){
				//後でエラーになるのでここでは無視
				e.printStackTrace();
			}finally{
				try{ con.close(); }catch(SQLException ex){}
			}
		}
		return "";
	}
	static void loadSysZeeta(Connection con) {
		Statement statement = null;
		try{
			statement = con.createStatement();
			ResultSet sysRs = statement.executeQuery("select * from sysZeeta where id=1");
			if(sysRs.next()){
				dbVersion = sysRs.getString("version");
			}else{
				//レコードが消されている
				statement.execute(INSERT_DEFAULT_SYS_RECORD);
				dbVersion = DB_VERSION;
			}
		}catch(SQLException e){		//sysZeetaが存在しない場合とみなす
			//version 0 でリタンする
		}	
	}
}
