package jp.tokyo.selj.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.XADataSource;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.S2ContainerFactory;

public class DbSetup {

	List<String> sqlList_;
	
	public DbSetup(List sqlList){
		sqlList_ = sqlList;
	}
	public void doScript(Connection con) throws SQLException{
		try{
			if(sqlList_ == null){
				throw new RuntimeException("sqlList_ is null.");
			}
			
			Statement st = con.createStatement();
			for(int i=0; i<sqlList_.size(); i++){
				System.out.println(sqlList_.get(i));
				st.execute(sqlList_.get(i));
			}
			
		}finally{
			con.close();
		}
		
	}
	
	public static void main(String[] args){
		String diconFileName = "dbSetup.dicon";	//default
		
		if(args.length > 0){
			diconFileName = args[0];
		}
		
		try{
			S2Container setupCont = S2ContainerFactory.create(diconFileName);
			setupCont.init();
			DbSetup setup = (DbSetup)setupCont.getComponent("dbSetup");
			
			Connection con = getConnection(setupCont);

			setup.doScript(con);
			
			System.out.println("!!! setup complete. !!!");
		}catch(Throwable e){
			e.printStackTrace();
		}
	}
	static Connection getConnection(S2Container setupCont) throws SQLException{
		XADataSource ds = (XADataSource)setupCont.getComponent("xaDataSource");
		return ds.getXAConnection().getConnection();
	}
}
