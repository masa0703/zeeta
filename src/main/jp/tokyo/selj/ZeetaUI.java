package jp.tokyo.selj;

import java.awt.Font;

import javax.swing.UIManager;

public class ZeetaUI {
	public static void setup(){
//		BasicTreeUI.MouseHandler();
		Font planeFont = new Font("Dialog", Font.PLAIN, 12);
		
		UIManager.put("Label.font", planeFont);
		UIManager.put("CheckBox.font", planeFont);
	}
}
