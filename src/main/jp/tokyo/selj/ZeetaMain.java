package jp.tokyo.selj;

import java.util.UUID;

import javax.swing.JFrame;

import jp.tokyo.selj.common.MessageView;
import jp.tokyo.selj.dao.ModelCheckException;
import jp.tokyo.selj.dao.SysZeeta;
import jp.tokyo.selj.view.FrmZeetaMain;

import org.apache.log4j.Logger;

public class ZeetaMain  {
	Logger log_ = Logger.getLogger(this.getClass());
	static FrmZeetaMain mainView_;
	static UUID PROCESS_ID = UUID.randomUUID();		//ノードのコピペ制御のために必要になった
	
	public static FrmZeetaMain getMainView(){
		return mainView_;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		ZeetaMain main = new ZeetaMain();
		main.go();
	}
	protected FrmZeetaMain newMainView(){
		//version check, ここでsysZeetaテーブルの内容がロードされる
		ZeetaDBManager.check();
		
		ZeetaUI.setup();
		
		FrmZeetaMain view = new FrmZeetaMain();
		view.setup();
		return view;
	}
	void go(){
		log_.trace("start zeeta.");
		
		//デフォルト例外ハンドラ登録
		Thread.setDefaultUncaughtExceptionHandler(
			new Thread.UncaughtExceptionHandler(){
				public void uncaughtException(Thread t, Throwable e) {
					log_.error("Exception",e);
					if(mainView_ == null){	//メイン画面が表示される前のエラー
						MessageView.show(new JFrame(), e);
						System.exit(1);
					}else{
						MessageView.show(mainView_, e);
					}
					
					if( e instanceof ModelCheckException){		//入力チェックエラー
						//何もしない
					}else{
						//状態を初期化する
//						mainView_.initState();
						//Docのinsert/updateで桁あふれエラーでもここにくるので、
						//へたに初期化しない方がいい
					}
				}
			}
		);
		
		//splash画面表示～Mein画面表示
		new Splash(){
			@Override
			public void execute() {
				log_.trace("creating MainView...");
				mainView_ = newMainView();
				mainView_.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//				mainView_.setLocationRelativeTo(null);	//画面の中央に表示
				mainView_.setVisible(true);
				log_.trace("show MainView.");
			}
			
		};
		
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
//				MainView thisClass = new MainView(true);
//				thisClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//				thisClass.setVisible(true);
//			}
//		});
	}

	public static String getProcessId() {
		return PROCESS_ID.toString();
	}
}
