package jp.tokyo.selj.common;

import java.awt.Component;

import javax.swing.JOptionPane;



public class MessageView {
	public static void show(Component c, Throwable e){
		int iconType;
		String title;
		if(e instanceof AppException){
			iconType = JOptionPane.INFORMATION_MESSAGE;
			title = "";
		}else{
			iconType = JOptionPane.ERROR_MESSAGE;
			title = "ヒットエンドラーン";
		}
		JOptionPane.showMessageDialog(
				c, new ExceptionPanel(e), title,iconType);
	}
}
