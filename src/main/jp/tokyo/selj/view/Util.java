package jp.tokyo.selj.view;

import java.awt.Cursor;

public class Util {
    public static Cursor WAIT_CURSOR = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
    
	public static String getClassName(Object o){
		return getClassName(o.getClass());
	}
	public static String getClassName(Class clazz){
		String src = clazz.getName();
		String[] split = src.split("\\.");
		return split[split.length-1];
	}

}
