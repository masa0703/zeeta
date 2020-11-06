package jp.tokyo.selj;

import jp.tokyo.selj.dao.SelJDaoContainer;
import jp.tokyo.selj.dao.SysZeeta;
import jp.tokyo.selj.dao.SysZeetaDao;

public class SysZeetaManager {
	
	static SysZeetaDao sysZeetaDao_ = (SysZeetaDao) SelJDaoContainer.SEL_DAO_CONT.getComponent(SysZeetaDao.class);
	static boolean sysZeeta_loaded_flag = false;
	static SysZeeta sysZeeta_ = null;

	public static SysZeeta getSysZeeta(){
		load();
		return sysZeeta_;
		
	}
	static void load() {
		if(sysZeeta_loaded_flag){
			return;
		}
		sysZeeta_ = sysZeetaDao_.find();
	}
	public static void save(){
		sysZeetaDao_.update(sysZeeta_);
	}

}
