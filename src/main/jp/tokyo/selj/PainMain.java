package jp.tokyo.selj;

import jp.tokyo.selj.view.FrmZeetaMain;
import jp.tokyo.selj.view.FrmPainMain;

public class PainMain  extends ZeetaMain {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ZeetaMain main = new PainMain();
		main.go();
	}
	protected FrmZeetaMain newMainView(){
		FrmZeetaMain view = new FrmPainMain();
		view.setup();
		return view;
	}
}
