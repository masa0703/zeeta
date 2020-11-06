package jp.tokyo.selj.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TestH2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		TestH2 test = new TestH2();
		test.doAll();
//		test.doQuery();
	}
	void doQuery() throws Exception{
		Connection con = getConnection();
		con.setAutoCommit(true);
		try{
			ResultSet rs = executeQuery(con, "select * from test");
			while(rs.next()){
				System.out.println(rs.getObject("ID") + 
						", " + rs.getObject("NAME") );
			}
		}finally{
			con.close();
		}
	}
	void doAll() throws Exception{
		Connection con = getConnection();
		con.setAutoCommit(true);
		try{
			execute(con,"drop table IF EXISTS TEST");
			execute(con,"create table TEST (" +
					"ID integer primary key," +
					"NAME varchar(10000)" +
					")");
			
			execute(con,"insert into TEST values(1, '‚«‚ñ‚½‚Ü')");
			execute(con,"insert into TEST values(2, '‚¿‚ñ‚½‚Ü')");
			execute(con,"insert into TEST values(3, '‚¤‚ñ‚±')");
			long t = System.currentTimeMillis();
			for(int i=0; i<10000; i++){
				execute(con,"insert into TEST values("+(i+100)+", '‚«‚ñ‚½‚Ü')");
			}
//			con.commit();
			System.out.println("time="+ (System.currentTimeMillis() - t));

		}finally{
			con.close();
		}
	}
	void execute(Connection con, String sql) throws Exception {
		Statement st = con.createStatement();
		st.execute(sql);
	}
	ResultSet executeQuery(Connection con, String sql) throws Exception {
		Statement st = con.createStatement();
		return st.executeQuery(sql);
	}
	
	Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");
        return DriverManager.getConnection("jdbc:h2:tcp://localhost/test", "sa", "");
//        return DriverManager.getConnection("jdbc:h2:./db/test", "sa", "");

	}
}
