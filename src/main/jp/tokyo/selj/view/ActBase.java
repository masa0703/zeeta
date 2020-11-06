package jp.tokyo.selj.view;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.transaction.TransactionManager;

import jp.tokyo.selj.common.MessageView;
import jp.tokyo.selj.dao.SelJDaoContainer;

import org.apache.log4j.Logger;

public abstract class  ActBase extends AbstractAction {
	Logger log = Logger.getLogger(this.getClass()); 

	protected TransactionManager trn_ = null;
	protected boolean useTransaction_ = false;
	
	public ActBase(){
	}
	public ActBase(ActionMap map){
		if(map != null){
			map.put(this.getClass(), this);
		}
	}
	public final void actionPerformed(ActionEvent e) {
		try{
			preProc();
			actionPerformed2(e);
			postProc();
		}catch(Exception ex){
			log.error(ex.getMessage(), ex);
			handleException(ex);
		}
	}
	protected void preProc(){
		if( !useTransaction_ ){
			return;
		}
		try {
	    	trn_ = (TransactionManager)SelJDaoContainer.SEL_DAO_CONT.
	    									getComponent(TransactionManager.class);
			trn_.begin();
			log.debug("トランザクション開始");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
	protected void postProc(){
		if( !useTransaction_ ){
			return;
		}
		try {
			trn_.commit();
			log.debug("トランザクションコミット");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
	protected void handleException(Exception ex) {
		if( useTransaction_ ){
			try {
				trn_.rollback();
				log.debug("トランザクションロールバック");
			} catch (Exception e) {
				//ここのエラーは無視
			}
		}
		MessageView.show(getOwnerComponent(), ex);
	}

	protected abstract void actionPerformed2(ActionEvent e);
	protected Component getOwnerComponent(){
		return null;
	}
}
