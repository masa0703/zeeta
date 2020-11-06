package jp.tokyo.selj.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.XADataSource;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.S2ContainerFactory;

public class TableConvertor extends DbSetup{

	public TableConvertor(List sqlList){
		super(sqlList);
	}
	
	public static void main(String[] args){
		try{
			S2Container setupCont = S2ContainerFactory.create("tableConv.dicon");
			setupCont.init();
			TableConvertor setup = (TableConvertor)setupCont.getComponent("TableConvertor");
			
			Connection con = getConnection(setupCont);

			setup.doScript(con);
			
			System.out.println("!!! complete. !!!");
		}catch(Throwable e){
			e.printStackTrace();
		}
	}
	static Connection getConnection(S2Container setupCont) throws SQLException{
		XADataSource ds = (XADataSource)setupCont.getComponent("xaDataSource");
		return ds.getXAConnection().getConnection();
	}
}
