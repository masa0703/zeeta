package jp.tokyo.selj.view;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.tree.TreeNode;

import jp.tokyo.selj.model.DocNode;

public class BookMarkManager {
	static String KEY = "bookMark";
	static String KEY_TITLE = "title";
	static String KEY_PATH = "path";
	static int MAX_SIZE=10;
	static String FILE_NAME = "bookmark.xml";
	
	class BookMark {
		String title_;
		String pathStr_;
		List<Long> path_;
		BookMark(String title, String path){
			title_ = title;
			pathStr_ = path;
		}
		public List<Long> getPath(){
			if(path_ == null){
				path_ = new ArrayList<Long>();
				String[] ids = pathStr_.split(",");
				for(int i=0; i< ids.length; i++){
					path_.add( Long.parseLong(ids[i]));
				}
			}
			return path_;
		}
	}
	List<BookMark> bookMarks = new ArrayList<BookMark>();
	
	public BookMarkManager(Object obj){
		loadBookMark();
	}
	public void removeAndStore(BookMark rbkm){
		remove(rbkm);
		storeBookMark();
	}
	void remove(BookMark rbkm){
		for(int i=0; i < bookMarks.size(); ){
			BookMark bkm = bookMarks.get(i);
			if(bkm.pathStr_.equals(rbkm.pathStr_)){
				bookMarks.remove(i);
			}else{
				i++;
			}
		}
	}
	
	public void addBookMark(DocNode node){
		BookMark bkm =new BookMark(
							node.getDoc().getDocTitle(),
							path2string(node.getPath())
						);
		remove(bkm);
		bookMarks.add(bkm);
		while(bookMarks.size() > MAX_SIZE){
			bookMarks.remove(0);
		}
		storeBookMark();
	}
	String path2string(TreeNode[] path){
		String ret = "";
		String sep = "";
		for(int i=0; i < path.length; i++){
			ret += sep + ((DocNode)path[i]).getDoc().getDocId();
			sep = ",";
		}
		return ret;
	}
	void loadBookMark(){
		Properties prop = new Properties();
		InputStream is = null;
		try {
			is = new FileInputStream(FILE_NAME);
			prop.loadFromXML(is);
		}catch(FileNotFoundException e1){
			//
		} catch (Exception e) {
			throw new RuntimeException(e);
		}finally{
			try {
				if(is!=null) is.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		bookMarks.clear();
		for(int i=0; i<MAX_SIZE; i++){
			String title = prop.getProperty(KEY+i+"."+KEY_TITLE, "");
			if("".equals(title)){
				break;
			}
			String path = prop.getProperty(KEY+i+"."+KEY_PATH);
			bookMarks.add(new BookMark(title, path));
		}
	}
	void storeBookMark(){
		Properties prop = new Properties();

		int i=0;
		for(BookMark bm :bookMarks){
			prop.put(KEY+i+"."+KEY_TITLE, bm.title_);
			prop.put(KEY+i+"."+KEY_PATH, bm.pathStr_);
			i++;
		}
		
		OutputStream os = null;
		try {
			os = new FileOutputStream(FILE_NAME);
			prop.storeToXML(os, "bookmark");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}finally{
			try {
				os.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
}
