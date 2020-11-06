package jp.tokyo.selj.view;

import javax.swing.ActionMap;

import org.apache.log4j.Logger;

public abstract class ActTransactionBase extends ActBase {
	Logger log = Logger.getLogger(this.getClass());

	
	public ActTransactionBase(){
		this(null);
	}
	public ActTransactionBase(ActionMap map){
		super(map);
		useTransaction_ = true;
    }
}
