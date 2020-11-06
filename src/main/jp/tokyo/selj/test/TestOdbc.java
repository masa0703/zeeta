package jp.tokyo.selj.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TestOdbc {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		TestOdbc test = new TestOdbc();
//		test.doAll();
		test.doQuery();
	}
	void doQuery() throws Exception{
		Connection con = getConnection();
		con.setAutoCommit(true);
		try{
			ResultSet rs = executeQuery(con, "select * from v");
			while(rs.next()){
				System.out.println(rs.getObject("vID") + 
						", " + rs.getObject("v^Cg") +
						", " + rs.getObject("vàe"));
			}
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
        Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
        return DriverManager.getConnection("jdbc:odbc:sell");
//        return DriverManager.getConnection("jdbc:h2:./db/test", "sa", "");

	}
}
