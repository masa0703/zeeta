package jp.tokyo.selj.view;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;

import javax.swing.ActionMap;
import javax.swing.JOptionPane;

import jp.tokyo.selj.model.DocNode;
import jp.tokyo.selj.view.FrmZeetaMain.ActCancelNewYouken;
import jp.tokyo.selj.view.FrmZeetaMain.ActCommitDoc;
import jp.tokyo.selj.view.FrmZeetaMain.ActDuplicateDoc;
import jp.tokyo.selj.view.FrmZeetaMain.ActMoveDown;
import jp.tokyo.selj.view.FrmZeetaMain.ActMoveUp;
import jp.tokyo.selj.view.FrmZeetaMain.ActPrepareCreateDocAsChild;
import jp.tokyo.selj.view.FrmZeetaMain.ActPrepareCreateDocAsSibling;
import jp.tokyo.selj.view.FrmZeetaMain.ActRefreshCurrent;
import jp.tokyo.selj.view.FrmZeetaMain.ActRemoveDoc;
import jp.tokyo.selj.view.FrmZeetaMain.ActShowDebugWindow;
import jp.tokyo.selj.view.FrmZeetaMain.ActShowSearchWindow;



public class MainViewKeyDispatcher implements KeyEventDispatcher{
	ActionMap actionMap_ = null;
	FrmZeetaMain mainFrame_ = null;
	
	public MainViewKeyDispatcher(ActionMap actionMap, FrmZeetaMain mainFrame){
		actionMap_ = actionMap;
		mainFrame_ = mainFrame;
	}
	
	//==========================================
	public boolean dispatchKeyEvent(KeyEvent evt) {
//		log.debug("evt="+evt);
		
		boolean ret = false;
		if(!mainFrame_.isActive()){		//mainFrameが非アクティブの場合は、何もしない
			return ret;
		}
		
		//共通のキー
		ret = dispatchKeyEvent_common(evt, ret);

		//Access版互換
		if( !ret ){
//			ret = dispatchKeyEvent_access(evt, ret); とりあえず外しておく
		}
		
		return ret;
	}
	private boolean dispatchKeyEvent_common(KeyEvent evt, boolean ret) {
		if (evt.getID()  == KeyEvent.KEY_TYPED){
//			log.debug("KEY_TYPED="+evt);
			switch((int)evt.getKeyChar()){
			case KeyEvent.VK_ESCAPE:
				actionMap_.get(ActCancelNewYouken.class).actionPerformed(null);
				ret = true;
				break;
			}
		}else {
			if (evt.getID()  == KeyEvent.KEY_PRESSED){
//				log.debug("KEY_PRESSED="+evt);
				if( (evt.getModifiers() & KeyEvent.CTRL_MASK )!=0 ){
					switch(evt.getKeyCode()){
					case KeyEvent.VK_UP:
						actionMap_.get(ActMoveUp.class).actionPerformed(null);
						ret = true;
						break;
					case KeyEvent.VK_DOWN:
						actionMap_.get(ActMoveDown.class).actionPerformed(null);
						ret = true;
						break;
					case KeyEvent.VK_LEFT:
						JOptionPane.showConfirmDialog(
								mainFrame_
								,"history back key was changed to ALT+left",""
								,JOptionPane.DEFAULT_OPTION
								,JOptionPane.INFORMATION_MESSAGE);
						ret = true;
						break;
					case KeyEvent.VK_RIGHT:
						JOptionPane.showConfirmDialog(
								mainFrame_
								,"history forward key was changed to ALT+right",""
								,JOptionPane.DEFAULT_OPTION
								,JOptionPane.INFORMATION_MESSAGE);
						ret = true;
						break;
					case KeyEvent.VK_D:
						actionMap_.get(ActRemoveDoc.class).actionPerformed(null);
						ret = true;
						break;
					case KeyEvent.VK_F:
						actionMap_.get(ActShowSearchWindow.class).actionPerformed(null);
						ret = true;
						break;
					case KeyEvent.VK_M:
						actionMap_.get(ActPrepareCreateDocAsSibling.class).actionPerformed(null);
						ret = true;
						break;
					case KeyEvent.VK_N:
						actionMap_.get(ActPrepareCreateDocAsChild.class).actionPerformed(null);
						ret = true;
						break;
					case KeyEvent.VK_W:
						actionMap_.get(ActDuplicateDoc.class).actionPerformed(null);
						ret = true;
						break;
					case KeyEvent.VK_S:
					case KeyEvent.VK_ENTER:
						actionMap_.get(ActCommitDoc.class).actionPerformed(null);
						ret = true;
						break;
					case KeyEvent.VK_F12:	//隠し機能
						actionMap_.get(ActShowDebugWindow.class).actionPerformed(null);
						ret = true;
						break;
					}
				}else if( (evt.getModifiers() & KeyEvent.ALT_MASK )!=0 ){
					switch(evt.getKeyCode()){
					case KeyEvent.VK_LEFT:
						DocNode docNode = mainFrame_.nodeHistory_.back();
						if(docNode != null ){
							mainFrame_.showDetailAndSelectWord(docNode, (String)null);
						}
						ret = true;
						break;
					case KeyEvent.VK_RIGHT:
						docNode = mainFrame_.nodeHistory_.forward();
						if(docNode != null ){
							mainFrame_.showDetailAndSelectWord(docNode, (String)null);
						}
						ret = true;
						break;
					}
				}else {
					switch(evt.getKeyCode()){
					case KeyEvent.VK_F5:
						actionMap_.get(ActRefreshCurrent.class).actionPerformed(null);
						ret = true;
						break;
					case KeyEvent.VK_INSERT:
						if( (evt.getModifiers() & KeyEvent.SHIFT_MASK )!=0 ){
							actionMap_.get(ActPrepareCreateDocAsSibling.class).actionPerformed(null);
							ret = true;
						}else{
							actionMap_.get(ActPrepareCreateDocAsChild.class).actionPerformed(null);
							ret = true;
						}
						break;
					}
				}
			}
		}
		return ret;
	}

	//==========================================Access版互換
//	private boolean dispatchKeyEvent_access(KeyEvent evt, boolean ret) {
//			if (evt.getID()  == KeyEvent.KEY_TYPED){
//				if( (evt.getModifiers() & KeyEvent.ALT_MASK )!=0 ){
//					switch(evt.getKeyChar()){
//					case 'z':
//					case 'Z':
//						actionMap_.get(ActClipNode.class).actionPerformed(null);
//						ret = true;
//						break;
//					case 'x':
//					case 'X':
//						actionMap_.get(ActPasteNodeMove.class).actionPerformed(null);
//						ret = true;
//						break;
//					case 'c':
//					case 'C':
//						actionMap_.get(ActPasteNodeCopy.class).actionPerformed(null);
//						ret = true;
//						break;
//					case 'n':
//					case 'N':
//						actionMap_.get(ActPrepareCreateYoukenAsChild.class).actionPerformed(null);
//						ret = true;
//						break;
//					case 'm':
//					case 'M':
//						actionMap_.get(ActPrepareCreateYoukenAsSibling.class).actionPerformed(null);
//						ret = true;
//						break;
//					case 'k':
//					case 'K':
//						actionMap_.get(ActCommitYouken.class).actionPerformed(null);
//						ret = true;
//						break;
//						
//					case 'd':
//					case 'D':
//						actionMap_.get(ActRemoveYouken.class).actionPerformed(null);
//						ret = true;
//						break;					
//					}
//				}
//			}
//			return ret;
//		}
}
