package jp.tokyo.selj.util;

import java.io.File;
import java.sql.SQLException;

import org.h2.tools.DeleteDbFiles;
import org.h2.tools.RunScript;
import org.h2.tools.Script;

public class CompactDb {
	static final String FILE_NAME = "____temp___.sql";
	static final String USER="sa";
	static final String PASSWORD="";

	static boolean endflag=false;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
        try {
        	printDot();
        	new CompactDb().compact();
        	endflag = true;
	        System.out.println("\nCompact DB complete.");
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}
	void compact() throws SQLException{
        String url = "jdbc:h2:db/sel";
		Script.execute(url, USER, PASSWORD, FILE_NAME);
		DeleteDbFiles.execute("db", "sel", true);
        RunScript.execute(url, USER, PASSWORD, FILE_NAME, null, false);
        new File(FILE_NAME).delete();
    }
	static void printDot(){
    	//試しにやってみよー
    	Thread t = new Thread(){
			public void run() {
				while(!endflag){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.print(".");
				}
			}
    	};
    	t.start();

	}
}
