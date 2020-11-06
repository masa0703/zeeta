package jp.tokyo.selj.common;

import java.util.ArrayList;
import java.util.List;

public class CatString {

	/**
	 * 複数エントリに分割されている行を１エントリにまとめる<br>
	 * <li>
	 * <ul>1."で囲まれた部分は、行が分かれていても次の"までを抽出する</ul>
	 * <ul>2.1で抽出した後に残った文字は、改行までを</ul>
	 * </li>
	 * @param lines
	 * @return
	 */
	public static String[] concatLine(String[] lines){
    	List<String> newLines = new ArrayList<String>();
    	boolean isFirstLine = true;
		int index = 0;
		
		while(index < lines.length){
			String catLine = "";
			String line = lines[index].replaceAll("\"\"", "&h0000"); 
			int tabCount = countTab(lines[index]);
			while(!"".equals(line)){
				line = line.trim();
				//先頭が"で始まる
				if(line.startsWith("\"")){
					//同じ行で次の"を探す
					int pos = line.indexOf('\"', 1);
					if(pos >= 0){
						catLine += (isFirstLine)? tabs(tabCount):"\t";
						catLine += line.substring(1, pos);
						line = line.substring(pos+1);
					}else{
						//次行にまたがる場合
						catLine += (isFirstLine)? tabs(tabCount):"\t";
						catLine += line.substring(1);
		    			line = "";
						//"が出てくるまでcatLineに連結
						boolean loop = true;
						while(loop && ((index+1) < lines.length)){
							index++;
							line = lines[index].replaceAll("\"\"", "&h0000");
				    		int to = line.indexOf('\"');
				    		if(to <= -1){
				    			to = line.length();
				    		}else{
				    			loop = false;
				    		}
			    			catLine += "\n" + line.substring(0, to);
			    			line = line.substring(to);
			    			if(!loop){	//"がみつかった場合は、"を削除
				    			line = line.substring(1);
			    			}
						}
					}
				}else{
					//先頭が"で始まらない場合はtabまでをタイトルとする
		    		int to = line.indexOf('\t');
		    		if(to <= -1){
		    			to = line.length();
		    		}
					catLine += (isFirstLine)? tabs(tabCount):"\t";
	    			catLine += line.substring(0, to);
	    			line = line.substring(to);
				}
				isFirstLine = false;
			}
			newLines.add(catLine);
			index++;
			isFirstLine = true;
		}

		String[] ret = new String[newLines.size()];
		ret = newLines.toArray(ret);
		//置換しておいた"を元に戻す
		for(int i=0; i<ret.length; i++){
			ret[i] = ret[i].replaceAll("&h0000", "\"");
		}
		return ret;
    }
	static String tabs(int count){
		String ret = "";
		for(int i=0;i < count; i++){
			ret += "\t";
		}
		return ret;
	}
    static String extractUntilTab(String line){
		if(line.startsWith("\"")){
			
		}
    	return line;
    }
    public static int countTab(String line){
    	int ret = 0;
    	for(int i=0;i<line.length(); i++){
    		if(line.charAt(i) == '\t'){
    			ret++;
    		}else{
    			break;
    		}
    	}
    	return ret;
    }

}
